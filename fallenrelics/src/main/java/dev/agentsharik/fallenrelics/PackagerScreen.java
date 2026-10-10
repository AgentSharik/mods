package dev.agentsharik.fallenrelics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The packager recipe editor window: the bundled Colourful Containers crafting
 * table art drawn 1:1, no block caption, and the same left column of border-less
 * icon buttons as the CraftBuilder: check = install the shown craft, cross = back
 * to automatic recipe scanning, dots = cycle the auto-scan mode.
 */
public final class PackagerScreen extends AbstractContainerScreen<PackagerMenu> {
    private static final ResourceLocation PACK_CRAFTING_TABLE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");

    private static final int LABEL_COLOR = 0xFF404040;
    private static final int CHECK_COLOR = 0xFF55C955;
    private static final int CHECK_SHADOW = 0xFF2E6B2E;
    private static final int CROSS_COLOR = 0xFFD14949;
    private static final int CROSS_SHADOW = 0xFF6B2430;
    private static final int DOTS_COLOR = 0xFFE8B820;
    private static final int DOTS_SHADOW = 0xFF8A5A10;
    private static final int HOVER_COLOR = 0x33FFFFFF;

    private int cleanTicks;

    public PackagerScreen(PackagerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new IconButton(leftPos + 15, topPos + 20, 0));
        addRenderableWidget(new IconButton(leftPos + 15, topPos + 36, 1));
        addRenderableWidget(new IconButton(leftPos + 15, topPos + 52, 2));
    }

    private void sendMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (cleanTicks < 60) {
            cleanTicks++;
            ClientScreenCleaner.removeForeignWidgets(this);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(PACK_CRAFTING_TABLE, leftPos, topPos, 0, 0f, 0f,
                imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("container.inventory"),
                8, imageHeight - 96, LABEL_COLOR, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Border-less icon button, same footprint and shadows as the CraftBuilder ones. */
    private final class IconButton extends AbstractWidget {
        private final int menuButtonId;

        private IconButton(int x, int y, int menuButtonId) {
            super(x, y, 12, 12, Component.empty());
            this.menuButtonId = menuButtonId;
            setTooltip(Tooltip.create(label()));
        }

        private Component label() {
            return switch (menuButtonId) {
                case 1 -> Component.translatable("gui.fallenrelics.packager.apply");
                case 2 -> Component.translatable("gui.fallenrelics.packager.clear");
                default -> Component.translatable("gui.fallenrelics.packager.mode");
            };
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (isHoveredOrFocused()) {
                graphics.fill(getX(), getY(), getX() + width, getY() + height, HOVER_COLOR);
            }
            int ox = getX();
            int oy = getY();
            switch (menuButtonId) {
                case 1 -> drawCheck(graphics, ox, oy);
                case 2 -> drawCross(graphics, ox, oy);
                default -> drawDots(graphics, ox, oy);
            }
        }

        private void drawCheck(GuiGraphics graphics, int ox, int oy) {
            int[][] pixels = {
                    {0, 6}, {1, 7}, {2, 8}, {3, 9}, {4, 9}, {5, 8}, {6, 7}, {7, 6}, {8, 5}, {9, 4}, {10, 3}
            };
            for (int[] pixel : pixels) {
                graphics.fill(ox + pixel[0] + 1, oy + pixel[1] + 1, ox + pixel[0] + 3, oy + pixel[1] + 3, CHECK_SHADOW);
            }
            for (int[] pixel : pixels) {
                graphics.fill(ox + pixel[0], oy + pixel[1], ox + pixel[0] + 2, oy + pixel[1] + 2, CHECK_COLOR);
            }
        }

        private void drawCross(GuiGraphics graphics, int ox, int oy) {
            for (int i = 0; i < 9; i++) {
                graphics.fill(ox + 2 + i, oy + 2 + i, ox + 4 + i, oy + 4 + i, CROSS_SHADOW);
                graphics.fill(ox + 10 - i, oy + 2 + i, ox + 12 - i, oy + 4 + i, CROSS_SHADOW);
            }
            for (int i = 0; i < 9; i++) {
                graphics.fill(ox + 1 + i, oy + 1 + i, ox + 3 + i, oy + 3 + i, CROSS_COLOR);
                graphics.fill(ox + 9 - i, oy + 1 + i, ox + 11 - i, oy + 3 + i, CROSS_COLOR);
            }
        }

        private void drawDots(GuiGraphics graphics, int ox, int oy) {
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    graphics.fill(ox + 2 + column * 4, oy + 2 + row * 4,
                            ox + 4 + column * 4, oy + 4 + row * 4, DOTS_SHADOW);
                }
            }
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    graphics.fill(ox + 1 + column * 4, oy + 1 + row * 4,
                            ox + 3 + column * 4, oy + 3 + row * 4, DOTS_COLOR);
                }
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            sendMenuButton(menuButtonId);
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narration) {
            defaultButtonNarrationText(narration);
        }
    }
}
