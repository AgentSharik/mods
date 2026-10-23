package dev.agentsharik.fallenrelics;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = FallenRelicsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModContent.CRAFT_BUILDER_MENU.get(), CraftBuilderScreen::new);
        event.register(ModContent.POCKET_CRAFT_BUILDER_MENU.get(), CraftBuilderScreen::new);
        event.register(ModContent.PACKAGER_MENU.get(), PackagerScreen::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModContent.EMBER.get(), RitualParticles.EmberProvider::new);
        event.registerSpriteSet(ModContent.SPARK.get(), RitualParticles.SparkProvider::new);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(RelicKeys.CAST);
    }
}
