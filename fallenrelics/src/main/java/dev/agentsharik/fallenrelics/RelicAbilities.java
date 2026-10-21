package dev.agentsharik.fallenrelics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Серверная половина ревизии 1.8.0: зеркальный опыт реликвии, бросок и реролл
 * рарности, три способности кольца со скейлингом от звёзд.
 *
 * <p>Способности: 0 = «Небесный приговор» (активная, контроль: левитация,
 * замедление и метка уязвимости по площади), 1 = «Багровое убежище» (активная,
 * зона в мире: лечение и защита своим, слабость и замедление врагам),
 * 2 = «Божественная защита» (пассивная: шанс полностью блокировать удар,
 * исцелиться и ослабить атакующего). Всё проверяет сервер.</p>
 */
@EventBusSubscriber(modid = FallenRelicsMod.MOD_ID)
public final class RelicAbilities {
    private RelicAbilities() {}

    /** Метки кулдаунов активных способностей в данных игрока. */
    public static final String CD_TAG = "angelring_cd";
    private static final String AEGIS_CD_TAG = "angelring_aegis_cd";
    private static final String MARK_END_TAG = "fallenrelics_mark_end";
    private static final String MARK_PCT_TAG = "fallenrelics_mark_pct";

    /** Реролл стоит два уровня опыта игрока. */
    public static final int REROLL_COST = 2;

    /** Веса звёзд при броске: низкая рарность чаще, шестая редка. */
    private static final int[] STAR_WEIGHTS = {22, 20, 17, 15, 11, 9, 6};

    private record Zone(ResourceKey<Level> dim, BlockPos pos, int endTick, int stars, int rank) {}
    private static final List<Zone> ZONES = new ArrayList<>();

    public static int rollStars(net.minecraft.util.RandomSource random) {
        int total = 0;
        for (int w : STAR_WEIGHTS) {
            total += w;
        }
        int roll = random.nextInt(total);
        for (int star = 0; star < STAR_WEIGHTS.length; star++) {
            roll -= STAR_WEIGHTS[star];
            if (roll < 0) {
                return star;
            }
        }
        return 0;
    }

    /** Старые и свежие кольца без броска получают рарность при первом обращении. */
    public static RelicData ensureRolled(ServerPlayer player, ItemStack stack, RelicData relic) {
        if (relic.rolled()) {
            return relic;
        }
        var random = player.getRandom();
        RelicData rolled = relic.withRoll(rollStars(random), rollStars(random), rollStars(random), rollStars(random));
        stack.set(ModContent.RELIC_DATA.get(), rolled);
        player.displayClientMessage(Component.translatable("message.fallenrelics.rarity.revealed",
                rolled.rarity()), true);
        return rolled;
    }

