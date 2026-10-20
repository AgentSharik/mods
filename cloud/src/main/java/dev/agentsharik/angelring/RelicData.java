package dev.agentsharik.angelring;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Прокачка кольца. Хранится прямо в предмете (компонент данных), поэтому уровень остаётся
 * у кольца, куда бы оно ни попало.
 *
 * @param level   уровень кольца (0..MAX_LEVEL)
 * @param xp      опыт внутри текущего уровня
 * @param points  непотраченные очки улучшений (+1 за каждый новый уровень)
 * @param speed   ранг улучшения «Скорость полёта» (0..MAX_STAT)
 * @param mastery ранг улучшения «Мастерство» — ускоряет набор опыта (0..MAX_STAT)
 */
public record RelicData(int level, int xp, int points, int speed, int mastery) {
    public static final int MAX_LEVEL = 10;
    public static final int MAX_STAT = 5;
    public static final int STAT_SPEED = 0;
    public static final int STAT_MASTERY = 1;

    /** Опыт за секунду полёта (до множителя «Мастерства»). */
    public static final int XP_PER_SECOND = 5;

    public static final RelicData EMPTY = new RelicData(0, 0, 0, 0, 0);

    public static final Codec<RelicData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("level", 0).forGetter(RelicData::level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(RelicData::xp),
            Codec.INT.optionalFieldOf("points", 0).forGetter(RelicData::points),
            Codec.INT.optionalFieldOf("speed", 0).forGetter(RelicData::speed),
            Codec.INT.optionalFieldOf("mastery", 0).forGetter(RelicData::mastery)
    ).apply(i, RelicData::new));

    public static final StreamCodec<ByteBuf, RelicData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RelicData::level,
            ByteBufCodecs.VAR_INT, RelicData::xp,
            ByteBufCodecs.VAR_INT, RelicData::points,
            ByteBufCodecs.VAR_INT, RelicData::speed,
            ByteBufCodecs.VAR_INT, RelicData::mastery,
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
        return 1.0f + 0.15f * speed;
    }

    /** Множитель набора опыта (+20% за ранг). */
    public float xpMultiplier() {
        return 1.0f + 0.20f * mastery;
    }

    public RelicData addXp(int amount) {
        if (maxed()) {
            return this;
        }
        int lvl = level;
        int x = xp + Math.round(amount * xpMultiplier());
        int pts = points;
        while (lvl < MAX_LEVEL && x >= 300 + 150 * lvl) {
            x -= 300 + 150 * lvl;
            lvl++;
            pts++;
        }
        if (lvl >= MAX_LEVEL) {
            x = 0;
        }
        return new RelicData(lvl, x, pts, speed, mastery);
    }

    /** Тратит одно очко на улучшение. Если нельзя — возвращает this без изменений. */
    public RelicData spend(int stat) {
        if (points <= 0) {
            return this;
        }
        if (stat == STAT_SPEED && speed < MAX_STAT) {
            return new RelicData(level, xp, points - 1, speed + 1, mastery);
        }
        if (stat == STAT_MASTERY && mastery < MAX_STAT) {
            return new RelicData(level, xp, points - 1, speed, mastery + 1);
        }
        return this;
    }
}
