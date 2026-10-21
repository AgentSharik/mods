package dev.agentsharik.angelring.client;

import dev.agentsharik.angelring.AngelRingMod;
import dev.agentsharik.angelring.RelicData;
import dev.agentsharik.angelring.client.ui.UiSounds;
import dev.agentsharik.angelring.network.RelicActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AngelRingMod.MOD_ID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientRingCache.refresh(mc);
        AbilityHud.tick(mc); // один поиск кольца за тик — для HUD и клавиш
        if (mc.player == null) {
            return;
        }

        // J — окно надетого кольца
        while (ClientModEvents.OPEN_KEY.consumeClick()) {
            if (mc.screen != null) {
                continue;
            }
            if (ClientRingCache.worn()) {
                mc.setScreen(new RelicScreen(null, null, -1));
            } else {
                mc.player.displayClientMessage(Component.translatable("message.angelring.not_worn"), true);
            }
        }

        // R и V — способности
        while (ClientModEvents.ABILITY_1.consumeClick()) {
            useAbility(mc, 0);
        }
        while (ClientModEvents.ABILITY_2.consumeClick()) {
            useAbility(mc, 1);
        }
    }

    /**
     * Предварительная проверка на клиенте нужна только для мгновенного отклика (звук ошибки,
     * тряска ячейки в HUD) и чтобы не слать лишние пакеты. Решает всё равно сервер.
     */
    private static void useAbility(Minecraft mc, int abilityId) {
        if (mc.screen != null || mc.player == null || mc.level == null) {
            return;
        }
        if (!ClientRingCache.worn()) {
            mc.player.displayClientMessage(Component.translatable("message.angelring.not_worn"), true);
            return;
        }
        RelicData data = ClientRingCache.data();
        if (data.abilityLevel(abilityId) <= 0) {
            mc.player.displayClientMessage(Component.translatable("message.angelring.not_learned"), true);
            AbilityHud.onError(abilityId);
            UiSounds.error();
            return;
        }
        long left = data.cooldownReadyAt(abilityId) - mc.level.getGameTime();
        if (left > 0) {
            mc.player.displayClientMessage(
                    Component.translatable("message.angelring.cooldown", (left + 19) / 20), true);
            AbilityHud.onError(abilityId);
            UiSounds.error();
            return;
        }
        PacketDistributor.sendToServer(new RelicActionPayload(RelicData.ACTION_USE, abilityId, -1, 0, 0));
    }
}
