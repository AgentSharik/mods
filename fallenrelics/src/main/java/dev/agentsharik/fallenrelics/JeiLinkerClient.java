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
import net.neoforged.neoforge.client.event.InputEvent;
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
        // Raw key events fire even while a container/JEI screen is open - unlike
        // KeyMapping#consumeClick, which vanilla only clicks when no screen is up.
        NeoForge.EVENT_BUS.addListener(JeiLinkerClient::onKeyInput);
    }

    private static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());
        if (!RECIPE_LINK_KEY.isActiveAndMatches(key) || !bindingModifiersDown()) {
            return;
        }
        handlePress();
    }

    /** Honours combos assigned in the controls screen (Ctrl/Shift/Alt + key). */
    private static boolean bindingModifiersDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        boolean ctrl = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
        boolean shift = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        boolean alt = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
        switch (RECIPE_LINK_KEY.getKeyModifier().name()) {
            case "CONTROL": return ctrl && !shift && !alt;
            case "SHIFT":   return shift && !ctrl && !alt;
            case "ALT":     return alt && !ctrl && !shift;
            default:        return !ctrl && !shift && !alt;
        }
    }

    private static void handlePress() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen instanceof ChatScreen) {
            return;
        }
        if (minecraft.screen != null
                && minecraft.screen.getFocused() instanceof net.minecraft.client.gui.components.EditBox) {
            return; // player is typing into a search box, not linking
        }
        ItemStack jeiStack = JeiLinkerJeiCalls.available() ? JeiLinkerJeiCalls.itemStackUnderMouse() : null;
        if (jeiStack != null && !jeiStack.isEmpty()) {
            if (hasCraftingRecipe(minecraft, jeiStack)) {
                openChatWithMarker(minecraft, jeiStack, false);
            } else {
                JeiLinkerJeiCalls.searchFor(jeiStack);
            }
            return;
        }
        // WoW-style: hover a slot in any open container (inventory included) and
        // link the exact stack, enchantments and all, without dropping it.
        // Works with or without JEI installed.
        ItemStack slotStack = stackUnderMouseInContainer(minecraft);
        if (slotStack != null) {
            openChatWithMarker(minecraft, slotStack, true);
        }
    }

    /** Opens the chat with the link already typed in, plus an actionbar hint. */
    private static void openChatWithMarker(Minecraft minecraft, ItemStack stack, boolean exactStack) {
        minecraft.setScreen(new ChatScreen(buildMarker(stack, exactStack)));
        minecraft.player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("fallenrelics.jei_linker.inserted"), true);
    }

    @org.jetbrains.annotations.Nullable
    private static ItemStack stackUnderMouseInContainer(Minecraft minecraft) {
        if (!(minecraft.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen)) {
            return null;
        }
        double mouseX = minecraft.mouseHandler.xpos();
        double mouseY = minecraft.mouseHandler.ypos();
        int left = screenField(screen, "leftPos");
        int top = screenField(screen, "topPos");
        for (net.minecraft.world.inventory.Slot slot : screen.getMenu().slots) {
            if (!slot.hasItem()) {
                continue;
            }
            int slotX = left + slot.x;
            int slotY = top + slot.y;
            if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                return slot.getItem();
            }
        }
        return null;
    }

    /** 1.21.1 keeps leftPos/topPos protected with no getters; read them once via reflection. */
    private static int screenField(net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen, String name) {
        try {
            java.lang.reflect.Field field = screen.getClass().getField(name);
            return field.getInt(screen);
        } catch (ReflectiveOperationException | LinkageError first) {
            try {
                java.lang.reflect.Field field = net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class
                        .getDeclaredField(name);
                field.setAccessible(true);
                return field.getInt(screen);
            } catch (ReflectiveOperationException second) {
                return 0;
            }
        }
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
