package dev.agentsharik.angelring;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Этап 3 цепочки: полёт как в творческом режиме, но не дольше 60 секунд за заряд. Ядро ритуала пробуждения. */
public class AwakenedRingItem extends Item {
    public AwakenedRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        FlightState state = stack.getOrDefault(ModComponents.FLIGHT_STATE.get(), FlightState.FULL);
        tooltip.add(Component.translatable("item.angelring.awakened_ring.tooltip1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.angelring.awakened_ring.tooltip2").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(state.locked() ? "item.angelring.awakened_ring.charge_locked"
                        : "item.angelring.awakened_ring.charge", state.ticks() / 20, FlightState.MAX_TICKS / 20)
                .withStyle(state.locked() ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.angelring.awakened_ring.ritual1").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.angelring.awakened_ring.ritual2", RitualManager.LEVEL_COST)
                .withStyle(ChatFormatting.DARK_RED));
    }

    /** Мягкое свечение — «сила Звезды Незера». */
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
