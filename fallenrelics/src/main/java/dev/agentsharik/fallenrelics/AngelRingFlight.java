package dev.agentsharik.fallenrelics;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

/**
 * Пока кольцо надето в слот Curios, игрок может летать как в творческом режиме,
 * а кольцо растёт как реликвия: опыт за полёт, уровни, очки улучшений, ранги
 * способностей «Полёт» (скорость) и «Мягкое падение» (длиннее страховка).
 * Всё считается на сервере; клиенту разрешение на полёт шлёт onUpdateAbilities(),
 * а свитку — снапшот статуса по запросу.
 * Регистрируется только при установленном Curios (см. FallenRelicsMod).
 */
public final class AngelRingFlight {
    private AngelRingFlight() {}

    /** Метка в данных игрока: «полёт выдан именно кольцом». Нужна, чтобы не отбирать полёт у других источников. */
    private static final String GRANTED_TAG = "angelring_flight_granted";

    /** Метка в данных игрока: суммарное время полёта с кольцом в тиках (для свитка реликвии). */
    private static final String FLIGHT_TICKS_TAG = "angelring_flight_ticks";

    /** Метка в данных игрока: запас страховки последнего надетого кольца (тики мягкого падения). */
    private static final String CUSHION_TAG = "angelring_cushion_ticks";

    /** Проверяем раз в 5 тиков (четыре раза в секунду) — этого хватает и не нагружает сервер. */
    private static final int CHECK_INTERVAL_TICKS = 5;

    /** Ванильная скорость полёта. */
    private static final float BASE_FLY_SPEED = 0.05F;

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return; // клиент ничего не решает
        }
        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(GRANTED_TAG) && player.getAbilities().flying) {
            data.putLong(FLIGHT_TICKS_TAG, data.getLong(FLIGHT_TICKS_TAG) + 1L);
        }
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return; // в этих режимах полётом управляет сама игра
        }

        Optional<ItemStack> ring = findRing(player);
        boolean granted = data.getBoolean(GRANTED_TAG);
        Abilities abilities = player.getAbilities();

        if (ring.isPresent()) {
            ItemStack stack = ring.get();
            RelicData relic = stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY);
            data.putInt(CUSHION_TAG, relic.cushionTicks());
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

            // опыт реликвии: раз в секунду, пока игрок реально летит
            if (abilities.flying && !player.onGround() && player.tickCount % 20 == 0) {
                RelicData updated = relic.addXp(RelicData.XP_PER_SECOND);
                if (updated != relic) {
                    stack.set(ModContent.RELIC_DATA.get(), updated);
                    if (updated.level() > relic.level()) {
                        player.displayClientMessage(
                                Component.translatable("message.fallenrelics.level_up", updated.level()), true);
                    }
                }
            }
        } else if (granted) {
            data.remove(GRANTED_TAG);
            abilities.mayfly = false;
            abilities.flying = false;
            abilities.setFlyingSpeed(BASE_FLY_SPEED);
            player.onUpdateAbilities();
            if (!player.onGround()) {
                // страховка: сняли кольцо в воздухе — не разбиваемся сразу
                int cushion = Math.max(200, data.getInt(CUSHION_TAG));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, cushion, 0, false, false));
            }
        }
    }

    /** Ищет надетое кольцо в слотах Curios. Работает и на клиенте, и на сервере. */
    public static Optional<ItemStack> findRing(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(ModContent.ANGEL_RING.get()))
                .map(SlotResult::stack);
    }

    /** Серверная трата очка улучшения по клику в свитке. Всё проверяет сервер. */
    public static void spendUpgrade(Player player, int stat) {
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        findRing(player).ifPresent(stack -> {
            RelicData old = stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY);
            RelicData updated = old.spend(stat);
            if (updated != old) {
                stack.set(ModContent.RELIC_DATA.get(), updated);
            }
        });
    }

    /** Builds and sends the ring status snapshot for the relic scroll screen. */
    public static void sendStatus(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CompoundTag data = serverPlayer.getPersistentData();
        boolean buffering = serverPlayer.hasEffect(MobEffects.SLOW_FALLING);
        int bufferTicks = buffering ? serverPlayer.getEffect(MobEffects.SLOW_FALLING).getDuration() : 0;
        RelicData relic = findRing(serverPlayer)
                .map(stack -> stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY))
                .orElse(RelicData.EMPTY);
        PacketDistributor.sendToPlayer(serverPlayer,
                new AngelRingStatusPayload(data.getBoolean(GRANTED_TAG),
                        serverPlayer.getAbilities().flying,
                        buffering,
                        bufferTicks,
                        data.getLong(FLIGHT_TICKS_TAG),
                        relic));
    }
}
