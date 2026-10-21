package dev.agentsharik.angelring;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Собственные звуковые события мода. Сами звуки описаны в assets/angelring/sounds.json и ссылаются на ванильные
 * события ("type": "event"), поэтому новых аудиофайлов не нужно. Тон и громкость каждого слоя задаются в коде
 * (см. client/ui/UiSounds), так что один и тот же звук можно «сыграть» аккордом на разной высоте.
 */
public final class ModSounds {
    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, AngelRingMod.MOD_ID);

    /** Открытие окна: тихий мистический гул + шёпот. */
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_OPEN_HUM = register("ui.open_hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_OPEN_WHISPER = register("ui.open_whisper");
    /** Реролл: тяжёлый глухой удар + шелест. */
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_REROLL_THUD = register("ui.reroll_thud");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_REROLL_RUSTLE = register("ui.reroll_rustle");
    /** Звонкий аккорд (играется тремя нотами) и «мерцание» поверх него. */
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_CHIME = register("ui.chime");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_SHIMMER = register("ui.shimmer");
    /** Ошибка: низкая нота + сухой щелчок. */
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_ERROR = register("ui.error");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_ERROR_CLICK = register("ui.error_click");
    /** Нажатие кнопки. */
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_PRESS = register("ui.press");
    /** Повышение уровня кольца в мире (слышит только сам игрок). */
    public static final DeferredHolder<SoundEvent, SoundEvent> RING_LEVEL_UP = register("ring.level_up");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
