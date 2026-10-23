package dev.agentsharik.angelring.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Мягкая багровая вспышка. Неподвижна, быстро нарастает и растворяется. Размер и непрозрачность ограничены,
 * чтобы вспышка никогда не закрывала экран.
 */
public class CrimsonGlowParticle extends TextureSheetParticle {
    private final float startSize;
    private final float endSize;
    private final float maxAlpha;

    protected CrimsonGlowParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.friction = 1.0F;
        this.gravity = 0F;
        this.hasPhysics = false;
        this.lifetime = 8 + random.nextInt(5);
        this.startSize = 0.18F;
        this.endSize = 0.55F + random.nextFloat() * 0.15F;
        this.quadSize = startSize;
        this.maxAlpha = 0.55F;
        this.rCol = 1.0F;
        this.gCol = 0.12F;
        this.bCol = 0.18F;
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
        float fade = life < 0.25F ? life / 0.25F : (1F - life) / 0.75F;
        this.alpha = Math.max(0F, maxAlpha * fade);
        // быстрое расширение в начале, затем замедление
        float grow = 1F - (1F - life) * (1F - life);
        this.quadSize = startSize + (endSize - startSize) * grow;
    }

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
            return new CrimsonGlowParticle(level, x, y, z, sprites);
        }
    }
}
