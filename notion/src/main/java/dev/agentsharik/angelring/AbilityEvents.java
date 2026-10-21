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
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import dev.agentsharik.angelring.network.RelicResultPayload;

/**
 * Две вещи, которые работают, пока кольцо надето:
 *  1) опыт кольца копится вместе с опытом игрока;
 *  2) пассивная способность «Кровавая защита».
 * Оба работают только по событиям (получение опыта / входящий урон) — в тике игрока ничего не проверяется.
 * LivingIncomingDamageEvent — это замена LivingAttackEvent в NeoForge 1.21 (срабатывает до брони и можно отменить).
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID)
public final class AbilityEvents {
    private AbilityEvents() {}

    /** Пауза между срабатываниями защиты (тики), чтобы серия мелких ударов не давала «вечный блок». */
    private static final long WARD_PAUSE_TICKS = 10L;
    private static final String WARD_TAG = "angelring_ward_ready";

    /**
     * Опыт кольца копится синхронно с обычным опытом игрока (шары опыта, команды /xp add ... points).
     * Низкий приоритет: если другой мод отменит или изменит начисление, кольцо получит ровно то, что
     * реально получил игрок (отменённые события сюда не доходят) — без «дюпа» опыта кольца.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
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
            if (updated.level() > data.level()) {
                player.displayClientMessage(Component.translatable("message.angelring.level_up", updated.level()), true);
                player.playNotifySound(ModSounds.RING_LEVEL_UP.get(), SoundSource.PLAYERS, 0.6f, 0.75f);
                player.playNotifySound(ModSounds.UI_CHIME.get(), SoundSource.PLAYERS, 0.4f, 1.0f);
                player.playNotifySound(ModSounds.UI_CHIME.get(), SoundSource.PLAYERS, 0.34f, 1.26f);
                player.playNotifySound(ModSounds.UI_CHIME.get(), SoundSource.PLAYERS, 0.3f, 1.5f);
            }
        }
    }

    /**
     * «Кровавая защита» (пассивная): при получении урона есть шанс (от 20% на 0 звёздах до 60% на 6 звёздах)
     * полностью заблокировать удар, вернуть немного здоровья и ослабить нападавшего.
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
        // дешёвая проверка паузы — до поиска кольца в слотах Curios
        long now = player.level().getGameTime();
        CompoundTag tag = player.getPersistentData();
        if (now < tag.getLong(WARD_TAG)) {
            return;
        }
        Optional<ItemStack> ring = RingLookup.find(player);
        if (ring.isEmpty()) {
            return;
        }
        RelicData data = ring.get().getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
        int id = RelicAbility.BLOOD_WARD.id;
        int level = data.abilityLevel(id);
        if (level <= 0) {
            return; // способность не изучена
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
        world.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1.0, player.getZ(),
                18, 0.4, 0.6, 0.4, 0.03);
        // вспышка иконки защиты в HUD
        PacketDistributor.sendToPlayer(player,
                new RelicResultPayload(RelicData.ACTION_WARD_PROC, id, RelicActions.Result.OK));
    }
}
