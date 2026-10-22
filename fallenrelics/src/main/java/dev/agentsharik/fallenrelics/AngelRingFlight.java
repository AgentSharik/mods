package dev.agentsharik.fallenrelics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private static final String GRANTED_TAG = "fallenrelics_flight_granted";

    /** Метка в данных игрока: суммарное время полёта с кольцом в тиках (для свитка реликвии). */
    private static final String FLIGHT_TICKS_TAG = "fallenrelics_flight_ticks";

    /** Метка в данных игрока: запас страховки последнего надетого кольца (тики мягкого падения). */
    private static final String CUSHION_TAG = "fallenrelics_cushion_ticks";

    /** Бюджет полёта пробуждённого кольца: 60 секунд (1200 тиков). */
    private static final String AWAKE_BUDGET_TAG = "fallenrelics_awake_budget";
    private static final int AWAKE_BUDGET_MAX = 1200;

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
        for (int i = 0; i < 3; i++) {
            int cd = data.getInt(RelicAbilities.CD_TAG + i);
            if (cd > 0) {
                data.putInt(RelicAbilities.CD_TAG + i, Math.max(0, cd - CHECK_INTERVAL_TICKS));
            }
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
                    if (!relic.canLevelUp() && updated.canLevelUp()) {
                        player.displayClientMessage(
                                Component.translatable("message.fallenrelics.levelup.ready"), true);
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

        // Этап 2: кольцо планирования — только плавное падение, пока надето
        if (findCurio(player, ModContent.GLIDE_RING.get()).isPresent()) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 220, 0, false, false));
        }

        // Этап 3: пробуждённое кольцо — полёт с бюджетом 60 секунд и перезарядкой на земле
        if (ring.isEmpty() && findCurio(player, ModContent.AWAKENED_RING.get()).isPresent()
                && !player.isCreative() && !player.isSpectator()) {
            if (!data.contains(AWAKE_BUDGET_TAG)) {
                data.putInt(AWAKE_BUDGET_TAG, AWAKE_BUDGET_MAX);
            }
            int budget = data.getInt(AWAKE_BUDGET_TAG);
            if (abilities.flying) {
                budget -= CHECK_INTERVAL_TICKS;
                if (budget <= 0) {
                    budget = 0;
                    abilities.mayfly = false;
                    abilities.flying = false;
                    abilities.setFlyingSpeed(BASE_FLY_SPEED);
                    player.onUpdateAbilities();
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false));
                    player.displayClientMessage(
                            Component.translatable("message.fallenrelics.awakened.exhausted"), true);
                }
                data.putInt(AWAKE_BUDGET_TAG, budget);
            } else if (player.onGround()) {
                if (budget < AWAKE_BUDGET_MAX) {
                    data.putInt(AWAKE_BUDGET_TAG, Math.min(AWAKE_BUDGET_MAX, budget + CHECK_INTERVAL_TICKS * 4));
                }
                if (!abilities.mayfly && data.getInt(AWAKE_BUDGET_TAG) > 0) {
                    abilities.mayfly = true;
                    player.onUpdateAbilities();
                }
            }
        }
    }

    /** Ищет надетый предмет конкретной стадии кольца в слотах Curios. */
    public static Optional<ItemStack> findCurio(Player player, net.minecraft.world.item.Item item) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(item))
                .map(SlotResult::stack);
    }

    /** Ищет надетое кольцо в слотах Curios. Работает и на клиенте, и на сервере. */
    public static Optional<ItemStack> findRing(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(ModContent.ANGEL_RING.get()))
                .map(SlotResult::stack);
    }

    /** Builds and sends the ring status snapshot for the relic scroll screen. */
    public static void sendStatus(Player player) {
        sendStatus(player, null);
    }

    /**
     * 1.8.1: snapshot of the ring stack the scroll is actually viewing.
     * target = UUID кольца-стека; nil UUID — ещё не брошенное кольцо (ищем
     * первое неролленное, оно и будет тем самым просмотренным); null — прежнее
     * поведение «надетое кольцо» (для хоткеев и фонового тика).
     */
    public static void sendStatus(Player player, UUID target) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CompoundTag data = serverPlayer.getPersistentData();
        boolean buffering = serverPlayer.hasEffect(MobEffects.SLOW_FALLING);
        int bufferTicks = buffering ? serverPlayer.getEffect(MobEffects.SLOW_FALLING).getDuration() : 0;
        Optional<ItemStack> viewed = target == null ? Optional.empty() : findStackByRelicId(serverPlayer, target);
        RelicData relic = viewed.or(() -> findRing(serverPlayer))
                .map(stack -> RelicAbilities.ensureRolled(serverPlayer, stack,
                        stack.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY)))
                .orElse(RelicData.EMPTY);
        PacketDistributor.sendToPlayer(serverPlayer,
                new AngelRingStatusPayload(data.getBoolean(GRANTED_TAG),
                        serverPlayer.getAbilities().flying,
                        buffering,
                        bufferTicks,
                        data.getLong(FLIGHT_TICKS_TAG),
                        relic,
                        data.getInt(RelicAbilities.CD_TAG + "0"),
                        data.getInt(RelicAbilities.CD_TAG + "1"),
                        data.getInt(RelicAbilities.CD_TAG + "2")));
    }

    /**
     * Ищет конкретный стек кольца по UUID реликвии во всех хранилищах игрока:
     * Curios-слоты, обычный инвентарь, вторая рука, броня. Nil UUID — первое
     * кольцо без броска рарности (ровно то, которое только что открыли).
     */
    public static Optional<ItemStack> findStackByRelicId(Player player, UUID target) {
        if (target == null) {
            return Optional.empty();
        }
        boolean unrolled = RelicData.NO_ID.equals(target);
        Optional<ItemStack> inCurios = CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(stack -> matchesTarget(stack, target, unrolled)))
                .map(SlotResult::stack);
        if (inCurios.isPresent()) {
            return inCurios;
        }
        List<ItemStack> bags = new ArrayList<>(player.getInventory().items);
        bags.addAll(player.getInventory().offhand);
        bags.addAll(player.getInventory().armor);
        for (ItemStack stack : bags) {
            if (matchesTarget(stack, target, unrolled)) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    private static boolean matchesTarget(ItemStack stack, UUID target, boolean unrolled) {
        if (!stack.is(ModContent.ANGEL_RING.get()) && !stack.has(ModContent.RELIC_DATA.get())) {
            return false;
        }
        RelicData relic = stack.get(ModContent.RELIC_DATA.get());
        if (unrolled) {
            return relic == null || !relic.rolled();
        }
        return relic != null && target.equals(relic.id());
    }
}
