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
 * @param xp            опыт кольца внутри текущего уровня
 * @param rarity        редкость кольца 0..6 звёзд; -1 — ещё не определена (определится, когда кольцо попадёт к игроку)
 * @param rerolls       сколько раз редкость перебрасывали (нужно клиенту, чтобы показать анимацию)
 * @param abilityLevels уровень каждой способности (0 = не изучена)
 * @param abilityRarity редкость каждой способности 0..6 звёзд (-1 — не определена)
 * @param cooldowns     игровое время (тики), когда способность снова будет готова
 */
public record RelicData(int level, int xp, int rarity, int rerolls,
                        List<Integer> abilityLevels, List<Integer> abilityRarity, List<Long> cooldowns) {
    public static final int MAX_LEVEL = 10;
    public static final int MAX_STARS = 6;
    public static final int ABILITY_COUNT = 3;
    /** Стоимость переброса редкости в уровнях опыта игрока. */
    public static final int REROLL_COST = 2;

    /** Действия, которые клиент может попросить у сервера. */
    public static final int ACTION_UPGRADE = 0;
    public static final int ACTION_REROLL = 1;
    public static final int ACTION_USE = 2;

    public static final RelicData EMPTY = new RelicData(0, 0, -1, 0, List.of(), List.of(), List.of());

    /** Приводит списки к нужной длине, чтобы старые и неполные данные не ломали код. */
    public RelicData {
        abilityLevels = normalize(abilityLevels, ABILITY_COUNT, 0);
        abilityRarity = normalize(abilityRarity, ABILITY_COUNT, -1);
        cooldowns = normalize(cooldowns, ABILITY_COUNT, 0L);
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
            Codec.INT.listOf().optionalFieldOf("ability_rarity", List.of()).forGetter(RelicData::abilityRarity),
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
            List<Integer> rarities = new ArrayList<>();
            List<Long> cooldowns = new ArrayList<>();
            for (int k = 0; k < ABILITY_COUNT; k++) {
                levels.add(VarInt.read(buf));
            }
            for (int k = 0; k < ABILITY_COUNT; k++) {
                rarities.add(VarInt.read(buf));
            }
            for (int k = 0; k < ABILITY_COUNT; k++) {
                cooldowns.add(VarLong.read(buf));
            }
            return new RelicData(level, xp, rarity, rerolls, levels, rarities, cooldowns);
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
                VarInt.write(buf, d.abilityRarity().get(k));
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

    public int abilityStars(int id) {
        return id >= 0 && id < ABILITY_COUNT ? Math.max(0, abilityRarity.get(id)) : 0;
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

    private RelicData withRarity(RandomSource random, int newRerolls) {
        List<Integer> rarities = new ArrayList<>();
        for (int k = 0; k < ABILITY_COUNT; k++) {
            rarities.add(randomStars(random));
        }
        return new RelicData(level, xp, randomStars(random), newRerolls, abilityLevels, rarities, cooldowns);
    }

    /** Первое определение редкости, когда предмет достаётся игроку. Если уже определена — возвращает this. */
    public RelicData ensureRolled(RandomSource random) {
        return rolled() ? this : withRarity(random, rerolls);
    }

    /** Переброс редкости кольца и всех трёх способностей. */
    public RelicData reroll(RandomSource random) {
        return withRarity(random, rerolls + 1);
    }

    // ---------- уровень и опыт кольца ----------

    public boolean maxed() {
        return level >= MAX_LEVEL;
    }

    public int xpToNext() {
        return 100 + 80 * level;
    }

    /** Кольцо копит чуть больше опыта, чем получает игрок: +5% за звезду (до +30%). */
    public float xpFactor() {
        return 1.0f + 0.05f * ringStars();
    }

    /** Бонус к скорости полёта от редкости кольца: +5% за звезду (до +30%). */
    public float flightMultiplier() {
        return 1.0f + 0.05f * ringStars();
    }

    public RelicData addXp(int playerXp) {
        if (maxed() || playerXp <= 0) {
            return this;
        }
        int lvl = level;
        int x = xp + Math.round(playerXp * xpFactor());
        while (lvl < MAX_LEVEL && x >= 100 + 80 * lvl) {
            x -= 100 + 80 * lvl;
            lvl++;
        }
        if (lvl >= MAX_LEVEL) {
            x = 0;
        }
        return new RelicData(lvl, x, rarity, rerolls, abilityLevels, abilityRarity, cooldowns);
    }

    // ---------- способности ----------

    public int abilityLevel(int id) {
        return id >= 0 && id < ABILITY_COUNT ? abilityLevels.get(id) : 0;
    }

    /** До какого уровня можно улучшать способности при текущем уровне кольца: 1 + уровень/3, максимум 4. */
    public int abilityCap() {
        return Math.min(RelicAbility.MAX_LEVEL, 1 + level / 3);
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
        return new RelicData(level, xp, rarity, rerolls, list, abilityRarity, cooldowns);
    }

    public long cooldownReadyAt(int id) {
        return id >= 0 && id < ABILITY_COUNT ? cooldowns.get(id) : 0L;
    }

    public RelicData withCooldown(int id, long readyAt) {
        List<Long> list = new ArrayList<>(cooldowns);
        list.set(id, readyAt);
        return new RelicData(level, xp, rarity, rerolls, abilityLevels, abilityRarity, list);
    }
}
