package dev.agentsharik.fallenrelics;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The relic scroll of the angel ring: a Relics-style relic sheet. Starfield
 * splat, two parchment rolls, the ring card with a level badge and level pips,
 * the candle-held experience bar, two ability cards with rank pips (click a
 * card to spend an upgrade point) and the right column of Relics counters.
 * Statuses and relic data arrive from the server once a second while open.
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
    private static final int STATUS_FLYING = 0xFF55C955;
    private static final int STATUS_WORN = 0xFFB07A20;
    private static final int STATUS_IDLE = 0xFF8A7A63;
    private static final int STATUS_CUSHION = 0xFF2E8C90;
    private static final int BAR_FILL = 0xFF7EE0FF;
    private static final int BAR_GLOSS = 0xFFD2F4FF;
    private static final int BAR_EMPTY = 0xFF3A2A52;
    private static final int PIP_FILL = 0xFFF0C050;
    private static final int HOVER_GOLD = 0xFFE8A33D;
    /** Level milestones behind the five pips under the ring card. */
    private static final int[] LEVEL_PIPS = {2, 4, 6, 8, 10};

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
        RelicData relic = AngelRingStatusClient.relic();
        boolean stale = AngelRingStatusClient.stale();
        graphics.blit(TEXTURE, left, top, 0, 0, WIDTH, HEIGHT);
        graphics.blit(PORTRAIT, left + 42, top + 30, 0, 0, 32, 32);
        // level badge over the card, Relics-style
        if (!stale) {
            centered(graphics, String.valueOf(relic.level()), left + 62, top + 11, BADGE_COLOR, true);
        }
        // level milestone pips under the card
        if (!stale) {
            for (int i = 0; i < LEVEL_PIPS.length; i++) {
                if (relic.level() >= LEVEL_PIPS[i]) {
                    int cx = 44 + i * 9;
                    graphics.fill(left + cx + 1, top + 78, left + cx + 6, top + 83, PIP_FILL);
                }
            }
        }
        // title, live status line and prose on the top roll
        graphics.drawString(font, title, left + 88, top + 24, TITLE_COLOR, false);
        graphics.fill(left + 88, top + 36, left + 184, top + 37, 0xFFB49A72);
        graphics.drawString(font, statusLine(), left + 88, top + 42, statusColor(), false);
        List<FormattedCharSequence> lines =
                font.split(Component.translatable("item.fallenrelics.angel_ring.tooltip"), 94);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), left + 88, top + 52 + i * 10, TEXT_COLOR, false);
        }
        // candle-held experience bar
        float ratio = relic.maxed() ? 1.0F : Math.min(1.0F, relic.xp() / (float) relic.xpToNext());
        int fill = Math.round(130 * ratio);
        graphics.fill(left + 45, top + 99, left + 175, top + 109, BAR_EMPTY);
        if (fill > 0) {
            graphics.fill(left + 45, top + 99, left + 45 + fill, top + 109, BAR_FILL);
            graphics.fill(left + 45, top + 99, left + 45 + fill, top + 101, BAR_GLOSS);
        }
        String barLabel = stale ? "--"
                : (relic.maxed() ? Component.translatable("gui.fallenrelics.angel_ring.max").getString()
                        : Math.round(ratio * 100F) + "%");
        centered(graphics, barLabel, left + 110, top + 100, 0xFF203040, true);
        // ability cards: rank pips and hover highlight when an upgrade is affordable
        rankPips(graphics, relic.speed(), 48);
        rankPips(graphics, relic.cushion(), 100);
        if (upgradable(RelicData.STAT_SPEED) && inside(mouseX, mouseY, 44, 126, 36, 44)) {
            outline(graphics, 44, 126, 36, 44);
        }
        if (upgradable(RelicData.STAT_CUSHION) && inside(mouseX, mouseY, 96, 126, 36, 44)) {
            outline(graphics, 96, 126, 36, 44);
        }
        // right column: Relics counters and the flight clock
        centered(graphics, Component.translatable("gui.fallenrelics.angel_ring.header").getString(),
                left + 223, top + 20, HEADER_COLOR, true);
        plaque(graphics, 0, stale ? "--" : String.valueOf(relic.points()));
        plaque(graphics, 1, stale ? "--" : String.valueOf(relic.xp()));
        plaque(graphics, 2, stale ? "--" : "+" + Math.round((relic.speedMultiplier() - 1.0F) * 100F) + "%");
        centered(graphics, stale ? "--" : timeText(), left + 223, top + 104, HEADER_COLOR, true);
        centered(graphics, Component.translatable("gui.fallenrelics.angel_ring.hint").getString(),
                left + 108, top + 204, 0xFFBFB3D9, true);
        tooltips(graphics, mouseX, mouseY, relic, stale);
    }

    private Component statusLine() {
        if (AngelRingStatusClient.stale()) {
            return Component.literal("--");
        }
        if (AngelRingStatusClient.buffering()) {
            int seconds = (AngelRingStatusClient.bufferTicks() + 19) / 20;
            return Component.translatable("gui.fallenrelics.angel_ring.status.buffering", seconds);
        }
        if (!AngelRingStatusClient.granted()) {
            return Component.translatable("gui.fallenrelics.angel_ring.status.idle");
        }
        return AngelRingStatusClient.flying()
                ? Component.translatable("gui.fallenrelics.angel_ring.status.flying")
                : Component.translatable("gui.fallenrelics.angel_ring.status.worn");
    }

    private int statusColor() {
        if (AngelRingStatusClient.stale()) {
            return STATUS_IDLE;
        }
        if (AngelRingStatusClient.buffering()) {
            return STATUS_CUSHION;
        }
        if (!AngelRingStatusClient.granted()) {
            return STATUS_IDLE;
        }
        return AngelRingStatusClient.flying() ? STATUS_FLYING : STATUS_WORN;
    }

    private String timeText() {
        long seconds = AngelRingStatusClient.flightTicks() / 20L;
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60L, seconds % 60L);
    }

    private void rankPips(GuiGraphics graphics, int rank, int x0) {
        for (int i = 0; i < RelicData.MAX_RANK; i++) {
            if (i < rank) {
                int cx = x0 + i * 7;
                graphics.fill(left + cx + 1, top + 155, left + cx + 6, top + 160, PIP_FILL);
            }
        }
    }

    private void outline(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(left + x - 1, top + y - 1, left + x + w + 1, top + y, HOVER_GOLD);
        graphics.fill(left + x - 1, top + y + h, left + x + w + 1, top + y + h + 1, HOVER_GOLD);
        graphics.fill(left + x - 1, top + y, left + x, top + y + h, HOVER_GOLD);
        graphics.fill(left + x + w, top + y, left + x + w + 1, top + y + h, HOVER_GOLD);
    }

    private boolean upgradable(int stat) {
        RelicData relic = AngelRingStatusClient.relic();
        if (AngelRingStatusClient.stale() || relic.points() <= 0) {
            return false;
        }
        return stat == RelicData.STAT_SPEED ? relic.speed() < RelicData.MAX_RANK
                : relic.cushion() < RelicData.MAX_RANK;
    }

    private void plaque(GuiGraphics graphics, int row, String text) {
        int y = top + 36 + row * 22;
        graphics.drawString(font, text, left + 211, y + 5, PLAQUE_COLOR, false);
    }

    private void centered(GuiGraphics graphics, String text, int centerX, int y, int color, boolean shadow) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, color, shadow);
    }

    private void tooltips(GuiGraphics graphics, int mouseX, int mouseY, RelicData relic, boolean stale) {
        Component tip = null;
        List<Component> body = null;
        if (inside(mouseX, mouseY, 44, 126, 36, 44)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.ability.flight");
            body = List.of(
                    Component.translatable("gui.fallenrelics.angel_ring.ability.flight.desc"),
                    Component.translatable("gui.fallenrelics.angel_ring.rank", relic.speed(), RelicData.MAX_RANK),
                    Component.translatable("gui.fallenrelics.angel_ring.click"));
        } else if (inside(mouseX, mouseY, 96, 126, 36, 44)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.ability.insurance");
            body = List.of(
                    Component.translatable("gui.fallenrelics.angel_ring.ability.insurance.desc"),
                    Component.translatable("gui.fallenrelics.angel_ring.rank", relic.cushion(), RelicData.MAX_RANK),
                    Component.translatable("gui.fallenrelics.angel_ring.click"));
        } else if (inside(mouseX, mouseY, 196, 36, 54, 18)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.plaque.points");
            body = List.of(Component.translatable("gui.fallenrelics.angel_ring.plaque.points.desc"));
        } else if (inside(mouseX, mouseY, 196, 58, 54, 18)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.plaque.xp");
            body = List.of(Component.translatable("gui.fallenrelics.angel_ring.plaque.xp.desc"));
        } else if (inside(mouseX, mouseY, 196, 80, 54, 18)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.plaque.speed");
            body = List.of(Component.translatable("gui.fallenrelics.angel_ring.plaque.speed.desc"));
        } else if (inside(mouseX, mouseY, 196, 98, 54, 14)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.plaque.time");
            body = List.of(Component.literal(timeText()));
        } else if (inside(mouseX, mouseY, 44, 77, 46, 7)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.pips");
            body = List.of(Component.translatable("gui.fallenrelics.angel_ring.level", relic.level(), RelicData.MAX_LEVEL));
        } else if (inside(mouseX, mouseY, 52, 10, 20, 10)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.badge");
            body = List.of(Component.translatable("gui.fallenrelics.angel_ring.level", relic.level(), RelicData.MAX_LEVEL));
        }
        if (tip != null && !stale) {
            List<FormattedCharSequence> lines = new java.util.ArrayList<>();
            lines.add(tip.getVisualOrderText());
            for (Component c : body) {
                lines.addAll(font.split(c, 160));
            }
            graphics.renderTooltip(font, lines, mouseX, mouseY);
        }
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= left + x && mouseX < left + x + w && mouseY >= top + y && mouseY < top + y + h;
    }

    /** Click an ability card to spend one upgrade point; the server validates. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (upgradable(RelicData.STAT_SPEED) && inside((int) mouseX, (int) mouseY, 44, 126, 36, 44)) {
                PacketDistributor.sendToServer(new AngelRingUpgradePayload(RelicData.STAT_SPEED));
                AngelRingStatusClient.request();
                return true;
            }
            if (upgradable(RelicData.STAT_CUSHION) && inside((int) mouseX, (int) mouseY, 96, 126, 36, 44)) {
                PacketDistributor.sendToServer(new AngelRingUpgradePayload(RelicData.STAT_CUSHION));
                AngelRingStatusClient.request();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
