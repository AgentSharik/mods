package dev.agentsharik.fallenrelics;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class CraftBuilderMenu extends AbstractContainerMenu {
    private static final int BUILDER_SLOTS = CraftBuilderBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = BUILDER_SLOTS;

    private final BlockPos blockPos;
    private final CraftBuilderBlockEntity blockEntity;
    private final ItemStackHandler itemHandler;
    private final ContainerLevelAccess access;
    private final SimpleContainerData data = new SimpleContainerData(1);

    public CraftBuilderMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readBlockPos());
    }

    public CraftBuilderMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(ModContent.CRAFT_BUILDER_MENU.get(), containerId);
        this.blockPos = blockPos;
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), blockPos);
        BlockEntity found = playerInventory.player.level().getBlockEntity(blockPos);
        this.blockEntity = found instanceof CraftBuilderBlockEntity builder ? builder : null;
        this.itemHandler = blockEntity == null
                ? new ItemStackHandler(BUILDER_SLOTS)
                : blockEntity.getInventory();
        data.set(0, blockEntity != null && blockEntity.isShapeless() ? 1 : 0);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(itemHandler, row * 3 + column,
                        14 + column * 18, 30 + row * 18));
            }
        }
        addSlot(new SlotItemHandler(itemHandler, CraftBuilderBlockEntity.RESULT_SLOT, 180, 48));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        10 + column * 18, 144 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 10 + column * 18, 202));
        }

        addDataSlots(data);
    }

    public boolean isShapeless() {
        return data.get(0) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (blockEntity == null || !stillValid(player)) {
            return false;
        }
        if (player.level().isClientSide) {
            return true;
        }

        switch (buttonId) {
            case 0 -> {
                data.set(0, isShapeless() ? 0 : 1);
                blockEntity.setShapeless(isShapeless());
                return true;
            }
            case 1 -> {
                CraftBuilderScripts.saveRecipe(player, blockPos, blockEntity);
                return true;
            }
            case 2 -> {
                CraftBuilderScripts.removeRecipe(player, blockPos, blockEntity, isShapeless());
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < BUILDER_SLOTS) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, BUILDER_SLOTS, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModContent.CRAFT_BUILDER.get());
    }
}
