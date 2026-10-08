package dev.agentsharik.fallenrelics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/** Persists generated ZenScript files in the same scripts directory CraftTweaker reloads. */
public final class CraftBuilderScripts {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LEGACY_DATAPACK_FOLDER = "fallenrelics_scripts";
    private static final String NAMESPACE = FallenRelicsMod.MOD_ID;
    private static final String SCRIPT_SUBDIRECTORY = "fallenrelics";

    private CraftBuilderScripts() {}

    public static void saveRecipe(Player player, ItemStackHandler inventory, boolean shapeless) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!requireCraftTweaker(player)) {
            return;
        }

        GeneratedRecipe recipe = generateRecipe(inventory, shapeless);
        if (recipe == null) {
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.invalid"), true);
            return;
        }

        try {
            Path script = scriptFile(recipe.id());
            Files.createDirectories(script.getParent());
            Files.writeString(script, recipe.script(), StandardCharsets.UTF_8);
            reloadScripts(level.getServer(), player,
                    Component.translatable("fallenrelics.craft_builder.saved", recipe.id()));
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not save CraftBuilder script for {}", player.getGameProfile().getName(), exception);
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.error"), true);
        }
    }

    public static void removeRecipe(Player player, ItemStackHandler inventory, boolean shapeless) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!requireCraftTweaker(player)) {
            return;
        }

        GeneratedRecipe recipe = generateRecipe(inventory, shapeless);
        if (recipe == null) {
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.invalid"), true);
            return;
        }

        try {
            if (!Files.deleteIfExists(scriptFile(recipe.id()))) {
                player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.not_found"), true);
                return;
            }
            reloadScripts(level.getServer(), player,
                    Component.translatable("fallenrelics.craft_builder.removed", recipe.id()));
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not remove CraftBuilder script {}", recipe.id(), exception);
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.error"), true);
        }
    }

    /** Converts scripts saved by the previous JSON-datapack version without losing recipes. */
    public static void migrateLegacyDatapack(MinecraftServer server) {
        if (!ModList.get().isLoaded("crafttweaker")) {
            return;
        }

        Path legacyRecipes = server.getWorldPath(LevelResource.DATAPACK_DIR)
                .resolve(LEGACY_DATAPACK_FOLDER)
                .resolve("data")
                .resolve(NAMESPACE)
                .resolve("recipe");
        if (!Files.isDirectory(legacyRecipes)) {
            return;
        }

        boolean changed = false;
        try (var files = Files.list(legacyRecipes)) {
            for (Path file : files.filter(path -> path.getFileName().toString().startsWith("craftbuilder_")
                    && path.getFileName().toString().endsWith(".json")).toList()) {
                try {
                    GeneratedRecipe migrated = convertLegacyRecipe(
                            JsonParser.parseString(Files.readString(file)).getAsJsonObject());
                    Path destination = scriptFile(migrated.id());
                    Files.createDirectories(destination.getParent());
                    if (!Files.exists(destination)) {
                        Files.writeString(destination, migrated.script(), StandardCharsets.UTF_8);
                    }
                    changed |= Files.deleteIfExists(file);
                } catch (RuntimeException | IOException exception) {
                    LOGGER.warn("Could not migrate legacy CraftBuilder recipe {}", file, exception);
                }
            }
        } catch (IOException exception) {
            LOGGER.error("Could not scan the legacy CraftBuilder datapack", exception);
        }

        if (changed) {
            LOGGER.info("Migrated legacy CraftBuilder recipes to CraftTweaker scripts");
            reloadScripts(server, null, null);
        }
    }

    private static boolean requireCraftTweaker(Player player) {
        if (ModList.get().isLoaded("crafttweaker")) {
            return true;
        }
        player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.crafttweaker_required"), true);
        return false;
    }

    private static GeneratedRecipe generateRecipe(ItemStackHandler inventory, boolean shapeless) {
        ItemStack result = inventory.getStackInSlot(CraftBuilderBlockEntity.RESULT_SLOT);
        if (result.isEmpty()) {
            return null;
        }

        List<ResourceLocation> ingredientIds = new ArrayList<>();
        for (int slot = 0; slot < CraftBuilderBlockEntity.INPUT_SLOTS; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            ingredientIds.add(stack.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        if (ingredientIds.stream().allMatch(java.util.Objects::isNull)) {
            return null;
        }

        ResourceLocation resultId = BuiltInRegistries.ITEM.getKey(result.getItem());
        String fingerprint = fingerprint(ingredientIds, resultId, result.getCount(), shapeless);
        String outputName = resultId.getNamespace() + "_" + resultId.getPath().replace('/', '_');
        String id = "craftbuilder_" + outputName + "_" + hash(fingerprint);
        return new GeneratedRecipe(id,
                createCraftTweakerScript(id, resultId, result.getCount(), ingredientIds, shapeless));
    }

    private static String fingerprint(
            List<ResourceLocation> ingredientIds, ResourceLocation resultId, int resultCount, boolean shapeless) {
        StringBuilder fingerprint = new StringBuilder(shapeless ? "shapeless|" : "shaped|");
        fingerprint.append(resultId).append('*').append(resultCount).append('|');
        if (shapeless) {
            ingredientIds.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(ResourceLocation::toString)
                    .sorted()
                    .forEach(id -> fingerprint.append(id).append('|'));
        } else {
            for (ResourceLocation ingredientId : ingredientIds) {
                fingerprint.append(ingredientId == null ? "-" : ingredientId.toString()).append('|');
            }
        }
        return fingerprint.toString();
    }

    private static String createCraftTweakerScript(
            String recipeId, ResourceLocation resultId, int resultCount,
            List<ResourceLocation> ingredientIds, boolean shapeless) {
        StringBuilder script = new StringBuilder();
        script.append("// Generated by Fallen Relics CraftBuilder.\n")
                .append("// This is a normal CraftTweaker ZenScript recipe.\n");
        script.append(shapeless ? "craftingTable.addShapeless(\"" : "craftingTable.addShaped(\"")
                .append(recipeId).append("\", ")
                .append(craftTweakerItem(resultId, resultCount)).append(", ");

        if (shapeless) {
            script.append("[ ");
            boolean first = true;
            for (ResourceLocation ingredientId : ingredientIds) {
                if (ingredientId == null) {
                    continue;
                }
                if (!first) {
                    script.append(", ");
                }
                script.append(craftTweakerItem(ingredientId, 1));
                first = false;
            }
            script.append(" ]);\n");
        } else {
            script.append("[\n");
            for (int row = 0; row < 3; row++) {
                script.append("    [");
                for (int column = 0; column < 3; column++) {
                    if (column > 0) {
                        script.append(", ");
                    }
                    ResourceLocation ingredientId = ingredientIds.get(row * 3 + column);
                    script.append(ingredientId == null
                            ? "<item:minecraft:air>"
                            : craftTweakerItem(ingredientId, 1));
                }
                script.append(row == 2 ? "]\n" : "],\n");
            }
            script.append("]);\n");
        }
        return script.toString();
    }

    private static GeneratedRecipe convertLegacyRecipe(JsonObject recipe) {
        JsonObject result = recipe.getAsJsonObject("result");
        ResourceLocation resultId = ResourceLocation.parse(result.get("id").getAsString());
        int resultCount = result.has("count") ? result.get("count").getAsInt() : 1;
        String type = recipe.get("type").getAsString();
        boolean shapeless = type.endsWith("crafting_shapeless");
        List<ResourceLocation> ingredientIds = new ArrayList<>();

        if (shapeless) {
            JsonArray ingredients = recipe.getAsJsonArray("ingredients");
            for (JsonElement ingredient : ingredients) {
                ingredientIds.add(ResourceLocation.parse(ingredient.getAsJsonObject().get("item").getAsString()));
            }
            while (ingredientIds.size() < CraftBuilderBlockEntity.INPUT_SLOTS) {
                ingredientIds.add(null);
            }
        } else if (type.endsWith("crafting_shaped")) {
            JsonArray pattern = recipe.getAsJsonArray("pattern");
            JsonObject key = recipe.getAsJsonObject("key");
            for (int row = 0; row < 3; row++) {
                String line = pattern.get(row).getAsString();
                for (int column = 0; column < 3; column++) {
                    char symbol = line.charAt(column);
                    if (symbol == ' ') {
                        ingredientIds.add(null);
                    } else {
                        JsonObject ingredient = key.get(String.valueOf(symbol)).getAsJsonObject();
                        ingredientIds.add(ResourceLocation.parse(ingredient.get("item").getAsString()));
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported old recipe type: " + type);
        }

        String id = "craftbuilder_" + resultId.getNamespace() + "_"
                + resultId.getPath().replace('/', '_') + "_"
                + hash(fingerprint(ingredientIds, resultId, resultCount, shapeless));
        return new GeneratedRecipe(id,
                createCraftTweakerScript(id, resultId, resultCount, ingredientIds, shapeless));
    }

    private static String craftTweakerItem(ResourceLocation itemId, int count) {
        String item = "<item:" + itemId + ">";
        return count > 1 ? item + " * " + count : item;
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
        return FMLPaths.GAMEDIR.get()
                .resolve("scripts")
                .resolve(SCRIPT_SUBDIRECTORY)
                .resolve(recipeId + ".zs");
    }

    private static void reloadScripts(
            MinecraftServer server, @Nullable Player player, @Nullable Component successMessage) {
        PackRepository repository = server.getPackRepository();
        List<String> selectedPacks = new ArrayList<>(repository.getSelectedIds());
        server.reloadResources(selectedPacks).whenComplete((ignored, failure) -> server.execute(() -> {
            if (failure == null) {
                if (player != null && successMessage != null) {
                    player.displayClientMessage(successMessage, true);
                }
            } else {
                LOGGER.error("Could not reload CraftTweaker scripts after a CraftBuilder change", failure);
                if (player != null) {
                    player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.reload_error"), true);
                }
            }
        }));
    }

    private record GeneratedRecipe(String id, String script) {}
}
