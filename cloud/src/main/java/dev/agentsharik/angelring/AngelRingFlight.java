package dev.agentsharik.angelring;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Эффекты надетых колец цепочки:
 *  - Кольцо планирования: только плавное падение (как у курицы);
 *  - Пробуждённое кольцо: полёт как в творческом режиме, но не дольше 60 секунд, потом плавное падение
 *    до перезарядки (заряд восстанавливается на земле);
 *  - Кольцо ангела: безлимитный полёт, скорость растёт с редкостью.
 * Здесь же кольцу ангела «определяется» редкость, когда оно попадает к игроку.
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID)
public final class AngelRingFlight {
    private AngelRingFlight() {}

    /** Метка в данных игрока: «полёт выдан именно кольцом». Нужна, чтобы не отбирать полёт у других источников. */
    private static final String GRANTED_TAG = "angelring_flight_granted";

    /** Проверяем раз в 5 тиков (четыре раза в секунду). */
    private static final int CHECK_INTERVAL_TICKS = 5;

    /** Заряд пробуждённого кольца на земле восстанавливается втрое быстрее, чем тратится (полный — за 20 секунд). */
    private static final int RECHARGE_PER_CHECK = CHECK_INTERVAL_TICKS * 3;

    /** Сколько тиков действует «мягкое падение» после снятия кольца в воздухе (200 тиков = 10 секунд). */
    private static final int SAFETY_SLOW_FALL_TICKS = 200;

    /** Ванильная скорость полёта. */
    private static final float BASE_FLY_SPEED = 0.05F;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return; // клиент ничего не решает
        }
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        if (player.tickCount % 20 == 0) {
            rollRarities(player); // нужно и в творческом режиме
        }
        if (player.isCreative() || player.isSpectator()) {
            return; // в этих режимах полётом управляет сама игра
        }

        boolean allowFlight = false;
        boolean slowFall = false;
        float speedMultiplier = 1f;

        Optional<RingLookup.Worn> worn = RingLookup.findWorn(player);
        if (worn.isPresent()) {
            RingLookup.Worn ring = worn.get();
            switch (ring.kind()) {
                case ANGEL -> {
                    allowFlight = true;
                    speedMultiplier = ring.stack().getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY)
                            .flightMultiplier();
                }
                case AWAKENED -> {
                    allowFlight = updateTimedFlight(player, ring.stack());
                    slowFall = !allowFlight && !player.onGround(); // заряд кончился — плавное падение
                }
                case GLIDING -> slowFall = true;
            }
        }
        applyFlight(player, allowFlight, speedMultiplier);
        if (slowFall) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false, true));
        }
    }

    /**
     * Заряд пробуждённого кольца: 60 секунд полёта в воздухе. Когда заряд кончается, полёт отключается и до полной
     * перезарядки недоступен (заряд восстанавливается, пока игрок стоит на земле). Возвращает: разрешён ли полёт.
     */
    private static boolean updateTimedFlight(ServerPlayer player, ItemStack stack) {
        FlightState state = stack.getOrDefault(ModComponents.FLIGHT_STATE.get(), FlightState.FULL);
        int ticks = state.ticks();
        boolean locked = state.locked();
        boolean flyingNow = player.getAbilities().flying && !player.onGround();

        if (flyingNow && !locked) {
            ticks -= CHECK_INTERVAL_TICKS;
        } else if (player.onGround()) {
            ticks += RECHARGE_PER_CHECK;
        }
        ticks = Mth.clamp(ticks, 0, FlightState.MAX_TICKS);

        if (!locked && ticks <= 0) {
            locked = true;
            player.displayClientMessage(Component.translatable("message.angelring.flight_empty"), true);
        } else if (locked && ticks >= FlightState.MAX_TICKS) {
            locked = false;
            player.displayClientMessage(Component.translatable("message.angelring.flight_ready"), true);
        }

        FlightState next = new FlightState(ticks, locked);
        if (!next.equals(state)) {
            stack.set(ModComponents.FLIGHT_STATE.get(), next);
        }
        if (!locked && flyingNow && player.tickCount % 20 == 0) {
            player.displayClientMessage(Component.translatable("message.angelring.flight_left", ticks / 20), true);
        }
        return !locked;
    }

    private static void applyFlight(ServerPlayer player, boolean allow, float speedMultiplier) {
        CompoundTag data = player.getPersistentData();
        boolean granted = data.getBoolean(GRANTED_TAG);
        Abilities abilities = player.getAbilities();

        if (allow) {
            boolean changed = false;
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                changed = true;
            }
            float wantedSpeed = BASE_FLY_SPEED * speedMultiplier;
            if (Math.abs(abilities.getFlyingSpeed() - wantedSpeed) > 0.0001F) {
                abilities.setFlyingSpeed(wantedSpeed);
                changed = true;
            }
            if (changed) {
                player.onUpdateAbilities();
            }
            if (!granted) {
                data.putBoolean(GRANTED_TAG, true);
            }
        } else if (granted) {
            data.remove(GRANTED_TAG);
            abilities.mayfly = false;
            abilities.flying = false;
            abilities.setFlyingSpeed(BASE_FLY_SPEED);
            player.onUpdateAbilities();
            if (!player.onGround()) {
                // страховка: полёт кончился или кольцо сняли в воздухе — не разбиваемся сразу
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SAFETY_SLOW_FALL_TICKS, 0, false, false));
            }
        }
    }

    /** Редкость «Кольца ангела» определяется один раз — когда кольцо впервые оказывается у игрока. */
    private static void rollRarities(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ensureRolled(player, inventory.getItem(i));
        }
        RingLookup.find(player).ifPresent(stack -> ensureRolled(player, stack));
    }

    private static void ensureRolled(ServerPlayer player, ItemStack stack) {
        if (!stack.is(ModItems.ANGEL_RING.get())) {
            return;
        }
        RelicData data = stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
        if (!data.rolled()) {
            stack.set(ModComponents.RELIC_DATA.get(), data.ensureRolled(player.getRandom()));
        }
    }
}
