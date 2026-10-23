package dev.agentsharik.angelring.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Багровая искра: маленькая, яркая (светится в темноте), плавно появляется, быстро гаснет и сжимается. */
public class CrimsonSparkParticle extends TextureSheetParticle {
    private final float startSize;

    protected CrimsonSparkParticle(ClientLevel level, double x, double y, double z,
                                   double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        // родительский конструктор подмешивает случайность в скорость — задаём нужную
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.friction = 0.9F;
        this.gravity = 0F;
        this.hasPhysics = false;
        this.lifetime = 12 + random.nextInt(9);
        // quadSize — половина стороны квадрата; искры мелкие и аккуратные
        this.startSize = 0.05F + random.nextFloat() * 0.07F;
        this.quadSize = startSize;
        this.rCol = 1.0F;
        this.gCol = 0.18F + random.nextFloat() * 0.2F;
        this.bCol = 0.16F + random.nextFloat() * 0.1F;
        this.alpha = 0F;
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isAlive()) {
            return;
        }
        float life = this.age / (float) this.lifetime;
        float fadeIn = Math.min(1F, this.age / 2F);
        this.alpha = Math.max(0F, fadeIn * (1F - life * life));
        this.quadSize = startSize * (1F - 0.55F * life);
        this.gCol *= 0.98F;
        this.bCol *= 0.98F;
    }

    /** Искра всегда светится на полную яркость. */
    @Override
    protected int getLightColor(float partialTick) {
        return 15728880;
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
            return new CrimsonSparkParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
