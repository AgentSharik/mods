package dev.agentsharik.angelring;

import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
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

/**
 * Серверная логика: прокачка за уровни игрока, реролл редкости и применение активных способностей.
 *
 * Все проверки выполняются здесь, на сервере и в главном потоке (пакет ставит задачу через enqueueWork), поэтому
 * «проверить → списать уровни → записать данные» идёт атомарно: два быстрых клика не могут потратить одни и те же
 * уровни дважды. Клиент ничего не меняет сам: он только просит и получает код результата (см. {@link Result}).
 */
public final class RelicActions {
    private RelicActions() {}

    /** Коды результата, которые сервер возвращает клиенту (звук и анимация играют только по ним). */
    public static final class Result {
        private Result() {}

        public static final int OK = 0;
        public static final int NO_LEVELS = 1;
        public static final int LOCKED = 2;
        public static final int MAXED = 3;
        public static final int NEED_CONFIRM = 4;
        public static final int COOLDOWN = 5;
        public static final int NOT_LEARNED = 6;
        public static final int NO_RING = 7;
        public static final int INVALID = 8;
        public static final int RATE_LIMITED = 9;
    }

    /** Флаг пакета: игрок подтвердил реролл топовой редкости (удержанием кнопки). */
    public static final int FLAG_CONFIRMED = 1;

    public static int perform(ServerPlayer player, ItemStack stack, int action, int arg, int flags) {
        RelicData data = stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
        if (!data.rolled()) {
            // на всякий случай: кольцо из сундука, которое ещё ни разу не попадало в инвентарь игрока
            data = data.ensureRolled(player.getRandom());
            stack.set(ModComponents.RELIC_DATA.get(), data);
        }
        return switch (action) {
            case RelicData.ACTION_UPGRADE -> upgrade(player, stack, data, arg);
            case RelicData.ACTION_REROLL -> reroll(player, stack, data, flags);
            case RelicData.ACTION_USE -> use(player, stack, data, arg);
            default -> Result.INVALID;
        };
    }

    /** У креативного игрока (мгновенная стройка) уровни не списываются — удобно для проверки. */
    private static boolean free(ServerPlayer player) {
        return player.getAbilities().instabuild;
    }

    private static int upgrade(ServerPlayer player, ItemStack stack, RelicData data, int id) {
        if (id < 0 || id >= RelicData.ABILITY_COUNT) {
            return Result.INVALID;
        }
        int cost = data.upgradeCost(id);
        if (cost < 0) {
            return Result.MAXED;
        }
        if (!data.canUpgrade(id)) {
            return Result.LOCKED; // не хватает уровня кольца
        }
        boolean free = free(player);
        if (!free && player.experienceLevel < cost) {
            return Result.NO_LEVELS;
        }
        // сначала записываем результат, потом списываем: оба шага в одном тике главного потока
        stack.set(ModComponents.RELIC_DATA.get(), data.upgrade(id));
        if (!free) {
            player.giveExperienceLevels(-cost);
        }
        return Result.OK;
    }

    private static int reroll(ServerPlayer player, ItemStack stack, RelicData data, int flags) {
        if (data.rerollNeedsConfirm() && (flags & FLAG_CONFIRMED) == 0) {
            return Result.NEED_CONFIRM; // защита топовой редкости дублируется на сервере
        }
        boolean free = free(player);
        if (!free && player.experienceLevel < RelicData.REROLL_COST) {
            return Result.NO_LEVELS;
        }
        stack.set(ModComponents.RELIC_DATA.get(), data.reroll(player.getRandom()));
        if (!free) {
            player.giveExperienceLevels(-RelicData.REROLL_COST);
        }
        AngelRingFlight.markDirty(player); // скорость полёта зависит от редкости
        return Result.OK;
    }

    private static int use(ServerPlayer player, ItemStack stack, RelicData data, int id) {
        RelicAbility ability = RelicAbility.byId(id);
        if (ability == null || !ability.active) {
            return Result.INVALID;
        }
        int level = data.abilityLevel(id);
        if (level <= 0) {
            return Result.NOT_LEARNED;
        }
        long now = player.level().getGameTime();
        if (now < data.cooldownReadyAt(id)) {
            return Result.COOLDOWN;
        }
        int stars = data.abilityStars(id);
        switch (ability) {
            case BONDS -> crimsonBonds(player, stars, level);
            case SANCTUARY -> fallenSanctuary(player, stars, level);
            default -> {
                return Result.INVALID;
            }
        }
        long ready = now + 20L * ability.cooldownSeconds(stars, level);
        stack.set(ModComponents.RELIC_DATA.get(), data.withCooldown(id, ready));
        return Result.OK;
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
