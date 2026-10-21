package dev.agentsharik.angelring;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import dev.agentsharik.angelring.network.RelicActionPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Пока кольцо надето в слоте Curios, игрок может летать. Скорость полёта растёт с редкостью.
 *
 * <p>Событийная модель вместо опроса в PlayerTickEvent: игрок помечается «грязным» только когда что-то
 * могло поменяться (надел/снял кольцо, реролл, вход, возрождение, смена измерения или режима игры), а
 * пересчёт делается один раз в конце тика сервера. В обычном тике обработчик — это одна проверка
 * {@code isEmpty()} на всех игроков сразу.
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID)
public final class AngelRingFlight {
    private AngelRingFlight() {}

    /** Метка в данных игрока: «полёт выдан именно кольцом». Нужна, чтобы не отбирать полёт у других источников. */
    private static final String GRANTED_TAG = "angelring_flight_granted";

    /** Сколько тиков действует «мягкое падение» после снятия кольца в воздухе (200 тиков = 10 секунд). */
    private static final int SAFETY_SLOW_FALL_TICKS = 200;

    /** Ванильная скорость полёта. */
    private static final float BASE_FLY_SPEED = 0.05F;

    /** Игроки, чьё состояние полёта нужно пересчитать. Только главный поток сервера. */
    private static final Set<UUID> DIRTY = new LinkedHashSet<>();
    /** Переиспользуемый буфер: список не создаётся заново в каждом тике. */
    private static final List<UUID> WORK = new ArrayList<>();

    public static void markDirty(ServerPlayer player) {
        DIRTY.add(player.getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (DIRTY.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        WORK.clear();
        WORK.addAll(DIRTY);
        DIRTY.clear();
        for (UUID id : WORK) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                reconcile(player);
            }
        }
        WORK.clear();
    }

    // ---- события, после которых состояние полёта могло сбиться ----

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            markDirty(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DIRTY.remove(event.getEntity().getUUID());
        RelicActionPayload.forget(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            markDirty(player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            markDirty(player);
        }
    }

    /** Событие приходит до смены режима, поэтому пересчёт отложен до конца тика — это как раз то, что нужно. */
    @SubscribeEvent
    public static void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            markDirty(player);
        }
    }

    /** Привести способности игрока в соответствие с тем, надето ли кольцо. Идемпотентно. */
    private static void reconcile(ServerPlayer player) {
        Optional<ItemStack> ring = RingLookup.find(player);
        ring.ifPresent(stack -> AngelRingItem.ensureRolled(player, stack));

        if (player.isCreative() || player.isSpectator()) {
            return; // в этих режимах полётом управляет сама игра
        }
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
}
