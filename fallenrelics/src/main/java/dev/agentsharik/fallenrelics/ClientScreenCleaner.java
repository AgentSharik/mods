package dev.agentsharik.fallenrelics;

import java.util.List;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Keeps third-party helper buttons (e.g. Crafting Tweaks' rotate/clear row and
 * corner button) off the Fallen Relics editor screens: the window must stay a
 * 1:1 copy of the vanilla crafting table / furnace art with only our three
 * icon buttons.
 */
@EventBusSubscriber(modid = FallenRelicsMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ClientScreenCleaner {
    private static final String[] FOREIGN_PACKAGES = {
            "net.blay09.mods.craftingtweaks",
    };

    private ClientScreenCleaner() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof CraftBuilderScreen screen)) {
            return;
        }
        List<GuiEventListener> foreign = screen.children().stream()
                .filter(child -> isForeign(child.getClass()))
                .toList();
        foreign.forEach(screen::removeWidget);
    }

    private static boolean isForeign(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            String name = current.getName();
            for (String prefix : FOREIGN_PACKAGES) {
                if (name.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }
}
