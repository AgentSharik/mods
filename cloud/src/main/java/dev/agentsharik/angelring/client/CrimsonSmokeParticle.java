package dev.agentsharik.angelring.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Багровый дым: небольшое облачко, медленно поднимается, чуть разрастается и растворяется. */
public class CrimsonSmokeParticle extends TextureSheetParticle {
    private final float maxAlpha;
    private final float startSize;
    private final float endSize;
    private final float spin;

    protected CrimsonSmokeParticle(ClientLevel level, double x, double y, double z,
                                   double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        // родительский конструктор подмешивает случайность в скорость — задаём нужную
        this.xd = vx + (random.nextDouble() - 0.5) * 0.01;
        this.yd = vy + (random.nextDouble() - 0.5) * 0.01;
        this.zd = vz + (random.nextDouble() - 0.5) * 0.01;
        this.friction = 0.94F;
        this.gravity = -0.0015F;
        this.hasPhysics = false;
        this.lifetime = 24 + random.nextInt(17);
        // quadSize — половина стороны: старт 0.2–0.32, к концу жизни не больше 0.45
        this.startSize = 0.20F + random.nextFloat() * 0.12F;
        this.endSize = Math.min(0.45F, startSize * 1.4F);
        this.quadSize = startSize;
        float shade = 0.85F + random.nextFloat() * 0.15F;
        this.rCol = 0.62F * shade;
        this.gCol = 0.04F * shade;
        this.bCol = 0.08F * shade;
        this.maxAlpha = 0.45F + random.nextFloat() * 0.15F;
        this.alpha = 0F;
        this.roll = this.oRoll = random.nextFloat() * ((float) Math.PI * 2F);
        this.spin = (random.nextFloat() - 0.5F) * 0.06F;
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isAlive()) {
            return;
        }
        float life = this.age / (float) this.lifetime;
        // плавное появление и растворение
        float fade = life < 0.2F ? life / 0.2F : (1F - life) / 0.8F;
        this.alpha = Math.max(0F, this.maxAlpha * fade);
        this.quadSize = startSize + (endSize - startSize) * life;
        this.oRoll = this.roll;
        this.roll += spin;
        // к концу жизни дым темнеет
        this.rCol *= 0.992F;
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
