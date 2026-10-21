package dev.agentsharik.angelring;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Кольцо само сообщает о своём надевании/снятии (Curios вызывает onEquip/onUnequip), поэтому серверу больше
 * не нужно каждые 5 тиков опрашивать инвентарь каждого игрока.
 */
public class AngelRingItem extends Item implements ICurioItem {
    public AngelRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        RelicData data = stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
        tooltip.add(Component.translatable("item.angelring.angel_ring.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.angelring.level", data.level(), RelicData.MAX_LEVEL)
                .withStyle(ChatFormatting.GOLD));
        String stars = data.rolled()
                ? "\u2605".repeat(data.ringStars()) + "\u2606".repeat(RelicData.MAX_STARS - data.ringStars())
                : "?";
        tooltip.add(Component.translatable("tooltip.angelring.rarity", stars).withStyle(ChatFormatting.RED));
    }

    /** Блеск зачарования — чтобы кольцо выглядело особенным. */
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    /**
     * Редкость определяется, когда кольцо впервые оказывается в инвентаре игрока. Это ванильный тик
     * самого предмета (он и так вызывается), а проверка — одно чтение компонента без аллокаций.
     * Раньше сервер раз в секунду перебирал все 41 слот инвентаря каждого игрока.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            ensureRolled(player, stack);
        }
    }

    // ---- Curios ----

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof ServerPlayer player) {
            AngelRingFlight.markDirty(player); // редкость и полёт применятся в ближайшем тике сервера
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof ServerPlayer player) {
            AngelRingFlight.markDirty(player); // снятие (или замена на другое кольцо) — пересчитаем состояние
        }
    }

    static void ensureRolled(ServerPlayer player, ItemStack stack) {
        RelicData data = stack.get(ModComponents.RELIC_DATA.get());
        if (data == null || !data.rolled()) {
            RelicData base = data == null ? RelicData.EMPTY : data;
            stack.set(ModComponents.RELIC_DATA.get(), base.ensureRolled(player.getRandom()));
        }
    }
}
