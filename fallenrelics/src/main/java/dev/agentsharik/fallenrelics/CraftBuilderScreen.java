package dev.agentsharik.fallenrelics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The screen no longer carries its own baked GUI art: the whole Colourful
 * Containers texture pack is bundled with the mod, so the background is
 * composed at render time from the pack's crafting table texture (wooden
 * header on top, grey panel below) exactly like the pack draws it, with a
 * row of widget buttons in between. Future Fallen Relics windows should be
 * composed from the bundled pack textures the same way.
 */
public final class CraftBuilderScreen extends AbstractContainerScreen<CraftBuilderMenu> {
    /** Bundled Colourful Containers (Kingybu) override of the vanilla texture. */
    private static final ResourceLocation PACK_CRAFTING_TABLE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");
    private static final ResourceLocation WIDGET_BUTTON =
            ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/button.png");
    private static final ResourceLocation WIDGET_BUTTON_HIGHLIGHTED =
            ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/button_highlighted.png");
    private static final ResourceLocation WIDGET_BUTTON_DISABLED =
            ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/button_disabled.png");

    /** Vanilla widget nine-slice border, see button.png.mcmeta. */
    private static final int WIDGET_BORDER = 3;
    private static final int WIDGET_WIDTH = 200;
    private static final int WIDGET_HEIGHT = 20;

    /** Height of the wooden header copied from the pack texture. */
    private static final int HEADER_HEIGHT = 76;
    /** Vertical position of a plain panel row inside the pack texture. */
    private static final int PANEL_ROW = 80;
    /** First row of the pack's lower panel. */
    private static final int LOWER_TOP = 76;
    private static final int LOWER_HEIGHT = 90;

    /** Vanilla container label grey for the light lower panel. */
    private static final int LABEL_COLOR = 0xFF404040;

    private ActionWidget modeButton;

    public CraftBuilderScreen(CraftBuilderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = HEADER_HEIGHT + 24 + LOWER_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        modeButton = addRenderableWidget(new ActionWidget(
                leftPos + 100, topPos + 78, 68, 20, modeLabel(), 0));
        addRenderableWidget(new ActionWidget(
                leftPos + 8, topPos + 78, 44, 20,
                Component.translatable("gui.fallenrelics.craft_builder.add"), 1));
        addRenderableWidget(new ActionWidget(
                leftPos + 56, topPos + 78, 40, 20,
                Component.translatable("gui.fallenrelics.craft_builder.remove"), 2));
    }

    private Component modeLabel() {
        return Component.translatable(menu.isShapeless()
                ? "gui.fallenrelics.craft_builder.mode_shapeless"
                : "gui.fallenrelics.craft_builder.mode_shaped");
    }

    private void sendMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (modeButton != null) {
            modeButton.setMessage(modeLabel());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Wooden header, 1:1 from the bundled pack texture.
        graphics.blit(PACK_CRAFTING_TABLE, leftPos, topPos, 0, 0f, 0f,
                imageWidth, HEADER_HEIGHT, 256, 256);
        // Button band: one plain panel row stretched to 24 px.
        graphics.blit(PACK_CRAFTING_TABLE, leftPos, topPos + HEADER_HEIGHT, 0, 0f, PANEL_ROW,
                imageWidth, 24, 256, 256);
        // Lower inventory panel, shifted below the button row.
        graphics.blit(PACK_CRAFTING_TABLE, leftPos, topPos + HEADER_HEIGHT + 24, 0, 0f, LOWER_TOP,
                imageWidth, LOWER_HEIGHT, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Dark plank-brown reads well on the wooden header, like the pack's own titles.
        graphics.drawString(font, title, 8, 6, 0xFF3A2008, false);
        graphics.drawString(font, Component.translatable("container.inventory"), 8, 100, LABEL_COLOR, false);
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

    private final class ActionWidget extends AbstractWidget {
        private final int menuButtonId;

        private ActionWidget(int x, int y, int width, int height, Component label, int menuButtonId) {
            super(x, y, width, height, label);
            this.menuButtonId = menuButtonId;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation sprite = !active
                    ? WIDGET_BUTTON_DISABLED
                    : isHoveredOrFocused() ? WIDGET_BUTTON_HIGHLIGHTED : WIDGET_BUTTON;
            blitWidget(graphics, sprite, getX(), getY(), width, height);
            int textColor = !active ? 0xFFA0A0A0 : isHoveredOrFocused() ? 0xFFFFA0 : 0xFFFFFFFF;
            if (menuButtonId == 0) {
                // A tiny pictogram left of the label: a 3x3 grid for shaped,
                // scattered dots for shapeless.
                boolean shaped = !menu.isShapeless();
                int gx = getX() + 6;
                int gy = getY() + (height - 9) / 2;
                if (shaped) {
                    for (int row = 0; row < 3; row++) {
                        for (int column = 0; column < 3; column++) {
                            graphics.fill(gx + column * 3, gy + row * 3,
                                    gx + column * 3 + 2, gy + row * 3 + 2, textColor);
                        }
                    }
                } else {
                    graphics.fill(gx, gy + 4, gx + 2, gy + 6, textColor);
                    graphics.fill(gx + 3, gy, gx + 5, gy + 2, textColor);
                    graphics.fill(gx + 6, gy + 5, gx + 8, gy + 7, textColor);
                    graphics.fill(gx + 5, gy + 2, gx + 7, gy + 4, textColor);
                }
                int textX = getX() + 18 + (width - 18 - font.width(getMessage())) / 2;
                graphics.drawString(font, getMessage(), textX, getY() + (height - 8) / 2, textColor, true);
            } else {
                int textX = getX() + (width - font.width(getMessage())) / 2;
                graphics.drawString(font, getMessage(), textX, getY() + (height - 8) / 2, textColor, true);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            sendMenuButton(menuButtonId);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }
    }
}
