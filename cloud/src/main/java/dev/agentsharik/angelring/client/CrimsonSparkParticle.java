package dev.agentsharik.angelring.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Багровая искра: яркая (светится в темноте), быстро гаснет и сжимается. */
public class CrimsonSparkParticle extends TextureSheetParticle {
    private final float startSize;

    protected CrimsonSparkParticle(ClientLevel level, double x, double y, double z,
                                   double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.friction = 0.9F;
        this.gravity = 0F;
        this.hasPhysics = false;
        this.lifetime = 14 + random.nextInt(10);
        this.startSize = 0.14F + random.nextFloat() * 0.12F;
        this.quadSize = startSize;
        this.rCol = 1.0F;
        this.gCol = 0.18F + random.nextFloat() * 0.2F;
        this.bCol = 0.16F + random.nextFloat() * 0.1F;
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        float life = this.age / (float) this.lifetime;
        this.alpha = Math.max(0F, 1F - life * life);
        this.quadSize = startSize * (1F - 0.6F * life);
        this.gCol *= 0.97F;
        this.bCol *= 0.97F;
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
