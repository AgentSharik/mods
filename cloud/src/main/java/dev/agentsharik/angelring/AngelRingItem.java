package dev.agentsharik.angelring;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class AngelRingItem extends Item {
    public AngelRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        RelicData data = stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
        tooltip.add(Component.translatable("item.angelring.angel_ring.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.angelring.level", data.level(), RelicData.MAX_LEVEL)
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.angelring.open_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    /** Блеск зачарования — чтобы кольцо выглядело особенным. */
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
