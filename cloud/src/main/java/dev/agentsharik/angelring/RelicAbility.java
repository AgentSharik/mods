package dev.agentsharik.angelring;

/** Способности кольца. Две активные (по клавише) и одна пассивная. */
public enum RelicAbility {
    DASH(0, false, "dash"),
    WARD(1, false, "ward"),
    FEATHER(2, true, "feather");

    public static final int MAX_LEVEL = 4;

    public final int id;
    public final boolean passive;
    /** Часть ключей перевода: gui.angelring.ability.<key>.name / .desc */
    public final String key;

    RelicAbility(int id, boolean passive, String key) {
        this.id = id;
        this.passive = passive;
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

    /** Перезарядка в секундах для данного уровня (0 для пассивных). */
    public int cooldownSeconds(int level) {
        return switch (this) {
            case DASH -> 12 - 2 * level;   // 10, 8, 6, 4
            case WARD -> 50 - 5 * level;   // 45, 40, 35, 30
            case FEATHER -> 0;
        };
    }

    /** Скорость рывка (блоков за тик). */
    public float dashPower(int level) {
        return 0.6f + 0.2f * level;
    }

    /** Сколько единиц урона (по 2 за сердце) поглощает «Небесный покров». */
    public int absorbHealth(int level) {
        return 4 * level;
    }

    /** Процент снижения урона от падения у пассивной способности. */
    public int fallReductionPercent(int level) {
        return 25 * level;
    }
}
