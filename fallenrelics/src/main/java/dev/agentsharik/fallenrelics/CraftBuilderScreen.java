package dev.agentsharik.fallenrelics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The window is the bundled Colourful Containers (Kingybu) pack art drawn 1:1,
 * exactly like the vanilla screens: shaped mode is the crafting table GUI,
 * shapeless mode is the furnace GUI (its printed input slots and flame are
 * covered by a wall patch and the 3x3 editor grid is drawn in their place).
 * No block caption is drawn. The three actions (add / remove / mode) are
 * border-less icon buttons in a column right of the window, mirroring the
 * vanilla recipe-book button spot.
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
    /** Mode pictograms: the machine the current mode will switch to. */
    private static final ResourceLocation FURNACE_ICON =
            ResourceLocation.withDefaultNamespace("textures/block/furnace_front.png");
    private static final ResourceLocation CRAFTING_TABLE_ICON =
            ResourceLocation.withDefaultNamespace("textures/block/crafting_table_front.png");

    /** 3x3 grid position, identical to the vanilla crafting table layout. */
    private static final int GRID_X = 30;
    private static final int GRID_Y = 17;
    /** Furnace wall area (right of the output frame) copied over the printed input slots. */
    private static final int PATCH_SRC_X = 146;
    private static final int PATCH_SRC_Y = 15;
    private static final int PATCH_WIDTH = 22;
    private static final int PATCH_HEIGHT = 58;
    private static final int PATCH_DST_X = 54;
    private static final int PATCH_DST_Y = 15;

    private static final int CHECK_COLOR = 0xFF55C955;
    private static final int CROSS_COLOR = 0xFFD14949;
    private static final int HOVER_COLOR = 0x33FFFFFF;

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
        modeButton.setTooltip(Tooltip.create(Component.translatable(
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
            // Cover the printed ingredient/fuel slots and flame with clean wall.
            graphics.blit(PACK_FURNACE, leftPos + PATCH_DST_X, topPos + PATCH_DST_Y, 0,
                    PATCH_SRC_X, PATCH_SRC_Y, PATCH_WIDTH, PATCH_HEIGHT, 256, 256);
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

    /** Border-less icon button: checkmark = save, cross = remove, pictogram = mode. */
    private final class IconButton extends AbstractWidget {
        private final int menuButtonId;

        private IconButton(int x, int y, int menuButtonId) {
            super(x, y, 20, 20, Component.empty());
            this.menuButtonId = menuButtonId;
            setTooltip(Tooltip.create(label()));
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
            if (isHoveredOrFocused()) {
                graphics.fill(getX(), getY(), getX() + width, getY() + height, HOVER_COLOR);
            }
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
