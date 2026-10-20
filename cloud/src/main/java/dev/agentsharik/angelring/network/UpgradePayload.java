package dev.agentsharik.angelring.network;

import dev.agentsharik.angelring.AngelRingMod;
import dev.agentsharik.angelring.ModComponents;
import dev.agentsharik.angelring.RelicData;
import dev.agentsharik.angelring.RingLookup;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Клиент просит сервер потратить очко улучшения. Всё проверяет и меняет сервер. */
public record UpgradePayload(int stat) implements CustomPacketPayload {
    public static final Type<UpgradePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, "upgrade"));

    public static final StreamCodec<ByteBuf, UpgradePayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(UpgradePayload::new, UpgradePayload::stat);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpgradePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            RingLookup.find(player).ifPresent(stack -> {
                RelicData old = stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
                RelicData updated = old.spend(payload.stat());
                if (updated != old) {
                    stack.set(ModComponents.RELIC_DATA.get(), updated);
                }
            });
        });
    }
}
