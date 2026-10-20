package dev.agentsharik.angelring;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.network.codec.StreamCodec;

/**
 * Вся прокачка кольца. Хранится прямо в предмете (компонент данных), поэтому уровень, ранг,
 * способности и перезарядки остаются у кольца, куда бы оно ни попало.
 *
 * @param level     уровень кольца (0..MAX_LEVEL)
 * @param xp        опыт внутри текущего уровня
 * @param points    непотраченные очки (+1 за новый уровень и за повышение ранга)
 * @param rank      ранг кольца (0..MAX_RANK)
 * @param speed     ранг характеристики «Скорость полёта»
 * @param mastery   ранг характеристики «Мастерство» (ускоряет набор опыта)
 * @param abilities уровень каждой способности (0 = не изучена), по одному числу на способность
 * @param equipped  id способностей в двух слотах (-1 = слот пуст)
 * @param cooldowns игровое время (тики), когда способность снова будет готова
 */
public record RelicData(int level, int xp, int points, int rank, int speed, int mastery,
                        List<Integer> abilities, List<Integer> equipped, List<Long> cooldowns) {
    public static final int MAX_LEVEL = 10;
    public static final int MAX_RANK = 5;
    public static final int BASE_MAX_STAT = 5;
    public static final int SLOTS = 2;
    public static final int ABILITY_COUNT = 3;

    /** Действия, которые клиент может попросить у сервера. */
    public static final int ACTION_STAT_SPEED = 0;
    public static final int ACTION_STAT_MASTERY = 1;
    public static final int ACTION_RANK_UP = 2;
    public static final int ACTION_ABILITY_LEARN = 3;
    public static final int ACTION_ABILITY_EQUIP = 4;
    public static final int ACTION_USE = 5;

    /** Опыт за секунду полёта (до множителя «Мастерства») и за применение способности. */
    public static final int XP_PER_SECOND = 5;
    public static final int XP_PER_ABILITY_USE = 12;

    public static final RelicData EMPTY =
            new RelicData(0, 0, 0, 0, 0, 0, List.of(), List.of(), List.of());

    /** Приводит списки к нужной длине, чтобы старые и неполные данные не ломали код. */
    public RelicData {
        abilities = normalize(abilities, ABILITY_COUNT, 0);
        equipped = normalize(equipped, SLOTS, -1);
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
            Codec.INT.optionalFieldOf("points", 0).forGetter(RelicData::points),
            Codec.INT.optionalFieldOf("rank", 0).forGetter(RelicData::rank),
            Codec.INT.optionalFieldOf("speed", 0).forGetter(RelicData::speed),
            Codec.INT.optionalFieldOf("mastery", 0).forGetter(RelicData::mastery),
            Codec.INT.listOf().optionalFieldOf("abilities", List.of()).forGetter(RelicData::abilities),
            Codec.INT.listOf().optionalFieldOf("equipped", List.of()).forGetter(RelicData::equipped),
            Codec.LONG.listOf().optionalFieldOf("cooldowns", List.of()).forGetter(RelicData::cooldowns)
    ).apply(i, RelicData::new));

    public static final StreamCodec<ByteBuf, RelicData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RelicData decode(ByteBuf buf) {
            int level = VarInt.read(buf);
            int xp = VarInt.read(buf);
            int points = VarInt.read(buf);
            int rank = VarInt.read(buf);
            int speed = VarInt.read(buf);
            int mastery = VarInt.read(buf);
            List<Integer> abilities = new ArrayList<>();
            for (int k = 0; k < ABILITY_COUNT; k++) {
                abilities.add(VarInt.read(buf));
            }
            List<Integer> equipped = new ArrayList<>();
            for (int k = 0; k < SLOTS; k++) {
                equipped.add(VarInt.read(buf));
            }
            List<Long> cooldowns = new ArrayList<>();
            for (int k = 0; k < ABILITY_COUNT; k++) {
                cooldowns.add(VarLong.read(buf));
            }
            return new RelicData(level, xp, points, rank, speed, mastery, abilities, equipped, cooldowns);
        }

        @Override
        public void encode(ByteBuf buf, RelicData d) {
            VarInt.write(buf, d.level());
            VarInt.write(buf, d.xp());
            VarInt.write(buf, d.points());
            VarInt.write(buf, d.rank());
            VarInt.write(buf, d.speed());
            VarInt.write(buf, d.mastery());
            for (int k = 0; k < ABILITY_COUNT; k++) {
                VarInt.write(buf, d.abilities().get(k));
            }
            for (int k = 0; k < SLOTS; k++) {
                VarInt.write(buf, d.equipped().get(k));
            }
            for (int k = 0; k < ABILITY_COUNT; k++) {
                VarLong.write(buf, d.cooldowns().get(k));
            }
        }
    };

    // ---------- уровень, ранг, характеристики ----------

    public boolean maxed() {
        return level >= MAX_LEVEL;
    }

    public boolean canRankUp() {
        return maxed() && rank < MAX_RANK;
    }

    /** Предел рангов характеристик растёт вместе с рангом кольца. */
    public int maxStat() {
        return BASE_MAX_STAT + rank;
    }

    private int needed(int lvl) {
        return Math.round((300 + 150 * lvl) * (1f + 0.5f * rank));
    }

    /** Сколько опыта нужно до следующего уровня (с каждым рангом дороже на 50%). */
    public int xpToNext() {
        return needed(level);
    }

    /** Множитель скорости полёта: +15% за ранг характеристики и +10% за ранг кольца. */
    public float speedMultiplier() {
        return 1.0f + 0.15f * speed + 0.10f * rank;
    }

    /** Множитель набора опыта (+20% за ранг «Мастерства»). */
    public float xpMultiplier() {
        return 1.0f + 0.20f * mastery;
    }

    private RelicData withProgress(int lvl, int x, int pts, int rk) {
        return new RelicData(lvl, x, pts, rk, speed, mastery, abilities, equipped, cooldowns);
    }

    public RelicData addXp(int amount) {
        if (maxed()) {
            return this;
        }
        int lvl = level;
        int x = xp + Math.round(amount * xpMultiplier());
        int pts = points;
        while (lvl < MAX_LEVEL && x >= needed(lvl)) {
            x -= needed(lvl);
            lvl++;
            pts++;
        }
        if (lvl >= MAX_LEVEL) {
            x = 0;
        }
        return withProgress(lvl, x, pts, rank);
    }

    // ---------- способности ----------

    public int abilityLevel(int id) {
        return id >= 0 && id < ABILITY_COUNT ? abilities.get(id) : 0;
    }

    /** Номер слота (0 или 1), в котором стоит способность, или -1. */
    public int equippedSlot(int id) {
        return equipped.indexOf(id);
    }

    public int equippedCount() {
        int n = 0;
        for (int id : equipped) {
            if (id >= 0) {
                n++;
            }
        }
        return n;
    }

    /** Уровень способности, если она стоит в слоте (иначе 0) — для пассивных эффектов. */
    public int equippedLevel(RelicAbility ability) {
        return equippedSlot(ability.id) >= 0 ? abilityLevel(ability.id) : 0;
    }

    /** Стоимость изучения/улучшения в очках, либо -1, если уже максимальный уровень. */
    public int abilityCost(int id) {
        int lvl = abilityLevel(id);
        return lvl >= RelicAbility.MAX_LEVEL ? -1 : Math.max(1, lvl);
    }

    public long cooldownReadyAt(int id) {
        return id >= 0 && id < ABILITY_COUNT ? cooldowns.get(id) : 0L;
    }

    public RelicData withCooldown(int id, long readyAt) {
        List<Long> list = new ArrayList<>(cooldowns);
        list.set(id, readyAt);
        return new RelicData(level, xp, points, rank, speed, mastery, abilities, equipped, list);
    }

    /** Чисто «данные»: действия, не требующие эффектов в мире. Если нельзя — возвращает this. */
    public RelicData act(int action, int arg) {
        switch (action) {
            case ACTION_STAT_SPEED:
                if (points > 0 && speed < maxStat()) {
                    return new RelicData(level, xp, points - 1, rank, speed + 1, mastery, abilities, equipped, cooldowns);
                }
                return this;
            case ACTION_STAT_MASTERY:
                if (points > 0 && mastery < maxStat()) {
                    return new RelicData(level, xp, points - 1, rank, speed, mastery + 1, abilities, equipped, cooldowns);
                }
                return this;
            case ACTION_RANK_UP:
                if (canRankUp()) {
                    // уровень и опыт сбрасываются, ранг растёт, в награду — одно очко
                    return withProgress(0, 0, points + 1, rank + 1);
                }
                return this;
            case ACTION_ABILITY_LEARN: {
                int cost = abilityCost(arg);
                if (arg < 0 || arg >= ABILITY_COUNT || cost < 0 || points < cost) {
                    return this;
                }
                List<Integer> list = new ArrayList<>(abilities);
                list.set(arg, list.get(arg) + 1);
                return new RelicData(level, xp, points - cost, rank, speed, mastery, list, equipped, cooldowns);
            }
            case ACTION_ABILITY_EQUIP: {
                if (abilityLevel(arg) <= 0) {
                    return this;
                }
                List<Integer> list = new ArrayList<>(equipped);
                int slot = list.indexOf(arg);
                if (slot >= 0) {
                    list.set(slot, -1);       // снять
                } else {
                    int free = list.indexOf(-1);
                    if (free < 0) {
                        return this;           // оба слота заняты
                    }
                    list.set(free, arg);
                }
                return new RelicData(level, xp, points, rank, speed, mastery, abilities, list, cooldowns);
            }
            default:
                return this;
        }
    }
}
