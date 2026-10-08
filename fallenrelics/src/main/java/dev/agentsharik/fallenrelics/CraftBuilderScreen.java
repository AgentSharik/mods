package dev.agentsharik.fallenrelics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CraftBuilderScreen extends AbstractContainerScreen<CraftBuilderMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/craft_builder.png");

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
                leftPos + 124, topPos + 76, 69, 18, modeLabel(), 0));
        addRenderableWidget(new ActionWidget(
                leftPos + 76, topPos + 7, 48, 18,
                Component.translatable("gui.fallenrelics.craft_builder.add"), 1));
        addRenderableWidget(new ActionWidget(
                leftPos + 127, topPos + 7, 50, 18,
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
        graphics.drawString(font, title, 8, 7, 0xFFE0E0E0, false);
        graphics.renderItem(new ItemStack(Items.CRAFTING_TABLE), 181, 6);
        Component ingredientsLabel = Component.translatable("gui.fallenrelics.craft_builder.ingredients");
        graphics.drawString(font, ingredientsLabel, 41 - font.width(ingredientsLabel) / 2, 19, 0xFF9F9F9F, false);
        Component resultLabel = Component.translatable("gui.fallenrelics.craft_builder.result");
        graphics.drawString(font, resultLabel, 173 - font.width(resultLabel) / 2, 31, 0xFF9F9F9F, false);
        graphics.drawString(font, Component.translatable("container.inventory"), 19, 88, 0xFFE0E0E0, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private final class ActionWidget extends AbstractWidget {
        private final int menuButtonId;

        private ActionWidget(int x, int y, int width, int height, Component label, int menuButtonId) {
            super(x, y, width, height, label);
            this.menuButtonId = menuButtonId;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = isHoveredOrFocused();
            boolean actionButton = menuButtonId != 0;
            int background = actionButton
                    ? (!active ? 0xFF777777 : hovered ? 0xFFE0E0E0 : 0xFFC6C6C6)
                    : (!active ? 0xFF25242B : hovered ? 0xFF47434C : 0xFF302E37);
            int edge = actionButton
                    ? (!active ? 0xFF888888 : hovered ? 0xFFFFFFFF : 0xFFEAEAEA)
                    : (!active ? 0xFF4D4A54 : hovered ? 0xFFD0AC68 : 0xFF81705A);
            int shadow = actionButton ? 0xFF343434 : 0xFF17171D;
            graphics.fill(getX(), getY(), getX() + width, getY() + height, background);
            graphics.fill(getX(), getY(), getX() + width, getY() + 1, edge);
            graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, shadow);
            graphics.fill(getX(), getY(), getX() + 1, getY() + height, edge);
            graphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, shadow);

            int textColor = actionButton
                    ? (!active ? 0xFF4A4A4A : 0xFF202020)
                    : (!active ? 0xFF77747D : hovered ? 0xFFFFE4A5 : 0xFFE1D5BC);
            if (menuButtonId == 0) {
                boolean shaped = !menu.isShapeless();
                int switchColor = shaped ? 0xFFB38D52 : 0xFF5D5964;
                graphics.fill(getX() + 6, getY() + 6, getX() + 23, getY() + 14, switchColor);
                graphics.fill(shaped ? getX() + 15 : getX() + 7, getY() + 7,
                        shaped ? getX() + 22 : getX() + 14, getY() + 13,
                        shaped ? 0xFFFFE3A0 : 0xFFC4C0C9);
                graphics.drawCenteredString(font, getMessage(), getX() + 47,
                        getY() + (height - 8) / 2, textColor);
            } else {
                graphics.drawCenteredString(font, getMessage(), getX() + width / 2,
                        getY() + (height - 8) / 2, textColor);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            sendMenuButton(menuButtonId);
        }
    }
}
