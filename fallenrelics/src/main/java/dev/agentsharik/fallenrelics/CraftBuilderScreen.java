package dev.agentsharik.fallenrelics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The window is the bundled Colourful Containers (Kingybu) pack art, drawn
 * 1:1 with no custom chrome: shaped mode shows the pack's crafting table GUI,
 * shapeless mode shows the pack's furnace GUI. No block title is drawn.
 * The three action buttons (add / remove / mode) live in a vertical column
 * right of the window, icon-only, like the vanilla recipe-book button spot.
 */
public final class CraftBuilderScreen extends AbstractContainerScreen<CraftBuilderMenu> {
    /** Bundled Colourful Containers (Kingybu) overrides of the vanilla textures. */
    private static final ResourceLocation PACK_CRAFTING_TABLE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");
    private static final ResourceLocation PACK_FURNACE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    /** Vanilla slot frame, drawn for the 3x3 grid on the furnace background. */
    private static final ResourceLocation SLOT_SPRITE =
            ResourceLocation.withDefaultNamespace("textures/gui/sprites/container/slot.png");
    private static final ResourceLocation WIDGET_BUTTON =
            ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/button.png");
    private static final ResourceLocation WIDGET_BUTTON_HIGHLIGHTED =
            ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/button_highlighted.png");
    /** Mode pictograms: the machine the current mode will switch to. */
    private static final ResourceLocation FURNACE_ICON =
            ResourceLocation.withDefaultNamespace("textures/block/furnace_front.png");
    private static final ResourceLocation CRAFTING_TABLE_ICON =
            ResourceLocation.withDefaultNamespace("textures/block/crafting_table_front.png");

    /** Vanilla widget nine-slice border, see button.png.mcmeta. */
    private static final int WIDGET_BORDER = 3;
    private static final int WIDGET_WIDTH = 200;
    private static final int WIDGET_HEIGHT = 20;

    /** 3x3 grid position, identical to the vanilla crafting table layout. */
    private static final int GRID_X = 30;
    private static final int GRID_Y = 17;

    private static final int CHECK_COLOR = 0xFF55C955;
    private static final int CROSS_COLOR = 0xFFD14949;

    private IconButton modeButton;

    public CraftBuilderScreen(CraftBuilderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int buttonX = leftPos + imageWidth + 4;
        int columnTop = topPos + (imageHeight - (20 * 3 + 4 * 2)) / 2;
        addRenderableWidget(new IconButton(buttonX, columnTop, 1));
        addRenderableWidget(new IconButton(buttonX, columnTop + 24, 2));
        modeButton = addRenderableWidget(new IconButton(buttonX, columnTop + 48, 0));
        refreshModeButton();
    }

    private void refreshModeButton() {
        if (modeButton == null) {
            return;
        }
        modeButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable(
                menu.isShapeless()
                        ? "gui.fallenrelics.craft_builder.to_shaped"
                        : "gui.fallenrelics.craft_builder.to_shapeless")));
    }

    private void sendMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshModeButton();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (menu.isShapeless()) {
            graphics.blit(PACK_FURNACE, leftPos, topPos, 0, 0f, 0f, imageWidth, imageHeight, 256, 256);
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    graphics.blit(SLOT_SPRITE, leftPos + GRID_X + column * 18, topPos + GRID_Y + row * 18,
                            0, 0f, 0f, 18, 18, 18, 18);
                }
            }
        } else {
            graphics.blit(PACK_CRAFTING_TABLE, leftPos, topPos, 0, 0f, 0f, imageWidth, imageHeight, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Deliberately empty: the window carries no block caption at all.
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Nine-slice blit of the vanilla widget texture, identical to gui scaling "nine_slice" border 3. */
    private static void blitWidget(GuiGraphics graphics, ResourceLocation texture,
                                   int x, int y, int width, int height) {
        int b = WIDGET_BORDER;
        int tw = WIDGET_WIDTH;
        int th = WIDGET_HEIGHT;
        graphics.blit(texture, x, y, 0, 0, b, b, tw, th);
        graphics.blit(texture, x + width - b, y, tw - b, 0, b, b, tw, th);
        graphics.blit(texture, x, y + height - b, 0, th - b, b, b, tw, th);
        graphics.blit(texture, x + width - b, y + height - b, tw - b, th - b, b, b, tw, th);
        graphics.blit(texture, x + b, y, width - 2 * b, b, b, 0, tw - 2 * b, b, tw, th);
        graphics.blit(texture, x + b, y + height - b, width - 2 * b, b, b, th - b, tw - 2 * b, b, tw, th);
        graphics.blit(texture, x, y + b, b, height - 2 * b, 0, b, b, th - 2 * b, tw, th);
        graphics.blit(texture, x + width - b, y + b, b, height - 2 * b, tw - b, b, b, th - 2 * b, tw, th);
        graphics.blit(texture, x + b, y + b, width - 2 * b, height - 2 * b, b, b, tw - 2 * b, th - 2 * b, tw, th);
    }

    /** Square icon-only button: checkmark = save recipe, cross = remove, pictogram = mode. */
    private final class IconButton extends AbstractWidget {
        private final int menuButtonId;

        private IconButton(int x, int y, int menuButtonId) {
            super(x, y, 20, 20, Component.empty());
            this.menuButtonId = menuButtonId;
            setTooltip(net.minecraft.client.gui.components.Tooltip.create(label()));
        }

        private Component label() {
            return switch (menuButtonId) {
                case 1 -> Component.translatable("gui.fallenrelics.craft_builder.add");
                case 2 -> Component.translatable("gui.fallenrelics.craft_builder.remove");
                default -> Component.translatable(menu.isShapeless()
                        ? "gui.fallenrelics.craft_builder.mode_shapeless"
                        : "gui.fallenrelics.craft_builder.mode_shaped");
            };
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation sprite = isHoveredOrFocused() ? WIDGET_BUTTON_HIGHLIGHTED : WIDGET_BUTTON;
            blitWidget(graphics, sprite, getX(), getY(), width, height);
            int ox = getX() + 2;
            int oy = getY() + 2;
            switch (menuButtonId) {
                case 1 -> drawCheck(graphics, ox, oy);
                case 2 -> drawCross(graphics, ox, oy);
                default -> graphics.blit(menu.isShapeless() ? CRAFTING_TABLE_ICON : FURNACE_ICON,
                        ox, oy, 0, 0f, 0f, 16, 16, 16, 16);
            }
        }

        private void drawCheck(GuiGraphics graphics, int ox, int oy) {
            int[][] pixels = {
                    {2, 8}, {3, 9}, {4, 10}, {5, 11}, {6, 10}, {7, 9},
                    {8, 8}, {9, 7}, {10, 6}, {11, 5}, {12, 4}
            };
            for (int[] pixel : pixels) {
                graphics.fill(ox + pixel[0], oy + pixel[1], ox + pixel[0] + 2, oy + pixel[1] + 2, CHECK_COLOR);
            }
        }

        private void drawCross(GuiGraphics graphics, int ox, int oy) {
            for (int i = 0; i < 10; i++) {
                graphics.fill(ox + 3 + i, oy + 3 + i, ox + 5 + i, oy + 5 + i, CROSS_COLOR);
                graphics.fill(ox + 12 - i, oy + 3 + i, ox + 14 - i, oy + 5 + i, CROSS_COLOR);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            sendMenuButton(menuButtonId);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            defaultButtonNarrationText(narrationElementOutput);
        }
    }
}
