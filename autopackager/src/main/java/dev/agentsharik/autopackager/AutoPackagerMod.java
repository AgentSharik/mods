package dev.agentsharik.autopackager;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(AutoPackagerMod.MOD_ID)
public final class AutoPackagerMod {
    public static final String MOD_ID = "autopackager";

    public AutoPackagerMod(IEventBus modEventBus) {
        ModContent.BLOCKS.register(modEventBus);
        ModContent.ITEMS.register(modEventBus);
        ModContent.BLOCK_ENTITIES.register(modEventBus);

        modEventBus.addListener(this::addCreativeContent);
        modEventBus.addListener(this::registerCapabilities);
    }

    private void addCreativeContent(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModContent.PACKAGER_ITEM);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModContent.PACKAGER_BLOCK_ENTITY.get(),
                PackagerBlockEntity::getItemHandlerForSide
        );
    }
}
