package dev.agentsharik.angelring;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

/**
 * Вся прокачка кольца. Хранится прямо в предмете (компонент данных).
 *
 * @param level         уровень кольца (0..MAX_LEVEL); растёт от опыта, который игрок получает в мире
 * @param xp            опыт кольца внутри текущего уровня (на максимальном уровне продолжает копиться)
 * @param rarity        редкость кольца 0..6 звёзд; -1 — ещё не определена (определится, когда кольцо попадёт к игроку).
 *                      Это же число — редкость всех способностей кольца.
 * @param rerolls       сколько раз редкость перебрасывали (нужно клиенту, чтобы показать анимацию)
 * @param abilityLevels уровень каждой способности (0 = не изучена)
 * @param cooldowns     игровое время (тики), когда способность снова будет готова
 */
public record RelicData(int level, int xp, int rarity, int rerolls,
                        List<Integer> abilityLevels, List<Long> cooldowns) {
    public static final int MAX_LEVEL = 10;
    public static final int MAX_STARS = 6;
    public static final int ABILITY_COUNT = 3;
    /** Стоимость переброса редкости в уровнях опыта игрока. */
    public static final int REROLL_COST = 2;
    /** С такой редкости реролл требует подтверждения (удержание кнопки). Проверяется и на сервере. */
    public static final int CONFIRM_STARS = 5;
    /** Веса для 0..6 звёзд (из 100). Один общий массив — без создания нового при каждом броске. */
    private static final int[] STAR_WEIGHTS = {24, 24, 20, 15, 10, 5, 2};

    /** Действия, которые клиент может попросить у сервера. */
    public static final int ACTION_UPGRADE = 0;
    public static final int ACTION_REROLL = 1;
    public static final int ACTION_USE = 2;
    /** Только сервер → клиент: сработала пассивная защита (вспышка в HUD). */
    public static final int ACTION_WARD_PROC = 3;

    public static final RelicData EMPTY = new RelicData(0, 0, -1, 0, List.of(), List.of());

    /** Опыт, нужный для перехода с уровня lvl на следующий. */
    public static int needed(int lvl) {
        return 100 + 80 * lvl;
    }

    /**
     * Приводит данные в порядок: списки нужной длины и «самолечение» уровня.
     * Если опыта накопилось больше порога (например, данные остались от старой версии), уровни добираются сами,
     * а лишний опыт переходит на следующий уровень.
     */
    public RelicData {
        abilityLevels = normalize(abilityLevels, ABILITY_COUNT, 0).stream()
                .map(v -> Math.max(0, Math.min(RelicAbility.MAX_LEVEL, v))).toList();
        rarity = Math.max(-1, Math.min(MAX_STARS, rarity));
        rerolls = Math.max(0, rerolls);
        cooldowns = normalize(cooldowns, ABILITY_COUNT, 0L);
        level = Math.max(0, Math.min(MAX_LEVEL, level));
        xp = Math.max(0, xp);
        while (level < MAX_LEVEL && xp >= needed(level)) {
            xp -= needed(level);
            level++;
        }
    }

    private static <T> List<T> normalize(List<T> in, int size, T fill) {
        List<T> out = new ArrayList<>(in);
        while (out.size() < size) {
            out.add(fill);
        }
        while (out.size() > size) {
            out.remove(out.size() - 1);
        }
        return List.copyOf(out);
    }

    public static final Codec<RelicData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("level", 0).forGetter(RelicData::level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(RelicData::xp),
            Codec.INT.optionalFieldOf("rarity", -1).forGetter(RelicData::rarity),
            Codec.INT.optionalFieldOf("rerolls", 0).forGetter(RelicData::rerolls),
            Codec.INT.listOf().optionalFieldOf("ability_levels", List.of()).forGetter(RelicData::abilityLevels),
            Codec.LONG.listOf().optionalFieldOf("cooldowns", List.of()).forGetter(RelicData::cooldowns)
    ).apply(i, RelicData::new));

    public static final StreamCodec<ByteBuf, RelicData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RelicData decode(ByteBuf buf) {
            int level = VarInt.read(buf);
            int xp = VarInt.read(buf);
            int rarity = VarInt.read(buf);
            int rerolls = VarInt.read(buf);
            List<Integer> levels = new ArrayList<>();
            List<Long> cooldowns = new ArrayList<>();
            for (int k = 0; k < ABILITY_COUNT; k++) {
                levels.add(VarInt.read(buf));
            }
            for (int k = 0; k < ABILITY_COUNT; k++) {
                cooldowns.add(VarLong.read(buf));
            }
            return new RelicData(level, xp, rarity, rerolls, levels, cooldowns);
        }

        @Override
        public void encode(ByteBuf buf, RelicData d) {
            VarInt.write(buf, d.level());
            VarInt.write(buf, d.xp());
            VarInt.write(buf, d.rarity());
            VarInt.write(buf, d.rerolls());
            for (int k = 0; k < ABILITY_COUNT; k++) {
                VarInt.write(buf, d.abilityLevels().get(k));
            }
            for (int k = 0; k < ABILITY_COUNT; k++) {
                VarLong.write(buf, d.cooldowns().get(k));
            }
        }
    };

    // ---------- редкость ----------

    public boolean rolled() {
        return rarity >= 0;
    }

    /** Звёзды кольца для показа и расчётов (нераспределённая редкость считается нулевой). */
    public int ringStars() {
        return Math.max(0, rarity);
    }

    /** Редкость способностей всегда равна редкости кольца. */
    public int abilityStars(int id) {
        return ringStars();
    }

    /** Случайная редкость: чем больше звёзд, тем реже. Веса для 0..6 звёзд: 24, 24, 20, 15, 10, 5, 2 (из 100). */
    public static int randomStars(RandomSource random) {
        int roll = random.nextInt(100);
        int acc = 0;
        for (int stars = 0; stars < STAR_WEIGHTS.length; stars++) {
            acc += STAR_WEIGHTS[stars];
            if (roll < acc) {
                return stars;
            }
        }
        return 0;
    }

    /** Вероятность (0..1) выбросить ровно stars звёзд. */
    public static float starChance(int stars) {
        return stars < 0 || stars >= STAR_WEIGHTS.length ? 0f : STAR_WEIGHTS[stars] / 100f;
    }

    /** Вероятность, что реролл даст больше (sign > 0), столько же (0) или меньше (sign < 0) звёзд, чем current. */
    public static float rerollOdds(int current, int sign) {
        float sum = 0f;
        for (int s = 0; s < STAR_WEIGHTS.length; s++) {
            int cmp = Integer.compare(s, current);
            if (cmp == Integer.signum(sign)) {
                sum += STAR_WEIGHTS[s];
            }
        }
        return sum / 100f;
    }

    /** Нужно ли подтверждение реролла (защита от случайного сброса топовой редкости). */
    public boolean rerollNeedsConfirm() {
        return ringStars() >= CONFIRM_STARS;
    }

    /** Первое определение редкости, когда предмет достаётся игроку. Если уже определена — возвращает this. */
    public RelicData ensureRolled(RandomSource random) {
        return rolled() ? this : new RelicData(level, xp, randomStars(random), rerolls, abilityLevels, cooldowns);
    }

    /** Переброс редкости кольца (вместе с ним меняется редкость всех способностей). */
    public RelicData reroll(RandomSource random) {
        return new RelicData(level, xp, randomStars(random), rerolls + 1, abilityLevels, cooldowns);
    }

    // ---------- уровень и опыт кольца ----------

    public boolean maxed() {
        return level >= MAX_LEVEL;
    }

    public int xpToNext() {
        return needed(level);
    }

    /** Сколько ещё опыта кольца нужно накопить, чтобы достичь уровня кольца target (0 — уже достигнут). */
    public int ringXpUntil(int target) {
        int t = Math.min(MAX_LEVEL, target);
        if (level >= t) {
            return 0;
        }
        int sum = needed(level) - xp;
        for (int l = level + 1; l < t; l++) {
            sum += needed(l);
        }
        return Math.max(0, sum);
    }

    /** Кольцо копит чуть больше опыта, чем получает игрок: +5% за звезду (до +30%). */
    public float xpFactor() {
        return 1.0f + 0.05f * ringStars();
    }

    /** Бонус к скорости полёта от редкости кольца: +5% за звезду (до +30%). */
    public float flightMultiplier() {
        return 1.0f + 0.05f * ringStars();
    }

    /**
     * Добавляет опыт игрока. Копится всегда: на каждом пороге уровень растёт сам (конструктор переносит остаток),
     * а на максимальном уровне опыт просто продолжает накапливаться.
     */
    public RelicData addXp(int playerXp) {
        if (playerXp <= 0) {
            return this;
        }
        return new RelicData(level, xp + Math.round(playerXp * xpFactor()), rarity, rerolls, abilityLevels, cooldowns);
    }

    // ---------- способности ----------

    public int abilityLevel(int id) {
        return id >= 0 && id < ABILITY_COUNT ? abilityLevels.get(id) : 0;
    }

    /** До какого уровня можно улучшать способности при текущем уровне кольца: 1 + уровень/3, максимум 4. */
    public int abilityCap() {
        return Math.min(RelicAbility.MAX_LEVEL, 1 + level / 3);
    }

    /** Какой уровень кольца нужен, чтобы способность можно было поднять до уровня abilityLevel. */
    public static int ringLevelFor(int abilityLevel) {
        return Math.max(0, 3 * (abilityLevel - 1));
    }

    /** Стоимость следующего уровня способности в уровнях опыта игрока: 2, 4, 6, 8. -1 — уже максимум. */
    public int upgradeCost(int id) {
        int lvl = abilityLevel(id);
        return lvl >= RelicAbility.MAX_LEVEL ? -1 : 2 + 2 * lvl;
    }

    /** Можно ли улучшить способность прямо сейчас (не считая уровней опыта игрока). */
    public boolean canUpgrade(int id) {
        int lvl = abilityLevel(id);
        return id >= 0 && id < ABILITY_COUNT && lvl < RelicAbility.MAX_LEVEL && lvl < abilityCap();
    }

    public RelicData upgrade(int id) {
        if (!canUpgrade(id)) {
            return this;
        }
        List<Integer> list = new ArrayList<>(abilityLevels);
        list.set(id, list.get(id) + 1);
        return new RelicData(level, xp, rarity, rerolls, list, cooldowns);
    }

    public long cooldownReadyAt(int id) {
        return id >= 0 && id < ABILITY_COUNT ? cooldowns.get(id) : 0L;
    }

    public RelicData withCooldown(int id, long readyAt) {
        List<Long> list = new ArrayList<>(cooldowns);
        list.set(id, readyAt);
        return new RelicData(level, xp, rarity, rerolls, abilityLevels, list);
    }
}
