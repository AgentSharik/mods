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
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Как в Relics: наведите курсор на кольцо в инвентаре и удерживайте Shift.
 * В подсказке заполняется шкала, и когда она заполнится — открывается окно кольца.
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID, value = Dist.CLIENT)
public final class HoldToOpen {
    private HoldToOpen() {}

    private static final long HOLD_MS = 700L;
    private static final int BAR_LENGTH = 24;

    private static long startMs = -1L;
    private static int heldSlot = -1;
    private static float progress = 0f;
    private static long lockUntil = 0L;

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            reset();
            return;
        }
        Slot slot = screen.getSlotUnderMouse();
        long now = Util.getMillis();
        boolean holding = slot != null
                && slot.hasItem()
                && slot.getItem().is(ModItems.ANGEL_RING.get())
                && Screen.hasShiftDown()
                && now > lockUntil;
        if (!holding) {
            reset();
            return;
        }
        if (heldSlot != slot.index || startMs < 0L) {
            heldSlot = slot.index;
            startMs = now;
        }
        progress = Math.min(1f, (now - startMs) / (float) HOLD_MS);
        if (progress >= 1f) {
            int index = slot.index;
            reset();
            lockUntil = now + 400L; // чтобы окно не открылось повторно сразу после закрытия
            Minecraft.getInstance().setScreen(new RelicScreen(screen, screen.getMenu(), index));
        }
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
