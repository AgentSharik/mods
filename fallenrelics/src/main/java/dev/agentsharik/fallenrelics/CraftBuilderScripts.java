package dev.agentsharik.fallenrelics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/** Standalone Fallen Relics .frs compiler and live recipe-manager bridge. */
public final class CraftBuilderScripts {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SCRIPT_SUBDIRECTORY = "fallenrelics";
    private static final String SCRIPT_EXTENSION = ".frs";
    private static final String LEGACY_DATAPACK_FOLDER = "fallenrelics_scripts";
    private static final Pattern LEGACY_SHAPED_ZEN = Pattern.compile(
            "craftingTable\\.addShaped\\(\"[^\"]+\",\\s*<item:([^>]+)>(?:\\s*\\*\\s*(\\d+))?,\\s*\\[(.*)\\]\\s*\\);",
            Pattern.DOTALL);
    private static final Pattern LEGACY_SHAPELESS_ZEN = Pattern.compile(
            "craftingTable\\.addShapeless\\(\"[^\"]+\",\\s*<item:([^>]+)>(?:\\s*\\*\\s*(\\d+))?,\\s*\\[(.*?)\\]\\s*\\);",
            Pattern.DOTALL);
    private static final Pattern ZEN_ITEM_BRACKET = Pattern.compile("<item:([^>]+)>");
    private static final Pattern ZEN_ROW = Pattern.compile("\\[([^\\[\\]]*)\\]");
    private static final Pattern ZEN_SHAPED = Pattern.compile(
            "\\w+\\.addShaped\\(\\s*\"([^\"]+)\"\\s*,\\s*<item:([^>]+)>\\s*(?:\\*\\s*(\\d+))?\\s*,\\s*\\[(.*?)\\]\\s*\\)\\s*;",
            Pattern.DOTALL);
    private static final Pattern ZEN_SHAPELESS = Pattern.compile(
            "\\w+\\.addShapeless\\(\\s*\"([^\"]+)\"\\s*,\\s*<item:([^>]+)>\\s*(?:\\*\\s*(\\d+))?\\s*,\\s*\\[(.*?)\\]\\s*\\)\\s*;",
            Pattern.DOTALL);
    private static final Pattern ZEN_REMOVE_ITEM = Pattern.compile(
            "\\w+\\.removeRecipe\\(\\s*<item:([^>]+)>\\s*\\)\\s*;");
    private static final Pattern ZEN_REMOVE_ID = Pattern.compile(
            "\\w+\\.removeRecipe\\(\\s*\"([^\"]+)\"\\s*\\)\\s*;");

    /** The maps below mirror CraftTweaker's live RecipeManager mutation path without depending on its classes. */
    @Nullable private static RecipeManager activeManager;
    private static Map<ResourceLocation, RecipeHolder<?>> byName;
    private static Multimap<RecipeType<?>, RecipeHolder<?>> byType;
    private static final Map<ResourceLocation, RecipeHolder<?>> activeScriptRecipes = new LinkedHashMap<>();
    private static final Map<ResourceLocation, RecipeHolder<?>> displacedRecipes = new LinkedHashMap<>();
    private static final Map<ResourceLocation, RecipeHolder<?>> removedByScript = new LinkedHashMap<>();
    private static final Set<ResourceLocation> migratedLegacyRecipeIds = new LinkedHashSet<>();

    private CraftBuilderScripts() {}

    public static void saveRecipe(Player player, ItemStackHandler inventory, boolean shapeless) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        GeneratedRecipe generated = generateRecipe(inventory, shapeless);
        if (generated == null) {
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.invalid"), true);
            return;
        }

