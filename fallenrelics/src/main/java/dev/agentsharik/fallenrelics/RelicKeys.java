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
 * 1.9.0: активная способность реликвии («Небесная кара», открывается на 10
 * уровне) применяется хоткеем прямо в мире — без открытия свитка.
 * Z по умолчанию, клавиша переназначается в настройках управления;
 * цель — надетое кольцо.
 */
@EventBusSubscriber(modid = FallenRelicsMod.MOD_ID, value = Dist.CLIENT)
public final class RelicKeys {
    public static final KeyMapping CAST = new KeyMapping("key.fallenrelics.cast_active",
            InputConstants.KEY_Z, "key.categories.fallenrelics");

    private RelicKeys() {}

    /** consumeClick() в клиентском тике: без зависимости от классов InputEvent. */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) {
            return;
        }
        while (CAST.consumeClick()) {
            PacketDistributor.sendToServer(new RelicActionPayloads.Cast(2,
                    AngelRingStatusRequestPayload.NO_TARGET));
        }
    }

    /** Подсказка клавиши для тултипа карточки. */
    public static String keyName() {
        return CAST.getTranslatedKeyMessage().getString();
    }
}
