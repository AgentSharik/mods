package dev.agentsharik.fallenrelics;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** "Open the JEI recipe view for this item" — sent to the player who clicked a chat link. */
public record JeiRecipePayload(ResourceLocation item) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JeiRecipePayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "jei_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JeiRecipePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, JeiRecipePayload::item,
                    JeiRecipePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
