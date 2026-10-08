package dev.agentsharik.fallenrelics;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FallenRelicsMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FallenRelicsMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FallenRelicsMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, FallenRelicsMod.MOD_ID);

    public static final DeferredBlock<PackagerBlock> PACKAGER = BLOCKS.register("packager", () -> new PackagerBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(10.0F, 10.0F)
                    .sound(SoundType.METAL)));
    public static final DeferredItem<BlockItem> PACKAGER_ITEM =
            ITEMS.registerSimpleBlockItem("packager", PACKAGER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PackagerBlockEntity>> PACKAGER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("packager", () -> BlockEntityType.Builder
                    .of(PackagerBlockEntity::new, PACKAGER.get())
                    .build(null));

    public static final DeferredBlock<CraftBuilderBlock> CRAFT_BUILDER = BLOCKS.register("craft_builder", () ->
            new CraftBuilderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(6.0F, 8.0F)
                    .sound(SoundType.METAL)));
    public static final DeferredItem<BlockItem> CRAFT_BUILDER_ITEM =
            ITEMS.registerSimpleBlockItem("craft_builder", CRAFT_BUILDER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraftBuilderBlockEntity>> CRAFT_BUILDER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("craft_builder", () -> BlockEntityType.Builder
                    .of(CraftBuilderBlockEntity::new, CRAFT_BUILDER.get())
                    .build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<CraftBuilderMenu>> CRAFT_BUILDER_MENU =
            MENUS.register("craft_builder", () -> IMenuTypeExtension.create(CraftBuilderMenu::new));
    public static final DeferredItem<PocketCraftBuilderItem> POCKET_CRAFT_BUILDER =
            ITEMS.register("pocket_craft_builder", () -> new PocketCraftBuilderItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<MenuType<?>, MenuType<CraftBuilderMenu>> POCKET_CRAFT_BUILDER_MENU =
            MENUS.register("pocket_craft_builder", () -> IMenuTypeExtension.create(CraftBuilderMenu::createPocketFromNetwork));

    public static final DeferredItem<Item> UPGRADE_TEMPLATE =
            ITEMS.registerSimpleItem("upgrade_template", new Item.Properties());
    public static final DeferredItem<Item> IRON_UPGRADE =
            ITEMS.registerSimpleItem("iron_upgrade", new Item.Properties());
    public static final DeferredItem<Item> GOLD_UPGRADE =
            ITEMS.registerSimpleItem("gold_upgrade", new Item.Properties());
    public static final DeferredItem<Item> DIAMOND_UPGRADE =
            ITEMS.registerSimpleItem("diamond_upgrade", new Item.Properties());
    public static final DeferredItem<Item> NETHERITE_UPGRADE =
            ITEMS.registerSimpleItem("netherite_upgrade", new Item.Properties());

    private ModContent() {}

    /** Returns the installed tier for one of the four upgrade items, or zero for other items. */
    public static int getUpgradeTier(Item item) {
        if (item == IRON_UPGRADE.get()) {
            return 1;
        }
        if (item == GOLD_UPGRADE.get()) {
            return 2;
        }
        if (item == DIAMOND_UPGRADE.get()) {
            return 3;
        }
        if (item == NETHERITE_UPGRADE.get()) {
            return 4;
        }
        return 0;
    }

    public static Item getUpgradeItem(int tier) {
        return switch (tier) {
            case 1 -> IRON_UPGRADE.get();
            case 2 -> GOLD_UPGRADE.get();
            case 3 -> DIAMOND_UPGRADE.get();
            case 4 -> NETHERITE_UPGRADE.get();
            default -> throw new IllegalArgumentException("No upgrade item for tier " + tier);
        };
    }
}
