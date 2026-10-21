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
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Серверная половина ревизии 1.9.0: зеркальный опыт реликвии с ручной прокачкой
 * уровня кнопкой, бросок и реролл рарности, способности со скейлингом от звёзд
 * и рубежами открытия по уровню артефакта.
 *
 * <p>Способности: 0 = «Кровавая защита» (пассивная, открыта с 1 уровня: шанс
 * полностью блокировать удар, исцелиться и ослабить атакующего), 1 = «Багровая
 * жажда» (пассивная, рубеж 5 уровня: убийства в полёте лечат, питают реликвию
 * опытом и дают силу), 2 = «Небесная кара» (единственная активная, рубеж 10
 * уровня: ультимативный контроль по площади с многослойным спектаклем).
 * Всё проверяет сервер.</p>
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
                if (!old.canLevelUp() && updated.canLevelUp()) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.fallenrelics.levelup.ready"), true);
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
    public static void upgrade(ServerPlayer player, int index, java.util.UUID target) {
        if (index < 0 || index >= RelicData.ABILITIES) {
            return;
        }
        var ring = resolveTarget(player, target);
        if (ring.isEmpty()) {
            return;
        }
        ItemStack stack = ring.get();
        RelicData relic = ensureRolled(player, stack, stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY));
        if (!relic.unlocked(index)) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.ability.locked",
                    relic.unlockLevel(index)), true);
            return;
        }
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
        AngelRingFlight.sendStatus(player, relic.id());
    }

    /**
     * 1.8.1: действия бьют по тому кольцу, которое открыто в свитке (по UUID
     * стека), а не по надетому. Хоткеи и старое поведение — null → надетое.
     */
    private static java.util.Optional<ItemStack> resolveTarget(ServerPlayer player, java.util.UUID target) {
        return target == null ? AngelRingFlight.findRing(player)
                : AngelRingFlight.findStackByRelicId(player, target).or(() -> AngelRingFlight.findRing(player));
    }

    /** Повышение уровня реликвии кнопкой: порог списывается, излишек опыта живёт дальше. */
    public static void levelUp(ServerPlayer player, java.util.UUID target) {
        var ring = resolveTarget(player, target);
        if (ring.isEmpty()) {
            return;
        }
        ItemStack stack = ring.get();
        RelicData relic = ensureRolled(player, stack, stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY));
        if (!relic.canLevelUp()) {
            return;
        }
        RelicData next = relic.withLevelUp();
        stack.set(ModContent.RELIC_DATA.get(), next);
        player.playSound(SoundEvents.PLAYER_LEVELUP, 0.9F, 1.6F);
        player.displayClientMessage(Component.translatable("message.fallenrelics.level_up", next.level()), true);
        if (next.unlocked(1) && !relic.unlocked(1)) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.ability.unlocked",
                    Component.translatable("gui.fallenrelics.angel_ring.ability.thirst")), true);
        }
        if (next.unlocked(2) && !relic.unlocked(2)) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.ability.unlocked",
                    Component.translatable("gui.fallenrelics.angel_ring.ability.ascension")), true);
        }
        AngelRingFlight.sendStatus(player, next.id());
    }

    /** Пассивка «Багровая жажда»: убийства в полёте лечат, питают реликвию и дают силу. */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide
                || !(event.getSource().getEntity() instanceof ServerPlayer killer)
                || !killer.getAbilities().flying) {
            return;
        }
        var ring = AngelRingFlight.findRing(killer);
        if (ring.isEmpty()) {
            return;
        }
        ItemStack stack = ring.get();
        RelicData relic = stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY);
        if (!relic.rolled() || !relic.unlocked(1)) {
            return;
        }
        int stars = relic.abilityRarity(1);
        killer.heal(1.0F + stars / 2.0F);
        stack.set(ModContent.RELIC_DATA.get(), relic.addXp(8 + 4 * stars));
        killer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
                40 + 20 * stars, stars >= 5 ? 1 : 0, false, true));
        killer.level().sendParticles(ParticleTypes.CRIMSON_SPORE, killer.getX(), killer.getY() + 1.0,
                killer.getZ(), 14, 0.5, 0.7, 0.5, 0.05);
        killer.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 1.4F);
    }

    /** Применение единственной активной способности — «Небесная кара» (индекс 2). */
    public static void cast(ServerPlayer player, int index, java.util.UUID target) {
        if (index != 2) {
            return;
        }
        var data = player.getPersistentData();
        if (data.getInt(CD_TAG + index) > 0) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.cast.cooldown"), true);
            return;
        }
        var ring = resolveTarget(player, target);
        if (ring.isEmpty()) {
            return;
        }
        ItemStack stack = ring.get();
        RelicData relic = ensureRolled(player, stack, stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY));
        if (!relic.unlocked(2)) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.ability.locked",
                    relic.unlockLevel(2)), true);
            return;
        }
        int rank = relic.rank(2);
        if (rank <= 0) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.cast.not_learned"), true);
            return;
        }
        int stars = relic.abilityRarity(2);
        float rankMult = 1.0F + 0.2F * (rank - 1);
        castAscension(player, stars, rankMult, rank);
        data.putInt(CD_TAG + 2, (int) (900 * (1.0F - 0.15F * (rank - 1))));
        AngelRingFlight.sendStatus(player, relic.id());
    }

    /**
     * Ультимативный контроль по площади, ревизия 1.9.0: многослойный спектакль —
     * два вращающихся рунических кольца из багровых спор, восемь столпов-лучей
     * от земли, расходящаяся ударная волна, дым и искры, финальная вспышка;
     * механически — левитация, замедление и метка уязвимости по врагам,
     * исцеление своим, сопротивление себе и освящённая земля под ногами.
     */
    private static void castAscension(ServerPlayer player, int stars, float rankMult, int rank) {
        double radius = 8.0 + stars;
        int duration = (int) ((3 + stars * 2 / 3) * 20 * rankMult);
        int markPct = rank >= 3 ? 40 : 25;
        ServerLevel level = player.serverLevel();
        double cx = player.getX();
        double cy = player.getY();
        double cz = player.getZ();
        for (Monster mob : level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(radius))) {
            mob.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, 0, false, true));
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1, false, true));
            mob.getPersistentData().putInt(MARK_END_TAG, mob.tickCount + 100);
            mob.getPersistentData().putInt(MARK_PCT_TAG, markPct);
            mob.hurt(player.damageSources().playerAttack(player), 6.0F + 2.0F * stars);
        }
        for (Player ally : level.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(radius))) {
            ally.heal(4.0F + stars);
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0, false, true));
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true));
        ZONES.add(new Zone(player.level().dimension(), player.blockPosition(),
                (int) (player.level().getGameTime() + (4 + stars) * 20 * rankMult), stars, rank));
        // слой 1: два рунических кольца, вращающихся в противоположные стороны
        for (int i = 0; i < 48; i++) {
            double a = i * Math.PI / 24.0;
            level.sendParticles(ParticleTypes.CRIMSON_SPORE,
                    cx + Math.cos(a) * radius, cy + 0.2, cz + Math.sin(a) * radius, 1, 0, 0.05, 0, 0.01);
            level.sendParticles(ParticleTypes.CRIMSON_SPORE,
                    cx + Math.cos(-a) * radius * 0.6, cy + 0.9, cz + Math.sin(-a) * radius * 0.6, 1, 0, 0.05, 0, 0.01);
        }
        // слой 2: восемь столпов-лучей от земли до роста игрока
        for (int b = 0; b < 8; b++) {
            double a = b * Math.PI / 4.0;
            double bx = cx + Math.cos(a) * radius * 0.85;
            double bz = cz + Math.sin(a) * radius * 0.85;
            for (double y = cy - 2.0; y <= cy + 4.0; y += 0.5) {
                level.sendParticles(ParticleTypes.CRIMSON_SPORE, bx, y, bz, 1, 0, 0, 0, 0.02);
                level.sendParticles(ParticleTypes.SMOKE, bx, y, bz, 1, 0, 0, 0, 0.01);
            }
        }
        // слой 3: ударная волна, дымовая шапка и финальная вспышка
        level.sendParticles(ParticleTypes.FLASH, cx, cy + 1.0, cz, 1, 0, 0, 0, 0.0);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, cx, cy + 1.0, cz, 160, radius / 2, 1.2, radius / 2, 0.08);
        level.sendParticles(ParticleTypes.SMOKE, cx, cy + 0.5, cz, 60, radius / 2, 0.8, radius / 2, 0.03);
        level.sendParticles(ParticleTypes.FLAME, cx, cy + 0.2, cz, 40, radius / 2, 0.2, radius / 2, 0.06);
        player.playSound(SoundEvents.END_PORTAL_SPAWN, 1.0F, 0.7F);
        player.playSound(SoundEvents.BEACON_ACTIVATE, 1.0F, 0.6F);
        player.playSound(SoundEvents.GENERIC_EXPLODE, 0.7F, 0.8F);
    }

    /** Реролл рарности артефакта и способностей за REROLL_COST уровня. */
    public static void reroll(ServerPlayer player, java.util.UUID target) {
        var ring = resolveTarget(player, target);
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
        AngelRingFlight.sendStatus(player, rolled.id());
    }
}
