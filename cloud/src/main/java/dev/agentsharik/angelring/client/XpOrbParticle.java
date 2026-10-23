package dev.agentsharik.angelring.client;

import dev.agentsharik.angelring.RelicFx;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * Светящаяся сфера опыта с мягким ореолом. Летит по заданной скорости ровно {@link RelicFx#ORB_FLIGHT_TICKS} тиков
 * (без трения, поэтому долетает до цели), слегка вьётся по спирали. Цвет: лаймово-зелёный / изумрудный / жёлто-зелёный,
 * а в момент поглощения кольцом сфера вспыхивает багровым и растворяется.
 */
public class XpOrbParticle extends TextureSheetParticle {
    private static final float[] CRIMSON = {1.0F, 0.10F, 0.18F};

    private final float baseSize;
    private final float phase;
    private final float amplitude;
    private final float turns;
    private final boolean emerald;

    protected XpOrbParticle(ClientLevel level, double x, double y, double z,
                            double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.friction = 1.0F;
        this.gravity = 0F;
        this.hasPhysics = false;
        this.lifetime = RelicFx.ORB_FLIGHT_TICKS;
        this.baseSize = 0.13F + random.nextFloat() * 0.05F;
        this.quadSize = baseSize;
        this.phase = random.nextFloat() * ((float) Math.PI * 2F);
        this.amplitude = 0.22F + random.nextFloat() * 0.28F;
        this.turns = 1.0F + random.nextFloat() * 0.8F;
        this.emerald = random.nextInt(5) == 0;
        this.alpha = 0F;
        this.pickSprite(sprites);
        applyColor(0F);
    }

    /** Смещение по спирали вокруг прямой траектории; плавно нарастает и сходит на нет к цели. */
    private double wobbleX(float t) {
        return Math.cos(phase + t * turns * Math.PI * 2) * amplitude * Math.sin(t * Math.PI);
    }

    private double wobbleY(float t) {
        return Math.sin(phase * 1.3 + t * turns * Math.PI * 2) * amplitude * 0.5 * Math.sin(t * Math.PI);
    }

    private double wobbleZ(float t) {
        return Math.sin(phase + t * turns * Math.PI * 2) * amplitude * Math.sin(t * Math.PI);
    }

    private void applyColor(float t) {
        // зелёно-жёлтое мерцание как у ванильных шаров опыта
        float f = (Mth.sin(this.age * 0.55F + phase) + 1F) * 0.5F;
        float r;
        float g;
        float b;
        if (emerald) {
            r = 0.10F + 0.20F * f;
            g = 0.95F;
            b = 0.35F + 0.15F * f;
        } else {
            r = 0.30F + 0.65F * f;
            g = 1.0F;
            b = 0.08F + 0.10F * f;
        }
        // в конце полёта — переход в багровый цвет реликвии
        float mix = smooth(0.58F, 0.84F, t);
        this.rCol = Mth.lerp(mix, r, CRIMSON[0]);
        this.gCol = Mth.lerp(mix, g, CRIMSON[1]);
        this.bCol = Mth.lerp(mix, b, CRIMSON[2]);
    }

    private static float smooth(float e0, float e1, float x) {
        float t = Mth.clamp((x - e0) / (e1 - e0), 0F, 1F);
        return t * t * (3F - 2F * t);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isAlive()) {
            return;
        }
        float t = this.age / (float) this.lifetime;
        float tPrev = (this.age - 1) / (float) this.lifetime;
        // спираль добавляется к прямолинейному полёту
        this.setPos(this.x + (wobbleX(t) - wobbleX(tPrev)),
                this.y + (wobbleY(t) - wobbleY(tPrev)),
                this.z + (wobbleZ(t) - wobbleZ(tPrev)));

        applyColor(t);
        // вспышка размера в момент смены цвета, затем сжатие при поглощении
        float flare = Mth.sin(Mth.clamp((t - 0.58F) / 0.34F, 0F, 1F) * (float) Math.PI);
        this.quadSize = baseSize * (1F + 0.45F * flare) * (1F - 0.5F * smooth(0.9F, 1F, t));
        this.alpha = Math.min(1F, t / 0.12F) * (1F - smooth(0.9F, 1F, t));
    }

    /** Сфера светится независимо от освещения. */
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
            return new XpOrbParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
