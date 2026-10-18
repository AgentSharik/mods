package dev.agentsharik.fallenrelics;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Client half of the Chat & Recipe Linker: one hotkey over JEI's ingredient list.
 * With a crafting recipe the chat opens with an interactive [Item] link inside;
 * without one the JEI search box is filled with the item name instead.
 */
@EventBusSubscriber(modid = FallenRelicsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class JeiLinkerClient {
    private static final net.minecraft.client.KeyMapping RECIPE_LINK_KEY = new net.minecraft.client.KeyMapping(
            "key.fallenrelics.jei_recipe_link",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.fallenrelics");

    private JeiLinkerClient() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(RECIPE_LINK_KEY);
        NeoForge.EVENT_BUS.addListener(JeiLinkerClient::onClientTick);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        if (RECIPE_LINK_KEY.consumeClick()) {
            handlePress();
        }
    }

    private static void handlePress() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen instanceof ChatScreen) {
            return;
        }
        if (!JeiLinkerJeiCalls.available()) {
            return;
        }
        ItemStack jeiStack = JeiLinkerJeiCalls.itemStackUnderMouse();
        if (jeiStack != null && !jeiStack.isEmpty()) {
            if (hasCraftingRecipe(minecraft, jeiStack)) {
                minecraft.setScreen(new ChatScreen(buildMarker(jeiStack, false)));
            } else {
                JeiLinkerJeiCalls.searchFor(jeiStack);
            }
            return;
        }
        // WoW-style: hover a slot in any open container (inventory included) and
        // link the exact stack, enchantments and all, without dropping it.
        ItemStack slotStack = stackUnderMouseInContainer(minecraft);
        if (slotStack != null) {
            minecraft.setScreen(new ChatScreen(buildMarker(slotStack, true)));
        }
    }

    @org.jetbrains.annotations.Nullable
    private static ItemStack stackUnderMouseInContainer(Minecraft minecraft) {
        if (!(minecraft.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen)) {
            return null;
        }
        double mouseX = minecraft.mouseHandler.xpos();
        double mouseY = minecraft.mouseHandler.ypos();
        for (net.minecraft.world.inventory.Slot slot : screen.getMenu().slots) {
            if (slot.hasItem() && slot.isMouseOver(mouseX, mouseY)) {
                return slot.getItem();
            }
        }
        return null;
    }

    private static boolean hasCraftingRecipe(Minecraft minecraft, ItemStack stack) {
        List<RecipeHolder<CraftingRecipe>> recipes = minecraft.player.level()
                .getRecipeManager()
                .getAllRecipesFor(RecipeType.CRAFTING);
        for (RecipeHolder<CraftingRecipe> holder : recipes) {
            ItemStack result = holder.value().getResultItem(minecraft.player.level().registryAccess());
            if (result.is(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    static String buildMarker(ItemStack stack, boolean exactStack) {
        String target;
        if (exactStack) {
            String token = java.util.UUID.randomUUID().toString();
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new LinkRegisterPayload(token, stack.copy()));
            target = "item:" + token;
        } else {
            target = "recipe:" + BuiltInRegistries.ITEM.getKey(stack.getItem());
        }
        return "<link:" + target + ">[" + stack.getHoverName().getString() + "]" + JeiLinkerServer.MARKER_SUFFIX;
    }

    /** Called from the network handler when somebody clicks a chat link. */
    public static void openRecipesById(ResourceLocation id) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!JeiLinkerJeiCalls.available()) {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("fallenrelics.jei_linker.missing"), true);
            }
            return;
        }
        BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> JeiLinkerJeiCalls.showRecipes(new ItemStack(item)));
    }
}
