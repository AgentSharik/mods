package dev.agentsharik.fallenrelics;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.items.ItemStackHandler;

/** A handheld CraftBuilder; its input grid and mode are stored on this individual item stack. */
public final class PocketCraftBuilderItem extends Item {
    private static final String DATA_KEY = "FallenRelicsPocketCraftBuilder";
    private static final String INVENTORY_KEY = "Inventory";
    private static final String SHAPELESS_KEY = "Shapeless";

    public PocketCraftBuilderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inventory, opener) -> CraftBuilderMenu.forPocket(containerId, inventory, hand),
                            Component.translatable("container.fallenrelics.pocket_craft_builder")),
                    buffer -> buffer.writeVarInt(hand.ordinal()));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** Item-backed storage used by the same recipe editor as the placed block. */
    public static final class PocketStorage extends ItemStackHandler {
        private final ItemStack itemStack;
        private final HolderLookup.Provider registries;
        private final boolean authoritative;
        private boolean shapeless;
        private boolean loading = true;

        public PocketStorage(ItemStack itemStack, HolderLookup.Provider registries, boolean authoritative) {
            super(CraftBuilderBlockEntity.SLOT_COUNT);
            this.itemStack = itemStack;
            this.registries = registries;
            this.authoritative = authoritative;

            CompoundTag root = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (root.contains(DATA_KEY, Tag.TAG_COMPOUND)) {
                CompoundTag data = root.getCompound(DATA_KEY);
                shapeless = data.getBoolean(SHAPELESS_KEY);
                if (data.contains(INVENTORY_KEY, Tag.TAG_COMPOUND)) {
                    deserializeNBT(registries, data.getCompound(INVENTORY_KEY));
                }
            }
            loading = false;
        }

        public boolean isShapeless() {
            return shapeless;
        }

        public void setShapeless(boolean shapeless) {
            if (this.shapeless != shapeless) {
                this.shapeless = shapeless;
                writeToItem();
            }
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (!loading) {
                writeToItem();
            }
        }

        private void writeToItem() {
            if (!authoritative) {
                return;
            }
            CompoundTag root = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            CompoundTag data = new CompoundTag();
            data.put(INVENTORY_KEY, serializeNBT(registries));
            data.putBoolean(SHAPELESS_KEY, shapeless);
            root.put(DATA_KEY, data);
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        }
    }
}
