package dev.agentsharik.angelring;

import java.util.Optional;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

/**
 * Всё, что работает, пока надето «Кольцо ангела»:
 *  1) опыт кольца копится вместе с опытом игрока;
 *  2) «Кровавая защита» (пассивная, с 1 уровня кольца);
 *  3) «Жатва падших» (пассивная, с 5 уровня кольца).
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID)
public final class AbilityEvents {
    private AbilityEvents() {}

    /** Пауза между срабатываниями защиты (тики), чтобы серия мелких ударов не давала «вечный блок». */
    private static final long WARD_PAUSE_TICKS = 10L;
    private static final String WARD_TAG = "angelring_ward_ready";

    /** Опыт кольца копится синхронно с обычным опытом игрока (шары опыта, команды /xp add ... points). */
    @SubscribeEvent
    public static void onPlayerXp(PlayerXpEvent.XpChange event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        int amount = event.getAmount();
        if (amount <= 0) {
            return;
        }
        Optional<ItemStack> ring = RingLookup.find(player);
        if (ring.isEmpty()) {
            return;
        }
        ItemStack stack = ring.get();
        RelicData data = stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
        RelicData updated = data.addXp(amount);
        if (updated != data) {
            stack.set(ModComponents.RELIC_DATA.get(), updated);
            if (!data.canLevelUp() && updated.canLevelUp()) {
                player.displayClientMessage(Component.translatable("message.angelring.level_ready"), true);
            }
        }
    }

    /**
     * «Кровавая защита»: при получении урона есть шанс (от 20% на 0 звёздах до 60% на 6 звёздах) полностью
     * заблокировать удар, вернуть немного здоровья и ослабить нападавшего.
     */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        DamageSource source = event.getSource();
        if (event.getAmount() <= 0f || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return; // пустоту и /kill защита не останавливает
        }
        Optional<ItemStack> ring = RingLookup.find(player);
        if (ring.isEmpty()) {
            return;
        }
        RelicData data = ring.get().getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
        int id = RelicAbility.BLOOD_WARD.id;
        int level = data.abilityLevel(id);
        if (level <= 0) {
            return; // способность ещё закрыта
        }
        long now = player.level().getGameTime();
        CompoundTag tag = player.getPersistentData();
        if (now < tag.getLong(WARD_TAG)) {
            return;
        }
        int stars = data.abilityStars(id);
        if (player.getRandom().nextFloat() >= RelicAbility.wardChance(stars, level)) {
            return;
        }

        event.setCanceled(true);
        tag.putLong(WARD_TAG, now + WARD_PAUSE_TICKS);

        player.heal(RelicAbility.wardHeal(level));
        if (source.getEntity() instanceof LivingEntity attacker && attacker != player) {
            attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                    20 * RelicAbility.wardWeaknessSeconds(stars), 0, false, true));
        }
        ServerLevel world = player.serverLevel();
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.7f);
        world.sendParticles(ModParticles.CRIMSON_SMOKE.get(), player.getX(), player.getY() + 1.0, player.getZ(),
                10, 0.4, 0.6, 0.4, 0.02);
        world.sendParticles(ModParticles.CRIMSON_SPARK.get(), player.getX(), player.getY() + 1.0, player.getZ(),
                18, 0.4, 0.6, 0.4, 0.12);
    }

    /**
     * «Жатва падших»: за каждого убитого врага вы возвращаете здоровье, получаете поглощение урона
     * и с шансом (от 10% до 40% в зависимости от звёзд) — бонусный опыт.
     */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Enemy)) {
            return;
        }
        Optional<ItemStack> ring = RingLookup.find(player);
        if (ring.isEmpty()) {
            return;
        }
        RelicData data = ring.get().getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
        int id = RelicAbility.HARVEST.id;
        int level = data.abilityLevel(id);
        if (level <= 0) {
            return; // откроется на 5 уровне кольца
        }
        int stars = data.abilityStars(id);
        player.heal(RelicAbility.harvestHeal(stars, level));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,
                20 * RelicAbility.harvestAbsorbSeconds(stars), level >= 3 ? 1 : 0, false, true));

        ServerLevel world = player.serverLevel();
        if (player.getRandom().nextFloat() < RelicAbility.harvestXpChance(stars, level)) {
            ExperienceOrb.award(world, victim.position(), RelicAbility.harvestBonusXp(level));
        }
        world.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.35f, 0.6f);
        world.sendParticles(ModParticles.CRIMSON_SMOKE.get(), victim.getX(), victim.getY() + 0.8, victim.getZ(),
                8, 0.3, 0.4, 0.3, 0.03);
        world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, victim.getX(), victim.getY() + 0.8, victim.getZ(),
                6, 0.2, 0.3, 0.2, 0.05);
        // душа убитого стремится к игроку
        for (int k = 0; k < 8; k++) {
            double t = k / 8.0;
            world.sendParticles(ModParticles.CRIMSON_SPARK.get(),
                    victim.getX() + (player.getX() - victim.getX()) * t,
                    victim.getY() + 1.0 + (player.getY() + 1.0 - victim.getY() - 1.0) * t,
                    victim.getZ() + (player.getZ() - victim.getZ()) * t, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
