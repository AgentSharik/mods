package dev.agentsharik.angelring.client;

import dev.agentsharik.angelring.AngelRingMod;
import dev.agentsharik.angelring.ModComponents;
import dev.agentsharik.angelring.ModItems;
import dev.agentsharik.angelring.RelicData;
import dev.agentsharik.angelring.RingLookup;
import dev.agentsharik.angelring.network.UpgradePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Окно кольца: карточка, описание, статы, шкала опыта и два улучшения.
 * Фон рисуется из textures/gui/relic_screen.png (256x256, окно занимает 256x176).
 * Макет (числа ниже) должен совпадать с картинкой.
 */
public class RelicScreen extends Screen {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, "textures/gui/relic_screen.png");

    private static final int W = 256;
    private static final int H = 176;

    // макет
    private static final int CARD_X = 12, CARD_Y = 12, CARD_W = 48, CARD_H = 64;
    private static final int INFO_X = 68, INFO_Y = 12, INFO_W = 124;
    private static final int STAT_X = 198, STAT_Y = 12, STAT_STEP = 22, STAT_W = 46, STAT_H = 18;
    private static final int BAR_X = 16, BAR_Y = 86, BAR_W = 224, BAR_H = 12;
    private static final int UP_Y = 112, UP_W = 100, UP_H = 46, UP1_X = 22, UP2_X = 134;

    private static final String[] CHIP_TIPS = {
            "gui.angelring.points", "gui.angelring.xp", "gui.angelring.flight_bonus"
    };

    private int left;
    private int top;
    private Button speedButton;
    private Button masteryButton;

    public RelicScreen() {
        super(Component.translatable("item.angelring.angel_ring"));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        speedButton = addRenderableWidget(Button.builder(Component.translatable("gui.angelring.upgrade"),
                        b -> send(RelicData.STAT_SPEED))
                .bounds(left + UP1_X + 6, top + UP_Y + 31, UP_W - 12, 12).build());
        masteryButton = addRenderableWidget(Button.builder(Component.translatable("gui.angelring.upgrade"),
                        b -> send(RelicData.STAT_MASTERY))
                .bounds(left + UP2_X + 6, top + UP_Y + 31, UP_W - 12, 12).build());
    }

    private void send(int stat) {
        PacketDistributor.sendToServer(new UpgradePayload(stat));
    }

    private RelicData data() {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return RelicData.EMPTY;
        }
        return RingLookup.find(player)
                .map(s -> s.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY))
                .orElse(RelicData.EMPTY);
    }

    @Override
    public void tick() {
        super.tick();
        LocalPlayer player = minecraft.player;
        if (player == null || RingLookup.find(player).isEmpty()) {
            onClose(); // кольцо сняли — закрываем окно
            return;
        }
        RelicData d = data();
        speedButton.active = d.points() > 0 && d.speed() < RelicData.MAX_STAT;
        masteryButton.active = d.points() > 0 && d.mastery() < RelicData.MAX_STAT;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Весь «неинтерактивный» рисунок окна — под кнопками, поэтому рисуем его в renderBackground. */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        g.blit(BG, left, top, 0, 0, W, H);
        drawContent(g, data());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        for (int i = 0; i < CHIP_TIPS.length; i++) {
            int x = left + STAT_X;
            int y = top + STAT_Y + i * STAT_STEP;
            if (mouseX >= x && mouseX < x + STAT_W && mouseY >= y && mouseY < y + STAT_H) {
                g.renderTooltip(font, Component.translatable(CHIP_TIPS[i]), mouseX, mouseY);
            }
        }
    }

    private void drawContent(GuiGraphics g, RelicData d) {
        // значок уровня над карточкой
        centered(g, String.valueOf(d.level()), left + CARD_X + CARD_W / 2, top + 9, 0x3A2200, false);

        // большая иконка кольца в карточке
        g.pose().pushPose();
        g.pose().translate(left + CARD_X + CARD_W / 2f - 16f, top + CARD_Y + CARD_H / 2f - 16f, 0f);
        g.pose().scale(2f, 2f, 1f);
        g.renderItem(new ItemStack(ModItems.ANGEL_RING.get()), 0, 0);
        g.pose().popPose();

        // название и описание
        g.drawString(font, Component.translatable("item.angelring.angel_ring").withStyle(ChatFormatting.BOLD),
                left + INFO_X + 6, top + INFO_Y + 6, 0x3A2A12, false);
        g.pose().pushPose();
        g.pose().translate(left + INFO_X + 6, top + INFO_Y + 19, 0);
        g.pose().scale(0.75f, 0.75f, 1f);
        int y = 0;
        for (FormattedCharSequence line : font.split(Component.translatable("item.angelring.angel_ring.desc"),
                (int) ((INFO_W - 12) / 0.75f))) {
            g.drawString(font, line, 0, y, 0x5A4426, false);
            y += 9;
        }
        g.pose().popPose();

        // три характеристики справа
        chip(g, 0, String.valueOf(d.points()));
        chip(g, 1, String.valueOf(d.xp()));
        chip(g, 2, "+" + Math.round((d.speedMultiplier() - 1f) * 100f) + "%");

        // шкала опыта
        int bx = left + BAR_X + 2;
        int by = top + BAR_Y + 2;
        int bw = BAR_W - 4;
        int bh = BAR_H - 4;
        float ratio = d.maxed() ? 1f : Math.min(1f, d.xp() / (float) d.xpToNext());
        int fill = Math.round(bw * ratio);
        if (fill > 0) {
            g.fillGradient(bx, by, bx + fill, by + bh, 0xFF7EE0FF, 0xFF2B8FC9);
        }
        String label = d.maxed() ? Component.translatable("gui.angelring.max").getString()
                : (int) (ratio * 100f) + "%";
        centered(g, label, bx + bw / 2, by, 0xFFFFFF, true);

        // два улучшения
        upgrade(g, UP1_X, new ItemStack(Items.FEATHER), "gui.angelring.upgrade.speed", d.speed(),
                Component.translatable("gui.angelring.speed_effect", Math.round(d.speed() * 15f)));
        upgrade(g, UP2_X, new ItemStack(Items.EXPERIENCE_BOTTLE), "gui.angelring.upgrade.mastery", d.mastery(),
                Component.translatable("gui.angelring.mastery_effect", d.mastery() * 20));
    }

    private void chip(GuiGraphics g, int index, String text) {
        g.drawString(font, text, left + STAT_X + 18, top + STAT_Y + index * STAT_STEP + 5, 0xFFF3D0, true);
    }

    private void upgrade(GuiGraphics g, int x, ItemStack icon, String titleKey, int rank, Component effect) {
        int cx = left + x;
        int cy = top + UP_Y;
        g.renderItem(icon, cx + 6, cy + 6);
        g.drawString(font, Component.translatable(titleKey), cx + 26, cy + 6, 0x3A2A12, false);
        small(g, Component.translatable("gui.angelring.rank", rank, RelicData.MAX_STAT), cx + 26, cy + 16, 0x5A4426);
        small(g, effect, cx + 26, cy + 24, 0x2B6B2B);
    }

    private void small(GuiGraphics g, Component text, int x, int y, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(0.75f, 0.75f, 1f);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    private void centered(GuiGraphics g, String text, int centerX, int y, int color, boolean shadow) {
        Font f = this.font;
        g.drawString(f, text, centerX - f.width(text) / 2, y, color, shadow);
    }
}
