package com.agentsharik.ipnpickblockfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Эквивалентный исходник класса, упакованного в JAR.
 *
 * IPN 2.2.5 вызывает этот метод на каждый pick-block. В многопользовательской
 * игре метод предполагает наличие слотов 36..44 в текущем контейнере и читает
 * их без проверки длины. Здесь отключается только этот необязательный хук
 * LockedSlotKeeper; vanilla pick-block продолжает выполняться дальше.
 */
@Mixin(targets = "org.anti_ad.mc.ipnext.event.LockedSlotKeeper", remap = false)
public class LockedSlotKeeperMixin {

    public LockedSlotKeeperMixin() {
    }

    @Inject(
        method = "ignoredSelectedHotbarSlot(I)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void ipnPickBlockFix$cancel(int fromSlot, CallbackInfo ci) {
        ci.cancel();
    }
}
