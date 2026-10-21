package dev.agentsharik.angelring.client;

import com.mojang.blaze3d.platform.InputConstants;

import dev.agentsharik.angelring.AngelRingMod;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Клавиши: J — окно кольца (для надетого кольца), R и V — способности из слотов 1 и 2. Меняются в настройках. */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    public static final KeyMapping OPEN_KEY =
            new KeyMapping("key.angelring.open_relic", InputConstants.KEY_J, "key.categories.angelring");
    public static final KeyMapping ABILITY_1 =
            new KeyMapping("key.angelring.ability_1", InputConstants.KEY_R, "key.categories.angelring");
    public static final KeyMapping ABILITY_2 =
            new KeyMapping("key.angelring.ability_2", InputConstants.KEY_V, "key.categories.angelring");

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_KEY);
        event.register(ABILITY_1);
        event.register(ABILITY_2);
    }

    /** Индикатор способностей — отдельный слой HUD поверх хотбара (другие моды могут его скрыть по id). */
    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, AbilityHud.LAYER_ID, AbilityHud::render);
    }
}
