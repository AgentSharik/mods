package dev.agentsharik.autopackager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class PackagerBlockEntity extends BlockEntity {
    private static final int INPUT_SLOTS = 9;
    private static final int OUTPUT_SLOTS = 9;
    private static final int ENERGY_CAPACITY = 100_000;
    private static final int ENERGY_TRANSFER = 2_000;
    private static final int ENERGY_PER_CYCLE = 1_000;
    private static final int NORMAL_DELAY = 10;
    private static final int IDLE_DELAY = 200;

    private final ItemStackHandler inputInventory = new ItemStackHandler(INPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirtyAndNotify();
        }
    };

    private final ItemStackHandler outputInventory = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirtyAndNotify();
        }
    };

    private final IItemHandler inputPipeView = new FilteredItemHandler(inputInventory, true, false);
    private final IItemHandler outputPipeView = new FilteredItemHandler(outputInventory, false, true);
    private final PackagerEnergyStorage energyStorage = new PackagerEnergyStorage();

    private PackagerMode mode = PackagerMode.HYBRID;
    private int tickCounter;
    private int tickDelay = NORMAL_DELAY;

    public PackagerBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.PACKAGER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PackagerBlockEntity packager) {
        if (level.isClientSide) {
            return;
        }
        packager.tickServer();
    }

    private void tickServer() {
        if (++tickCounter < tickDelay) {
            return;
        }
        tickCounter = 0;

        // Match Auto-Packager's default RF cost and timing: one operation every
        // 10 ticks while working, with a slower retry when there is no recipe.
        if (energyStorage.getEnergyStored() <= ENERGY_PER_CYCLE) {
            tickDelay = NORMAL_DELAY;
            return;
        }

        if (tryCraft()) {
            energyStorage.extractEnergy(ENERGY_PER_CYCLE, false);
            tickDelay = NORMAL_DELAY;
        } else {
            tickDelay = IDLE_DELAY;
        }
    }

    private boolean tryCraft() {
        if (level == null || level.isClientSide) {
            return false;
        }

        for (int slot = 0; slot < inputInventory.getSlots(); slot++) {
            ItemStack candidate = inputInventory.getStackInSlot(slot);
            if (candidate.isEmpty()) {
                continue;
            }

            ItemStack oneItem = candidate.copyWithCount(1);
            for (CraftPlan plan : getPlansForMode()) {
                if (countMatchingInput(oneItem) < plan.itemCount()) {
                    continue;
                }
                if (tryCraftPlan(oneItem, plan)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryCraftPlan(ItemStack ingredient, CraftPlan plan) {
        NonNullList<ItemStack> grid = NonNullList.withSize(plan.width() * plan.height(), ItemStack.EMPTY);
        for (int slot : plan.filledSlots()) {
            grid.set(slot, ingredient.copyWithCount(1));
        }
        CraftingInput craftingInput = CraftingInput.of(plan.width(), plan.height(), grid);
        Optional<RecipeHolder<CraftingRecipe>> matchingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftingInput, level);
        if (matchingRecipe.isEmpty()) {
            return false;
        }

        ItemStack result = matchingRecipe.get().value().assemble(craftingInput, level.registryAccess());
        if (result.isEmpty()) {
            return false;
        }

        List<ItemStack> products = new ArrayList<>();
        products.add(result.copy());
        NonNullList<ItemStack> remainingItems = level.getRecipeManager()
                .getRemainingItemsFor(RecipeType.CRAFTING, craftingInput, level);
        for (ItemStack remaining : remainingItems) {
            if (!remaining.isEmpty()) {
                products.add(remaining.copy());
            }
        }

        if (!canFitProducts(products)) {
            return false;
        }

        consumeMatchingInput(ingredient, plan.itemCount());
        for (ItemStack product : products) {
            insertIntoOutput(product.copy());
        }
        return true;
    }

    private int countMatchingInput(ItemStack sample) {
        int count = 0;
        for (int slot = 0; slot < inputInventory.getSlots(); slot++) {
            ItemStack stack = inputInventory.getStackInSlot(slot);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(sample, stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void consumeMatchingInput(ItemStack sample, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inputInventory.getSlots() && remaining > 0; slot++) {
            ItemStack stack = inputInventory.getStackInSlot(slot);
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(sample, stack)) {
                continue;
            }
            int extracted = Math.min(remaining, stack.getCount());
            inputInventory.extractItem(slot, extracted, false);
            remaining -= extracted;
        }
    }

    private boolean canFitProducts(List<ItemStack> products) {
        List<ItemStack> stagedOutput = new ArrayList<>(outputInventory.getSlots());
        for (int slot = 0; slot < outputInventory.getSlots(); slot++) {
            ItemStack stack = outputInventory.getStackInSlot(slot);
            stagedOutput.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }

        for (ItemStack product : products) {
            ItemStack remaining = product.copy();
            for (int slot = 0; slot < stagedOutput.size() && !remaining.isEmpty(); slot++) {
                ItemStack existing = stagedOutput.get(slot);
                if (existing.isEmpty()) {
                    int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                    stagedOutput.set(slot, remaining.copyWithCount(moved));
                    remaining.shrink(moved);
                } else if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                    int room = existing.getMaxStackSize() - existing.getCount();
                    if (room > 0) {
                        int moved = Math.min(room, remaining.getCount());
                        existing.grow(moved);
                        remaining.shrink(moved);
                    }
                }
            }
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void insertIntoOutput(ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < outputInventory.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = outputInventory.insertItem(slot, remaining, false);
        }
        // canFitProducts() is checked before consuming ingredients, so this can
        // only be non-empty if another mod mutates the inventory mid-tick.
        if (!remaining.isEmpty()) {
            setChanged();
        }
    }

    private List<CraftPlan> getPlansForMode() {
        return switch (mode) {
            case HYBRID -> List.of(CraftPlan.square(2), CraftPlan.square(3));
            case HYBRID2 -> List.of(CraftPlan.square(3), CraftPlan.square(2));
            case SMALL -> List.of(CraftPlan.square(2));
            case LARGE -> List.of(CraftPlan.square(3));
            case HOLLOW -> List.of(CraftPlan.hollowThreeByThree());
            case UNPACKAGE -> List.of(CraftPlan.single());
        };
    }

    public PackagerMode getMode() {
        return mode;
    }

    public void cycleMode() {
        mode = mode.next();
        markDirtyAndNotify();
    }

    public IItemHandler getItemHandlerForSide(@Nullable Direction side) {
        if (side == null || !getBlockState().hasProperty(PackagerBlock.FACING)) {
            return inputPipeView;
        }
        Direction facing = getBlockState().getValue(PackagerBlock.FACING);
        // The original machine takes items from its left and sends products to
        // its right. Other faces also accept pipe input for easier automation.
        return side == facing.getClockWise() ? outputPipeView : inputPipeView;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        dropInventory(inputInventory);
        dropInventory(outputInventory);
    }

    private void dropInventory(ItemStackHandler inventory) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("InputInventory", inputInventory.serializeNBT(registries));
        tag.put("OutputInventory", outputInventory.serializeNBT(registries));
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putInt("Mode", mode.ordinal());
        tag.putInt("TickDelay", tickDelay);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("InputInventory")) {
            inputInventory.deserializeNBT(registries, tag.getCompound("InputInventory"));
        }
        if (tag.contains("OutputInventory")) {
            outputInventory.deserializeNBT(registries, tag.getCompound("OutputInventory"));
        }
        energyStorage.setStoredEnergy(tag.getInt("Energy"));
        mode = PackagerMode.fromId(tag.getInt("Mode"));
        tickDelay = tag.getInt("TickDelay") == IDLE_DELAY ? IDLE_DELAY : NORMAL_DELAY;
    }

    private void markDirtyAndNotify() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    private record CraftPlan(int width, int height, int[] filledSlots, int itemCount) {
        private static CraftPlan square(int size) {
            int[] filled = new int[size * size];
            for (int i = 0; i < filled.length; i++) {
                filled[i] = i;
            }
            return new CraftPlan(size, size, filled, filled.length);
        }

        private static CraftPlan hollowThreeByThree() {
            return new CraftPlan(3, 3, new int[]{0, 1, 2, 3, 5, 6, 7, 8}, 8);
        }

        private static CraftPlan single() {
            return new CraftPlan(1, 1, new int[]{0}, 1);
        }
    }

    private static final class FilteredItemHandler implements IItemHandler {
        private final ItemStackHandler delegate;
        private final boolean canInsert;
        private final boolean canExtract;

        private FilteredItemHandler(ItemStackHandler delegate, boolean canInsert, boolean canExtract) {
            this.delegate = delegate;
            this.canInsert = canInsert;
            this.canExtract = canExtract;
        }

        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return canInsert ? delegate.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return canExtract ? delegate.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return delegate.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return canInsert && delegate.isItemValid(slot, stack);
        }
    }

    private final class PackagerEnergyStorage extends EnergyStorage {
        private PackagerEnergyStorage() {
            super(ENERGY_CAPACITY, ENERGY_TRANSFER, ENERGY_TRANSFER);
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = super.receiveEnergy(toReceive, simulate);
            if (!simulate && received > 0) {
                markDirtyAndNotify();
            }
            return received;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            int extracted = super.extractEnergy(toExtract, simulate);
            if (!simulate && extracted > 0) {
                markDirtyAndNotify();
            }
            return extracted;
        }

        private void setStoredEnergy(int storedEnergy) {
            this.energy = Math.max(0, Math.min(this.capacity, storedEnergy));
        }
    }
}
