package dev.agentsharik.fallenrelics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Финальный ритуал пробуждения реликвии, ревизия 1.9.0: Shift + ПКМ по
 * чародейскому столу с «Пробуждённым кольцом полёта» в руке и 50 уровнями
 * опыта. Уровни списываются сразу (защита от прерывания и дюпа), кольцо
 * вылетает из рук и поднимается над столом, от игрока к нему тянется поток
 * сфер опыта, вокруг бьют столпы-лучи и расходятся энергетические волны в
 * фирменной палитре, на пике — вспышка с клубами багрового дыма, после чего
 * готовое «Кольцо ангела» плавно опускается и выдаётся игроку (или падает
 * над столом с защитой от пропадания), а игрок получает испытание
 * «Божественные Крылья».
 */
@EventBusSubscriber(modid = FallenRelicsMod.MOD_ID)
public final class AscensionRitual {
    private AscensionRitual() {}

    /** Цена ритуала в уровнях опыта игрока. */
    public static final int LEVEL_COST = 50;
    private static final int PEAK_TICK = 120;
    private static final int RITUAL_TICKS = 160;

    private static final class State {
        private UUID player;
        private BlockPos table;
        private net.minecraft.server.level.ServerLevel level;
        private int tick;
        private ItemEntity floater;
        private ItemEntity descender;
    }

    private static final List<State> ACTIVE = new ArrayList<>();

