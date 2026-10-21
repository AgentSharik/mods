package dev.agentsharik.fallenrelics;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 1.8.1: активные способности реликвии применяются хоткеями прямо в мире —
 * без открытия свитка. Z = «Небесный приговор», X = «Убежище». Клавиши
 * переназначаются в настройках управления; цель — надетое кольцо.
 */
@EventBusSubscriber(modid = FallenRelicsMod.MOD_ID, value = Dist.CLIENT)
public final class RelicKeys {
    public static final KeyMapping VERDICT = new KeyMapping("key.fallenrelics.cast_verdict",
            InputConstants.KEY_Z, "key.categories.fallenrelics");
    public static final KeyMapping SANCTUARY = new KeyMapping("key.fallenrelics.cast_sanctuary",
            InputConstants.KEY_X, "key.categories.fallenrelics");

    private RelicKeys() {}

    /** consumeClick() в клиентском тике: без зависимости от классов InputEvent. */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) {
            return;
        }
        while (VERDICT.consumeClick()) {
            send(0);
        }
        while (SANCTUARY.consumeClick()) {
            send(1);
        }
    }

    private static void send(int index) {
        PacketDistributor.sendToServer(new RelicActionPayloads.Cast(index,
                AngelRingStatusRequestPayload.NO_TARGET));
    }

    /** Подсказка клавиши для тултипа карточки. */
    public static String keyName(int index) {
        return (index == 0 ? VERDICT : SANCTUARY).getTranslatedKeyMessage().getString();
    }
}
