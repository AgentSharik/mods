package dev.agentsharik.angelring;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Собственные частицы мода: багровый дым, багровые искры, сфера опыта, мягкое свечение и тонкий луч.
 * Внешний вид задают json и текстуры в assets, поведение — классы в пакете client.
 */
public final class ModParticles {
    private ModParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, AngelRingMod.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CRIMSON_SMOKE =
            PARTICLES.register("crimson_smoke", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CRIMSON_SPARK =
            PARTICLES.register("crimson_spark", () -> new SimpleParticleType(false));

    /** Светящаяся сфера опыта: летит зелёно-жёлтой, при поглощении кольцом вспыхивает багровым. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> XP_ORB =
            PARTICLES.register("xp_orb", () -> new SimpleParticleType(false));

    /** Мягкое багровое свечение для вспышек. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CRIMSON_GLOW =
            PARTICLES.register("crimson_glow", () -> new SimpleParticleType(false));

    /** Тонкий луч: направление и длина задаются «скоростью» (направление вектора, длина вектора в блоках). */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CRIMSON_BEAM =
            PARTICLES.register("crimson_beam", () -> new SimpleParticleType(false));
}
