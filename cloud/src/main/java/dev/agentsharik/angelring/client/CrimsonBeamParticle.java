package dev.agentsharik.angelring.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Тонкий энергетический луч: вытянутый прямоугольник с мягким градиентом, который поворачивается вокруг своей оси
 * лицом к камере. Рисуется тем же полупрозрачным типом, что и остальные частицы, поэтому не даёт белых полос.
 * Направление луча — направление переданной «скорости», длина луча в блоках — длина этого вектора.
 * Сама частица неподвижна, центр луча находится в точке появления.
 */
public class CrimsonBeamParticle extends TextureSheetParticle {
    private static final float MAX_LENGTH = 32F;

    private final float axisX;
    private final float axisY;
    private final float axisZ;
    private final float halfLength;
    private final float baseHalfWidth;
    private final float maxAlpha;

    protected CrimsonBeamParticle(ClientLevel level, double x, double y, double z,
                                  double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.friction = 1.0F;
        this.gravity = 0F;
        this.hasPhysics = false;
        this.lifetime = 7 + random.nextInt(4);

        double len = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (len < 1.0E-4) {
            this.axisX = 0F;
            this.axisY = 1F;
            this.axisZ = 0F;
            len = 2.0;
        } else {
            this.axisX = (float) (vx / len);
            this.axisY = (float) (vy / len);
            this.axisZ = (float) (vz / len);
        }
        this.halfLength = (float) Math.min(MAX_LENGTH, Math.max(0.3, len)) * 0.5F;
        this.baseHalfWidth = 0.035F + random.nextFloat() * 0.02F;
        this.maxAlpha = 0.65F + random.nextFloat() * 0.25F;
        this.rCol = 1.0F;
        this.gCol = 0.22F + random.nextFloat() * 0.12F;
        this.bCol = 0.28F + random.nextFloat() * 0.1F;
        this.alpha = 0F;
        this.pickSprite(sprites);

        // рамка отсечения должна охватывать весь луч, иначе он пропадёт, когда центр уйдёт за край экрана
        float hx = Math.abs(axisX) * halfLength + baseHalfWidth;
        float hy = Math.abs(axisY) * halfLength + baseHalfWidth;
        float hz = Math.abs(axisZ) * halfLength + baseHalfWidth;
        this.setBoundingBox(new AABB(x - hx, y - hy, z - hz, x + hx, y + hy, z + hz));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isAlive()) {
            return;
        }
        float life = this.age / (float) this.lifetime;
        float fade = life < 0.2F ? life / 0.2F : (1F - life) / 0.8F;
        float flicker = 0.85F + 0.15F * Mth.sin(this.age * 1.7F + halfLength);
        this.alpha = Math.max(0F, maxAlpha * fade * flicker);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 cam = camera.getPosition();
        float cx = (float) (Mth.lerp(partialTick, this.xo, this.x) - cam.x());
        float cy = (float) (Mth.lerp(partialTick, this.yo, this.y) - cam.y());
        float cz = (float) (Mth.lerp(partialTick, this.zo, this.z) - cam.z());

        // «вправо» для наблюдателя: (взгляд на центр луча) x (ось луча); так луч всегда повёрнут к камере плоскостью
        float rx = cy * axisZ - cz * axisY;
        float ry = cz * axisX - cx * axisZ;
        float rz = cx * axisY - cy * axisX;
        float rl = Mth.sqrt(rx * rx + ry * ry + rz * rz);
        if (rl < 1.0E-4F) {
            return; // смотрим ровно вдоль луча — торец не рисуем
        }
        float life = this.age / (float) this.lifetime;
        float halfWidth = baseHalfWidth * (1F - 0.35F * life);
        float k = halfWidth / rl;
        rx *= k;
        ry *= k;
        rz *= k;
        float ux = axisX * halfLength;
        float uy = axisY * halfLength;
        float uz = axisZ * halfLength;

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = 15728880;

        buffer.addVertex(cx - rx - ux, cy - ry - uy, cz - rz - uz).setUv(u1, v1).setColor(rCol, gCol, bCol, alpha).setLight(light);
        buffer.addVertex(cx - rx + ux, cy - ry + uy, cz - rz + uz).setUv(u1, v0).setColor(rCol, gCol, bCol, alpha).setLight(light);
        buffer.addVertex(cx + rx + ux, cy + ry + uy, cz + rz + uz).setUv(u0, v0).setColor(rCol, gCol, bCol, alpha).setLight(light);
        buffer.addVertex(cx + rx - ux, cy + ry - uy, cz + rz - uz).setUv(u0, v1).setColor(rCol, gCol, bCol, alpha).setLight(light);
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
            return new CrimsonBeamParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
