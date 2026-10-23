package dev.agentsharik.fallenrelics;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Кастомные частицы ритуала: багровый уголёк и золотая искра вместо ванильной солянки. */
public final class RitualParticles {
    private RitualParticles() {}

    public static class Ember extends TextureSheetParticle {
        Ember(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
            super(level, x, y, z, vx, vy, vz);
            this.xd = vx * 0.4;
            this.yd = vy * 0.4 + 0.03;
            this.zd = vz * 0.4;
            this.rCol = 0.72F;
            this.gCol = 0.12F;
            this.bCol = 0.16F;
            this.lifetime = 26 + this.random.nextInt(18);
            this.quadSize = 0.09F;
            this.setSpriteFromAge(sprites);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        }

        @Override
        public void tick() {
            super.tick();
            this.quadSize = Math.max(0.02F, this.quadSize * 0.96F);
            if (this.age > this.lifetime / 2) {
                this.gCol *= 0.96F;
                this.bCol *= 0.96F;
            }
        }
    }

    public static class Spark extends TextureSheetParticle {
        Spark(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
            super(level, x, y, z, vx, vy, vz);
            this.xd = vx;
            this.yd = vy + 0.02;
            this.zd = vz;
            this.rCol = 1.0F;
            this.gCol = 0.80F;
            this.bCol = 0.35F;
            this.lifetime = 18 + this.random.nextInt(12);
            this.quadSize = 0.14F;
            this.gravity = 0.015F;
            this.setSpriteFromAge(sprites);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        }

        @Override
        public void tick() {
            super.tick();
            this.quadSize = Math.max(0.02F, this.quadSize * 0.93F);
        }
    }

    public static class EmberProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public EmberProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double vx, double vy, double vz) {
            return new Ember(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }

    public static class SparkProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SparkProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double vx, double vy, double vz) {
            return new Spark(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
