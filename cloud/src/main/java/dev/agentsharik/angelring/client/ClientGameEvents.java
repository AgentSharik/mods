package dev.agentsharik.angelring.client;

import dev.agentsharik.angelring.AngelRingMod;
import dev.agentsharik.angelring.RingLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AngelRingMod.MOD_ID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (ClientModEvents.OPEN_KEY.consumeClick()) {
            if (mc.player == null || mc.screen != null) {
                continue;
            }
            if (RingLookup.find(mc.player).isPresent()) {
                mc.setScreen(new RelicScreen());
            } else {
                mc.player.displayClientMessage(Component.translatable("message.angelring.not_worn"), true);
            }
        }
    }
}
