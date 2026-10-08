package dev.agentsharik.fallenrelics;

public enum PackagerMode {
    HYBRID("fallenrelics.mode.hybrid"),
    HYBRID2("fallenrelics.mode.hybrid2"),
    SMALL("fallenrelics.mode.small"),
    LARGE("fallenrelics.mode.large");

    private final String translationKey;

    PackagerMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }

    public PackagerMode next() {
        PackagerMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static PackagerMode fromId(int id) {
        PackagerMode[] values = values();
        return id >= 0 && id < values.length ? values[id] : HYBRID;
    }
}
