package dev.agentsharik.angelring;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Заряд полёта «Пробуждённого кольца»: сколько тиков полёта осталось (максимум 1200 = 60 секунд) и
 * не заблокирован ли полёт до полной перезарядки.
 */
public record FlightState(int ticks, boolean locked) {
    public static final int MAX_TICKS = 20 * 60;
    public static final FlightState FULL = new FlightState(MAX_TICKS, false);

    public static final Codec<FlightState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("ticks", MAX_TICKS).forGetter(FlightState::ticks),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(FlightState::locked)
    ).apply(i, FlightState::new));

    public static final StreamCodec<ByteBuf, FlightState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FlightState::ticks,
            ByteBufCodecs.BOOL, FlightState::locked,
            FlightState::new);
}
