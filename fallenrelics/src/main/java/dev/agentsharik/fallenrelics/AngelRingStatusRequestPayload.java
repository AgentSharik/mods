package dev.agentsharik.fallenrelics;

import java.util.UUID;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-to-server ping sent while the relic scroll screen is open: the server
 * answers with an {@link AngelRingStatusPayload} snapshot. 1.8.1: the request
 * carries the UUID of the ring stack being viewed, so the snapshot (and every
 * action) belongs to that exact ring — equipped, in inventory, anywhere.
 * A nil UUID means "not rolled yet": the server resolves it to the first
 * unrolled ring stack and assigns the UUID during the roll.
 */
public record AngelRingStatusRequestPayload(UUID target) implements CustomPacketPayload {
    public static final UUID NO_TARGET = new UUID(0L, 0L);

    public static final CustomPacketPayload.Type<AngelRingStatusRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "angel_ring_status_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AngelRingStatusRequestPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AngelRingStatusRequestPayload decode(RegistryFriendlyByteBuf buf) {
                    return new AngelRingStatusRequestPayload(buf.readUUID());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, AngelRingStatusRequestPayload payload) {
                    buf.writeUUID(payload.target());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
