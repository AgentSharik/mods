package dev.agentsharik.angelring;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Пока кольцо надето в слоте Curios, игрок может летать как в творческом режиме.
 * Скорость полёта растёт с редкостью кольца. Здесь же кольцу «определяется» редкость, когда оно попадает к игроку.
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID)
public final class AngelRingFlight {
    private AngelRingFlight() {}

    /** Метка в данных игрока: «полёт выдан именно кольцом». Нужна, чтобы не отбирать полёт у других источников. */
    private static final String GRANTED_TAG = "angelring_flight_granted";

    /** Проверяем раз в 5 тиков (четыре раза в секунду). */
    private static final int CHECK_INTERVAL_TICKS = 5;

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

        Optional<ItemStack> ring = RingLookup.find(player);
        CompoundTag data = player.getPersistentData();
        boolean granted = data.getBoolean(GRANTED_TAG);
        Abilities abilities = player.getAbilities();

        if (ring.isPresent()) {
            RelicData relic = ring.get().getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
            boolean changed = false;

            if (!abilities.mayfly) {
                abilities.mayfly = true;
                changed = true;
            }
            float wantedSpeed = BASE_FLY_SPEED * relic.flightMultiplier();
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
                // страховка: сняли кольцо в воздухе — не разбиваемся сразу
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SAFETY_SLOW_FALL_TICKS, 0, false, false));
            }
        }
    }

    /** Редкость кольца (и его способностей) определяется один раз — когда кольцо впервые оказывается у игрока. */
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
