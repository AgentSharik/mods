package dev.agentsharik.angelring;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Цепочка предметов: Оболочка кольца → Кольцо планирования → Пробуждённое кольцо полёта → (ритуал) → Кольцо ангела.
 */
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AngelRingMod.MOD_ID);

    /** Этап 1: заготовка, обычный компонент без эффектов. */
    public static final DeferredItem<Item> RING_BLANK = ITEMS.registerItem("ring_blank", Item::new);

    /** Этап 2: кольцо планирования — только плавное падение. */
    public static final DeferredItem<Item> GLIDING_RING = ITEMS.registerItem("gliding_ring",
            props -> new GlidingRingItem(props.stacksTo(1).rarity(Rarity.UNCOMMON)));

    /** Этап 3: пробуждённое кольцо — полёт на 60 секунд. */
    public static final DeferredItem<Item> AWAKENED_RING = ITEMS.registerItem("awakened_ring",
            props -> new AwakenedRingItem(props.stacksTo(1).rarity(Rarity.RARE)));

    /** Финал: реликвия «Кольцо ангела» (получается только ритуалом). */
    public static final DeferredItem<Item> ANGEL_RING = ITEMS.registerItem("angel_ring",
            props -> new AngelRingItem(props.stacksTo(1).rarity(Rarity.EPIC)));

    /** Показываем все предметы цепочки во вкладке «Инструменты» творческого меню. */
    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(RING_BLANK);
            event.accept(GLIDING_RING);
            event.accept(AWAKENED_RING);
            event.accept(ANGEL_RING);
        }
    }
}
