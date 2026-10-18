package dev.agentsharik.fallenrelics;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Registers the exact stack (components included) behind a chat link token, so the
 * server can show the real tooltip for linked inventory items without the item
 * ever leaving the player's inventory.
 */
public record LinkRegisterPayload(String token, ItemStack stack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LinkRegisterPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "link_register"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LinkRegisterPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, LinkRegisterPayload::token,
                    ItemStack.OPTIONAL_STREAM_CODEC, LinkRegisterPayload::stack,
                    LinkRegisterPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
