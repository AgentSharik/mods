package dev.agentsharik.fallenrelics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Relics-способ открытия свитка: навести курсор на кольцо в любом инвентаре и
 * нажать Shift (ловим момент нажатия, чтобы не переоткрывать окно после
 * возврата). Esc в свитке вернёт обратно в тот же инвентарь.
 */
@EventBusSubscriber(modid = FallenRelicsMod.MOD_ID, value = Dist.CLIENT)
public final class RelicHoverClient {
    private RelicHoverClient() {}

    /** findSlot is private in 1.21.1, so hit-test the menu slots by their screen rects. */
    private static Slot slotUnder(AbstractContainerScreen<?> container, double mx, double my) {
        for (Slot slot : container.getMenu().slots) {
            if (slot.isActive()
                    && mx >= slot.x && mx < slot.x + 16
                    && my >= slot.y && my < slot.y + 16) {
                return slot;
            }
        }
        return null;
    }

    private static boolean prevShift;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        boolean shift = Screen.hasShiftDown();
        boolean pressed = shift && !prevShift;
        prevShift = shift;
        if (!pressed || mc.player == null || !(mc.screen instanceof AbstractContainerScreen<?> container)) {
            return;
        }
        double scale = mc.getWindow().getGuiScale();
        double mx = mc.mouseHandler.xpos() / scale;
        double my = mc.mouseHandler.ypos() / scale;
        Slot slot = slotUnder(container, mx, my);
        if (slot == null || !slot.hasItem()) {
            return;
        }
        ItemStack stack = slot.getItem();
        if (!stack.is(ModContent.ANGEL_RING.get()) && !stack.has(ModContent.RELIC_DATA.get())) {
            return;
        }
        mc.setScreen(new AngelRingScreen(stack, container));
    }
}
