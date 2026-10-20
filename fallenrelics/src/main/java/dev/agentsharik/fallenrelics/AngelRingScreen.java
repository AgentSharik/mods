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
 * The relic scroll of the angel ring, laid out after the Relics mod screen:
 * starfield splat, two parchment rolls with wooden rollers, the ring card with
 * a badge and milestone pips, a candle-held bar, two ability cards and a right
 * column of status plaques. Nothing is drawn over anything else: captions live
 * in hover tooltips, values sit inside their plaques.
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
    private static final int HEADER_COLOR = 0xFFE8A33D;
    private static final int BADGE_COLOR = 0xFFE8C060;
    private static final int BAR_FILL = 0xFF5AE6EB;
    private static final int BAR_GLOSS = 0xFFB9F6F8;
    private static final int BAR_EMPTY = 0xFF3A2A52;
    private static final int PIP_FILL = 0xFFF0C050;
    /** Must match AngelRingFlight.SAFETY_SLOW_FALL_TICKS. */
    private static final int SAFETY_TICKS = 200;
    /** Flight-minute milestones behind the five pips under the ring card. */
    private static final int[] PIP_MINUTES = {1, 5, 15, 30, 60};

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
        graphics.blit(PORTRAIT, left + 42, top + 30, 0, 0, 32, 32);
        // badge over the card: seconds of burning insurance, empty when charged
        if (AngelRingStatusClient.buffering() && !AngelRingStatusClient.stale()) {
            int seconds = (AngelRingStatusClient.bufferTicks() + 19) / 20;
            centered(graphics, String.valueOf(seconds), left + 62, top + 11, BADGE_COLOR, true);
        }
        // milestone pips under the card
        long minutes = AngelRingStatusClient.flightTicks() / 20L / 60L;
        for (int i = 0; i < PIP_MINUTES.length; i++) {
            if (!AngelRingStatusClient.stale() && minutes >= PIP_MINUTES[i]) {
                int cx = 44 + i * 9;
                graphics.fill(left + cx + 1, top + 78, left + cx + 6, top + 83, PIP_FILL);
            }
        }
        // title and prose on the top roll
        graphics.drawString(font, title, left + 88, top + 24, TITLE_COLOR, false);
        graphics.fill(left + 88, top + 36, left + 184, top + 37, 0xFFB49A72);
        List<FormattedCharSequence> lines =
                font.split(Component.translatable("item.fallenrelics.angel_ring.tooltip"), 94);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), left + 88, top + 42 + i * 10, TEXT_COLOR, false);
        }
        // candle-held bar: soft-falling insurance charge
        float fraction = AngelRingStatusClient.buffering()
                ? AngelRingStatusClient.bufferTicks() / (float) SAFETY_TICKS
                : 1.0F;
        fraction = Math.min(1.0F, Math.max(0.0F, fraction));
        int fill = Math.round(130 * fraction);
        graphics.fill(left + 45, top + 99, left + 175, top + 109, BAR_EMPTY);
        if (fill > 0) {
            graphics.fill(left + 45, top + 99, left + 45 + fill, top + 109, BAR_FILL);
            graphics.fill(left + 45, top + 99, left + 45 + fill, top + 101, BAR_GLOSS);
        }
        String barLabel = AngelRingStatusClient.stale() ? "--"
                : (AngelRingStatusClient.buffering()
                        ? (AngelRingStatusClient.bufferTicks() + 19) / 20 + " "
                                + Component.translatable("gui.fallenrelics.angel_ring.seconds").getString()
                        : Math.round(fraction * 100F) + "%");
        centered(graphics, barLabel, left + 110, top + 100, 0xFFFFFFFF, true);
        // right column: header and status plaques
        centered(graphics, Component.translatable("gui.fallenrelics.angel_ring.header").getString(),
                left + 223, top + 20, HEADER_COLOR, true);
        plaque(graphics, 0, flightStatus());
        plaque(graphics, 1, insuranceStatus());
        plaque(graphics, 2, timeStatus());
        // hint on the splat under the bottom roll
        centered(graphics, Component.translatable("gui.fallenrelics.angel_ring.hint").getString(),
                left + 108, top + 204, 0xFFBFB3D9, true);
        tooltips(graphics, mouseX, mouseY);
    }

    private void plaque(GuiGraphics graphics, int row, Component text) {
        int y = top + 36 + row * 22;
        graphics.drawString(font, text, left + 211, y + 5, PLAQUE_COLOR, false);
    }

    private void centered(GuiGraphics graphics, String text, int centerX, int y, int color, boolean shadow) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, color, shadow);
    }

    /** Hover tooltips carry every caption, so nothing is painted over the art. */
    private void tooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        Component tip = null;
        Component desc = null;
        if (inside(mouseX, mouseY, 44, 126, 36, 44)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.ability.flight");
            desc = Component.translatable("gui.fallenrelics.angel_ring.ability.flight.desc");
        } else if (inside(mouseX, mouseY, 96, 126, 36, 44)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.ability.insurance");
            desc = Component.translatable("gui.fallenrelics.angel_ring.ability.insurance.desc");
        } else if (inside(mouseX, mouseY, 196, 36, 54, 18)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.plaque.status");
            desc = flightStatus();
        } else if (inside(mouseX, mouseY, 196, 58, 54, 18)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.plaque.insurance");
            desc = insuranceStatus();
        } else if (inside(mouseX, mouseY, 196, 80, 54, 18)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.plaque.time");
            desc = timeStatus();
        } else if (inside(mouseX, mouseY, 44, 77, 46, 7)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.pips");
            desc = null;
        }
        if (tip != null) {
            List<Component> lines = desc == null ? List.of(tip) : List.of(tip, desc);
            graphics.renderTooltip(font, lines.stream().map(Component::getVisualOrderText).toList(),
                    mouseX, mouseY);
        }
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= left + x && mouseX < left + x + w && mouseY >= top + y && mouseY < top + y + h;
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
