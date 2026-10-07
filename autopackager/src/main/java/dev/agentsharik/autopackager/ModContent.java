package dev.agentsharik.autopackager;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AutoPackagerMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AutoPackagerMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AutoPackagerMod.MOD_ID);

    public static final DeferredBlock<PackagerBlock> PACKAGER = BLOCKS.register("packager", () -> new PackagerBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(10.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> PACKAGER_ITEM =
            ITEMS.registerSimpleBlockItem("packager", PACKAGER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PackagerBlockEntity>> PACKAGER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("packager", () -> BlockEntityType.Builder
                    .of(PackagerBlockEntity::new, PACKAGER.get())
                    .build(null));

    private ModContent() {}
}
