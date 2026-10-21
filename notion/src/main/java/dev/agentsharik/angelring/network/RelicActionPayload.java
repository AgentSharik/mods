package dev.agentsharik.angelring.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import dev.agentsharik.angelring.AngelRingMod;
import dev.agentsharik.angelring.RelicActions;
import dev.agentsharik.angelring.RelicData;
import dev.agentsharik.angelring.RingLookup;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Клиент → сервер: «прошу выполнить действие». Пакет несёт только намерение (5 коротких чисел),
 * никаких цен или данных кольца: всё считает и проверяет сервер, потом отвечает {@link RelicResultPayload}.
 *
 * @param action      одно из RelicData.ACTION_UPGRADE / ACTION_REROLL / ACTION_USE
 * @param arg         id способности (0..2)
 * @param menuSlot    номер слота открытого меню, где лежит кольцо; -1 — надетое кольцо
 * @param containerId id меню, из которого открыто окно (защита от устаревшего номера слота)
 * @param flags       биты RelicActions.FLAG_* (например, подтверждение реролла)
 */
public record RelicActionPayload(int action, int arg, int menuSlot, int containerId, int flags)
        implements CustomPacketPayload {
    public static final Type<RelicActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, "action"));

    public static final StreamCodec<ByteBuf, RelicActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RelicActionPayload::action,
            ByteBufCodecs.VAR_INT, RelicActionPayload::arg,
            ByteBufCodecs.VAR_INT, RelicActionPayload::menuSlot,
            ByteBufCodecs.VAR_INT, RelicActionPayload::containerId,
            ByteBufCodecs.VAR_INT, RelicActionPayload::flags,
            RelicActionPayload::new);

    /** Не чаще одного действия за столько тиков (защита от спама пакетами и макросов). */
    private static final long MIN_INTERVAL_TICKS = 3L;
    /** Последнее действие игрока. Только главный поток сервера; чистится при выходе игрока. */
    private static final Map<UUID, Long> LAST_ACTION = new HashMap<>();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void forget(UUID player) {
        LAST_ACTION.remove(player);
    }

    public static void handle(RelicActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            int code = process(player, payload);
            if (code != RelicActions.Result.RATE_LIMITED) {
                PacketDistributor.sendToPlayer(player, new RelicResultPayload(payload.action(), payload.arg(), code));
            }
        });
    }

    private static int process(ServerPlayer player, RelicActionPayload p) {
        // 1) формат пакета
        int action = p.action();
        if (action != RelicData.ACTION_UPGRADE && action != RelicData.ACTION_REROLL && action != RelicData.ACTION_USE) {
            return RelicActions.Result.INVALID;
        }
        if (p.arg() < 0 || p.arg() >= RelicData.ABILITY_COUNT || p.menuSlot() < -1 || (p.flags() & ~RelicActions.FLAG_CONFIRMED) != 0) {
            return RelicActions.Result.INVALID;
        }
        // 2) состояние игрока
        if (!player.isAlive() || player.isSpectator()) {
            return RelicActions.Result.INVALID;
        }
        // 3) частота (для способностей есть своя перезарядка, но лимит общий — так проще и надёжнее)
        long now = player.serverLevel().getGameTime();
        Long last = LAST_ACTION.get(player.getUUID());
        if (last != null && now - last < MIN_INTERVAL_TICKS && now >= last) {
            return RelicActions.Result.RATE_LIMITED;
        }
        LAST_ACTION.put(player.getUUID(), now);

        // 4) где кольцо: способности — только надетое; для слота меню проверяем, что это то же самое открытое меню
        int slot = action == RelicData.ACTION_USE ? -1 : p.menuSlot();
        AbstractContainerMenu menu = player.containerMenu;
        if (slot >= 0 && (menu == null || menu.containerId != p.containerId() || !menu.stillValid(player))) {
            return RelicActions.Result.NO_RING;
        }
        Optional<ItemStack> ring = RingLookup.resolve(player, menu, slot);
        if (ring.isEmpty()) {
            return RelicActions.Result.NO_RING;
        }
        return RelicActions.perform(player, ring.get(), action, p.arg(), p.flags());
    }
}
