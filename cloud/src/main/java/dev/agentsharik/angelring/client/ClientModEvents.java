package dev.agentsharik.angelring.client;

import com.mojang.blaze3d.platform.InputConstants;

import dev.agentsharik.angelring.AngelRingMod;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/** Регистрация клавиши открытия окна кольца (по умолчанию J, меняется в настройках управления). */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    public static final KeyMapping OPEN_KEY =
            new KeyMapping("key.angelring.open_relic", InputConstants.KEY_J, "key.categories.angelring");

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_KEY);
    }
}
