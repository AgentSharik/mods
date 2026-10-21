package dev.agentsharik.angelring.client;

import dev.agentsharik.angelring.RelicActions;
import dev.agentsharik.angelring.RelicData;
import dev.agentsharik.angelring.client.ui.UiSounds;
import dev.agentsharik.angelring.network.RelicResultPayload;
import net.minecraft.client.Minecraft;

/** Ответ сервера на действие. Загружается только на клиенте (см. AngelRingMod.registerPayloads). */
public final class ClientResultHandler {
    private ClientResultHandler() {}

    public static void handle(RelicResultPayload p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof RelicScreen screen) {
            screen.onServerResult(p.action(), p.arg(), p.code());
        }
        if (p.action() == RelicData.ACTION_WARD_PROC) {
            AbilityHud.onWardProc();
            return;
        }
        if (p.action() == RelicData.ACTION_USE) {
            if (p.code() == RelicActions.Result.OK) {
                AbilityHud.onUsed(p.arg());
            } else {
                AbilityHud.onError(p.arg());
                UiSounds.error();
            }
        }
    }
}
