package dev.agentsharik.fallenrelics;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(FallenRelicsMod.MOD_ID)
public final class FallenRelicsMod {
    public static final String MOD_ID = "fallenrelics";

    public FallenRelicsMod(IEventBus modEventBus) {
        ModContent.BLOCKS.register(modEventBus);
        ModContent.ITEMS.register(modEventBus);
        ModContent.BLOCK_ENTITIES.register(modEventBus);

        modEventBus.addListener(this::addCreativeContent);
        modEventBus.addListener(this::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onPackagerRightClick);
    }

    private void addCreativeContent(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModContent.PACKAGER_ITEM);
            event.accept(ModContent.UPGRADE_TEMPLATE);
            event.accept(ModContent.IRON_UPGRADE);
            event.accept(ModContent.GOLD_UPGRADE);
            event.accept(ModContent.DIAMOND_UPGRADE);
            event.accept(ModContent.NETHERITE_UPGRADE);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModContent.PACKAGER_BLOCK_ENTITY.get(),
                PackagerBlockEntity::getItemHandlerForSide
        );
    }

    private void onPackagerRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled()) {
            return;
        }
        Level level = event.getLevel();
        ItemStack heldItem = event.getItemStack();
        int requestedTier = ModContent.getUpgradeTier(heldItem.getItem());
        if (requestedTier == 0 || !level.getBlockState(event.getPos()).is(ModContent.PACKAGER.get())) {
            return;
        }

        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        BlockPos pos = event.getPos();
        var state = level.getBlockState(pos);
        int installedTier = state.getValue(PackagerBlock.UPGRADE_TIER);
        if (requestedTier <= installedTier) {
            event.getEntity().displayClientMessage(
                    Component.translatable("fallenrelics.upgrade.not_higher"), true);
        } else if (level.setBlock(pos, state.setValue(PackagerBlock.UPGRADE_TIER, requestedTier), 3)) {
            if (!event.getEntity().getAbilities().instabuild) {
                heldItem.shrink(1);
            }
            event.getEntity().displayClientMessage(
                    Component.translatable("fallenrelics.upgrade.installed",
                            Component.translatable(PackagerBlock.upgradeNameKey(requestedTier))),
                    true);
        } else {
            event.getEntity().displayClientMessage(
                    Component.translatable("fallenrelics.upgrade.failed"), true);
        }

        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }
}
