package dev.agentsharik.fallenrelics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class PackagerBlockEntity extends BlockEntity {
    private static final int INPUT_SLOTS = 9;
    private static final int OUTPUT_SLOTS = 9;
    private static final int NORMAL_DELAY = 1;
    private static final int IDLE_DELAY = 20;
    /** Hard wall-clock budget for one crafting cycle so big modpacks never stall a tick. */
    private static final long MAX_CYCLE_NANOS = 4_000_000L;
    /** Per recipe-attempt cap on backtracking nodes. */
    private static final int SEARCH_NODES = 1500;

    private final ItemStackHandler inputInventory = new ItemStackHandler(INPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            tickCounter = 0;
            tickDelay = NORMAL_DELAY;
            recipeScanIndex = 0;
            markDirtyAndNotify();
        }
    };

    private final ItemStackHandler outputInventory = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirtyAndNotify();
        }
    };

    private final IItemHandler pipeView = new CombinedItemHandler();

    private PackagerMode mode = PackagerMode.HYBRID;
    private int tickCounter;
    private int tickDelay = NORMAL_DELAY;
    private int recipeScanIndex;

    public PackagerBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.PACKAGER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PackagerBlockEntity packager) {
        if (!level.isClientSide) {
            packager.tickServer(state);
        }
    }

    private void tickServer(BlockState state) {
        if (++tickCounter < tickDelay) {
            return;
        }
        tickCounter = 0;

        long deadline = System.nanoTime() + MAX_CYCLE_NANOS;
        int craftsCompleted = 0;
        int craftsPerCycle = getCraftsPerCycle(state);
        while (craftsCompleted < craftsPerCycle && System.nanoTime() < deadline && tryCraft(deadline)) {
            craftsCompleted++;
        }
        boolean pushed = pushOutputToAdjacent();
        tickDelay = craftsCompleted > 0 || pushed ? NORMAL_DELAY : IDLE_DELAY;
    }

    private int getCraftsPerCycle(BlockState state) {
        int tier = state.getValue(PackagerBlock.UPGRADE_TIER);
        return switch (tier) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 8;
            case 4 -> 16;
            default -> 1;
        };
    }

    private boolean tryCraft(long deadline) {
        if (level == null || level.isClientSide) {
            return false;
        }

        List<PoolEntry> availableItems = collectAvailableItems();
        if (availableItems.isEmpty()) {
            return false;
        }

        List<RecipeHolder<CraftingRecipe>> recipes = level.getRecipeManager()
                .getAllRecipesFor(RecipeType.CRAFTING);
        int total = recipes.size();
        if (total == 0) {
            return false;
        }

        // Scan the recipe list round-robin under a time budget: a modpack with
        // thousands of crafting recipes must never freeze the server tick.
        List<GridPlan> plans = getPlansForMode();
        int start = recipeScanIndex % total;
        for (int i = 0; i < total; i++) {
            int index = (start + i) % total;
            if (System.nanoTime() > deadline) {
                recipeScanIndex = index;
                return false;
            }
            CraftingRecipe recipe = recipes.get(index).value();
            for (GridPlan plan : plans) {
                if (!recipe.canCraftInDimensions(plan.width(), plan.height())) {
                    continue;
                }
                if (tryRecipe(recipe, plan, availableItems, deadline)) {
                    recipeScanIndex = (index + 1) % total;
                    return true;
                }
            }
        }
        recipeScanIndex = 0;
        return false;
    }

    private boolean tryRecipe(CraftingRecipe recipe, GridPlan plan, List<PoolEntry> availableItems, long deadline) {
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            return false;
        }
        if (!allIngredientsAvailable(ingredients, availableItems)) {
            return false;
        }

        if (plan.width() == 3) {
            return tryFullGridRecipe(recipe, ingredients, availableItems);
        }

        if (tryUnshapedRecipe(recipe, ingredients, plan, availableItems, deadline)) {
            return true;
        }
        return tryIngredientLayouts(recipe, ingredients, plan, availableItems, deadline);
    }

    /**
     * A 3x3 cycle only compresses nine identical resources into their block
     * form, exactly like the coal block recipe: same full-grid shape, same
     * single ingredient in every slot. The check is a direct scan over the
     * input pools and never backtracks, so it cannot stall the server tick.
     */
    private boolean tryFullGridRecipe(CraftingRecipe recipe, List<Ingredient> ingredients, List<PoolEntry> availableItems) {
        int nonEmpty = 0;
        for (Ingredient ingredient : ingredients) {
            if (ingredient != null && !ingredient.isEmpty()) {
                nonEmpty++;
            }
        }
        if (nonEmpty != 9) {
            return false;
        }

        for (PoolEntry pool : availableItems) {
            if (pool.count() < 9) {
                continue;
            }
            boolean allMatch = true;
            for (Ingredient ingredient : ingredients) {
                if (ingredient == null || ingredient.isEmpty() || !ingredient.test(pool.stack())) {
                    allMatch = false;
                    break;
                }
            }
            if (!allMatch) {
                continue;
            }

            NonNullList<ItemStack> grid = NonNullList.withSize(9, ItemStack.EMPTY);
            for (int i = 0; i < 9; i++) {
                grid.set(i, pool.stack().copyWithCount(1));
            }
            CraftingInput input = CraftingInput.of(3, 3, grid);
            if (recipe.matches(input, level) && craftRecipe(recipe, input)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryUnshapedRecipe(
            CraftingRecipe recipe,
            List<Ingredient> ingredients,
            GridPlan plan,
            List<PoolEntry> availableItems,
            long deadline) {
        List<Ingredient> required = ingredients.stream()
                .filter(ingredient -> ingredient != null && !ingredient.isEmpty())
                .toList();
        if (required.isEmpty() || required.size() > plan.allowedSlots().length) {
            return false;
        }

        List<IngredientSlot> requirements = new ArrayList<>(required.size());
        int[] allowedSlots = plan.allowedSlots();
        for (int i = 0; i < required.size(); i++) {
            requirements.add(new IngredientSlot(allowedSlots[i], required.get(i)));
        }

        CraftingInput input = findMatchingInput(recipe, plan, requirements, availableItems, deadline);
        return input != null && craftRecipe(recipe, input);
    }

    private boolean tryIngredientLayouts(
            CraftingRecipe recipe,
            List<Ingredient> ingredients,
            GridPlan plan,
            List<PoolEntry> availableItems,
            long deadline) {
        int ingredientCount = ingredients.size();
        if (ingredientCount > plan.width() * plan.height()) {
            return false;
        }

        // Shaped recipes expose their ingredient slots in row-major order. Their
        // dimensions are not part of the common recipe API on every NeoForge
        // 1.21.1 build, so try each possible factorization and let matches()
        // identify the recipe's actual shape.
        for (int recipeWidth = 1; recipeWidth <= plan.width(); recipeWidth++) {
            if (ingredientCount % recipeWidth != 0) {
                continue;
            }
            int recipeHeight = ingredientCount / recipeWidth;
            if (recipeHeight < 1 || recipeHeight > plan.height()) {
                continue;
            }

            for (boolean mirrored : new boolean[]{false, true}) {
                for (int offsetY = 0; offsetY <= plan.height() - recipeHeight; offsetY++) {
                    for (int offsetX = 0; offsetX <= plan.width() - recipeWidth; offsetX++) {
                        if (System.nanoTime() > deadline) {
                            return false;
                        }
                        List<IngredientSlot> requirements = new ArrayList<>();
                        boolean fitsPlan = true;
                        for (int y = 0; y < recipeHeight && fitsPlan; y++) {
                            for (int x = 0; x < recipeWidth; x++) {
                                int sourceX = mirrored ? recipeWidth - 1 - x : x;
                                Ingredient ingredient = ingredients.get(y * recipeWidth + sourceX);
                                if (ingredient == null || ingredient.isEmpty()) {
                                    continue;
                                }
                                int gridSlot = (offsetY + y) * plan.width() + offsetX + x;
                                if (!plan.allows(gridSlot)) {
                                    fitsPlan = false;
                                    break;
                                }
                                requirements.add(new IngredientSlot(gridSlot, ingredient));
                            }
                        }
                        if (!fitsPlan || requirements.isEmpty()) {
                            continue;
                        }

                        CraftingInput input = findMatchingInput(recipe, plan, requirements, availableItems, deadline);
                        if (input != null && craftRecipe(recipe, input)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    private CraftingInput findMatchingInput(
            CraftingRecipe recipe,
            GridPlan plan,
            List<IngredientSlot> requirements,
            List<PoolEntry> availableItems,
            long deadline) {
        List<IngredientSlot> orderedRequirements = new ArrayList<>(requirements);
        orderedRequirements.sort(Comparator.comparingInt(slot -> countCompatiblePools(slot.ingredient(), availableItems)));
        for (IngredientSlot requirement : orderedRequirements) {
            if (countCompatiblePools(requirement.ingredient(), availableItems) == 0) {
                return null;
            }
        }

        NonNullList<ItemStack> grid = NonNullList.withSize(plan.width() * plan.height(), ItemStack.EMPTY);
        int[] remainingCounts = new int[availableItems.size()];
        for (int i = 0; i < availableItems.size(); i++) {
            remainingCounts[i] = availableItems.get(i).count();
        }
        InputHolder matchedInput = new InputHolder();
        if (assignIngredients(recipe, plan, orderedRequirements, availableItems,
                remainingCounts, grid, 0, matchedInput, new SearchBudget(deadline))) {
            return matchedInput.input;
        }
        return null;
    }

    private boolean assignIngredients(
            CraftingRecipe recipe,
            GridPlan plan,
            List<IngredientSlot> requirements,
            List<PoolEntry> availableItems,
            int[] remainingCounts,
            NonNullList<ItemStack> grid,
            int requirementIndex,
            InputHolder matchedInput,
            SearchBudget budget) {
        if (budget.exhausted()) {
            return false;
        }
        if (requirementIndex == requirements.size()) {
            NonNullList<ItemStack> gridCopy = NonNullList.withSize(grid.size(), ItemStack.EMPTY);
            for (int i = 0; i < grid.size(); i++) {
                gridCopy.set(i, grid.get(i).isEmpty() ? ItemStack.EMPTY : grid.get(i).copy());
            }
            CraftingInput candidate = CraftingInput.of(plan.width(), plan.height(), gridCopy);
            if (recipe.matches(candidate, level)) {
                matchedInput.input = candidate;
                return true;
            }
            return false;
        }

        IngredientSlot requirement = requirements.get(requirementIndex);
        for (int itemIndex = 0; itemIndex < availableItems.size(); itemIndex++) {
            PoolEntry item = availableItems.get(itemIndex);
            if (remainingCounts[itemIndex] <= 0 || !requirement.ingredient().test(item.stack())) {
                continue;
            }

            remainingCounts[itemIndex]--;
            grid.set(requirement.gridSlot(), item.stack().copyWithCount(1));
            if (assignIngredients(recipe, plan, requirements, availableItems,
                    remainingCounts, grid, requirementIndex + 1, matchedInput, budget)) {
                return true;
            }
            grid.set(requirement.gridSlot(), ItemStack.EMPTY);
            remainingCounts[itemIndex]++;
        }
        return false;
    }

    /** Cheap pre-filter: every required ingredient must exist in the input pools. */
    private boolean allIngredientsAvailable(List<Ingredient> ingredients, List<PoolEntry> availableItems) {
        for (Ingredient ingredient : ingredients) {
            if (ingredient == null || ingredient.isEmpty()) {
                continue;
            }
            boolean any = false;
            for (PoolEntry pool : availableItems) {
                if (pool.count() > 0 && ingredient.test(pool.stack())) {
                    any = true;
                    break;
                }
            }
            if (!any) {
                return false;
            }
        }
        return true;
    }

    private int countCompatiblePools(Ingredient ingredient, List<PoolEntry> availableItems) {
        int matches = 0;
        for (PoolEntry item : availableItems) {
            if (item.count() > 0 && ingredient.test(item.stack())) {
                matches++;
            }
        }
        return matches;
    }

    private boolean craftRecipe(CraftingRecipe recipe, CraftingInput input) {
        ItemStack result = recipe.assemble(input, level.registryAccess());
        if (result.isEmpty()) {
            return false;
        }

        List<ItemStack> products = new ArrayList<>();
        products.add(result.copy());
        NonNullList<ItemStack> remainingItems = level.getRecipeManager()
                .getRemainingItemsFor(RecipeType.CRAFTING, input, level);
        for (ItemStack remaining : remainingItems) {
            if (!remaining.isEmpty()) {
                products.add(remaining.copy());
            }
        }
        if (!canFitProducts(products)) {
            return false;
        }

        for (int gridSlot = 0; gridSlot < input.size(); gridSlot++) {
            ItemStack ingredient = input.getItem(gridSlot);
            if (!ingredient.isEmpty()) {
                consumeMatchingInput(ingredient, 1);
            }
        }
        for (ItemStack product : products) {
            insertIntoOutput(product.copy());
        }
        return true;
    }

    private List<PoolEntry> collectAvailableItems() {
        List<PoolEntry> available = new ArrayList<>();
        for (int slot = 0; slot < inputInventory.getSlots(); slot++) {
            ItemStack stack = inputInventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack sample = stack.copyWithCount(1);
            PoolEntry matchingEntry = null;
            for (PoolEntry entry : available) {
                if (ItemStack.isSameItemSameComponents(entry.stack(), sample)) {
                    matchingEntry = entry;
                    break;
                }
            }
            if (matchingEntry == null) {
                available.add(new PoolEntry(sample, stack.getCount()));
            } else {
                matchingEntry.addCount(stack.getCount());
            }
        }
        return available;
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
        if (!remaining.isEmpty()) {
            setChanged();
        }
    }

    /**
     * Drains finished products into an adjacent inventory (a chest and the like).
     * Only the OUTPUT side ever leaves the machine this way; inputs still arrive
     * through the machine's own slots.
     */
    private boolean pushOutputToAdjacent() {
        if (level == null || level.isClientSide) {
            return false;
        }
        boolean movedAnything = false;
        for (int slot = 0; slot < outputInventory.getSlots(); slot++) {
            ItemStack stack = outputInventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            for (Direction side : Direction.values()) {
                if (stack.isEmpty()) {
                    break;
                }
                IItemHandler target = level.getCapability(
                        Capabilities.ItemHandler.BLOCK, worldPosition.relative(side), side.getOpposite());
                if (target == null) {
                    continue;
                }
                for (int targetSlot = 0; targetSlot < target.getSlots() && !stack.isEmpty(); targetSlot++) {
                    if (!target.isItemValid(targetSlot, stack)) {
                        continue;
                    }
                    stack = target.insertItem(targetSlot, stack, false);
                }
            }
            if (!ItemStack.isSameItemSameComponents(stack, outputInventory.getStackInSlot(slot))
                    || stack.getCount() != outputInventory.getStackInSlot(slot).getCount()) {
                outputInventory.setStackInSlot(slot, stack);
                movedAnything = true;
            }
        }
        return movedAnything;
    }

    private List<GridPlan> getPlansForMode() {
        return switch (mode) {
            case HYBRID -> List.of(GridPlan.square(2), GridPlan.square(3));
            case HYBRID2 -> List.of(GridPlan.square(3), GridPlan.square(2));
            case SMALL -> List.of(GridPlan.square(2));
            case LARGE -> List.of(GridPlan.square(3));
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
        // The combined handler is deliberately exposed on every face so any
        // item pipe can insert into input slots and extract from output slots.
        return pipeView;
    }

    public void dropContents(int installedTier) {
        if (level == null || level.isClientSide) {
            return;
        }
        dropInventory(inputInventory);
        dropInventory(outputInventory);
        if (installedTier > 0) {
            ItemStack upgrade = new ItemStack(ModContent.getUpgradeItem(installedTier));
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), upgrade);
        }
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

    private record GridPlan(int width, int height, int[] allowedSlots) {
        private static GridPlan square(int size) {
            int[] slots = new int[size * size];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = i;
            }
            return new GridPlan(size, size, slots);
        }

        private boolean allows(int slot) {
            for (int allowedSlot : allowedSlots) {
                if (allowedSlot == slot) {
                    return true;
                }
            }
            return false;
        }
    }

    private record IngredientSlot(int gridSlot, Ingredient ingredient) {}

    /** Caps backtracking so a saturated pool can never explode into a long freeze. */
    private static final class SearchBudget {
        private final long deadline;
        private int nodes;

        private SearchBudget(long deadline) {
            this.deadline = deadline;
            this.nodes = SEARCH_NODES;
        }

        private boolean exhausted() {
            return nodes-- <= 0 || System.nanoTime() > deadline;
        }
    }

    private static final class PoolEntry {
        private final ItemStack stack;
        private int count;

        private PoolEntry(ItemStack stack, int count) {
            this.stack = stack;
            this.count = count;
        }

        private ItemStack stack() {
            return stack;
        }

        private int count() {
            return count;
        }

        private void addCount(int additional) {
            count += additional;
        }
    }

    private static final class InputHolder {
        private CraftingInput input;
    }

    private final class CombinedItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return INPUT_SLOTS + OUTPUT_SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot < INPUT_SLOTS) {
                return inputInventory.getStackInSlot(slot);
            }
            return outputInventory.getStackInSlot(slot - INPUT_SLOTS);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot < 0 || slot >= INPUT_SLOTS) {
                return stack;
            }
            return inputInventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < INPUT_SLOTS || slot >= INPUT_SLOTS + OUTPUT_SLOTS) {
                return ItemStack.EMPTY;
            }
            return outputInventory.extractItem(slot - INPUT_SLOTS, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot < 0 || slot >= INPUT_SLOTS + OUTPUT_SLOTS) {
                return 0;
            }
            return slot < INPUT_SLOTS
                    ? inputInventory.getSlotLimit(slot)
                    : outputInventory.getSlotLimit(slot - INPUT_SLOTS);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < INPUT_SLOTS && inputInventory.isItemValid(slot, stack);
        }
    }
}
