package dev.agentsharik.fallenrelics;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Wires the "open JEI recipes" payload to the client handler. */
@EventBusSubscriber(modid = FallenRelicsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class JeiLinkerNetwork {
    private JeiLinkerNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        if (net.neoforged.fml.loading.FMLLoader.getDist().isClient()) {
            registrar.playToClient(
                    JeiRecipePayload.TYPE,
                    JeiRecipePayload.STREAM_CODEC,
                    (payload, context) -> context.enqueueWork(() -> JeiLinkerClient.openRecipesById(payload.item())));
        }
    }
}
