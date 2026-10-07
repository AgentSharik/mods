package dev.agentsharik.autopackager;

public enum PackagerMode {
    HYBRID("autopackager.mode.hybrid"),
    HYBRID2("autopackager.mode.hybrid2"),
    SMALL("autopackager.mode.small"),
    LARGE("autopackager.mode.large"),
    HOLLOW("autopackager.mode.hollow"),
    UNPACKAGE("autopackager.mode.unpackage");

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
