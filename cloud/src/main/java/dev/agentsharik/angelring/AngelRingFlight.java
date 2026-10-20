package dev.agentsharik.angelring;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Пока кольцо надето в слоте Curios, игрок может летать как в творческом режиме.
 * Чем дольше летаешь, тем выше уровень кольца. Всё считается на сервере.
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID)
public final class AngelRingFlight {
    private AngelRingFlight() {}

    /** Метка в данных игрока: «полёт выдан именно кольцом». Нужна, чтобы не отбирать полёт у других источников. */
    private static final String GRANTED_TAG = "angelring_flight_granted";

    /** Проверяем раз в 5 тиков (четыре раза в секунду) — этого хватает и не нагружает сервер. */
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
        if (player.isCreative() || player.isSpectator()) {
            return; // в этих режимах полётом управляет сама игра
        }

        Optional<ItemStack> ring = RingLookup.find(player);
        CompoundTag data = player.getPersistentData();
        boolean granted = data.getBoolean(GRANTED_TAG);
        Abilities abilities = player.getAbilities();

        if (ring.isPresent()) {
            ItemStack stack = ring.get();
            RelicData relic = stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
            boolean changed = false;

            if (!abilities.mayfly) {
                abilities.mayfly = true;
                changed = true;
            }
            float wantedSpeed = BASE_FLY_SPEED * relic.speedMultiplier();
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

            // опыт кольца: раз в секунду, пока игрок реально летит
            if (abilities.flying && !player.onGround() && player.tickCount % 20 == 0) {
                RelicData updated = relic.addXp(RelicData.XP_PER_SECOND);
                if (updated != relic) {
                    stack.set(ModComponents.RELIC_DATA.get(), updated);
                    if (updated.level() > relic.level()) {
                        player.displayClientMessage(
                                Component.translatable("message.angelring.level_up", updated.level()), true);
                    }
                }
            }
        } else if (granted) {
            data.remove(GRANTED_TAG);
            abilities.mayfly = false;
            abilities.flying = false;
            abilities.flyingSpeed = BASE_FLY_SPEED;
            player.onUpdateAbilities();
            if (!player.onGround()) {
                // страховка: сняли кольцо в воздухе — не разбиваемся сразу
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SAFETY_SLOW_FALL_TICKS, 0, false, false));
            }
        }
    }
}
