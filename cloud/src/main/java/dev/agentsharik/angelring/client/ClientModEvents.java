package dev.agentsharik.angelring.client;

import com.mojang.blaze3d.platform.InputConstants;

import dev.agentsharik.angelring.AngelRingMod;
import dev.agentsharik.angelring.ModParticles;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * Клавиши (меняются в Настройки → Управление → «Кольцо ангела»): J — окно надетого кольца, R — активная способность.
 * Здесь же регистрируются собственные частицы.
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    public static final KeyMapping OPEN_KEY =
            new KeyMapping("key.angelring.open_relic", InputConstants.KEY_J, "key.categories.angelring");
    public static final KeyMapping ABILITY_KEY =
            new KeyMapping("key.angelring.ability", InputConstants.KEY_R, "key.categories.angelring");

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_KEY);
        event.register(ABILITY_KEY);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.CRIMSON_SMOKE.get(), CrimsonSmokeParticle.Provider::new);
        event.registerSpriteSet(ModParticles.CRIMSON_SPARK.get(), CrimsonSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticles.XP_ORB.get(), XpOrbParticle.Provider::new);
        event.registerSpriteSet(ModParticles.CRIMSON_GLOW.get(), CrimsonGlowParticle.Provider::new);
        event.registerSpriteSet(ModParticles.CRIMSON_BEAM.get(), CrimsonBeamParticle.Provider::new);
    }
}
