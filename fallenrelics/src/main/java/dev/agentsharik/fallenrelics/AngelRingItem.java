package dev.agentsharik.fallenrelics;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Angel ring item as designed in the angel-ring addon: foiled, epic, with a grey hint line. */
public class AngelRingItem extends Item {
    public AngelRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.fallenrelics.angel_ring.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.fallenrelics.angel_ring.scroll_hint").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.fallenrelics.angel_ring.shift_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    /** Right-click in hand opens the relic scroll screen (client side). */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new AngelRingScreen(stack, null));
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.consume(stack);
    }
}
