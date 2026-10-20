package dev.agentsharik.fallenrelics;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-to-server request to spend one upgrade point on a relic ability
 * (RelicData.STAT_SPEED / STAT_CUSHION). The server validates and applies it.
 */
public record AngelRingUpgradePayload(int stat) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AngelRingUpgradePayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "angel_ring_upgrade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AngelRingUpgradePayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(AngelRingUpgradePayload::new, AngelRingUpgradePayload::stat);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
