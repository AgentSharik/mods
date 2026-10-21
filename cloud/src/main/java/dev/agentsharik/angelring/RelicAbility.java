package dev.agentsharik.angelring;

/**
 * Три способности кольца. Две активные (клавиши R и V) и одна пассивная (работает, пока кольцо надето).
 * Сила каждой способности зависит от её редкости (0..6 звёзд) и уровня (1..4).
 */
public enum RelicAbility {
    BONDS(0, true, "bonds"),
    SANCTUARY(1, true, "sanctuary"),
    BLOOD_WARD(2, false, "blood_ward");

    public static final int MAX_LEVEL = 4;
    public static final int MAX_STARS = 6;

    public final int id;
    public final boolean active;
    /** Часть ключей перевода: gui.angelring.ability.<key>.name / .desc / .short */
    public final String key;

    RelicAbility(int id, boolean active, String key) {
        this.id = id;
        this.active = active;
        this.key = key;
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

    /** Каждый уровень выше первого усиливает длительность на 15%. */
    private static float levelMul(int level) {
        return 1f + 0.15f * (Math.max(1, level) - 1);
    }

    // ---- «Багровые узы» (контроль) ----
    public static float bondsRadius(int stars) {
        return lerp(4f, 8f, stars);
    }

    public static float bondsSeconds(int stars, int level) {
        return lerp(2f, 6f, stars) * levelMul(level);
    }

    // ---- «Святилище падшего» (выживаемость и взаимодействие с окружением) ----
    public static float sanctuaryRadius(int stars) {
        return lerp(3f, 5.5f, stars);
    }

    public static float sanctuarySeconds(int stars, int level) {
        return lerp(5f, 10f, stars) * levelMul(level);
    }

    // ---- «Кровавая защита» (пассивная) ----
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

    /** Перезарядка активных способностей в секундах (0 для пассивной). */
    public int cooldownSeconds(int stars, int level) {
        float base = switch (this) {
            case BONDS -> lerp(45f, 32f, stars);
            case SANCTUARY -> lerp(60f, 42f, stars);
            case BLOOD_WARD -> 0f;
        };
        return Math.round(base * (1f - 0.05f * (Math.max(1, level) - 1)));
    }
}
