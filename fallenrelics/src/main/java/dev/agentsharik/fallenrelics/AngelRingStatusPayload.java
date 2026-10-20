package dev.agentsharik.fallenrelics;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server-to-client snapshot of the angel ring state for the relic scroll screen:
 * whether the ring currently grants flight, whether the player is airborne,
 * whether the slow-falling insurance is burning and how much of it is left,
 * plus the lifetime flight time in ticks.
 */
public record AngelRingStatusPayload(boolean granted, boolean flying, boolean buffering, int bufferTicks,
        long flightTicks, RelicData relic) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AngelRingStatusPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "angel_ring_status"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AngelRingStatusPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AngelRingStatusPayload decode(RegistryFriendlyByteBuf buf) {
                    return new AngelRingStatusPayload(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                            buf.readVarInt(), buf.readVarLong(), RelicData.STREAM_CODEC.decode(buf));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, AngelRingStatusPayload payload) {
                    buf.writeBoolean(payload.granted());
                    buf.writeBoolean(payload.flying());
                    buf.writeBoolean(payload.buffering());
                    buf.writeVarInt(payload.bufferTicks());
                    buf.writeVarLong(payload.flightTicks());
                    RelicData.STREAM_CODEC.encode(buf, payload.relic());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
