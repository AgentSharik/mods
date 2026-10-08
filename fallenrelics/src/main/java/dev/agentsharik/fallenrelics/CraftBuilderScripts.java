package dev.agentsharik.fallenrelics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HexFormat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.slf4j.Logger;

/** Saves in-game recipes as a normal world datapack so Minecraft syncs them to every player. */
public final class CraftBuilderScripts {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SCRIPT_PACK_FOLDER = "fallenrelics_scripts";
    private static final String SCRIPT_PACK_ID = "file/" + SCRIPT_PACK_FOLDER;
    private static final String NAMESPACE = FallenRelicsMod.MOD_ID;
    private static final String PACK_METADATA = "{\n  \"pack\": {\n    \"pack_format\": 48,\n    \"description\": \"Fallen Relics CraftBuilder recipes\"\n  }\n}\n";

    private CraftBuilderScripts() {}

    public static void saveRecipe(Player player, BlockPos pos, CraftBuilderBlockEntity builder) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        GeneratedRecipe recipe = generateRecipe(builder.getInventory(), builder.isShapeless());
        if (recipe == null) {
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.invalid"), true);
            return;
        }

        Path file = recipeFile(level.getServer(), recipe.id());
        try {
            ensureScriptPack(level.getServer());
            Files.createDirectories(file.getParent());
            Files.writeString(file, recipe.json().toString() + "\n", StandardCharsets.UTF_8);
            reloadScriptPack(level.getServer(), player,
                    Component.translatable("fallenrelics.craft_builder.saved", recipe.id()));
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not save CraftBuilder recipe at {}", pos, exception);
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.error"), true);
        }
    }

    public static void removeRecipe(
            Player player, BlockPos pos, CraftBuilderBlockEntity builder, boolean shapeless) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        GeneratedRecipe recipe = generateRecipe(builder.getInventory(), shapeless);
        if (recipe == null) {
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.invalid"), true);
            return;
        }

        Path file = recipeFile(level.getServer(), recipe.id());
        try {
            if (!Files.deleteIfExists(file)) {
                player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.not_found"), true);
                return;
            }
            reloadScriptPack(level.getServer(), player,
                    Component.translatable("fallenrelics.craft_builder.removed", recipe.id()));
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not remove CraftBuilder recipe at {}", pos, exception);
            player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.error"), true);
        }
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

        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", shapeless ? "minecraft:crafting_shapeless" : "minecraft:crafting_shaped");
        recipe.addProperty("group", "fallenrelics:craftbuilder");

        if (shapeless) {
            JsonArray ingredients = new JsonArray();
            for (ResourceLocation ingredientId : ingredientIds) {
                if (ingredientId != null) {
                    ingredients.add(ingredient(ingredientId));
                }
            }
            recipe.add("ingredients", ingredients);
        } else {
            Map<ResourceLocation, Character> symbols = new LinkedHashMap<>();
            JsonArray pattern = new JsonArray();
            for (int row = 0; row < 3; row++) {
                StringBuilder line = new StringBuilder(3);
                for (int column = 0; column < 3; column++) {
                    ResourceLocation ingredientId = ingredientIds.get(row * 3 + column);
                    if (ingredientId == null) {
                        line.append(' ');
                    } else {
                        char symbol = symbols.computeIfAbsent(ingredientId, key -> (char) ('A' + symbols.size()));
                        line.append(symbol);
                    }
                }
                pattern.add(line.toString());
            }
            JsonObject key = new JsonObject();
            for (Map.Entry<ResourceLocation, Character> entry : symbols.entrySet()) {
                key.add(String.valueOf(entry.getValue()), ingredient(entry.getKey()));
            }
            recipe.add("pattern", pattern);
            recipe.add("key", key);
        }

        ResourceLocation resultId = BuiltInRegistries.ITEM.getKey(result.getItem());
        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("id", resultId.toString());
        resultJson.addProperty("count", result.getCount());
        recipe.add("result", resultJson);

        String json = recipe.toString();
        String outputName = resultId.getNamespace() + "_" + resultId.getPath().replace('/', '_');
        return new GeneratedRecipe("craftbuilder_" + outputName + "_" + hash(json), recipe);
    }

    private static JsonObject ingredient(ResourceLocation itemId) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", itemId.toString());
        return ingredient;
    }

    private static String hash(String content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 10);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static Path recipeFile(MinecraftServer server, String recipeId) {
        return scriptPackRoot(server)
                .resolve("data")
                .resolve(NAMESPACE)
                .resolve("recipe")
                .resolve(recipeId + ".json");
    }

    private static Path scriptPackRoot(MinecraftServer server) {
        return server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(SCRIPT_PACK_FOLDER);
    }

    private static void ensureScriptPack(MinecraftServer server) throws IOException {
        Path root = scriptPackRoot(server);
        Files.createDirectories(root);
        Path metadata = root.resolve("pack.mcmeta");
        if (!Files.exists(metadata)) {
            Files.writeString(metadata, PACK_METADATA, StandardCharsets.UTF_8);
        }
    }

    private static void reloadScriptPack(MinecraftServer server, Player player, Component successMessage) {
        PackRepository repository = server.getPackRepository();
        repository.reload();
        if (!repository.getAvailableIds().contains(SCRIPT_PACK_ID)) {
            throw new IllegalStateException("CraftBuilder datapack was not discovered by the server");
        }

        List<String> selectedPacks = new ArrayList<>(repository.getSelectedIds());
        if (!selectedPacks.contains(SCRIPT_PACK_ID)) {
            selectedPacks.add(SCRIPT_PACK_ID);
        }
        server.reloadResources(selectedPacks).whenComplete((ignored, failure) -> server.execute(() -> {
            if (failure == null) {
                player.displayClientMessage(successMessage, true);
            } else {
                LOGGER.error("Could not reload the Fallen Relics CraftBuilder datapack", failure);
                player.displayClientMessage(Component.translatable("fallenrelics.craft_builder.reload_error"), true);
            }
        }));
    }

    private record GeneratedRecipe(String id, JsonObject json) {}
}
