package dev.agentsharik.angelring.client;

import java.util.List;

import dev.agentsharik.angelring.AngelRingMod;
import dev.agentsharik.angelring.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Как в Relics: наведите курсор на кольцо в инвентаре и удерживайте Shift.
 * В подсказке заполняется шкала; как только она дойдёт до конца, окно открывается, а загрузка прекращается.
 *
 * Окно открывается не прямо во время отрисовки, а на следующем тике клиента (так надёжнее). После открытия
 * новая загрузка начнётся только когда вы отпустите Shift или уберёте курсор с кольца, поэтому «зацикливания» нет.
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID, value = Dist.CLIENT)
public final class HoldToOpen {
    private HoldToOpen() {}

    private static final long HOLD_MS = 700L;
    private static final int BAR_LENGTH = 24;

    private static long startMs = -1L;
    private static int heldSlot = -1;
    private static float progress = 0f;
    /** После открытия окна ждём, пока игрок отпустит Shift или уберёт курсор с кольца. */
    private static boolean needRelease = false;

    // что нужно открыть на ближайшем тике
    private static Screen pendingScreen = null;
    private static AbstractContainerMenu pendingMenu = null;
    private static int pendingSlot = -1;

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            reset();
            needRelease = false;
            return;
        }
        Slot slot = screen.getSlotUnderMouse();
        boolean overRing = slot != null && slot.hasItem() && slot.getItem().is(ModItems.ANGEL_RING.get());
        boolean shift = Screen.hasShiftDown();
        if (!overRing || !shift) {
            reset();
            needRelease = false;
            return;
        }
        if (needRelease || pendingScreen != null) {
            return; // окно уже открыто (или открывается) — ждём, пока игрок отпустит Shift
        }

        long now = Util.getMillis();
        if (heldSlot != slot.index || startMs < 0L) {
            heldSlot = slot.index;
            startMs = now;
        }
        progress = Math.min(1f, (now - startMs) / (float) HOLD_MS);
        if (progress >= 1f) {
            // шкала заполнена: загрузка закончена, окно откроется на ближайшем тике
            pendingScreen = screen;
            pendingMenu = screen.getMenu();
            pendingSlot = slot.index;
            needRelease = true;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (pendingScreen == null) {
            return;
        }
        Screen previous = pendingScreen;
        AbstractContainerMenu menu = pendingMenu;
        int slot = pendingSlot;
        pendingScreen = null;
        pendingMenu = null;
        pendingSlot = -1;
        reset();
        Minecraft.getInstance().setScreen(new RelicScreen(previous, menu, slot));
    }

    private static void reset() {
        startMs = -1L;
        heldSlot = -1;
        progress = 0f;
    }

    /** Строка «Удерживайте [Shift] для исследования…» и шкала под описанием кольца. */
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(ModItems.ANGEL_RING.get())) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.angelring.hold_shift").withStyle(ChatFormatting.GRAY));
        int filled = Math.round(progress * BAR_LENGTH);
        tooltip.add(Component.literal("\u2588".repeat(filled)).withStyle(ChatFormatting.DARK_RED)
                .append(Component.literal("\u2588".repeat(BAR_LENGTH - filled)).withStyle(ChatFormatting.DARK_GRAY)));
    }
}
