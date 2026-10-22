package dev.agentsharik.angelring;

/**
 * Три способности кольца: две пассивные и одна активная (ультимативная). Открываются по уровню кольца:
 * «Кровавая защита» — с 1 уровня, «Жатва падших» — с 5, «Багровое вознесение» — с 10.
 * Сила каждой способности зависит от редкости кольца (0..6 звёзд) и её уровня улучшения (1..4).
 */
public enum RelicAbility {
    BLOOD_WARD(0, false, "blood_ward", 1),
    HARVEST(1, false, "harvest", 5),
    ASCENSION(2, true, "ascension", 10);

    public static final int MAX_LEVEL = 4;
    public static final int MAX_STARS = 6;

    public final int id;
    public final boolean active;
    /** Часть ключей перевода: gui.angelring.ability.<key>.name / .desc / .short */
    public final String key;
    /** С какого уровня кольца способность открыта. */
    public final int unlockLevel;

    RelicAbility(int id, boolean active, String key, int unlockLevel) {
        this.id = id;
        this.active = active;
        this.key = key;
        this.unlockLevel = unlockLevel;
    }

    public static RelicAbility byId(int id) {
        for (RelicAbility a : values()) {
            if (a.id == id) {
                return a;
            }
        }
        return null;
    }

    /** Линейный рост от min (0 звёзд) до max (6 звёзд). */
    public static float lerp(float min, float max, int stars) {
        int s = Math.max(0, Math.min(MAX_STARS, stars));
        return min + (max - min) * s / (float) MAX_STARS;
    }

    /** Каждый уровень способности выше первого усиливает числовые параметры на 15%. */
    public static float levelMul(int level) {
        return 1f + 0.15f * (Math.max(1, level) - 1);
    }

    // ---- «Кровавая защита» (пассивная, с 1 уровня кольца) ----

    /** Шанс сработать: от 20% (0 звёзд) до 60% (6 звёзд), +4% за каждый уровень выше первого, не больше 75%. */
    public static float wardChance(int stars, int level) {
        return Math.min(0.75f, lerp(0.20f, 0.60f, stars) + 0.04f * (Math.max(1, level) - 1));
    }

    /** Сколько единиц здоровья возвращает сработавшая защита. */
    public static float wardHeal(int level) {
        return 2f + level;
    }

    /** Сколько секунд нападавший будет ослаблен. */
    public static int wardWeaknessSeconds(int stars) {
        return 3 + Math.max(0, stars);
    }

    // ---- «Жатва падших» (пассивная, с 5 уровня кольца) ----

    /** Здоровье, которое возвращается за убийство врага: от 2 (0 звёзд) до 6 (6 звёзд), +10% за уровень. */
    public static float harvestHeal(int stars, int level) {
        return lerp(2f, 6f, stars) * (1f + 0.10f * (Math.max(1, level) - 1));
    }

    /** Длительность поглощения урона после убийства, секунды: от 6 до 15. */
    public static int harvestAbsorbSeconds(int stars) {
        return Math.round(lerp(6f, 15f, stars));
    }

    /** Шанс получить бонусный опыт за убийство: от 10% до 40%, +3% за уровень выше первого, не больше 60%. */
    public static float harvestXpChance(int stars, int level) {
        return Math.min(0.60f, lerp(0.10f, 0.40f, stars) + 0.03f * (Math.max(1, level) - 1));
    }

    /** Размер бонусной порции опыта. */
    public static int harvestBonusXp(int level) {
        return 3 + 2 * Math.max(1, level);
    }

    // ---- «Багровое вознесение» (активная, с 10 уровня кольца) ----

    public static float ascensionRadius(int stars) {
        return lerp(7f, 12f, stars);
    }

    public static float ascensionDamage(int stars, int level) {
        return lerp(14f, 36f, stars) * levelMul(level);
    }

    public static float ascensionStunSeconds(int stars, int level) {
        return lerp(3f, 6f, stars) * levelMul(level);
    }

    /** Перезарядка активной способности в секундах (0 для пассивных): от 120 до 90, −5% за уровень. */
    public int cooldownSeconds(int stars, int level) {
        float base = switch (this) {
            case ASCENSION -> lerp(120f, 90f, stars);
            case BLOOD_WARD, HARVEST -> 0f;
        };
        return Math.round(base * (1f - 0.05f * (Math.max(1, level) - 1)));
    }
}
