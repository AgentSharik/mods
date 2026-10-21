package dev.agentsharik.angelring;

import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Серверная логика: прокачка за уровни игрока, реролл редкости и применение активных способностей. */
public final class RelicActions {
    private RelicActions() {}

    public static void perform(ServerPlayer player, ItemStack stack, int action, int arg) {
        RelicData data = stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
        switch (action) {
            case RelicData.ACTION_UPGRADE -> upgrade(player, stack, data, arg);
            case RelicData.ACTION_REROLL -> reroll(player, stack, data);
            case RelicData.ACTION_USE -> use(player, stack, data, arg);
            default -> { }
        }
    }

    /** Улучшение способности стоит уровни опыта игрока; креативу уровни не нужны (удобно для проверки). */
    private static void upgrade(ServerPlayer player, ItemStack stack, RelicData data, int id) {
        int cost = data.upgradeCost(id);
        if (cost < 0 || !data.canUpgrade(id)) {
            return; // максимум или не хватает уровня кольца
        }
        boolean free = player.isCreative();
        if (!free && player.experienceLevel < cost) {
            return;
        }
        if (!free) {
            player.giveExperienceLevels(-cost);
        }
        stack.set(ModComponents.RELIC_DATA.get(), data.upgrade(id));
        ServerLevel world = player.serverLevel();
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
    }

    private static void reroll(ServerPlayer player, ItemStack stack, RelicData data) {
        boolean free = player.isCreative();
        if (!free && player.experienceLevel < RelicData.REROLL_COST) {
            return;
        }
        if (!free) {
            player.giveExperienceLevels(-RelicData.REROLL_COST);
        }
        stack.set(ModComponents.RELIC_DATA.get(), data.reroll(player.getRandom()));
        ServerLevel world = player.serverLevel();
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 0.7f);
    }

    private static void use(ServerPlayer player, ItemStack stack, RelicData data, int id) {
        RelicAbility ability = RelicAbility.byId(id);
        if (ability == null || !ability.active) {
            return;
        }
        int level = data.abilityLevel(id);
        long now = player.level().getGameTime();
        if (level <= 0 || now < data.cooldownReadyAt(id)) {
            return; // не изучена или ещё перезаряжается
        }
        int stars = data.abilityStars(id);
        switch (ability) {
            case BONDS -> crimsonBonds(player, stars, level);
            case SANCTUARY -> fallenSanctuary(player, stars, level);
            default -> {
                return;
            }
        }
        long ready = now + 20L * ability.cooldownSeconds(stars, level);
        stack.set(ModComponents.RELIC_DATA.get(), data.withCooldown(id, ready));
    }

    /**
     * «Багровые узы» — контроль: все враги в радиусе подтягиваются к вам, на время обездвиживаются,
     * ослабляются и начинают светиться (их видно сквозь стены).
     */
    private static void crimsonBonds(ServerPlayer player, int stars, int level) {
        ServerLevel world = player.serverLevel();
        float radius = RelicAbility.bondsRadius(stars);
        int ticks = Math.round(RelicAbility.bondsSeconds(stars, level) * 20f);

        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                e -> e != player && e instanceof Enemy && e.isAlive() && e.distanceToSqr(player) <= radius * radius);
        for (LivingEntity target : targets) {
            Vec3 pull = player.position().subtract(target.position()).multiply(1, 0, 1).normalize().scale(0.45);
            target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.1, pull.z));
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 6, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 1, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false));
            world.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 1.0, target.getZ(),
                    10, 0.3, 0.5, 0.3, 0.02);
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 0.6f);
        world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.2, player.getZ(),
                30, radius * 0.4, 0.1, radius * 0.4, 0.02);
    }

    /** «Святилище падшего» — зона под ногами: отталкивает врагов, лечит игроков, стирает чужие снаряды. */
    private static void fallenSanctuary(ServerPlayer player, int stars, int level) {
        ServerLevel world = player.serverLevel();
        float radius = RelicAbility.sanctuaryRadius(stars);
        int ticks = Math.round(RelicAbility.sanctuarySeconds(stars, level) * 20f);
        SanctuaryZones.add(world, player.position(), radius, ticks);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 0.7f);
        world.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.5, player.getZ(),
                20, radius * 0.3, 0.3, radius * 0.3, 0.02);
    }
}
