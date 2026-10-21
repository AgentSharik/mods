package dev.agentsharik.fallenrelics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Реликвия-кольцо в духе исходного мода Relics, ревизия 1.8.0: хранится прямо в
 * предмете (data-компонент), поэтому рарность, звёзды и ранги путешествуют с
 * кольцом куда угодно. Опыт реликвия набирает зеркально к опыту игрока, а
 * улучшения способностей оплачиваются уровнями игрока.
 *
 * @param level  уровень реликвии (0..MAX_LEVEL), растёт от зеркального опыта
 * @param xp     опыт внутри текущего уровня
 * @param rolled true, когда рарность уже брошена (флаг миграции старых колец)
 * @param rarity рарность артефакта, 0..6 звёзд: базовые параметры кольца
 * @param a0..a2 рарность способностей (звёзды 0..6): сила эффектов
 * @param r0..r2 ранги способностей (0..3): 0 = не изучено, улучшение за уровни
 */
public record RelicData(int level, int xp, boolean rolled, int rarity,
        int a0, int a1, int a2, int r0, int r1, int r2) {
    public static final int MAX_LEVEL = 10;
    /** Опыт реликвии за секунду полёта (добавка к зеркальному опыту игрока). */
    public static final int XP_PER_SECOND = 5;
    public static final int MAX_STAR = 6;
    public static final int MAX_RANK = 3;
    public static final int ABILITIES = 3;

    public static final RelicData EMPTY = new RelicData(0, 0, false, 0, 0, 0, 0, 0, 0, 0);

    public static final Codec<RelicData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("level", 0).forGetter(RelicData::level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(RelicData::xp),
            Codec.BOOL.optionalFieldOf("rolled", false).forGetter(RelicData::rolled),
            Codec.INT.optionalFieldOf("rarity", 0).forGetter(RelicData::rarity),
            Codec.INT.optionalFieldOf("a0", 0).forGetter(RelicData::a0),
            Codec.INT.optionalFieldOf("a1", 0).forGetter(RelicData::a1),
            Codec.INT.optionalFieldOf("a2", 0).forGetter(RelicData::a2),
            Codec.INT.optionalFieldOf("r0", 0).forGetter(RelicData::r0),
            Codec.INT.optionalFieldOf("r1", 0).forGetter(RelicData::r1),
            Codec.INT.optionalFieldOf("r2", 0).forGetter(RelicData::r2)
    ).apply(i, RelicData::new));

    public static final StreamCodec<ByteBuf, RelicData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RelicData decode(ByteBuf buf) {
            return new RelicData(ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf));
        }

        @Override
        public void encode(ByteBuf buf, RelicData d) {
            ByteBufCodecs.VAR_INT.encode(buf, d.level());
            ByteBufCodecs.VAR_INT.encode(buf, d.xp());
            ByteBufCodecs.BOOL.encode(buf, d.rolled());
            ByteBufCodecs.VAR_INT.encode(buf, d.rarity());
            ByteBufCodecs.VAR_INT.encode(buf, d.a0());
            ByteBufCodecs.VAR_INT.encode(buf, d.a1());
            ByteBufCodecs.VAR_INT.encode(buf, d.a2());
            ByteBufCodecs.VAR_INT.encode(buf, d.r0());
            ByteBufCodecs.VAR_INT.encode(buf, d.r1());
            ByteBufCodecs.VAR_INT.encode(buf, d.r2());
        }
    };

    public boolean maxed() {
        return level >= MAX_LEVEL;
    }

    /** Сколько опыта нужно до следующего уровня. */
    public int xpToNext() {
        return 300 + 150 * level;
    }

    public int abilityRarity(int index) {
        return clampStar(index == 0 ? a0 : index == 1 ? a1 : a2);
    }

    public int rank(int index) {
        return Math.max(0, Math.min(MAX_RANK, index == 0 ? r0 : index == 1 ? r1 : r2));
    }

    public static int clampStar(int stars) {
        return Math.max(0, Math.min(MAX_STAR, stars));
    }

    /** Потолок ранга открыт уровнями реликвии: 1 сразу, 2 с 3-го, 3 с 6-го. */
    public int rankCap() {
        return 1 + (level >= 3 ? 1 : 0) + (level >= 6 ? 1 : 0);
    }

    /** Цена ранга в уровнях игрока: изучить 2, далее 4, далее 6. */
    public static int rankCost(int nextRank) {
        return nextRank * 2;
    }

    /** Зеркальный опыт игрока копится в реликвии и растит её уровень. */
    public RelicData addXp(int amount) {
        if (maxed()) {
            return this;
        }
        int lvl = level;
        int x = xp + amount;
        while (lvl < MAX_LEVEL && x >= 300 + 150 * lvl) {
            x -= 300 + 150 * lvl;
            lvl++;
        }
        if (lvl >= MAX_LEVEL) {
            x = 0;
        }
        return new RelicData(lvl, x, rolled, rarity, a0, a1, a2, r0, r1, r2);
    }

    public RelicData withRank(int index, int value) {
        int v = Math.max(0, Math.min(MAX_RANK, value));
        return new RelicData(level, xp, rolled, rarity,
                a0, a1, a2,
                index == 0 ? v : r0, index == 1 ? v : r1, index == 2 ? v : r2);
    }

    public RelicData withRoll(int artifact, int s0, int s1, int s2) {
        return new RelicData(level, xp, true, clampStar(artifact),
                clampStar(s0), clampStar(s1), clampStar(s2), r0, r1, r2);
    }

    /* Базовые параметры кольца от рарности артефакта (0..6 звёзд). */

    /** Множитель скорости полёта: +6% за звезду. */
    public float speedMultiplier() {
        return 1.0F + 0.06F * rarity;
    }

    /** Тики страховки мягкого падения: 2..6 секунд от звёзд. */
    public int cushionTicks() {
        return 120 + 40 * rarity;
    }
}