        try {
            Path script = scriptFile(generated.id());
            Files.createDirectories(script.getParent());
            Files.writeString(script, generated.source(), StandardCharsets.UTF_8);
            reloadGeneratedRecipes(level.getServer(), player,
                    Component.translatable("fallenrelics.craft_builder.saved", generated.id()));
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not save Fallen Relics recipe script for {}",
                    player.getGameProfile().getName(), exception);
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.error"), true);
        }
    }

    public static void removeRecipe(Player player, ItemStackHandler inventory, boolean shapeless) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        GeneratedRecipe generated = generateRecipe(inventory, shapeless);
        if (generated == null) {
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.invalid"), true);
            return;
        }

        try {
            Set<Path> matches = findMatchingScripts(generated);
            Path exact = scriptFile(generated.id());
            if (Files.isRegularFile(exact)) {
                matches.add(exact);
            }
            if (matches.isEmpty()) {
                player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.not_found"), true);
                return;
            }

            int removed = 0;
            for (Path match : matches) {
                if (Files.deleteIfExists(match)) {
                    removed++;
                }
            }
            if (removed == 0) {
                player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.not_found"), true);
                return;
            }
            reloadGeneratedRecipes(level.getServer(), player,
                    Component.translatable("fallenrelics.craft_builder.removed", generated.id()));
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not remove Fallen Relics recipe script {}", generated.id(), exception);
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.error"), true);
        }
    }

    /** Converts scripts and recipes from earlier CraftBuilder builds to the standalone .frs format. */
    public static void migrateLegacyDatapack(MinecraftServer server) {
        boolean changed = migrateLegacyZenScripts();
        Path legacyRecipes = server.getWorldPath(LevelResource.DATAPACK_DIR)
                .resolve(LEGACY_DATAPACK_FOLDER)
                .resolve("data")
                .resolve(FallenRelicsMod.MOD_ID)
                .resolve("recipe");

        if (Files.isDirectory(legacyRecipes)) {
            try (var files = Files.list(legacyRecipes)) {
                for (Path file : files.filter(path -> path.getFileName().toString().startsWith("craftbuilder_")
                        && path.getFileName().toString().endsWith(".json")).toList()) {
                    try {
                        RecipeScript migrated = convertLegacyRecipe(
                                JsonParser.parseString(Files.readString(file)).getAsJsonObject());
                        String migratedId = recipeId(migrated);
                        Path destination = scriptFile(migratedId);
                        Files.createDirectories(destination.getParent());
                        if (!Files.exists(destination)) {
                            Files.writeString(destination, serializeScript(migrated), StandardCharsets.UTF_8);
                        }
                        boolean removedLegacyFile = Files.deleteIfExists(file);
                        changed |= removedLegacyFile;
                        if (removedLegacyFile || !Files.exists(file)) {
                            migratedLegacyRecipeIds.add(ResourceLocation.fromNamespaceAndPath(
                                    FallenRelicsMod.MOD_ID, migratedId));
                        }
                    } catch (RuntimeException | IOException exception) {
                        LOGGER.warn("Could not migrate old CraftBuilder recipe {}", file, exception);
                    }
                }
            } catch (IOException exception) {
                LOGGER.error("Could not scan the previous CraftBuilder datapack", exception);
            }
        }

        if (changed) {
            LOGGER.info("Migrated earlier CraftBuilder recipes to standalone Fallen Relics scripts");
        }
    }

    private static boolean migrateLegacyZenScripts() {
        Path scripts = scriptDirectory();
        if (!Files.isDirectory(scripts)) {
            return false;
        }

        boolean changed = false;
        try (Stream<Path> files = Files.walk(scripts)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".zs")).toList()) {
                try {
                    String oldSource = Files.readString(file, StandardCharsets.UTF_8);
                    if (!oldSource.contains("// Generated by Fallen Relics CraftBuilder.")) {
                        continue;
                    }
                    RecipeScript migrated = convertLegacyZenScript(oldSource);
                    Path destination = scriptFile(recipeId(migrated));
                    Files.createDirectories(destination.getParent());
                    if (!Files.exists(destination)) {
                        Files.writeString(destination, serializeScript(migrated), StandardCharsets.UTF_8);
                    }
                    changed |= Files.deleteIfExists(file);
                } catch (RuntimeException | IOException exception) {
                    LOGGER.warn("Could not migrate an old generated CraftBuilder script {}", file, exception);
                }
            }
        } catch (IOException exception) {
            LOGGER.error("Could not scan old CraftBuilder scripts", exception);
        }
        return changed;
    }

    /** Called after the server has loaded its datapacks. No datapack reload is needed. */
    public static void loadForServer(MinecraftServer server) {
        applyScripts(server, false, null, null);
    }

    /** The NeoForge datapack-sync event fires before recipes are sent to joining/reloading clients. */
    public static void refreshBeforeDatapackSync(MinecraftServer server) {
        if (activeManager != server.getRecipeManager()) {
            applyScripts(server, false, null, null);
        }
    }

    public static void reloadFromCommand(MinecraftServer server, CommandSourceStack source) {
        applyScripts(server, true, null, null, source);
    }

    private static void reloadGeneratedRecipes(
            MinecraftServer server, @Nullable Player player, @Nullable Component successMessage) {
        applyScripts(server, true, player, successMessage, null);
    }

    private static void applyScripts(
            MinecraftServer server,
            boolean syncClients,
            @Nullable Player player,
            @Nullable Component successMessage) {
        applyScripts(server, syncClients, player, successMessage, null);
    }

    private static void applyScripts(
            MinecraftServer server,
            boolean syncClients,
            @Nullable Player player,
            @Nullable Component successMessage,
            @Nullable CommandSourceStack commandSource) {
        try {
            RecipeManager manager = server.getRecipeManager();
            ensureMutableManager(manager);
            CompileResult compiled = compileScripts(server);
            replaceScriptRecipes(manager, compiled.recipes());
            applyRemovals(server, compiled.removalOutputs(), compiled.removalIds());

            if (syncClients) {
                syncRecipes(server, manager);
            }
            if (player != null && successMessage != null) {
                player.displayClientMessage(successMessage, true);
            }
            if (commandSource != null) {
                commandSource.sendSuccess(
                        () -> Component.translatable("fallenrelics.craft_builder.reloaded"), true);
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not compile or apply Fallen Relics scripts", exception);
            if (player != null) {
                player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.reload_error"), true);
            }
            if (commandSource != null) {
                commandSource.sendFailure(Component.translatable("fallenrelics.craft_builder.reload_error"));
            }
        }
    }

    private static void ensureMutableManager(RecipeManager manager) {
        if (activeManager == manager) {
            return;
        }

        try {
            Multimap<RecipeType<?>, RecipeHolder<?>> mutableByType =
                    LinkedHashMultimap.create(getByType(manager));
            Map<ResourceLocation, RecipeHolder<?>> mutableByName =
                    new LinkedHashMap<>(getByName(manager));
            ObfuscationReflectionHelper.setPrivateValue(RecipeManager.class, manager, mutableByType, "byType");
            ObfuscationReflectionHelper.setPrivateValue(RecipeManager.class, manager, mutableByName, "byName");
            byType = mutableByType;
            byName = mutableByName;
            activeManager = manager;
            activeScriptRecipes.clear();
            displacedRecipes.clear();
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Could not access the live RecipeManager maps", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static Multimap<RecipeType<?>, RecipeHolder<?>> getByType(RecipeManager manager) {
        return (Multimap<RecipeType<?>, RecipeHolder<?>>)
                ObfuscationReflectionHelper.getPrivateValue(RecipeManager.class, manager, "byType");
    }

    @SuppressWarnings("unchecked")
    private static Map<ResourceLocation, RecipeHolder<?>> getByName(RecipeManager manager) {
        return (Map<ResourceLocation, RecipeHolder<?>>)
                ObfuscationReflectionHelper.getPrivateValue(RecipeManager.class, manager, "byName");
    }

    private static void replaceScriptRecipes(RecipeManager manager, List<RecipeHolder<?>> replacements) {
        for (ResourceLocation migratedId : migratedLegacyRecipeIds) {
            RecipeHolder<?> active = activeScriptRecipes.remove(migratedId);
            if (active != null) {
                byName.remove(migratedId, active);
                byType.remove(active.value().getType(), active);
            }
            displacedRecipes.remove(migratedId);
            RecipeHolder<?> staleLegacy = byName.remove(migratedId);
            if (staleLegacy != null) {
                byType.remove(staleLegacy.value().getType(), staleLegacy);
            }
        }
        migratedLegacyRecipeIds.clear();

        // Restore recipes removed by an earlier .zs removeRecipe() pass; the current
        // pass re-applies whatever removals are still requested below.
        for (Map.Entry<ResourceLocation, RecipeHolder<?>> entry : removedByScript.entrySet()) {
            RecipeHolder<?> restored = entry.getValue();
            byName.put(entry.getKey(), restored);
            byType.put(restored.value().getType(), restored);
        }
        removedByScript.clear();

        for (Map.Entry<ResourceLocation, RecipeHolder<?>> entry : activeScriptRecipes.entrySet()) {
            ResourceLocation id = entry.getKey();
            RecipeHolder<?> active = entry.getValue();
            byName.remove(id, active);
            byType.remove(active.value().getType(), active);

            RecipeHolder<?> displaced = displacedRecipes.remove(id);
            if (displaced != null) {
                byName.put(id, displaced);
                byType.put(displaced.value().getType(), displaced);
            }
        }
        activeScriptRecipes.clear();

        for (RecipeHolder<?> replacement : replacements) {
            ResourceLocation id = replacement.id();
            RecipeHolder<?> old = byName.get(id);
            if (old != null) {
                displacedRecipes.put(id, old);
                byType.remove(old.value().getType(), old);
            }
            byName.put(id, replacement);
            byType.put(replacement.value().getType(), replacement);
            activeScriptRecipes.put(id, replacement);
        }
    }

    /** Mirrors CraftTweaker's removeRecipe semantics on the live RecipeManager. */
    private static void applyRemovals(
            MinecraftServer server, Set<ResourceLocation> removalOutputs, Set<ResourceLocation> removalIds) {
        if (removalOutputs.isEmpty() && removalIds.isEmpty()) {
            return;
        }
        HolderLookup.Provider registries = server.registryAccess();
        byName.entrySet().removeIf(entry -> {
            if (activeScriptRecipes.containsKey(entry.getKey())) {
                return false;
            }
            RecipeHolder<?> holder = entry.getValue();
            boolean remove = removalIds.contains(entry.getKey());
            if (!remove && !removalOutputs.isEmpty()) {
                ItemStack output = holder.value().getResultItem(registries);
                remove = !output.isEmpty()
                        && removalOutputs.contains(BuiltInRegistries.ITEM.getKey(output.getItem()));
            }
            if (remove) {
                removedByScript.put(entry.getKey(), holder);
                byType.remove(holder.value().getType(), holder);
            }
            return remove;
        });
    }

    private static void syncRecipes(MinecraftServer server, RecipeManager manager) {
        for (ServerPlayer connected : server.getPlayerList().getPlayers()) {
            connected.connection.send(new ClientboundUpdateRecipesPacket(manager.getOrderedRecipes()));
        }
    }

    private static CompileResult compileScripts(MinecraftServer server) throws IOException {
        Path scripts = scriptDirectory();
        Files.createDirectories(scripts);
        RegistryOps<com.google.gson.JsonElement> registryOps =
                server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        Set<ResourceLocation> recipeIds = new LinkedHashSet<>();
        Set<ResourceLocation> removalOutputs = new LinkedHashSet<>();
        Set<ResourceLocation> removalIds = new LinkedHashSet<>();

        try (Stream<Path> scriptFiles = Files.walk(scripts)) {
            for (Path scriptFile : scriptFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.endsWith(SCRIPT_EXTENSION) || name.endsWith(".zs");
                    })
                    .sorted()
                    .toList()) {
                try {
                    String source = Files.readString(scriptFile, StandardCharsets.UTF_8);
                    if (source.contains("addShaped(") || source.contains("addShapeless(")
                            || source.contains("removeRecipe(")) {
                        ZenScriptResult zen = parseZenScript(source);
                        removalOutputs.addAll(zen.removalOutputs());
                        removalIds.addAll(zen.removalIds());
                        for (ZenAddition addition : zen.additions()) {
                            addCompiledRecipe(recipes, recipeIds, scriptFile, registryOps,
                                    addition.id(), addition.recipe());
                        }
                        continue;
                    }
                    RecipeScript script = parseScript(source);
                    String path = recipeIdFromScriptPath(scripts.relativize(scriptFile));
                    addCompiledRecipe(recipes, recipeIds, scriptFile, registryOps, path, script);
                } catch (RuntimeException exception) {
                    LOGGER.error("Ignoring invalid Fallen Relics script {}", scriptFile, exception);
                }
            }
        }
        return new CompileResult(recipes, removalOutputs, removalIds);
    }

    private static void addCompiledRecipe(
            List<RecipeHolder<?>> recipes,
            Set<ResourceLocation> recipeIds,
            Path scriptFile,
            RegistryOps<com.google.gson.JsonElement> registryOps,
            String path,
            RecipeScript script) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, path);
        Recipe<?> recipe = Recipe.CODEC.parse(registryOps, compileRecipe(script))
                .resultOrPartial(error -> LOGGER.error("Invalid recipe script {}: {}", scriptFile, error))
                .orElse(null);
        if (recipe == null) {
            return;
        }
        if (recipe.getType() != RecipeType.CRAFTING) {
            LOGGER.error("Ignoring non-crafting recipe in script {}", scriptFile);
            return;
        }
        if (!recipeIds.add(id)) {
            LOGGER.error("Ignoring duplicate recipe id {} in script {}", id, scriptFile);
            return;
        }
        recipes.add(new RecipeHolder<>(id, recipe));
    }

    /** Parses the CraftTweaker ZenScript subset that Fallen Relics executes natively. */
    private static ZenScriptResult parseZenScript(String source) {
        List<ZenAddition> additions = new ArrayList<>();
        Set<ResourceLocation> removalOutputs = new LinkedHashSet<>();
        Set<ResourceLocation> removalIds = new LinkedHashSet<>();
        Set<String> usedIds = new LinkedHashSet<>();

        Matcher shaped = ZEN_SHAPED.matcher(source);
        while (shaped.find()) {
            additions.add(new ZenAddition(uniqueId(shaped.group(1), usedIds),
                    parseZenShaped(shaped.group(1), shaped.group(2), shaped.group(3), shaped.group(4))));
        }
        Matcher shapeless = ZEN_SHAPELESS.matcher(source);
        while (shapeless.find()) {
            additions.add(new ZenAddition(uniqueId(shapeless.group(1), usedIds),
                    parseZenShapeless(shapeless.group(1), shapeless.group(2), shapeless.group(3), shapeless.group(4))));
        }
        Matcher removeItem = ZEN_REMOVE_ITEM.matcher(source);
        while (removeItem.find()) {
            removalOutputs.add(parseItemId(removeItem.group(1)));
        }
        Matcher removeId = ZEN_REMOVE_ID.matcher(source);
        while (removeId.find()) {
            removalIds.add(parseItemId(removeId.group(1)));
        }
        return new ZenScriptResult(additions, removalOutputs, removalIds);
    }

    private static String uniqueId(String name, Set<String> usedIds) {
        String id = "ct_" + name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_/-]+", "_");
        if (id.length() > 190) {
            id = id.substring(0, 190);
        }
        if (ResourceLocation.tryParse(FallenRelicsMod.MOD_ID + ":" + id) == null) {
            id = "ct_recipe_" + hash(name);
        }
        String unique = id;
        for (int suffix = 2; !usedIds.add(unique); suffix++) {
            unique = id + "_" + suffix;
        }
        return unique;
    }

    private static RecipeScript parseZenShaped(
            String name, String result, String count, String gridSource) {
        List<ResourceLocation> ingredients = new ArrayList<>();
        Matcher rowMatcher = ZEN_ROW.matcher(gridSource);
        int rowCount = 0;
        while (rowMatcher.find()) {
            if (rowCount >= 3) {
                throw new IllegalArgumentException("CraftTweaker shaped recipe '" + name + "' has more than 3 rows");
            }
            String[] cells = rowMatcher.group(1).split(",");
            if (cells.length < 1 || cells.length > 3) {
                throw new IllegalArgumentException("CraftTweaker shaped recipe '" + name
                        + "' row must have 1-3 cells");
            }
            for (int column = 0; column < 3; column++) {
                if (column < cells.length) {
                    ingredients.add(parseZenCell(cells[column].trim(), name));
                } else {
                    ingredients.add(null);
                }
            }
            rowCount++;
        }
        while (rowCount < 3) {
            ingredients.add(null);
            ingredients.add(null);
            ingredients.add(null);
            rowCount++;
        }
        if (ingredients.stream().allMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("CraftTweaker shaped recipe '" + name + "' has no ingredients");
        }
        return new RecipeScript(false, parseItemId(result), zenCount(count), ingredients);
    }

    private static RecipeScript parseZenShapeless(
            String name, String result, String count, String listSource) {
        List<ResourceLocation> ingredients = new ArrayList<>();
        for (String cell : listSource.split(",")) {
            ResourceLocation item = parseZenCell(cell.trim(), name);
            if (item != null) {
                ingredients.add(item);
            }
        }
        if (ingredients.isEmpty() || ingredients.size() > CraftBuilderBlockEntity.INPUT_SLOTS) {
            throw new IllegalArgumentException("CraftTweaker shapeless recipe '" + name
                    + "' needs between 1 and 9 ingredients");
        }
        return new RecipeScript(true, parseItemId(result), zenCount(count), ingredients);
    }

    @Nullable
    private static ResourceLocation parseZenCell(String cell, String name) {
        if (cell.isEmpty() || cell.equals("null")) {
            return null;
        }
        Matcher item = ZEN_ITEM_BRACKET.matcher(cell);
        if (item.matches()) {
            ResourceLocation id = parseItemId(item.group(1));
            return id.toString().equals("minecraft:air") ? null : id;
        }
        throw new IllegalArgumentException("Unsupported ingredient '" + cell
                + "' in CraftTweaker recipe '" + name + "'");
    }

    private static int zenCount(String count) {
        if (count == null) {
            return 1;
        }
        int parsed = Integer.parseInt(count);
        if (parsed < 1 || parsed > 64) {
            throw new IllegalArgumentException("Result count must be between 1 and 64");
        }
        return parsed;
    }

    private static GeneratedRecipe generateRecipe(ItemStackHandler inventory, boolean shapeless) {
        ItemStack result = inventory.getStackInSlot(CraftBuilderBlockEntity.RESULT_SLOT);
        if (result.isEmpty()) {
            return null;
        }

        List<ResourceLocation> ingredients = new ArrayList<>();
        for (int slot = 0; slot < CraftBuilderBlockEntity.INPUT_SLOTS; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            ingredients.add(stack.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        if (ingredients.stream().allMatch(java.util.Objects::isNull)) {
            return null;
        }

        RecipeScript recipe = new RecipeScript(
                shapeless,
                BuiltInRegistries.ITEM.getKey(result.getItem()),
                result.getCount(),
                ingredients);
        String id = recipeId(recipe);
        return new GeneratedRecipe(id, serializeScript(recipe), fingerprint(recipe));
    }

    private static String recipeId(RecipeScript recipe) {
        String outputName = recipe.result().getNamespace() + "_" + recipe.result().getPath().replace('/', '_');
        return "craftbuilder_" + outputName + "_" + hash(fingerprint(recipe));
    }

    private static String fingerprint(RecipeScript recipe) {
        StringBuilder fingerprint = new StringBuilder(recipe.shapeless() ? "shapeless|" : "shaped|");
        fingerprint.append(recipe.result()).append('*').append(recipe.resultCount()).append('|');
        if (recipe.shapeless()) {
            recipe.ingredients().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(ResourceLocation::toString)
                    .sorted()
                    .forEach(id -> fingerprint.append(id).append('|'));
        } else {
            for (ResourceLocation ingredient : recipe.ingredients()) {
                fingerprint.append(ingredient == null ? "-" : ingredient.toString()).append('|');
            }
        }
        return fingerprint.toString();
    }

    private static String serializeScript(RecipeScript recipe) {
        StringBuilder source = new StringBuilder();
        source.append("# Fallen Relics standalone recipe script v1\n")
                .append("type ").append(recipe.shapeless() ? "shapeless" : "shaped").append('\n')
                .append("result ").append(recipe.result()).append(' ').append(recipe.resultCount()).append('\n');
        if (recipe.shapeless()) {
            source.append("ingredients\n");
            for (ResourceLocation ingredient : recipe.ingredients()) {
                if (ingredient != null) {
                    source.append(ingredient).append('\n');
                }
            }
        } else {
            source.append("grid\n");
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    if (column > 0) {
                        source.append(' ');
                    }
                    ResourceLocation ingredient = recipe.ingredients().get(row * 3 + column);
                    source.append(ingredient == null ? "_" : ingredient.toString());
                }
                source.append('\n');
            }
        }
        return source.toString();
    }

    private static RecipeScript parseScript(String source) {
        List<String> lines = source.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#") && !line.startsWith("//"))
                .toList();
        if (lines.size() < 4) {
            throw new IllegalArgumentException("Script is incomplete");
        }

        String[] typeLine = words(lines.get(0));
        if (typeLine.length != 2 || !typeLine[0].equals("type")) {
            throw new IllegalArgumentException("Expected 'type shaped' or 'type shapeless' on the first line");
        }
        boolean shapeless = switch (typeLine[1]) {
            case "shaped" -> false;
            case "shapeless" -> true;
            default -> throw new IllegalArgumentException("Unknown recipe type: " + typeLine[1]);
        };

        String[] resultLine = words(lines.get(1));
        if (!resultLine[0].equals("result") || resultLine.length < 2 || resultLine.length > 3) {
            throw new IllegalArgumentException("Expected 'result <item_id> [count]' on the second line");
        }
        ResourceLocation result = parseItemId(resultLine[1]);
        int resultCount = resultLine.length == 3 ? Integer.parseInt(resultLine[2]) : 1;
        if (resultCount < 1 || resultCount > 64) {
            throw new IllegalArgumentException("Result count must be between 1 and 64");
        }

        List<ResourceLocation> ingredients = new ArrayList<>();
        if (shapeless) {
            if (!lines.get(2).equals("ingredients")) {
                throw new IllegalArgumentException("Expected 'ingredients' after the result line");
            }
            for (int index = 3; index < lines.size(); index++) {
                String[] ingredientLine = words(lines.get(index));
                if (ingredientLine.length != 1) {
                    throw new IllegalArgumentException("Each shapeless ingredient must be one item id");
                }
                ingredients.add(parseItemId(ingredientLine[0]));
            }
            if (ingredients.isEmpty() || ingredients.size() > CraftBuilderBlockEntity.INPUT_SLOTS) {
                throw new IllegalArgumentException("A shapeless recipe needs between 1 and 9 ingredients");
            }
        } else {
            if (!lines.get(2).equals("grid") || lines.size() != 6) {
                throw new IllegalArgumentException("A shaped recipe needs 'grid' followed by exactly three rows");
            }
            for (int row = 0; row < 3; row++) {
                String[] cells = words(lines.get(3 + row));
                if (cells.length != 3) {
                    throw new IllegalArgumentException("Each shaped grid row must have exactly three cells");
                }
                for (String cell : cells) {
                    ingredients.add(cell.equals("_") || cell.equals("minecraft:air") ? null : parseItemId(cell));
                }
            }
            if (ingredients.stream().allMatch(java.util.Objects::isNull)) {
                throw new IllegalArgumentException("A shaped recipe must have at least one ingredient");
            }
        }
        return new RecipeScript(shapeless, result, resultCount, ingredients);
    }

    private static String[] words(String line) {
        return line.trim().split("\\s+");
    }

    private static ResourceLocation parseItemId(String id) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Invalid item id: " + id);
        }
        return resourceLocation;
    }

    private static JsonObject compileRecipe(RecipeScript recipe) {
        JsonObject json = new JsonObject();
        json.addProperty("type", recipe.shapeless()
                ? "minecraft:crafting_shapeless"
                : "minecraft:crafting_shaped");
        json.addProperty("group", "fallenrelics:scripted_crafting");

        if (recipe.shapeless()) {
            JsonArray ingredients = new JsonArray();
            recipe.ingredients().stream()
                    .filter(java.util.Objects::nonNull)
                    .forEach(ingredient -> ingredients.add(ingredientJson(ingredient)));
            json.add("ingredients", ingredients);
        } else {
            Map<ResourceLocation, Character> symbols = new LinkedHashMap<>();
            JsonArray pattern = new JsonArray();
            for (int row = 0; row < 3; row++) {
                StringBuilder line = new StringBuilder(3);
                for (int column = 0; column < 3; column++) {
                    ResourceLocation ingredient = recipe.ingredients().get(row * 3 + column);
                    if (ingredient == null) {
                        line.append(' ');
                    } else {
                        char symbol = symbols.computeIfAbsent(ingredient, key -> (char) ('A' + symbols.size()));
                        line.append(symbol);
                    }
                }
                pattern.add(line.toString());
            }
            JsonObject key = new JsonObject();
            for (Map.Entry<ResourceLocation, Character> entry : symbols.entrySet()) {
                key.add(String.valueOf(entry.getValue()), ingredientJson(entry.getKey()));
            }
            json.add("pattern", pattern);
            json.add("key", key);
        }

        JsonObject result = new JsonObject();
        result.addProperty("id", recipe.result().toString());
        result.addProperty("count", recipe.resultCount());
        json.add("result", result);
        return json;
    }

    private static JsonObject ingredientJson(ResourceLocation itemId) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", itemId.toString());
        return ingredient;
    }

    private static RecipeScript convertLegacyZenScript(String source) {
        Matcher shapedMatcher = LEGACY_SHAPED_ZEN.matcher(source);
        Matcher shapelessMatcher = LEGACY_SHAPELESS_ZEN.matcher(source);
        if (shapedMatcher.find()) {
            ResourceLocation result = parseItemId(shapedMatcher.group(1));
            int count = shapedMatcher.group(2) == null ? 1 : Integer.parseInt(shapedMatcher.group(2));
            List<ResourceLocation> ingredients = new ArrayList<>();
            Matcher rowMatcher = ZEN_ROW.matcher(shapedMatcher.group(3));
            int rowCount = 0;
            while (rowMatcher.find()) {
                Matcher itemMatcher = ZEN_ITEM_BRACKET.matcher(rowMatcher.group(1));
                int cellCount = 0;
                while (itemMatcher.find()) {
                    ResourceLocation item = parseItemId(itemMatcher.group(1));
                    ingredients.add(item.toString().equals("minecraft:air") ? null : item);
                    cellCount++;
                }
                if (cellCount != 3) {
                    throw new IllegalArgumentException("Old shaped ZenScript row did not contain three cells");
                }
                rowCount++;
            }
            if (rowCount != 3) {
                throw new IllegalArgumentException("Old shaped ZenScript did not contain three rows");
            }
            return new RecipeScript(false, result, count, ingredients);
        }
        if (shapelessMatcher.find()) {
            ResourceLocation result = parseItemId(shapelessMatcher.group(1));
            int count = shapelessMatcher.group(2) == null ? 1 : Integer.parseInt(shapelessMatcher.group(2));
            List<ResourceLocation> ingredients = new ArrayList<>();
            Matcher itemMatcher = ZEN_ITEM_BRACKET.matcher(shapelessMatcher.group(3));
            while (itemMatcher.find()) {
                ingredients.add(parseItemId(itemMatcher.group(1)));
            }
            if (ingredients.isEmpty() || ingredients.size() > CraftBuilderBlockEntity.INPUT_SLOTS) {
                throw new IllegalArgumentException("Old shapeless ZenScript has an invalid ingredient count");
            }
            return new RecipeScript(true, result, count, ingredients);
        }
        throw new IllegalArgumentException("Unrecognized generated recipe script");
    }

    private static RecipeScript convertLegacyRecipe(JsonObject recipe) {
        JsonObject result = recipe.getAsJsonObject("result");
        ResourceLocation resultId = parseItemId(result.get("id").getAsString());
        int resultCount = result.has("count") ? result.get("count").getAsInt() : 1;
        String type = recipe.get("type").getAsString();
        boolean shapeless = type.endsWith("crafting_shapeless");
        List<ResourceLocation> ingredients = new ArrayList<>();

        if (shapeless) {
            JsonArray legacyIngredients = recipe.getAsJsonArray("ingredients");
            for (var ingredient : legacyIngredients) {
                ingredients.add(parseItemId(ingredient.getAsJsonObject().get("item").getAsString()));
            }
        } else if (type.endsWith("crafting_shaped")) {
            JsonArray pattern = recipe.getAsJsonArray("pattern");
            JsonObject key = recipe.getAsJsonObject("key");
            for (int row = 0; row < 3; row++) {
                String line = row < pattern.size() ? pattern.get(row).getAsString() : "";
                for (int column = 0; column < 3; column++) {
                    char symbol = column < line.length() ? line.charAt(column) : ' ';
                    if (symbol == ' ') {
                        ingredients.add(null);
                    } else {
                        JsonObject ingredient = key.get(String.valueOf(symbol)).getAsJsonObject();
                        ingredients.add(parseItemId(ingredient.get("item").getAsString()));
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported old recipe type: " + type);
        }
        return new RecipeScript(shapeless, resultId, resultCount, ingredients);
    }

    private static String recipeIdFromScriptPath(Path relativeScriptPath) {
        String relative = relativeScriptPath.toString().replace('\\', '/');
        String path = relative.substring(0, relative.length() - SCRIPT_EXTENSION.length()).toLowerCase(Locale.ROOT);
        if (ResourceLocation.tryParse(FallenRelicsMod.MOD_ID + ":" + path) == null) {
            throw new IllegalArgumentException("Script filename is not a valid recipe id: " + relative);
        }
        return path;
    }

    private static String hash(String content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 10);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static Path scriptFile(String recipeId) {
        Path directory = scriptDirectory();
        Path script = directory.resolve(recipeId + SCRIPT_EXTENSION).normalize();
        if (!script.startsWith(directory)) {
            throw new IllegalArgumentException("Invalid recipe script id: " + recipeId);
        }
        return script;
    }

    private static Set<Path> findMatchingScripts(GeneratedRecipe generated) throws IOException {
        Set<Path> matches = new LinkedHashSet<>();
        Path scripts = scriptDirectory();
        if (!Files.isDirectory(scripts)) {
            return matches;
        }
        try (Stream<Path> files = Files.walk(scripts)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(SCRIPT_EXTENSION)).toList()) {
                try {
                    RecipeScript candidate = parseScript(Files.readString(file, StandardCharsets.UTF_8));
                    if (fingerprint(candidate).equals(generated.fingerprint())) {
                        matches.add(file);
                    }
                } catch (RuntimeException exception) {
                    LOGGER.warn("Skipping invalid script while searching for removal: {}", file, exception);
                }
            }
        }
        return matches;
    }

    private static Path scriptDirectory() {
        return FMLPaths.GAMEDIR.get().resolve("scripts").resolve(SCRIPT_SUBDIRECTORY);
    }

    private record CompileResult(
            List<RecipeHolder<?>> recipes,
            Set<ResourceLocation> removalOutputs,
            Set<ResourceLocation> removalIds) {}
    private record ZenAddition(String id, RecipeScript recipe) {}
    private record ZenScriptResult(
            List<ZenAddition> additions,
            Set<ResourceLocation> removalOutputs,
            Set<ResourceLocation> removalIds) {}
    private record GeneratedRecipe(String id, String source, String fingerprint) {}
    private record RecipeScript(
            boolean shapeless,
            ResourceLocation result,
            int resultCount,
            List<ResourceLocation> ingredients) {}
}
