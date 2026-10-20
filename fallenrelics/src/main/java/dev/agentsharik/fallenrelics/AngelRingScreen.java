package dev.agentsharik.fallenrelics;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * The relic scroll of the angel ring: a parchment sheet in the spirit of the
 * Relics mod screen (starfield blob, wooden rollers, golden cards and plaques)
 * showing the ring portrait, its prose, the soft-falling insurance bar and the
 * live status plaques (worn / airborne / cushion / lifetime flight time).
 * Opens by right-clicking the ring in hand; statuses arrive from the server
 * once a second while the scroll is open.
 */
public final class AngelRingScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/angel_ring_screen.png");
    private static final ResourceLocation PORTRAIT =
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/ring_portrait.png");

    /** Panel art occupies the top 224 rows of the 256x256 sheet. */
    private static final int WIDTH = 256;
    private static final int HEIGHT = 224;

    private static final int TITLE_COLOR = 0xFF3B2A18;
    private static final int TEXT_COLOR = 0xFF6B5B45;
    private static final int PLAQUE_COLOR = 0xFF2E2010;
    private static final int BAR_FILL = 0xFF5AE6EB;
    private static final int BAR_GLOSS = 0xFFB9F6F8;
    private static final int BAR_EMPTY = 0xFF3A2A52;
    /** Must match AngelRingFlight.SAFETY_SLOW_FALL_TICKS. */
    private static final int SAFETY_TICKS = 200;
    private static final int BAR_X = 93;
    private static final int BAR_Y = 89;
    private static final int BAR_W = 122;
    private static final int BAR_H = 10;

    private final ItemStack ring;
    private int left;
    private int top;
    private int ticks;

    public AngelRingScreen(ItemStack ring) {
        super(Component.translatable("gui.fallenrelics.angel_ring.title"));
        this.ring = ring;
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        this.ticks = 0;
        AngelRingStatusClient.request();
    }

    @Override
    public void tick() {
        super.tick();
        if (++this.ticks % 20 == 0) {
            AngelRingStatusClient.request();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.blit(TEXTURE, left, top, 0, 0, WIDTH, HEIGHT);
        // ring portrait inside the golden card
        graphics.blit(PORTRAIT, left + 46, top + 38, 0, 0, 32, 32);
        // title and prose
        graphics.drawString(font, title, left + 92, top + 30, TITLE_COLOR, false);
        graphics.fill(left + 92, top + 42, left + 216, top + 43, 0xFFB49A72);
        List<FormattedCharSequence> lines =
                font.split(Component.translatable("item.fallenrelics.angel_ring.tooltip"), 122);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), left + 92, top + 50 + i * 10, TEXT_COLOR, false);
        }
        // soft-falling insurance bar
        float fraction = AngelRingStatusClient.buffering()
                ? AngelRingStatusClient.bufferTicks() / (float) SAFETY_TICKS
                : 1.0F;
        int fill = Math.round(BAR_W * Math.min(1.0F, Math.max(0.0F, fraction)));
        graphics.fill(left + BAR_X, top + BAR_Y, left + BAR_X + BAR_W, top + BAR_Y + BAR_H, BAR_EMPTY);
        if (fill > 0) {
            graphics.fill(left + BAR_X, top + BAR_Y, left + BAR_X + fill, top + BAR_Y + BAR_H, BAR_FILL);
            graphics.fill(left + BAR_X, top + BAR_Y, left + BAR_X + fill, top + BAR_Y + 2, BAR_GLOSS);
        }
        // ability captions under the two cards
        graphics.drawCenteredString(font,
                Component.translatable("gui.fallenrelics.angel_ring.ability.flight"), left + 62, top + 160, TEXT_COLOR);
        graphics.drawCenteredString(font,
                Component.translatable("gui.fallenrelics.angel_ring.ability.insurance"), left + 114, top + 160, TEXT_COLOR);
        // status plaques
        plaque(graphics, 0, flightStatus());
        plaque(graphics, 1, insuranceStatus());
        plaque(graphics, 2, timeStatus());
        graphics.drawCenteredString(font,
                Component.translatable("gui.fallenrelics.angel_ring.stat.time_caption"), left + 182, top + 176, TEXT_COLOR);
        graphics.drawCenteredString(font,
                Component.translatable("gui.fallenrelics.angel_ring.hint"), left + WIDTH / 2, top + 196, TEXT_COLOR);
    }

    private void plaque(GuiGraphics graphics, int row, Component text) {
        int y = top + 112 + row * 22;
        graphics.drawString(font, text, left + 164, y + 5, PLAQUE_COLOR, false);
    }

    private Component flightStatus() {
        if (AngelRingStatusClient.stale()) {
            return Component.literal("--");
        }
        if (!AngelRingStatusClient.granted()) {
            return Component.translatable("gui.fallenrelics.angel_ring.status.idle");
        }
        return AngelRingStatusClient.flying()
                ? Component.translatable("gui.fallenrelics.angel_ring.status.flying")
                : Component.translatable("gui.fallenrelics.angel_ring.status.worn");
    }

    private Component insuranceStatus() {
        if (AngelRingStatusClient.stale()) {
            return Component.literal("--");
        }
        if (AngelRingStatusClient.buffering()) {
            int seconds = (AngelRingStatusClient.bufferTicks() + 19) / 20;
            return Component.translatable("gui.fallenrelics.angel_ring.status.buffering", seconds);
        }
        return Component.translatable("gui.fallenrelics.angel_ring.status.ready");
    }

    private Component timeStatus() {
        if (AngelRingStatusClient.stale()) {
            return Component.literal("--");
        }
        long seconds = AngelRingStatusClient.flightTicks() / 20L;
        return Component.literal(String.format(Locale.ROOT, "%d:%02d", seconds / 60L, seconds % 60L));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
