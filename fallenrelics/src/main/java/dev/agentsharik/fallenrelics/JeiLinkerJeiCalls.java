package dev.agentsharik.fallenrelics;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Every direct JEI call lives here so the rest of the mod never loads JEI classes. */
final class JeiLinkerJeiCalls {
    private JeiLinkerJeiCalls() {}

    static boolean available() {
        try {
            return JeiLinkerPlugin.runtime() != null;
        } catch (LinkageError | RuntimeException missing) {
            return false; // JEI not installed: item linking still works
        }
    }

    /** Any JEI-rendered ingredient under the mouse: ingredient list, recipes gui, bookmarks. */
    @org.jetbrains.annotations.Nullable
    static ItemStack ingredientUnderMouseAnywhere(net.minecraft.client.gui.screens.Screen screen, double mouseX, double mouseY) {
        IJeiRuntime runtime = JeiLinkerPlugin.runtime();
        if (runtime == null || screen == null) {
            return null;
        }
        try {
            return runtime.getScreenHelper()
                    .getClickableIngredientUnderMouse(screen, mouseX, mouseY)
                    .map(meazz -> meazz.getIngredient())
                    .filter(ItemStack.class::isInstance)
                    .map(ItemStack.class::cast)
                    .filter(stack -> !stack.isEmpty())
                    .findFirst()
                    .orElse(null);
        } catch (LinkageError | RuntimeException missing) {
            return null;
        }
    }

    /** The item stack under the mouse in JEI's ingredient list, if it is shown. */
    @Nullable
    static ItemStack itemStackUnderMouse() {
        IJeiRuntime runtime = JeiLinkerPlugin.runtime();
        if (runtime == null) {
            return null;
        }
        IIngredientListOverlay overlay = runtime.getIngredientListOverlay();
        if (!overlay.isListDisplayed() || overlay.hasKeyboardFocus()) {
            return null;
        }
        return overlay.getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
    }

    /** On-click behaviour of the chat link: the same view the R key opens. */
    static void showRecipes(ItemStack stack) {
        IJeiRuntime runtime = JeiLinkerPlugin.runtime();
        if (runtime == null) {
            return;
        }
        IFocus<ItemStack> focus = runtime.getJeiHelpers().getFocusFactory()
                .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack.copy());
        runtime.getRecipesGui().show(focus);
    }
}