    @SubscribeEvent
    public static void onRightClickTable(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof EnchantingTableBlock)) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isShiftKeyDown()) {
            return;
        }
        ItemStack hand = player.getMainHandItem();
        if (!hand.is(ModContent.AWAKENED_RING.get())) {
            return;
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (ACTIVE.stream().anyMatch(s -> s.player.equals(player.getUUID()))) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.ritual.busy"), true);
            return;
        }
        if (player.experienceLevel < LEVEL_COST) {
            player.displayClientMessage(Component.translatable("message.fallenrelics.ritual.levels", LEVEL_COST), true);
            return;
        }
        start(player, event.getPos().immutable(), hand);
    }

    private static void start(ServerPlayer player, BlockPos table, ItemStack hand) {
        var level = player.serverLevel();
        State st = new State();
        st.player = player.getUUID();
        st.table = table;
        st.level = level;
        ItemStack ring = hand.copy();
        ring.setCount(1);
        hand.shrink(1);
        player.giveExperienceLevels(-LEVEL_COST);
        ItemEntity ie = new ItemEntity(level, table.getX() + 0.5, table.getY() + 1.2, table.getZ() + 0.5, ring);
        ie.setNoGravity(true);
        ie.setPickUpDelay(32767);
        ie.setDeltaMovement(0, 0, 0);
        level.addFreshEntity(ie);
        st.floater = ie;
        ACTIVE.add(st);
        level.playSound(null, table, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.6F);
        player.displayClientMessage(Component.translatable("message.fallenrelics.ritual.start"), true);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        Iterator<State> it = ACTIVE.iterator();
        while (it.hasNext()) {
            State st = it.next();
            st.tick++;
            var level = st.level;
            double cx = st.table.getX() + 0.5;
            double cz = st.table.getZ() + 0.5;
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(st.player);
            if (st.tick <= PEAK_TICK) {
                double rise = 1.2 + Math.min(3.0, st.tick * 0.025);
                double ringY = st.table.getY() + rise;
                if (st.floater != null && st.floater.isAlive()) {
                    st.floater.setPos(cx, ringY, cz);
                    st.floater.setDeltaMovement(0, 0, 0);
                    if (st.tick % 3 == 0) {
                        level.sendParticles(ParticleTypes.CRIMSON_SPORE, cx, ringY, cz, 4, 0.3, 0.2, 0.3, 0.03);
                    }
                }
                // поток сфер опыта от игрока к парящему кольцу
                if (player != null && st.tick % 2 == 0) {
                    Vec3 from = player.position().add(0, 1.2, 0);
                    Vec3 to = new Vec3(cx, ringY, cz);
                    for (int i = 0; i < 6; i++) {
                        Vec3 p = from.lerp(to, i / 6.0);
                        level.sendParticles(i % 2 == 0 ? ParticleTypes.ENCHANT : ParticleTypes.CRIMSON_SPORE,
                                p.x, p.y, p.z, 1, 0, 0, 0, 0.0);
                    }
                }
                // столпы-лучи вокруг кольца, как при гибели дракона, но в нашей палитре
                if (st.tick > 40 && st.tick % 4 == 0) {
                    for (int b = 0; b < 8; b++) {
                        double a = b * Math.PI / 4.0 + st.tick * 0.02;
                        double bx = cx + Math.cos(a) * 2.2;
                        double bz = cz + Math.sin(a) * 2.2;
                        for (double y = st.table.getY(); y <= ringY; y += 0.5) {
                            level.sendParticles(ParticleTypes.CRIMSON_SPORE, bx, y, bz, 1, 0, 0, 0, 0.02);
                            level.sendParticles(ParticleTypes.SMOKE, bx, y, bz, 1, 0, 0, 0, 0.01);
                        }
                    }
                }
                // расходящиеся энергетические волны
                if (st.tick % 20 == 0) {
                    double r = st.tick % 40 == 0 ? 1.5 : 3.0;
                    for (int i = 0; i < 32; i++) {
                        double a = i * Math.PI / 16.0;
                        level.sendParticles(ParticleTypes.SMOKE, cx + Math.cos(a) * r, st.table.getY() + 1.0,
                                cz + Math.sin(a) * r, 1, 0, 0.02, 0, 0.01);
                    }
                    level.playSound(null, st.table, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.5F, 0.8F);
                }
                if (st.tick == PEAK_TICK) {
                    // пик: вспышка, клубы багрового дыма, трансформация
                    if (st.floater != null) {
                        st.floater.discard();
                    }
                    double peakY = st.table.getY() + 4.2;
                    level.sendParticles(ParticleTypes.FLASH, cx, peakY, cz, 1, 0, 0, 0, 0.0);
                    level.sendParticles(ParticleTypes.CRIMSON_SPORE, cx, peakY, cz, 240, 1.6, 1.6, 1.6, 0.1);
                    level.sendParticles(ParticleTypes.SMOKE, cx, peakY - 0.2, cz, 120, 1.4, 1.2, 1.4, 0.04);
                    level.playSound(null, st.table, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.8F, 0.7F);
                    ItemEntity d = new ItemEntity(level, cx, peakY, cz, new ItemStack(ModContent.ANGEL_RING.get()));
                    d.setNoGravity(true);
                    d.setPickUpDelay(32767);
                    d.setDeltaMovement(0, 0, 0);
                    level.addFreshEntity(d);
                    st.descender = d;
                }
            } else {
                double t = (st.tick - PEAK_TICK) / 40.0;
                double y = st.table.getY() + 4.2 - 3.0 * Math.min(1.0, t);
                if (st.descender != null && st.descender.isAlive()) {
                    st.descender.setPos(cx, y, cz);
                    st.descender.setDeltaMovement(0, 0, 0);
                    if (st.tick % 3 == 0) {
                        level.sendParticles(ParticleTypes.CRIMSON_SPORE, cx, y, cz, 6, 0.4, 0.3, 0.4, 0.03);
                    }
                }
                if (st.tick >= RITUAL_TICKS) {
                    finish(st, player);
                    it.remove();
                }
            }
        }
    }

    private static void finish(State st, ServerPlayer player) {
        var level = st.level;
        if (st.descender != null) {
            st.descender.discard();
        }
        ItemStack finalRing = new ItemStack(ModContent.ANGEL_RING.get());
        if (player != null && player.getInventory().add(finalRing)) {
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8F, 1.2F);
        } else {
            // защита от пропадания: долгая жизнь дропа над столом
            ItemEntity drop = new ItemEntity(level, st.table.getX() + 0.5, st.table.getY() + 1.2,
                    st.table.getZ() + 0.5, finalRing);
            drop.lifespan = Integer.MAX_VALUE;
            level.addFreshEntity(drop);
        }
        if (player != null) {
            var adv = player.server.getAdvancements()
                    .get(ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "divine_wings"));
            if (adv != null) {
                player.getAdvancements().award(adv, "ritual");
            }
            player.displayClientMessage(Component.translatable("message.fallenrelics.ritual.done"), true);
        }
        level.playSound(null, st.table, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 0.9F, 1.0F);
    }
}
