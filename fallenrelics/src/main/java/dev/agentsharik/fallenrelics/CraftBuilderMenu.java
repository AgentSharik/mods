package dev.agentsharik.fallenrelics;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
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
import org.jetbrains.annotations.Nullable;

public final class CraftBuilderMenu extends AbstractContainerMenu {
    /** Container slots: 9 grid + shaped result + 2 furnace inputs + shapeless result. */
    private static final int BUILDER_SLOTS = CraftBuilderBlockEntity.SLOT_COUNT + 1;
    private static final int PLAYER_INVENTORY_START = BUILDER_SLOTS;

    @Nullable private final BlockPos blockPos;
    @Nullable private final CraftBuilderBlockEntity blockEntity;
    @Nullable private final InteractionHand portableHand;
    @Nullable private final ItemStack portableItemStack;
    @Nullable private final PocketCraftBuilderItem.PocketStorage pocketStorage;
    private final ItemStackHandler itemHandler;
    private final ContainerLevelAccess access;
    private final SimpleContainerData data = new SimpleContainerData(1);

    public CraftBuilderMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readBlockPos());
    }

    public CraftBuilderMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(ModContent.CRAFT_BUILDER_MENU.get(), containerId);
        this.blockPos = blockPos;
        this.portableHand = null;
        this.portableItemStack = null;
        this.pocketStorage = null;
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), blockPos);

        BlockEntity found = playerInventory.player.level().getBlockEntity(blockPos);
        this.blockEntity = found instanceof CraftBuilderBlockEntity builder ? builder : null;
        this.itemHandler = blockEntity == null
                ? new ItemStackHandler(BUILDER_SLOTS)
                : blockEntity.getInventory();
        data.set(0, blockEntity != null && blockEntity.isShapeless() ? 1 : 0);
        addSlots(playerInventory);
    }

    private CraftBuilderMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModContent.POCKET_CRAFT_BUILDER_MENU.get(), containerId);
        this.blockPos = null;
        this.blockEntity = null;
        this.portableHand = hand;
        this.portableItemStack = playerInventory.player.getItemInHand(hand);
        this.pocketStorage = new PocketCraftBuilderItem.PocketStorage(
                portableItemStack,
                playerInventory.player.level().registryAccess(),
                !playerInventory.player.level().isClientSide);
        this.itemHandler = pocketStorage;
        this.access = ContainerLevelAccess.NULL;
        data.set(0, pocketStorage.isShapeless() ? 1 : 0);
        addSlots(playerInventory);
    }

    public static CraftBuilderMenu createPocketFromNetwork(
            int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        int handId = extraData.readVarInt();
        InteractionHand[] hands = InteractionHand.values();
        InteractionHand hand = handId >= 0 && handId < hands.length ? hands[handId] : InteractionHand.MAIN_HAND;
        return new CraftBuilderMenu(containerId, playerInventory, hand);
    }

    public static CraftBuilderMenu forPocket(int containerId, Inventory playerInventory, InteractionHand hand) {
        return new CraftBuilderMenu(containerId, playerInventory, hand);
    }

    private void addSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new ModeSlot(itemHandler, row * 3 + column,
                        29 + column * 18, 16 + row * 18, false));
            }
        }
        addSlot(new ModeSlot(itemHandler, CraftBuilderBlockEntity.RESULT_SLOT, 124, 35, false));
        addSlot(new ModeSlot(itemHandler, CraftBuilderBlockEntity.FURNACE_INPUT_FIRST, 56, 17, true));
        addSlot(new ModeSlot(itemHandler, CraftBuilderBlockEntity.FURNACE_INPUT_SECOND, 56, 53, true));
        addSlot(new ModeSlot(itemHandler, CraftBuilderBlockEntity.RESULT_SLOT, 120, 36, true));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addPlayerSlot(playerInventory, column + row * 9 + 9,
                        7 + column * 18, 83 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            addPlayerSlot(playerInventory, column, 7 + column * 18, 141);
        }

        addDataSlots(data);
    }

    /** Grid slots exist only in shaped mode, furnace-style inputs only in shapeless mode. */
    private final class ModeSlot extends SlotItemHandler {
        private final boolean furnaceSide;

        private ModeSlot(net.neoforged.neoforge.items.IItemHandler handler, int index, int x, int y,
                         boolean furnaceSide) {
            super(handler, index, x, y);
            this.furnaceSide = furnaceSide;
        }

        @Override
        public boolean isActive() {
            return furnaceSide == isShapeless();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isActive() && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return isActive() && super.mayPickup(player);
        }
    }

    private void addPlayerSlot(Inventory playerInventory, int inventoryIndex, int x, int y) {
        addSlot(new Slot(playerInventory, inventoryIndex, x, y) {
            @Override
            public boolean mayPickup(Player player) {
                return portableItemStack == null || getItem() != portableItemStack;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return portableItemStack == null || stack != portableItemStack;
            }
        });
    }

    public boolean isShapeless() {
        return data.get(0) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!stillValid(player)) {
            return false;
        }
        if (player.level().isClientSide) {
            return true;
        }

        switch (buttonId) {
            case 0 -> {
                data.set(0, isShapeless() ? 0 : 1);
                if (blockEntity != null) {
                    blockEntity.setShapeless(isShapeless());
                } else if (pocketStorage != null) {
                    pocketStorage.setShapeless(isShapeless());
                }
                return true;
            }
            case 1 -> {
                CraftBuilderScripts.saveRecipe(player, itemHandler, isShapeless());
                return true;
            }
            case 2 -> {
                CraftBuilderScripts.removeRecipe(player, itemHandler, isShapeless());
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
        if (portableItemStack != null && stack == portableItemStack) {
            return ItemStack.EMPTY;
        }
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
        if (portableHand != null) {
            ItemStack held = player.getItemInHand(portableHand);
            return held == portableItemStack && held.is(ModContent.POCKET_CRAFT_BUILDER.get());
        }
        return blockPos != null && stillValid(access, player, ModContent.CRAFT_BUILDER.get());
    }
}
