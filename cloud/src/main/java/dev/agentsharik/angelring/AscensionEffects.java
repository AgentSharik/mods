package dev.agentsharik.angelring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * «Багровое вознесение» — ультимативная активная способность (открывается на 10 уровне кольца).
 * Длится 4,8 секунды и состоит из слоёв:
 *  0 – 1,5 с  заряд: на земле разгорается вращающийся круг из рун (кольца и гексаграмма), вверх бьют багровые лучи,
 *             по спирали поднимается дым; игрок получает неуязвимость на время каста;
 *  1,5 – 2,8 с затягивание: враги в радиусе подтягиваются к центру и теряют подвижность, в центре «искривляется
 *             пространство» (портальные частицы);
 *  2,8 с      взрыв: урон всем врагам в радиусе (часть урона возвращается игроку здоровьем), подброс, оглушение,
 *             подсветка; чужие снаряды стираются;
 *  после      расходящиеся ударные волны с багровым дымом и искрами.
 * Все частицы — собственные «багровый дым» и «багровые искры» плюс несколько ванильных.
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID)
public final class AscensionEffects {
    private AscensionEffects() {}

    private static final int TOTAL = 96;
    private static final int PULL_START = 30;
    private static final int DETONATE = 56;

    private static final class Cast {
        final ResourceKey<Level> dimension;
        final Vec3 center;
        final UUID owner;
        final float radius;
        final float damage;
        final int stunTicks;
        final long startTick;

        Cast(ResourceKey<Level> dimension, Vec3 center, UUID owner, float radius, float damage, int stunTicks, long startTick) {
            this.dimension = dimension;
            this.center = center;
            this.owner = owner;
            this.radius = radius;
            this.damage = damage;
            this.stunTicks = stunTicks;
            this.startTick = startTick;
        }
    }

    private static final List<Cast> CASTS = new ArrayList<>();

