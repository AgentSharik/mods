package dev.agentsharik.angelring;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Этап 2 цепочки. Пока надето в слоте колец, даёт плавное падение (как у курицы). Летать не позволяет. */
public class GlidingRingItem extends Item {
    public GlidingRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.angelring.gliding_ring.tooltip1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.angelring.gliding_ring.tooltip2").withStyle(ChatFormatting.DARK_GRAY));
    }
}
