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
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
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
        // Client-only fallback: decorate own links and answer link clicks locally
        // when the server does not have the mod installed.
        NeoForge.EVENT_BUS.addListener(JeiLinkerClient::onChatReceived);
        // Universal hover source: whatever item tooltip is on screen right now
        // (any container, AE2/Ars terminals, JEI list and recipes gui included).
        NeoForge.EVENT_BUS.addListener(JeiLinkerClient::onTooltipRender);
        NeoForge.EVENT_BUS.addListener(JeiLinkerClient::registerClientCommands);
    }

    private static volatile ItemStack lastTooltipStack = ItemStack.EMPTY;
    private static volatile long lastTooltipAt;

    private static void onTooltipRender(net.neoforged.neoforge.client.event.RenderTooltipEvent.Pre event) {
        lastTooltipStack = event.getItemStack();
        lastTooltipAt = System.currentTimeMillis();
    }

    @org.jetbrains.annotations.Nullable
    private static ItemStack freshTooltipStack() {
        ItemStack stack = lastTooltipStack;
        if (stack != null && !stack.isEmpty() && System.currentTimeMillis() - lastTooltipAt < 250L) {
            return stack;
        }
        return null;
    }

    /** Client-side twin of the server token registry, for servers without the mod. */
    private static final java.util.Map<String, ClientToken> CLIENT_TOKENS = new java.util.concurrent.ConcurrentHashMap<>();

    private record ClientToken(ItemStack stack, long expiresAt) {}

    private static void rememberToken(String token, ItemStack stack) {
        if (CLIENT_TOKENS.size() > 512) {
            long now = System.currentTimeMillis();
            CLIENT_TOKENS.entrySet().removeIf(entry -> entry.getValue().expiresAt() < now);
        }
        CLIENT_TOKENS.put(token, new ClientToken(stack.copy(), System.currentTimeMillis() + 600_000L));
    }

    @org.jetbrains.annotations.Nullable
    private static ItemStack clientTokenStack(String token) {
        ClientToken entry = CLIENT_TOKENS.get(token);
        if (entry == null || entry.expiresAt() < System.currentTimeMillis()) {
            return null;
        }
        return entry.stack();
    }

    /** The server never decorated the message (no mod there): do it locally. */
    private static void onChatReceived(ClientChatReceivedEvent event) {
        net.minecraft.network.chat.Component message = event.getMessage();
        String raw = message.getString();
        if (raw != null && raw.contains(JeiLinkerServer.MARKER_PREFIX)) {
            event.setMessage(JeiLinkerServer.decorate(raw, JeiLinkerClient::clientTokenStack));
            actionBar(Minecraft.getInstance(), "fallenrelics.jei_linker.ctrl_hint");
            return;
        }
        if (hasLinkClickEvent(message)) {
            actionBar(Minecraft.getInstance(), "fallenrelics.jei_linker.ctrl_hint");
        }
    }

    /** True when any part of the message carries our recipe-opening click event. */
    private static boolean hasLinkClickEvent(net.minecraft.network.chat.Component message) {
        net.minecraft.network.chat.ClickEvent click = message.getStyle().getClickEvent();
        if (click != null && click.getValue() != null && click.getValue().startsWith("/fallenrelics jei_recipe")) {
            return true;
        }
        for (net.minecraft.network.chat.Component sibling : message.getSiblings()) {
            if (hasLinkClickEvent(sibling)) {
                return true;
            }
        }
        return false;
    }

    /** NeoForge routes RUN_COMMAND clicks through client commands, so this answers link clicks locally. */
    private static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("fallenrelics")
                .then(net.minecraft.commands.Commands.literal("jei_recipe")
                        .then(net.minecraft.commands.Commands.argument("item", net.minecraft.commands.arguments.ResourceLocationArgument.id())
                                .executes(context -> {
                                    openRecipesById(net.minecraft.commands.arguments.ResourceLocationArgument.getId(context, "item"));
                                    return 1;
                                }))));
    }

    /** Ctrl now, or pressed a moment ago: on modded servers the click reply arrives over the network. */
    private static boolean ctrlHeldForClick() {
        return net.minecraft.client.gui.screens.Screen.hasControlDown()
                || System.currentTimeMillis() - lastCtrlPress < 700L;
    }

    private static volatile long lastCtrlPress;

    private static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS
                && (event.getKey() == GLFW.GLFW_KEY_LEFT_CONTROL || event.getKey() == GLFW.GLFW_KEY_RIGHT_CONTROL)) {
            lastCtrlPress = System.currentTimeMillis();
        }
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());
        if (!RECIPE_LINK_KEY.isActiveAndMatches(key) || !bindingModifiersDown()) {
            return;
        }
        handlePress();
    }

    /** Letters/digits/numpad type into a focused search box; F-keys and the like do not. */
    private static boolean isTypeableKey(int keyCode) {
        return keyCode >= org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE && keyCode <= org.lwjgl.glfw.GLFW.GLFW_KEY_MENU
                && keyCode < org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
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
                && minecraft.screen.getFocused() instanceof net.minecraft.client.gui.components.EditBox
                && isTypeableKey(RECIPE_LINK_KEY.getKey().getValue())) {
            return; // player is typing into a search box and the bound key would type a char
        }
        try {
            // 1) the item tooltip currently on screen: exact stack in ANY gui
            ItemStack tooltipStack = freshTooltipStack();
            if (tooltipStack != null) {
                openChatWithMarker(minecraft, tooltipStack, true);
                return;
            }
            // 2) a container slot under the cursor: carries the exact stack too
            ItemStack slotStack = stackUnderMouseInContainer(minecraft);
            if (slotStack != null) {
                openChatWithMarker(minecraft, slotStack, true);
                return;
            }
            // 3) anything JEI renders under the cursor: list, recipes gui, bookmarks, groups
            double mouseX = minecraft.mouseHandler.xpos();
            double mouseY = minecraft.mouseHandler.ypos();
            ItemStack jeiStack = JeiLinkerJeiCalls.available()
                    ? JeiLinkerJeiCalls.ingredientUnderMouseAnywhere(minecraft.screen, mouseX, mouseY)
                    : null;
            if (jeiStack == null && JeiLinkerJeiCalls.available()) {
                jeiStack = JeiLinkerJeiCalls.itemStackUnderMouse();
            }
            if (jeiStack != null && !jeiStack.isEmpty()) {
                openChatWithMarker(minecraft, jeiStack, false);
                return;
            }
            if (minecraft.screen != null) {
                actionBar(minecraft, "fallenrelics.jei_linker.no_item");
            }
        } catch (Throwable boom) {
            actionBar(minecraft, "fallenrelics.jei_linker.error", boom.getClass().getSimpleName());
        }
    }

    private static void actionBar(Minecraft minecraft, String key, Object... args) {
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(key, args), true);
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
        // 1) vanilla tracks the hovered slot itself every frame - true for any container gui,
        //    including modpack screens with non-vanilla layouts
        net.minecraft.world.inventory.Slot slot = hoveredSlotField(screen);
        if (slot != null && slot.hasItem()) {
            return slot.getItem();
        }
        // 2) vanilla's own coordinate lookup
        slot = findSlotMethod(screen, mouseX, mouseY);
        if (slot != null && slot.hasItem()) {
            return slot.getItem();
        }
        // 3) last resort: manual math with a centred-window guess
        int left = screenField(screen, "leftPos");
        int top = screenField(screen, "topPos");
        if (left <= 0 && top <= 0) {
            left = (screen.width - 176) / 2;
            top = (screen.height - 166) / 2;
        }
        for (net.minecraft.world.inventory.Slot each : screen.getMenu().slots) {
            if (!each.hasItem()) {
                continue;
            }
            int slotX = left + each.x;
            int slotY = top + each.y;
            if (mouseX >= slotX - 1 && mouseX < slotX + 17 && mouseY >= slotY - 1 && mouseY < slotY + 17) {
                return each.getItem();
            }
        }
        return null;
    }

    @org.jetbrains.annotations.Nullable
    private static net.minecraft.world.inventory.Slot hoveredSlotField(
            net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen) {
        for (String name : new String[]{"hoveredSlot", "hoverSlot"}) {
            try {
                java.lang.reflect.Field field = net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class
                        .getDeclaredField(name);
                field.setAccessible(true);
                if (field.get(screen) instanceof net.minecraft.world.inventory.Slot slot) {
                    return slot;
                }
            } catch (ReflectiveOperationException ignored) {
                // try the next candidate name
            }
        }
        return null;
    }

    @org.jetbrains.annotations.Nullable
    private static net.minecraft.world.inventory.Slot findSlotMethod(
            net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        try {
            java.lang.reflect.Method method = net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class
                    .getDeclaredMethod("findSlot", double.class, double.class);
            method.setAccessible(true);
            if (method.invoke(screen, mouseX, mouseY) instanceof net.minecraft.world.inventory.Slot slot) {
                return slot;
            }
        } catch (ReflectiveOperationException ignored) {
            // fall through to manual math
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

    static String buildMarker(ItemStack stack, boolean exactStack) {
        String target;
        if (exactStack) {
            String token = java.util.UUID.randomUUID().toString();
            rememberToken(token, stack);
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
        if (!ctrlHeldForClick()) {
            // agreed behaviour: recipes open on Ctrl+LMB over the link only
            actionBar(minecraft, "fallenrelics.jei_linker.ctrl_hint");
            return;
        }
        if (!JeiLinkerJeiCalls.available()) {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("fallenrelics.jei_linker.missing"), true);
            }
            return;
        }
        BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> {
            ItemStack stack = new ItemStack(item);
            JeiLinkerJeiCalls.showRecipes(stack);
            actionBar(minecraft, "fallenrelics.jei_linker.opening", stack.getHoverName());
        });
    }
}
