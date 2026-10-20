package dev.agentsharik.fallenrelics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Пока кольцо надето в слот Curios, игрок может летать как в творческом режиме.
 * Всё считается на сервере; клиенту разрешение на полёт отправляет onUpdateAbilities().
 * Регистрируется только при установленном Curios (см. FallenRelicsMod).
 */
public final class AngelRingFlight {
    private AngelRingFlight() {}

    /** Метка в данных игрока: «полёт выдан именно кольцом». Нужна, чтобы не отбирать полёт у других источников. */
    private static final String GRANTED_TAG = "angelring_flight_granted";

    /** Проверяем раз в 5 тиков (четыре раза в секунду) — этого хватает и не нагружает сервер. */
    private static final int CHECK_INTERVAL_TICKS = 5;

    /** Сколько тиков действует «мягкое падение» после снятия кольца в воздухе (200 тиков = 10 секунд). */
    private static final int SAFETY_SLOW_FALL_TICKS = 200;

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

        boolean wearing = isWearingRing(player);
        CompoundTag data = player.getPersistentData();
        boolean granted = data.getBoolean(GRANTED_TAG);
        Abilities abilities = player.getAbilities();

        if (wearing) {
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                player.onUpdateAbilities();
            }
            if (!granted) {
                data.putBoolean(GRANTED_TAG, true);
            }
        } else if (granted) {
            data.remove(GRANTED_TAG);
            abilities.mayfly = false;
            abilities.flying = false;
            player.onUpdateAbilities();
            if (!player.onGround()) {
                // страховка: сняли кольцо в воздухе — не разбиваемся сразу
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SAFETY_SLOW_FALL_TICKS, 0, false, false));
            }
        }
    }

    private static boolean isWearingRing(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(ModContent.ANGEL_RING.get()).isPresent())
                .orElse(false);
    }
}
