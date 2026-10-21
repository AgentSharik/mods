package dev.agentsharik.fallenrelics;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Wires the relic scroll status request/answer pair to their handlers. */
@EventBusSubscriber(modid = FallenRelicsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class AngelRingNetwork {
    private AngelRingNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                RelicActionPayloads.Upgrade.TYPE,
                RelicActionPayloads.Upgrade.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> RelicAbilities.upgrade((net.minecraft.server.level.ServerPlayer) context.player(),
                                payload.index())));
        registrar.playToServer(
                RelicActionPayloads.Cast.TYPE,
                RelicActionPayloads.Cast.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> RelicAbilities.cast((net.minecraft.server.level.ServerPlayer) context.player(),
                                payload.index())));
        registrar.playToServer(
                RelicActionPayloads.Reroll.TYPE,
                RelicActionPayloads.Reroll.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> RelicAbilities.reroll((net.minecraft.server.level.ServerPlayer) context.player())));
        registrar.playToServer(
                AngelRingStatusRequestPayload.TYPE,
                AngelRingStatusRequestPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AngelRingFlight.sendStatus(context.player())));
        if (net.neoforged.fml.loading.FMLLoader.getDist().isClient()) {
            registrar.playToClient(
                    AngelRingStatusPayload.TYPE,
                    AngelRingStatusPayload.STREAM_CODEC,
                    (payload, context) -> context.enqueueWork(() -> AngelRingStatusClient.apply(payload)));
        }
    }
}
