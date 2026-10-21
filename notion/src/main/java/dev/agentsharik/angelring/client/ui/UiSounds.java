package dev.agentsharik.angelring.client.ui;

import dev.agentsharik.angelring.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

/** Звуки интерфейса: играют только у самого игрока (канал «Интерфейс»), не в мире. */
public final class UiSounds {
    private UiSounds() {}

    private static void play(SoundEvent sound, float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    /** Тихий мистический гул при открытии окна. */
    public static void open() {
        play(ModSounds.UI_OPEN_HUM.get(), 0.6f, 0.35f);
        play(ModSounds.UI_OPEN_WHISPER.get(), 1.3f, 0.18f);
    }

    /** Тяжёлый глухой удар с шелестом при реролле. */
    public static void reroll() {
        play(ModSounds.UI_REROLL_THUD.get(), 0.55f, 0.55f);
        play(ModSounds.UI_REROLL_RUSTLE.get(), 0.75f, 0.6f);
    }

    /** Звонкий мажорный аккорд (тоника, терция, квинта) + мерцание — прокачка и повышение уровня. */
    public static void chord() {
        SoundEvent chime = ModSounds.UI_CHIME.get();
        play(chime, 1.0f, 0.45f);
        play(chime, 1.26f, 0.38f);
        play(chime, 1.5f, 0.34f);
        play(ModSounds.UI_SHIMMER.get(), 1.2f, 0.5f);
    }

    /** Ошибка: не хватает уровней, заблокировано, перезарядка. */
    public static void error() {
        play(ModSounds.UI_ERROR.get(), 0.55f, 0.6f);
        play(ModSounds.UI_ERROR_CLICK.get(), 0.8f, 0.35f);
    }

    /** Лёгкий щелчок нажатия. */
    public static void press(float pitch) {
        play(ModSounds.UI_PRESS.get(), pitch, 0.25f);
    }
}
