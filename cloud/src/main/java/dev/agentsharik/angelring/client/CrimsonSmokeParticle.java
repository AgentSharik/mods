package dev.agentsharik.angelring.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Багровый дым: медленно поднимается, разрастается, темнеет и растворяется. */
public class CrimsonSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float maxAlpha;

    protected CrimsonSmokeParticle(ClientLevel level, double x, double y, double z,
                                   double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        // родительский конструктор подмешивает случайность в скорость — задаём нужную
        this.xd = vx + (random.nextDouble() - 0.5) * 0.02;
        this.yd = vy + (random.nextDouble() - 0.5) * 0.02;
        this.zd = vz + (random.nextDouble() - 0.5) * 0.02;
        this.friction = 0.94F;
        this.gravity = -0.002F;
        this.hasPhysics = false;
        this.lifetime = 40 + random.nextInt(30);
        this.quadSize = 0.45F + random.nextFloat() * 0.45F;
        float shade = 0.85F + random.nextFloat() * 0.15F;
        this.rCol = 0.45F * shade;
        this.gCol = 0.03F * shade;
        this.bCol = 0.07F * shade;
        this.maxAlpha = 0.75F + random.nextFloat() * 0.2F;
        this.alpha = 0F;
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        float life = this.age / (float) this.lifetime;
        // плавное появление и растворение
        float fade = life < 0.15F ? life / 0.15F : (1F - life) / 0.85F;
        this.alpha = Math.max(0F, this.maxAlpha * fade);
        this.quadSize *= 1.012F;
        // к концу жизни дым темнеет
        this.rCol *= 0.995F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public net.minecraft.client.particle.Particle createParticle(SimpleParticleType type, ClientLevel level,
                                                                     double x, double y, double z,
                                                                     double vx, double vy, double vz) {
            return new CrimsonSmokeParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