    public static void start(ServerPlayer player, int stars, int level) {
        ServerLevel world = player.serverLevel();
        Cast cast = new Cast(world.dimension(), player.position(), player.getUUID(),
                RelicAbility.ascensionRadius(stars), RelicAbility.ascensionDamage(stars, level),
                Math.round(RelicAbility.ascensionStunSeconds(stars, level) * 20f), world.getGameTime());
        CASTS.add(cast);
        // неуязвимость на время каста
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, TOTAL, 4, false, false));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (CASTS.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        Iterator<Cast> it = CASTS.iterator();
        while (it.hasNext()) {
            Cast cast = it.next();
            ServerLevel world = server.getLevel(cast.dimension);
            if (world == null) {
                it.remove();
                continue;
            }
            int age = (int) (world.getGameTime() - cast.startTick);
            if (age > TOTAL) {
                it.remove();
                continue;
            }
            step(server, world, cast, age);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        CASTS.clear();
    }

    // ------------------------------------------------------------------ сценарий

    private static void step(MinecraftServer server, ServerLevel w, Cast cast, int age) {
        Vec3 c = cast.center;
        float r = cast.radius;

        // звук по таймлайну
        if (age == 0) {
            sound(w, c, SoundEvents.ENDER_DRAGON_GROWL, 2.0f, 0.5f);
            sound(w, c, SoundEvents.BEACON_ACTIVATE, 2.0f, 0.5f);
        } else if (age == 18) {
            sound(w, c, SoundEvents.WITHER_SPAWN, 0.5f, 1.8f);
        } else if (age == PULL_START) {
            sound(w, c, SoundEvents.EVOKER_CAST_SPELL, 2.0f, 0.5f);
            sound(w, c, SoundEvents.BEACON_AMBIENT, 2.0f, 0.5f);
        } else if (age == DETONATE - 4) {
            sound(w, c, SoundEvents.WARDEN_SONIC_BOOM, 2.5f, 0.6f);
        } else if (age == DETONATE) {
            sound(w, c, SoundEvents.LIGHTNING_BOLT_THUNDER, 4.0f, 0.5f);
            sound(w, c, SoundEvents.WITHER_BREAK_BLOCK, 2.0f, 0.6f);
        } else if (age == DETONATE + 8) {
            sound(w, c, SoundEvents.ENDER_DRAGON_FLAP, 2.0f, 0.7f);
        }

        // слой 1: круг из рун и лучи света
        double spin = age * 0.05;
        if (age % 4 == 0 && age < DETONATE + 18) {
            ring(w, c, r, Math.max(16, (int) (r * 2.5)), spin, 0.08);
            ring(w, c, r * 0.62, Math.max(12, (int) (r * 1.6)), -spin * 1.4, 0.08);
            ring(w, c, r * 0.28, 8, spin * 2.0, 0.08);
            hexagram(w, c, r * 0.92, spin * 0.3);
        }
        if (age % 4 == 0 && age < DETONATE + 8) {
            beams(w, c, r, spin * 0.4, age >= PULL_START ? 9 : 5);
        }

        // слой 2: дымная спираль внутрь
        if (age < DETONATE && age % 2 == 0) {
            for (int arm = 0; arm < 3; arm++) {
                double a = age * 0.22 + arm * (Math.PI * 2 / 3);
                double rr = Math.max(1.0, r * 0.95 * (1.0 - age / (double) DETONATE));
                double x = c.x + Math.cos(a) * rr;
                double z = c.z + Math.sin(a) * rr;
                smoke(w, x, c.y + 0.2 + age * 0.03, z, -Math.cos(a) * 0.05, 0.07, -Math.sin(a) * 0.05);
            }
        }

        // слой 3: затягивание врагов, «искривление пространства» в центре
        if (age >= PULL_START && age < DETONATE) {
            if (age % 2 == 0) {
                pull(w, cast);
            }
            if (age % 2 == 0) {
                for (int k = 0; k < 3; k++) {
                    double a = w.random.nextDouble() * Math.PI * 2;
                    double e = (w.random.nextDouble() - 0.5) * 1.6;
                    double dx = Math.cos(a) * Math.cos(e);
                    double dy = Math.sin(e);
                    double dz = Math.sin(a) * Math.cos(e);
                    spark(w, c.x + dx * 1.8, c.y + 1.0 + dy * 1.8, c.z + dz * 1.8, -dx * 0.14, -dy * 0.14, -dz * 0.14);
                }
            }
        }

        // слой 4: взрыв и ударные волны
        if (age == DETONATE) {
            detonate(server, w, cast);
        }
        if (age > DETONATE && age <= DETONATE + 16 && age % 2 == 0) {
            shockwave(w, c, r * (age - DETONATE) / 16.0);
        }
    }

    // ------------------------------------------------------------------ геометрия частиц

    private static void spark(ServerLevel w, double x, double y, double z, double vx, double vy, double vz) {
        w.sendParticles(ModParticles.CRIMSON_SPARK.get(), x, y, z, 0, vx, vy, vz, 1.0);
    }

    private static void smoke(ServerLevel w, double x, double y, double z, double vx, double vy, double vz) {
        w.sendParticles(ModParticles.CRIMSON_SMOKE.get(), x, y, z, 0, vx, vy, vz, 1.0);
    }

    private static void sound(ServerLevel w, Vec3 c, SoundEvent sound, float volume, float pitch) {
        w.playSound(null, c.x, c.y, c.z, sound, SoundSource.PLAYERS, volume, pitch);
    }

    /** Окружность на земле: тонкая сплошная линия из касательных лучей и несколько искр, поднимающихся над ней. */
    private static void ring(ServerLevel w, Vec3 c, double radius, int points, double rot, double lift) {
        RelicFx.beamCircle(w, new Vec3(c.x, c.y + 0.08, c.z), radius, points, rot);
        for (int k = 0; k < 4; k++) {
            double a = w.random.nextDouble() * Math.PI * 2;
            spark(w, c.x + Math.cos(a) * radius, c.y + 0.08, c.z + Math.sin(a) * radius, 0, lift * 0.5, 0);
        }
    }

    /** Гексаграмма: два вписанных треугольника, каждое ребро — один тонкий луч. */
    private static void hexagram(ServerLevel w, Vec3 c, double radius, double rot) {
        for (int tri = 0; tri < 2; tri++) {
            double base = rot + tri * Math.PI / 3;
            for (int edge = 0; edge < 3; edge++) {
                double a0 = base + edge * (Math.PI * 2 / 3);
                double a1 = base + (edge + 1) * (Math.PI * 2 / 3);
                Vec3 p0 = new Vec3(c.x + Math.cos(a0) * radius, c.y + 0.1, c.z + Math.sin(a0) * radius);
                Vec3 p1 = new Vec3(c.x + Math.cos(a1) * radius, c.y + 0.1, c.z + Math.sin(a1) * radius);
                RelicFx.beamBetween(w, p0, p1);
            }
        }
    }

    /** Шесть тонких багровых лучей света, бьющих вверх от вершин гексаграммы. */
    private static void beams(ServerLevel w, Vec3 c, double radius, double rot, int segments) {
        double height = segments * 1.3;
        for (int k = 0; k < 6; k++) {
            double a = rot + k * (Math.PI / 3);
            double x = c.x + Math.cos(a) * radius * 0.92;
            double z = c.z + Math.sin(a) * radius * 0.92;
            RelicFx.beam(w, x, c.y + 0.2 + height / 2, z, 0, 1, 0, height);
        }
    }

    /** Расходящаяся ударная волна: тонкое кольцо лучей, искры и редкие клубы дыма на заданном радиусе. */
    private static void shockwave(ServerLevel w, Vec3 c, double radius) {
        if (radius < 0.5) {
            return;
        }
        Vec3 level = new Vec3(c.x, c.y + 0.3, c.z);
        RelicFx.beamCircle(w, level, radius, Math.max(12, (int) (radius * 2.5)), 0);
        int points = Math.max(16, (int) (radius * 2.5));
        for (int k = 0; k < points; k++) {
            double a = k * (Math.PI * 2 / points);
            double dx = Math.cos(a);
            double dz = Math.sin(a);
            spark(w, c.x + dx * radius, c.y + 0.3, c.z + dz * radius, dx * 0.2, 0.05, dz * 0.2);
            if (k % 4 == 0) {
                smoke(w, c.x + dx * radius, c.y + 0.2, c.z + dz * radius, dx * 0.06, 0.06, dz * 0.06);
            }
        }
    }

    // ------------------------------------------------------------------ механика

    private static boolean isTarget(LivingEntity e, UUID owner) {
        return e instanceof Enemy && e.isAlive() && !e.getUUID().equals(owner);
    }

    private static void pull(ServerLevel w, Cast cast) {
        Vec3 c = cast.center;
        double r = cast.radius;
        AABB box = new AABB(c.x - r, c.y - 2, c.z - r, c.x + r, c.y + r * 0.6 + 2, c.z + r);
        List<LivingEntity> targets = w.getEntitiesOfClass(LivingEntity.class, box,
                e -> isTarget(e, cast.owner) && e.position().distanceTo(c) <= r);
        for (LivingEntity e : targets) {
            Vec3 toCenter = c.subtract(e.position()).multiply(1, 0, 1).normalize().scale(0.3);
            e.setDeltaMovement(e.getDeltaMovement().scale(0.6).add(toCenter.x, 0.08, toCenter.z));
            e.hurtMarked = true;
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 12, 3, false, false));
        }
    }

    private static void detonate(MinecraftServer server, ServerLevel w, Cast cast) {
        Vec3 c = cast.center;
        double r = cast.radius;
        ServerPlayer owner = server.getPlayerList().getPlayer(cast.owner);
        DamageSource source = owner != null ? w.damageSources().indirectMagic(owner, owner) : w.damageSources().magic();

        AABB box = new AABB(c.x - r, c.y - 2, c.z - r, c.x + r, c.y + r * 0.6 + 2, c.z + r);
        List<LivingEntity> targets = w.getEntitiesOfClass(LivingEntity.class, box,
                e -> isTarget(e, cast.owner) && e.position().distanceTo(c) <= r);
        float dealt = 0f;
        for (LivingEntity e : targets) {
            float before = e.getHealth();
            e.hurt(source, cast.damage);
            dealt += Math.max(0f, before - Math.max(0f, e.getHealth()));
            Vec3 away = e.position().subtract(c).multiply(1, 0, 1).normalize().scale(0.8);
            e.setDeltaMovement(away.x, 0.85, away.z);
            e.hurtMarked = true;
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, cast.stunTicks, 6, false, true));
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, cast.stunTicks, 1, false, true));
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, cast.stunTicks, 0, false, false));
        }
        // кровь питает владельца: часть нанесённого урона возвращается здоровьем
        if (owner != null && dealt > 0f) {
            owner.heal(Math.min(owner.getMaxHealth() * 0.3f, dealt * 0.2f));
        }
        // чужие снаряды стираются
        for (Projectile p : w.getEntitiesOfClass(Projectile.class, box, p -> !(p.getOwner() instanceof ServerPlayer))) {
            p.discard();
        }

        // вспышка: мягкое свечение, умеренные клубы дыма, искры и тонкие лучи
        RelicFx.glow(w, c.x, c.y + 1.0, c.z);
        RelicFx.glow(w, c.x, c.y + 1.2, c.z);
        w.sendParticles(ModParticles.CRIMSON_SMOKE.get(), c.x, c.y + 0.5, c.z, 30, r * 0.1, 0.4, r * 0.1, 0.12);
        w.sendParticles(ModParticles.CRIMSON_SPARK.get(), c.x, c.y + 1.0, c.z, 70, 0.4, 0.6, 0.4, 0.45);
        w.sendParticles(ModParticles.CRIMSON_SPARK.get(), c.x, c.y + 0.5, c.z, 20, r * 0.15, 0.3, r * 0.15, 0.1);
        w.sendParticles(ModParticles.CRIMSON_SMOKE.get(), c.x, c.y + 0.5, c.z, 14, r * 0.12, 0.3, r * 0.12, 0.08);
        // вертикальные лучи столбом
        RelicFx.beam(w, c.x, c.y + 10.0, c.z, 0, 1, 0, 20.0);
        RelicFx.beam(w, c.x + 0.25, c.y + 7.0, c.z, 0, 1, 0, 14.0);
        RelicFx.beam(w, c.x - 0.25, c.y + 7.0, c.z, 0, 1, 0, 14.0);
    }
}
