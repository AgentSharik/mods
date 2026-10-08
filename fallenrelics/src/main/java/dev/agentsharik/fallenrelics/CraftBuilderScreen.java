package dev.agentsharik.fallenrelics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class CraftBuilderScreen extends AbstractContainerScreen<CraftBuilderMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/craft_builder.png");

    private Button modeButton;

    public CraftBuilderScreen(CraftBuilderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 220;
        imageHeight = 226;
    }

    @Override
    protected void init() {
        super.init();
        modeButton = addRenderableWidget(Button.builder(modeLabel(), button -> sendMenuButton(0))
                .bounds(leftPos + 7, topPos + 105, 94, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.fallenrelics.craft_builder.add"),
                        button -> sendMenuButton(1))
                .bounds(leftPos + 104, topPos + 105, 53, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.fallenrelics.craft_builder.remove"),
                        button -> sendMenuButton(2))
                .bounds(leftPos + 161, topPos + 105, 52, 20)
                .build());
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
        graphics.drawString(font, title, 10, 7, 0xFFF2E9, false);
        graphics.drawString(font, Component.translatable("gui.fallenrelics.craft_builder.ingredients"),
                10, 19, 0xE6D7F4, false);
        graphics.drawString(font, Component.translatable("gui.fallenrelics.craft_builder.result"),
                178, 29, 0xE6D7F4, false);
        graphics.drawString(font, Component.translatable("container.inventory"), 10, 132, 0xFFF2E9, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
