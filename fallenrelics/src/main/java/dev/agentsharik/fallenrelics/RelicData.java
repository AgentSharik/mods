package dev.agentsharik.fallenrelics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Прокачка кольца ангела в духе исходного мода Relics: хранится прямо в предмете
 * (data-компонент), поэтому уровень и ранги путешествуют с кольцом куда угодно.
 *
 * @param level   уровень реликвии (0..MAX_LEVEL)
 * @param xp      опыт внутри текущего уровня
 * @param points  непотраченные очки улучшений (+1 за каждый новый уровень)
 * @param speed   ранг способности «Полёт» (+15% скорости полёта за ранг)
 * @param cushion ранг способности «Мягкое падение» (+40 тиков страховки за ранг)
 */
public record RelicData(int level, int xp, int points, int speed, int cushion) {
    public static final int MAX_LEVEL = 10;
    public static final int MAX_RANK = 5;
    public static final int STAT_SPEED = 0;
    public static final int STAT_CUSHION = 1;

    /** Опыт за секунду полёта. */
    public static final int XP_PER_SECOND = 5;

    public static final RelicData EMPTY = new RelicData(0, 0, 0, 0, 0);

    public static final Codec<RelicData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("level", 0).forGetter(RelicData::level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(RelicData::xp),
            Codec.INT.optionalFieldOf("points", 0).forGetter(RelicData::points),
            Codec.INT.optionalFieldOf("speed", 0).forGetter(RelicData::speed),
            Codec.INT.optionalFieldOf("cushion", 0).forGetter(RelicData::cushion)
    ).apply(i, RelicData::new));

    public static final StreamCodec<ByteBuf, RelicData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RelicData::level,
            ByteBufCodecs.VAR_INT, RelicData::xp,
            ByteBufCodecs.VAR_INT, RelicData::points,
            ByteBufCodecs.VAR_INT, RelicData::speed,
            ByteBufCodecs.VAR_INT, RelicData::cushion,
            RelicData::new);

    public boolean maxed() {
        return level >= MAX_LEVEL;
    }

    /** Сколько опыта нужно до следующего уровня. */
    public int xpToNext() {
        return 300 + 150 * level;
    }

    /** Множитель скорости полёта (+15% за ранг). */
    public float speedMultiplier() {
        return 1.0F + 0.15F * speed;
    }

    /** Тики страховки мягкого падения (10 секунд плюс 2 секунды за ранг). */
    public int cushionTicks() {
        return 200 + 40 * cushion;
    }

    public RelicData addXp(int amount) {
        if (maxed()) {
            return this;
        }
        int lvl = level;
        int x = xp + amount;
        int pts = points;
        while (lvl < MAX_LEVEL && x >= 300 + 150 * lvl) {
            x -= 300 + 150 * lvl;
            lvl++;
            pts++;
        }
        if (lvl >= MAX_LEVEL) {
            x = 0;
        }
        return new RelicData(lvl, x, pts, speed, cushion);
    }

    /** Тратит одно очко на способность. Если нельзя — возвращает this без изменений. */
    public RelicData spend(int stat) {
        if (points <= 0) {
            return this;
        }
        if (stat == STAT_SPEED && speed < MAX_RANK) {
            return new RelicData(level, xp, points - 1, speed + 1, cushion);
        }
        if (stat == STAT_CUSHION && cushion < MAX_RANK) {
            return new RelicData(level, xp, points - 1, speed, cushion + 1);
        }
        return this;
    }
}
