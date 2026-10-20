package dev.agentsharik.fallenrelics;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-to-server ping sent while the relic scroll screen is open: the server
 * answers with an {@link AngelRingStatusPayload} snapshot.
 */
public record AngelRingStatusRequestPayload() implements CustomPacketPayload {
    public static final AngelRingStatusRequestPayload INSTANCE = new AngelRingStatusRequestPayload();

    public static final CustomPacketPayload.Type<AngelRingStatusRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "angel_ring_status_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AngelRingStatusRequestPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AngelRingStatusRequestPayload decode(RegistryFriendlyByteBuf buf) {
                    return INSTANCE;
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, AngelRingStatusRequestPayload payload) {
                    // empty payload: nothing to write
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
