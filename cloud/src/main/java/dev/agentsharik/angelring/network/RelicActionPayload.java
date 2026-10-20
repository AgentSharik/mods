package dev.agentsharik.angelring.network;

import dev.agentsharik.angelring.AngelRingMod;
import dev.agentsharik.angelring.RelicActions;
import dev.agentsharik.angelring.RelicData;
import dev.agentsharik.angelring.RingLookup;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Клиент просит сервер выполнить действие. Всё проверяет и меняет сервер.
 *
 * @param action   одно из RelicData.ACTION_*
 * @param arg      id способности, номер слота (для ACTION_USE) и т. п.
 * @param menuSlot номер слота открытого меню, где лежит кольцо; -1 — надетое кольцо
 */
public record RelicActionPayload(int action, int arg, int menuSlot) implements CustomPacketPayload {
    public static final Type<RelicActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, "action"));

    public static final StreamCodec<ByteBuf, RelicActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RelicActionPayload::action,
            ByteBufCodecs.VAR_INT, RelicActionPayload::arg,
            ByteBufCodecs.VAR_INT, RelicActionPayload::menuSlot,
            RelicActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RelicActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            // применять способности можно только с надетым кольцом
            int slot = payload.action() == RelicData.ACTION_USE ? -1 : payload.menuSlot();
            RingLookup.resolve(player, player.containerMenu, slot)
                    .ifPresent(stack -> RelicActions.perform(player, stack, payload.action(), payload.arg()));
        });
    }
}
