package dev.agentsharik.fallenrelics;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * The packager's own recipe editor: the same 1:1 pack window as the CraftBuilder,
 * but the 3x3 grid here only stores a TEMPLATE of the craft the machine should
 * perform, and the framed slot shows the matched result. With a template set the
 * packager crafts exactly that recipe (mixed input types included); without one
 * it falls back to scanning every registered recipe.
 */
public final class PackagerMenu extends AbstractContainerMenu {
    private static final int PATTERN_SLOTS = 9;
    private static final int RESULT_SLOT = PATTERN_SLOTS;
    private static final int PLAYER_INVENTORY_START = RESULT_SLOT + 1;

    @Nullable
    private final PackagerBlockEntity packager;

    public PackagerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readBlockPos());
    }

    public PackagerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModContent.PACKAGER_MENU.get(), containerId);
        BlockEntity found = playerInventory.player.level().getBlockEntity(pos);
        this.packager = found instanceof PackagerBlockEntity existing ? existing : null;
        ItemStackHandler pattern = packager != null
                ? packager.getPatternInventory()
                : new ItemStackHandler(PATTERN_SLOTS);
        ItemStackHandler result = packager != null
                ? packager.getPatternResultInventory()
                : new ItemStackHandler(1);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(pattern, row * 3 + column,
                        30 + column * 18, 17 + row * 18));
            }
        }
        addSlot(new ViewSlot(result, 0, 124, 35));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    /** The matched result is display-only: it never leaves the window by hand. */
    private static final class ViewSlot extends SlotItemHandler {
        private ViewSlot(ItemStackHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return packager == null || !packager.isRemoved();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (packager != null) {
            switch (buttonId) {
                case 1 -> packager.applyPattern(player);
                case 2 -> packager.clearPattern();
                case 0 -> packager.cycleModeWithMessage(player);
                default -> {
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < PLAYER_INVENTORY_START) {
                if (index == RESULT_SLOT || !this.moveItemStackTo(stack, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, PATTERN_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }
}
