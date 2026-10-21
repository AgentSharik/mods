package dev.agentsharik.angelring.network;

import dev.agentsharik.angelring.AngelRingMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Сервер → клиент: результат действия (3 числа). По нему клиент играет звук успеха/ошибки
 * и снимает «ожидание» с кнопок. Сами данные кольца приходят обычной синхронизацией компонента предмета,
 * поэтому цифры на экране всегда равны серверным.
 *
 * @param action RelicData.ACTION_*
 * @param arg    id способности
 * @param code   RelicActions.Result.*
 */
public record RelicResultPayload(int action, int arg, int code) implements CustomPacketPayload {
    public static final Type<RelicResultPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, "result"));

    public static final StreamCodec<ByteBuf, RelicResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RelicResultPayload::action,
            ByteBufCodecs.VAR_INT, RelicResultPayload::arg,
            ByteBufCodecs.VAR_INT, RelicResultPayload::code,
            RelicResultPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
