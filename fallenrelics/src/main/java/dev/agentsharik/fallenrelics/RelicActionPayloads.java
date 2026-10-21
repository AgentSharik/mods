package dev.agentsharik.fallenrelics;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Три действия свитка реликвии, все исполняются и проверяются на сервере:
 * улучшение ранга способности за уровни игрока, применение активной способности
 * и реролл рарности за 2 уровня опыта.
 */
public final class RelicActionPayloads {
    private RelicActionPayloads() {}

    public record Upgrade(int index) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Upgrade> TYPE =
                new CustomPacketPayload.Type<>(
                        ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "relic_upgrade"));
        public static final StreamCodec<FriendlyByteBuf, Upgrade> STREAM_CODEC =
                StreamCodec.of((buf, p) -> buf.writeVarInt(p.index), buf -> new Upgrade(buf.readVarInt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Cast(int index) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Cast> TYPE =
                new CustomPacketPayload.Type<>(
                        ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "relic_cast"));
        public static final StreamCodec<FriendlyByteBuf, Cast> STREAM_CODEC =
                StreamCodec.of((buf, p) -> buf.writeVarInt(p.index), buf -> new Cast(buf.readVarInt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Reroll() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Reroll> TYPE =
                new CustomPacketPayload.Type<>(
                        ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "relic_reroll"));
        public static final StreamCodec<FriendlyByteBuf, Reroll> STREAM_CODEC =
                StreamCodec.of((buf, p) -> buf.writeVarInt(0), buf -> new Reroll());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
