package dev.agentsharik.angelring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * «Святилище падшего»: багровая зона на земле. Пока она горит:
 * враги выталкиваются наружу и замедляются, игроки внутри лечатся и перестают гореть,
 * чужие снаряды (стрелы, огненные шары) внутри зоны исчезают.
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID)
public final class SanctuaryZones {
    private SanctuaryZones() {}

    private record Zone(ResourceKey<Level> dimension, Vec3 center, float radius, long endTick) {}

    private static final List<Zone> ZONES = new ArrayList<>();

    public static void add(ServerLevel level, Vec3 center, float radius, int durationTicks) {
        ZONES.add(new Zone(level.dimension(), center, radius, level.getGameTime() + durationTicks));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (ZONES.isEmpty() || server.getTickCount() % 5 != 0) {
            return;
        }
        Iterator<Zone> it = ZONES.iterator();
        while (it.hasNext()) {
            Zone zone = it.next();
            ServerLevel level = server.getLevel(zone.dimension());
            if (level == null || level.getGameTime() >= zone.endTick()) {
                it.remove();
                continue;
            }
            apply(level, zone);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ZONES.clear();
    }

    private static void apply(ServerLevel level, Zone zone) {
        Vec3 c = zone.center();
        float r = zone.radius();
        AABB box = new AABB(c.x - r, c.y - 1.0, c.z - r, c.x + r, c.y + 3.0, c.z + r);

        for (Entity e : level.getEntities((Entity) null, box, entity -> true)) {
            double dx = e.getX() - c.x;
            double dz = e.getZ() - c.z;
            if (dx * dx + dz * dz > r * r) {
                continue;
            }
            if (e instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, true));
                player.clearFire();
            } else if (e instanceof Enemy && e instanceof LivingEntity living) {
                Vec3 away = new Vec3(dx, 0, dz).normalize().scale(0.45);
                living.setDeltaMovement(away.x, Math.max(living.getDeltaMovement().y, 0.1), away.z);
                living.hurtMarked = true;
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, true, false));
            } else if (e instanceof Projectile projectile && !(projectile.getOwner() instanceof Player)) {
                projectile.discard();
            }
        }

        // кольцо из багровых искр по границе зоны
        for (int k = 0; k < 24; k++) {
            double angle = k * (Math.PI * 2 / 24) + level.getGameTime() * 0.05;
            double px = c.x + Math.cos(angle) * r;
            double pz = c.z + Math.sin(angle) * r;
            level.sendParticles(ParticleTypes.CRIMSON_SPORE, px, c.y + 0.2, pz, 1, 0.0, 0.1, 0.0, 0.0);
            if (k % 3 == 0) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, c.y + 0.1, pz, 1, 0.0, 0.05, 0.0, 0.0);
            }
        }
    }
}
