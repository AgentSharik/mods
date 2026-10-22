package dev.agentsharik.angelring;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Собственные частицы мода: багровый дым и багровые искры. Внешний вид задают json и текстуры в assets. */
public final class ModParticles {
    private ModParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, AngelRingMod.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CRIMSON_SMOKE =
            PARTICLES.register("crimson_smoke", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CRIMSON_SPARK =
            PARTICLES.register("crimson_spark", () -> new SimpleParticleType(false));
}
