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
                    .map(clickable -> clickable.getIngredient())
                    .map(JeiLinkerJeiCalls::flattenIngredient)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        } catch (LinkageError | RuntimeException missing) {
            return null;
        }
    }

    /** Groups (Collapsible Groups etc.) are not stacks: take the first stack inside them. */
    @org.jetbrains.annotations.Nullable
    private static ItemStack flattenIngredient(Object ingredient) {
        return firstStack(ingredient, 0);
    }

    /** Groups hide their stacks in fields of fields: walk two levels deep. */
    @org.jetbrains.annotations.Nullable
    private static ItemStack firstStack(Object node, int depth) {
        if (node == null || depth > 2) {
            return null;
        }
        if (node instanceof ItemStack stack) {
            return stack.isEmpty() ? null : stack;
        }
        if (node instanceof net.minecraft.world.item.Item item) {
            return new ItemStack(item);
        }
        if (node instanceof Iterable<?> iterable) {
            for (Object element : iterable) {
                ItemStack found = firstStack(element, depth + 1);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }
        if (node instanceof Object[] array) {
            for (Object element : array) {
                ItemStack found = firstStack(element, depth + 1);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }
        if (node instanceof String || node instanceof Number || node instanceof Boolean) {
            return null;
        }
        try {
            Class<?> clazz = node.getClass();
            while (clazz != null && clazz != Object.class) {
                for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                    if (field.getType().isPrimitive() || field.isSynthetic()) {
                        continue;
                    }
                    field.setAccessible(true);
                    ItemStack found = firstStack(field.get(node), depth + 1);
                    if (found != null) {
                        return found;
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (LinkageError | RuntimeException | ReflectiveOperationException ignored) {
            // unknown ingredient shape: no link
        }
        return null;
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
