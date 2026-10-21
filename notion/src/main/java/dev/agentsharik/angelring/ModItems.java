package dev.agentsharik.angelring;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AngelRingMod.MOD_ID);

    public static final DeferredItem<Item> ANGEL_RING = ITEMS.registerItem(
            "angel_ring",
            props -> new AngelRingItem(props.stacksTo(1).rarity(Rarity.EPIC))
    );

    /** Показываем кольцо во вкладке «Инструменты» творческого меню. */
    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ANGEL_RING);
        }
    }
}