    /** Опыт игрока зеркально копится в реликвии (только приобретения). */
    @SubscribeEvent
    public static void onXpChange(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || event.getAmount() <= 0 || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AngelRingFlight.findRing(serverPlayer).ifPresent(stack -> {
            RelicData old = stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY);
            RelicData updated = ensureRolled(serverPlayer, stack, old).addXp(event.getAmount());
            if (updated != old) {
                stack.set(ModContent.RELIC_DATA.get(), updated);
                if (updated.level() > old.level()) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.fallenrelics.level_up", updated.level()), true);
                }
            }
        });
    }

    /** Метка «Приговора» усиливает входящий урон; пассивка блокирует удар. */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        var marked = event.getEntity().getPersistentData();
        // метка уязвимости: помеченные «Приговором» получают больше урона от всего
        if (marked.getInt(MARK_END_TAG) > event.getEntity().tickCount) {
            event.setAmount(event.getAmount() * (1.0F + marked.getInt(MARK_PCT_TAG) / 100.0F));
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        var data = player.getPersistentData();
        // божественная защита: шанс обнулить удар, исцелиться и ослабить врага
        if (data.getInt(AEGIS_CD_TAG) > 0) {
            return;
        }
        RelicData relic = AngelRingFlight.findRing(player)
                .map(stack -> stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY))
                .orElse(RelicData.EMPTY);
        int rank = relic.rank(2);
        if (rank <= 0) {
            return;
        }
        int stars = relic.abilityRarity(2);
        float chance = (20.0F + 40.0F * stars / RelicData.MAX_STAR) / 100.0F;
        if (player.getRandom().nextFloat() >= chance) {
            return;
        }
        event.setAmount(0.0F);
        data.putInt(AEGIS_CD_TAG, 60);
        player.heal(4.0F + stars * 2.0F / 3.0F);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, true));
        var attacker = event.getSource().getDirectEntity();
        if (attacker instanceof Mob mob) {
            mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, true));
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1.0,
                    player.getZ(), 24, 0.4, 0.6, 0.4, 0.02);
            player.playSound(SoundEvents.BELL_BLOCK, 0.8F, 1.2F);
        }
        player.displayClientMessage(Component.translatable("message.fallenrelics.aegis"), true);
    }

    /** Тик зон «Багрового убежища»: аура раз в секунду плюс багровые искры. */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // кулдаун пассивки тикает всегда
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            var data = player.getPersistentData();
            if (data.getInt(AEGIS_CD_TAG) > 0) {
                data.putInt(AEGIS_CD_TAG, data.getInt(AEGIS_CD_TAG) - 1);
            }
        }
        if (ZONES.isEmpty()) {
            return;
        }
        List<Zone> dead = null;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            long now = level.getGameTime();
            for (Zone zone : ZONES) {
                if (zone.dim() != level.dimension() || now >= zone.endTick()) {
                    if (now >= zone.endTick() && dead == null) {
                        dead = new ArrayList<>();
                    }
                    if (now >= zone.endTick()) {
                        dead.add(zone);
                    }
                    continue;
                }
                if (now % 20 != 0) {
                    continue;
                }
                var box = new net.minecraft.world.phys.AABB(zone.pos()).inflate(5.0);
                for (Player target : level.getEntitiesOfClass(Player.class, box)) {
                    target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, false, true));
                    target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, false, true));
                }
                for (Monster mob : level.getEntitiesOfClass(Monster.class, box)) {
                    mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, false, true));
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, true));
                }
                level.sendParticles(ParticleTypes.CRIMSON_SPORE, zone.pos().getX() + 0.5, zone.pos().getY() + 1.0,
                        zone.pos().getZ() + 0.5, 16, 3.5, 1.2, 3.5, 0.02);
                level.sendParticles(ParticleTypes.SMOKE, zone.pos().getX() + 0.5, zone.pos().getY() + 0.4,
                        zone.pos().getZ() + 0.5, 8, 3.0, 0.6, 3.0, 0.01);
            }
        }
        if (dead != null) {
            ZONES.removeAll(dead);
        }
    }

    /** Улучшение ранга способности за уровни игрока; потолок ранга от уровня реликвии. */
    public static void upgrade(ServerPlayer player, int index) {
        if (index < 0 || index >= RelicData.ABILITIES) {
            return;
        }
        var ring = AngelRingFlight.findRing(player);
        if (ring.isEmpty()) {
            return;
        }
        ItemStack stack = ring.get();
        RelicData relic = ensureRolled(player, stack, stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY));
        int next = relic.rank(index) + 1;
        if (next > RelicData.MAX_RANK || next > relic.rankCap()) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.rank.cap"), true);
            return;
        }
        int cost = RelicData.rankCost(next);
        if (player.experienceLevel < cost) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.rank.xp", cost), true);
            return;
        }
        player.giveExperienceLevels(-cost);
        stack.set(ModContent.RELIC_DATA.get(), relic.withRank(index, next));
        player.playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.4F);
        AngelRingFlight.sendStatus(player);
    }

    /** Применение активной способности (0 = приговор, 1 = убежище). */
    public static void cast(ServerPlayer player, int index) {
        if (index != 0 && index != 1) {
            return;
        }
        var data = player.getPersistentData();
        if (data.getInt(CD_TAG + index) > 0) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.cast.cooldown"), true);
            return;
        }
        var ring = AngelRingFlight.findRing(player);
        if (ring.isEmpty()) {
            return;
        }
        ItemStack stack = ring.get();
        RelicData relic = ensureRolled(player, stack, stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY));
        int rank = relic.rank(index);
        if (rank <= 0) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.cast.not_learned"), true);
            return;
        }
        int stars = relic.abilityRarity(index);
        float rankMult = 1.0F + 0.2F * (rank - 1);
        if (index == 0) {
            castVerdict(player, stars, rankMult, rank);
        } else {
            int duration = (int) ((4 + stars) * 20 * rankMult);
            ZONES.add(new Zone(player.level().dimension(), player.blockPosition(),
                    (int) (player.level().getGameTime() + duration), stars, rank));
            player.playSound(SoundEvents.EVOKER_CAST_SPELL, 0.8F, 0.9F);
        }
        data.putInt(CD_TAG + index, (int) ((index == 0 ? 600 : 900) * (1.0F - 0.15F * (rank - 1))));
        AngelRingFlight.sendStatus(player);
    }

    /** Контроль по площади: левитация, замедление и метка уязвимости. */
    private static void castVerdict(ServerPlayer player, int stars, float rankMult, int rank) {
        double radius = 5.0 + stars / 2.0;
        int duration = (int) ((2 + stars * 2 / 3) * 20 * rankMult);
        int markPct = rank >= 3 ? 40 : 25;
        ServerLevel level = player.serverLevel();
        for (Monster mob : level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(radius))) {
            mob.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, 0, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1, false, true));
            mob.getPersistentData().putInt(MARK_END_TAG, mob.tickCount + 100);
            mob.getPersistentData().putInt(MARK_PCT_TAG, markPct);
        }
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1.0, player.getZ(),
                48, radius / 2, 1.0, radius / 2, 0.05);
        level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.5, player.getZ(),
                24, radius / 2, 0.5, radius / 2, 0.02);
        player.playSound(SoundEvents.EVOKER_CAST_SPELL, 0.9F, 1.1F);
    }

    /** Реролл рарности артефакта и способностей за REROLL_COST уровня. */
    public static void reroll(ServerPlayer player) {
        var ring = AngelRingFlight.findRing(player);
        if (ring.isEmpty()) {
            return;
        }
        if (player.experienceLevel < REROLL_COST) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.reroll.xp", REROLL_COST), true);
            return;
        }
        ItemStack stack = ring.get();
        RelicData relic = stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY);
        player.giveExperienceLevels(-REROLL_COST);
        var random = player.getRandom();
        RelicData rolled = relic.withRoll(rollStars(random), rollStars(random), rollStars(random), rollStars(random));
        stack.set(ModContent.RELIC_DATA.get(), rolled);
        player.playSound(SoundEvents.END_PORTAL_SPAWN, 0.5F, 1.4F);
        player.displayClientMessage(Component.translatable("message.fallenrelics.reroll.done",
                rolled.rarity(), rolled.a0(), rolled.a1(), rolled.a2()), true);
        AngelRingFlight.sendStatus(player);
    }
}
