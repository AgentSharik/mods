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
 * Вся прокачка «Кольца ангела». Хранится прямо в предмете (компонент данных).
 *
 * @param level           уровень кольца (1..MAX_LEVEL). Растёт только по нажатию кнопки «Повысить уровень»
 * @param xp              накопленный опыт кольца (копится всегда, даже сверх порога и на максимальном уровне)
 * @param rarity          редкость кольца 0..6 звёзд; -1 — ещё не определена. Это же число — редкость всех способностей
 * @param rerolls         сколько раз редкость перебрасывали (нужно клиенту для анимации)
 * @param abilityUpgrades сколько улучшений куплено у каждой способности (0..MAX_UPGRADES)
 * @param cooldowns       игровое время (тики), когда способность снова будет готова
 */
public record RelicData(int level, int xp, int rarity, int rerolls,
                        List<Integer> abilityUpgrades, List<Long> cooldowns) {
    public static final int MAX_LEVEL = 10;
    public static final int MAX_STARS = 6;
    public static final int ABILITY_COUNT = 3;
    public static final int MAX_UPGRADES = 3;
    /** Стоимость переброса редкости в уровнях опыта игрока. */
    public static final int REROLL_COST = 2;

    /** Действия, которые клиент может попросить у сервера. */
    public static final int ACTION_UPGRADE = 0;
    public static final int ACTION_REROLL = 1;
    public static final int ACTION_USE = 2;
    public static final int ACTION_LEVEL_UP = 3;

    public static final RelicData EMPTY = new RelicData(1, 0, -1, 0, List.of(), List.of());

    /** Опыт, нужный для перехода с уровня lvl на следующий: 180 для 1 уровня, дальше +80 за каждый уровень. */
    public static int needed(int lvl) {
        return 100 + 80 * lvl;
    }

    /** Приводит данные в порядок: списки нужной длины, уровень в пределах 1..MAX_LEVEL. */
    public RelicData {
        abilityUpgrades = normalize(abilityUpgrades, ABILITY_COUNT, 0);
        cooldowns = normalize(cooldowns, ABILITY_COUNT, 0L);
        level = Math.max(1, Math.min(MAX_LEVEL, level));
        xp = Math.max(0, xp);
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
            Codec.INT.optionalFieldOf("level", 1).forGetter(RelicData::level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(RelicData::xp),
            Codec.INT.optionalFieldOf("rarity", -1).forGetter(RelicData::rarity),
            Codec.INT.optionalFieldOf("rerolls", 0).forGetter(RelicData::rerolls),
            Codec.INT.listOf().optionalFieldOf("ability_upgrades", List.of()).forGetter(RelicData::abilityUpgrades),
            Codec.LONG.listOf().optionalFieldOf("cooldowns", List.of()).forGetter(RelicData::cooldowns)
    ).apply(i, RelicData::new));

    public static final StreamCodec<ByteBuf, RelicData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RelicData decode(ByteBuf buf) {
            int level = VarInt.read(buf);
            int xp = VarInt.read(buf);
            int rarity = VarInt.read(buf);
            int rerolls = VarInt.read(buf);
            List<Integer> upgrades = new ArrayList<>();
            List<Long> cooldowns = new ArrayList<>();
            for (int k = 0; k < ABILITY_COUNT; k++) {
                upgrades.add(VarInt.read(buf));
            }
            for (int k = 0; k < ABILITY_COUNT; k++) {
                cooldowns.add(VarLong.read(buf));
            }
            return new RelicData(level, xp, rarity, rerolls, upgrades, cooldowns);
        }

        @Override
        public void encode(ByteBuf buf, RelicData d) {
            VarInt.write(buf, d.level());
            VarInt.write(buf, d.xp());
            VarInt.write(buf, d.rarity());
            VarInt.write(buf, d.rerolls());
            for (int k = 0; k < ABILITY_COUNT; k++) {
                VarInt.write(buf, d.abilityUpgrades().get(k));
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
        int[] weights = {24, 24, 20, 15, 10, 5, 2};
        int roll = random.nextInt(100);
        int acc = 0;
        for (int stars = 0; stars < weights.length; stars++) {
            acc += weights[stars];
            if (roll < acc) {
                return stars;
            }
        }
        return 0;
    }

    /** Первое определение редкости, когда предмет достаётся игроку. Если уже определена — возвращает this. */
    public RelicData ensureRolled(RandomSource random) {
        return rolled() ? this : new RelicData(level, xp, randomStars(random), rerolls, abilityUpgrades, cooldowns);
    }

    /** Переброс редкости кольца (вместе с ним меняется редкость всех способностей). */
    public RelicData reroll(RandomSource random) {
        return new RelicData(level, xp, randomStars(random), rerolls + 1, abilityUpgrades, cooldowns);
    }

    // ---------- уровень и опыт кольца ----------

    public boolean maxed() {
        return level >= MAX_LEVEL;
    }

    public int xpToNext() {
        return needed(level);
    }

    /** Можно ли прямо сейчас повысить уровень кольца: опыта хватает, и уровень ещё не максимальный. */
    public boolean canLevelUp() {
        return !maxed() && xp >= needed(level);
    }

    /**
     * Повышает уровень на 1: из опыта вычитается порог, весь остаток (сверх нормы) переходит на следующий уровень,
     * а порог для следующего уровня становится выше. Если нельзя — возвращает this.
     */
    public RelicData levelUp() {
        if (!canLevelUp()) {
            return this;
        }
        return new RelicData(level + 1, xp - needed(level), rarity, rerolls, abilityUpgrades, cooldowns);
    }

    /** Кольцо копит чуть больше опыта, чем получает игрок: +5% за звезду (до +30%). */
    public float xpFactor() {
        return 1.0f + 0.05f * ringStars();
    }

    /** Бонус к скорости полёта от редкости кольца: +5% за звезду (до +30%). */
    public float flightMultiplier() {
        return 1.0f + 0.05f * ringStars();
    }

    /** Добавляет опыт игрока. Копится всегда, без верхней границы; уровень повышается только кнопкой. */
    public RelicData addXp(int playerXp) {
        if (playerXp <= 0) {
            return this;
        }
        return new RelicData(level, xp + Math.round(playerXp * xpFactor()), rarity, rerolls, abilityUpgrades, cooldowns);
    }

    // ---------- способности ----------

    /** Открыта ли способность на текущем уровне кольца. */
    public boolean unlocked(int id) {
        RelicAbility ability = RelicAbility.byId(id);
        return ability != null && level >= ability.unlockLevel;
    }

    /** Уровень способности: 0 — закрыта, 1 — открыта, дальше +1 за каждое купленное улучшение (максимум 4). */
    public int abilityLevel(int id) {
        return unlocked(id) ? 1 + abilityUpgrades.get(id) : 0;
    }

    /** Стоимость следующего улучшения в уровнях опыта игрока: 2, 4, 6. -1 — уже максимум. */
    public int upgradeCost(int id) {
        if (id < 0 || id >= ABILITY_COUNT) {
            return -1;
        }
        int bought = abilityUpgrades.get(id);
        return bought >= MAX_UPGRADES ? -1 : 2 + 2 * bought;
    }

    /** Можно ли улучшить способность (она открыта и ещё не максимальная), не считая уровней опыта игрока. */
    public boolean canUpgrade(int id) {
        return id >= 0 && id < ABILITY_COUNT && unlocked(id) && abilityUpgrades.get(id) < MAX_UPGRADES;
    }

    public RelicData upgrade(int id) {
        if (!canUpgrade(id)) {
            return this;
        }
        List<Integer> list = new ArrayList<>(abilityUpgrades);
        list.set(id, list.get(id) + 1);
        return new RelicData(level, xp, rarity, rerolls, list, cooldowns);
    }

    public long cooldownReadyAt(int id) {
        return id >= 0 && id < ABILITY_COUNT ? cooldowns.get(id) : 0L;
    }

    public RelicData withCooldown(int id, long readyAt) {
        List<Long> list = new ArrayList<>(cooldowns);
        list.set(id, readyAt);
        return new RelicData(level, xp, rarity, rerolls, abilityUpgrades, list);
    }
}
