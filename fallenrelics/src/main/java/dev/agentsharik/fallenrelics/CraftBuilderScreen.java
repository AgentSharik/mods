package dev.agentsharik.fallenrelics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Renders exactly like a vanilla Minecraft container screen: the same
 * #C6C6C6 panel, the same recessed slots and the same widget buttons,
 * matching the reference screenshots the user provided.
 */
public final class CraftBuilderScreen extends AbstractContainerScreen<CraftBuilderMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/craft_builder.png");
    private static final ResourceLocation WIDGET_BUTTON =
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/widget/button.png");
    private static final ResourceLocation WIDGET_BUTTON_HIGHLIGHTED =
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/widget/button_highlighted.png");
    private static final ResourceLocation WIDGET_BUTTON_DISABLED =
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/widget/button_disabled.png");

    /** Vanilla widget nine-slice border, see button.png.mcmeta. */
    private static final int WIDGET_BORDER = 3;
    private static final int WIDGET_WIDTH = 200;
    private static final int WIDGET_HEIGHT = 20;

    /** Vanilla container label grey. */
    private static final int LABEL_COLOR = 0xFF404040;

    private ActionWidget modeButton;

    public CraftBuilderScreen(CraftBuilderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 200;
        imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init();
        modeButton = addRenderableWidget(new ActionWidget(
                leftPos + 124, topPos + 76, 69, 20, modeLabel(), 0));
        addRenderableWidget(new ActionWidget(
                leftPos + 92, topPos + 6, 50, 20,
                Component.translatable("gui.fallenrelics.craft_builder.add"), 1));
        addRenderableWidget(new ActionWidget(
                leftPos + 144, topPos + 6, 50, 20,
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
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 7, LABEL_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.fallenrelics.craft_builder.ingredients"),
                14, 19, LABEL_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.fallenrelics.craft_builder.result"),
                160, 33, LABEL_COLOR, false);
        graphics.drawString(font, Component.translatable("container.inventory"), 8, 88, LABEL_COLOR, false);
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
            int textX = getX() + (width - font.width(getMessage())) / 2;
            graphics.drawString(font, getMessage(), textX, getY() + (height - 8) / 2, textColor, true);
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
