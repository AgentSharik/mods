package dev.agentsharik.fallenrelics;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Angel ring item as designed in the angel-ring addon: foiled, epic, with a grey hint line. */
public class AngelRingItem extends Item {
    public AngelRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.fallenrelics.angel_ring.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
