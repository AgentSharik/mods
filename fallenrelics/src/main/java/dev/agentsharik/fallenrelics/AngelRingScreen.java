package dev.agentsharik.fallenrelics;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Свиток реликвии ревизии 1.8.1: чёрно-серо-багровый композит на живой дымке
 * (кросс-фейд кадров каждый render-кадр), ряд звёзд рарности под портретом,
 * три карточки способностей со звёздами, рангами и кулдаунами, кнопка РЕРОЛЛ
 * за два уровня игрока. Вспышка прокачки — сгущение дымки на карточке, реролл —
 * вихрь вокруг портрета с пересборкой звёзд. Всё числовое приходит снапшотом
 * с сервера; клики шлют payload'ы, сервер проверяет и считает. 1.8.1: панель
 * расширена до 320px, свиток и все действия привязаны к UUID открытого кольца,
 * вспышка улучшения стартует по клику, повышение уровня — настоящий бёрст,
 * каст активных способностей продублирован хоткеями Z/X.
 */
public final class AngelRingScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/angel_ring_screen.png");
    private static final ResourceLocation PORTRAIT =
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/ring_portrait.png");
    private static final ResourceLocation[] SMOKE = {
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/relic_smoke_0.png"),
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/relic_smoke_1.png"),
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/relic_smoke_2.png"),
            ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "textures/gui/relic_smoke_3.png"),
    };

    private static final int WIDTH = 320;
    private static final int HEIGHT = 224;

    private static final int TITLE_COLOR = 0xFFE8E8EC;
    private static final int TEXT_COLOR = 0xFFA8A8B0;
    private static final int PLAQUE_COLOR = 0xFFDCDCE2;
    private static final int HEADER_COLOR = 0xFFC85A5A;
    private static final int BADGE_COLOR = 0xFFF0B4B4;
    private static final int STATUS_FLYING = 0xFF78DC78;
    private static final int STATUS_WORN = 0xFFDCB45A;
    private static final int STATUS_IDLE = 0xFF8A8A92;
    private static final int STATUS_CUSHION = 0xFF78C8C8;
    private static final int BAR_FILL = 0xFFBE3C3C;
    private static final int BAR_GLOSS = 0xFFE67878;
    private static final int BAR_EMPTY = 0xFF141418;
    private static final int PANEL_DARK = 0xFF1E1E22;
    private static final int LABEL_DIM = 0xFF8A8A92;
    private static final int STAR_FILL = 0xFFD86464;
    private static final int STAR_EMPTY = 0xFF3A3A40;
    private static final int ICON_ON = 0xFFE67878;
    private static final int ICON_OFF = 0xFF6A6A72;
    private static final int HOVER_CRIMSON = 0xFFE67878;

    /** Пиксель-арт иконок способностей 8x8, масштаб x2. */
    private static final String[][] ABILITY_ICONS = {
            {".######.", "#..##..#", "#..##..#", "#......#", ".#....#.", "..#..#..", "...##...", "........"},
            {"...##...", "..####..", ".######.", ".######.", "..####..", "...##...","....#...", "........"},
            {"#......#", "##....##", "#.#..#.#", "..####..", ".######.", "..####..", "...##...", "........"},
    };
    /** Замок для заблокированных рубежами способностей. */
    private static final String[] LOCK_PIXELS = {".###.", "#...#", "#.#.#", "#...#", "#...#", ".###."};
    private static final String[] STAR_PIXELS = {"..#..", ".###.", "#####", ".###.", "#...#"};
    private static final String[] MINI_STAR_PIXELS = {".#.", "###", "#.#"};

    private final ItemStack ring;
    private final Screen returnTo;
    /** UUID кольца-стека, из которого открыт свиток; nil — рарность ещё не брошена. */
    private UUID viewId;
    private int left;
    private int top;
    private int ticks;
    private RelicData prevRelic;
    private final int[] flash = {0, 0, 0};
    private int rerollAnim;
    private int levelAnim;

    public AngelRingScreen(ItemStack ring, Screen returnTo) {
        super(Component.translatable("gui.fallenrelics.angel_ring.title"));
        this.ring = ring;
        this.returnTo = returnTo;
        this.viewId = ring.getOrDefault(ModContent.RELIC_DATA.get(), RelicData.EMPTY).id();
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        this.ticks = 0;
        AngelRingStatusClient.request(viewId);
    }

    @Override
    public void tick() {
        super.tick();
        if (++this.ticks % 20 == 0) {
            AngelRingStatusClient.request(viewId);
        }
        for (int i = 0; i < flash.length; i++) {
            if (flash[i] > 0) {
                flash[i]--;
            }
        }
        if (rerollAnim > 0) {
            rerollAnim--;
        }
        if (levelAnim > 0) {
            levelAnim--;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        RelicData relic = AngelRingStatusClient.relic();
        boolean stale = AngelRingStatusClient.stale();
        if (RelicData.NO_ID.equals(viewId) && !RelicData.NO_ID.equals(relic.id())) {
            viewId = relic.id();
        }
        diffAnimations(relic);
        drawSmoke(graphics, partialTick);
        graphics.blit(TEXTURE, left, top, 0, 0, WIDTH, HEIGHT);
        if (rerollAnim > 0) {
            drawRerollVortex(graphics);
        }
        if (levelAnim > 0) {
            drawLevelBurst(graphics, relic);
        }
        graphics.blit(PORTRAIT, left + 42, top + 30, 0, 0, 32, 32);
        if (!stale) {
            centered(graphics, String.valueOf(relic.level()), left + 62, top + 11, BADGE_COLOR, true);
            int stars = rerollAnim > 12 ? (int) ((ticks * 7L) % 8) : relic.rarity();
            for (int i = 0; i <= RelicData.MAX_STAR; i++) {
                drawStar(graphics, 44 + i * 5, 76, i < stars, false);
            }
        }
        graphics.drawString(font, title, left + 88, top + 20, TITLE_COLOR, false);
        graphics.fill(left + 88, top + 31, left + 224, top + 32, 0xFF5A4646);
        Component status = statusLine();
        List<FormattedCharSequence> statusLines = font.split(status, 134);
        if (!statusLines.isEmpty()) {
            graphics.drawString(font, statusLines.get(0), left + 88, top + 35, statusColor(), false);
        }
        List<FormattedCharSequence> lines =
                font.split(Component.translatable("item.fallenrelics.angel_ring.tooltip"), 134);
        for (int i = 0; i < lines.size() && i < 4; i++) {
            graphics.drawString(font, lines.get(i), left + 88, top + 46 + i * 10, TEXT_COLOR, false);
        }
        float ratio = relic.maxed() ? 1.0F : Math.min(1.0F, relic.xp() / (float) relic.xpToNext());
        int fill = Math.round(126 * ratio);
        graphics.fill(left + 45, top + 99, left + 171, top + 109, BAR_EMPTY);
        if (fill > 0) {
            graphics.fill(left + 45, top + 99, left + 45 + fill, top + 109, BAR_FILL);
            graphics.fill(left + 45, top + 99, left + 45 + fill, top + 101, BAR_GLOSS);
        }
        String barLabel = stale ? "--"
                : (relic.maxed() ? Component.translatable("gui.fallenrelics.angel_ring.max").getString()
                        : Math.round(ratio * 100F) + "%");
        centered(graphics, barLabel, left + 108, top + 100, 0xFFFFFFFF, true);
        drawLevelButton(graphics, relic, stale, mouseX, mouseY);
        graphics.fill(left + 44, top + 110, left + 216, top + 120, PANEL_DARK);
        for (int i = 0; i < RelicData.ABILITIES; i++) {
            drawAbilityCard(graphics, i, relic, stale, mouseX, mouseY);
        }
        drawRerollButton(graphics, relic, stale, mouseX, mouseY);
        centered(graphics, Component.translatable("gui.fallenrelics.angel_ring.header").getString(),
                left + 283, top + 20, HEADER_COLOR, true);
        plaque(graphics, 0, Component.translatable("gui.fallenrelics.angel_ring.plaque.label.level").getString(),
                stale ? "--" : String.valueOf(relic.level()));
        plaque(graphics, 1, Component.translatable("gui.fallenrelics.angel_ring.plaque.label.xp").getString(),
                stale ? "--" : String.valueOf(relic.xp()));
        plaque(graphics, 2, Component.translatable("gui.fallenrelics.angel_ring.plaque.label.speed").getString(),
                stale ? "--" : "+" + Math.round((relic.speedMultiplier() - 1.0F) * 100F) + "%");
        centered(graphics, stale ? "--" : timeText(), left + 283, top + 104, HEADER_COLOR, true);
        centered(graphics, Component.translatable("gui.fallenrelics.angel_ring.hint").getString(),
                left + 120, top + 204, 0xFF9A8CA0, true);
        tooltips(graphics, mouseX, mouseY, relic, stale);
    }

    /**
     * Сравнивает снапшоты: рост ранга даёт вспышку карточки, смена звёзд — вихрь,
     * рост уровня реликвии — бёрст badges. Диффер — только страховка для чужих
     * изменений; свои клики запускают анимации мгновенно (фикс 1.8.1: анимация
     * больше не «живёт своей жизнью» с задержкой до секунды).
     */
    private void diffAnimations(RelicData relic) {
        if (prevRelic != null && prevRelic.rolled()) {
            for (int i = 0; i < RelicData.ABILITIES; i++) {
                if (relic.rank(i) > prevRelic.rank(i) && flash[i] == 0) {
                    flash[i] = 24;
                }
            }
            if ((relic.rarity() != prevRelic.rarity() || relic.a0() != prevRelic.a0()
                    || relic.a1() != prevRelic.a1() || relic.a2() != prevRelic.a2()) && rerollAnim == 0) {
                rerollAnim = 40;
            }
            if (relic.level() > prevRelic.level() && levelAnim == 0) {
                levelAnim = 36;
            }
        }
        prevRelic = relic;
    }

    /** Повышение уровня реликвии: расходящееся кольцо, дымок и всплывающая цифра. */
    private void drawLevelBurst(GuiGraphics graphics, RelicData relic) {
        int t = 36 - levelAnim;
        int cx = left + 62;
        int cy = top + 15;
        int radius = 5 + t;
        int alpha = Math.max(0, 235 - t * 7);
        int ringColor = (alpha << 24) | 0xF0B4B4;
        for (int a = 0; a < 24; a++) {
            double ang = a * Math.PI / 12.0;
            int px = cx + (int) Math.round(radius * Math.cos(ang));
            int py = cy + (int) Math.round(radius * Math.sin(ang) * 0.8);
            graphics.fill(px, py, px + 1, py + 1, ringColor);
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0);
        float s = 0.55F + t * 0.045F;
        pose.scale(s, s, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F * (1.0F - t / 36.0F));
        graphics.blit(SMOKE[(t / 9) % SMOKE.length], -24, -24, 96, 96, 48, 48);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
        String msg = Component.translatable("gui.fallenrelics.angel_ring.levelup", relic.level()).getString();
        int textAlpha = Math.max(30, 255 - t * 9);
        centered(graphics, msg, cx, top + 2 - t / 3, (textAlpha << 24) | 0xFFE8E8, true);
    }

    /** Живая дымка: кросс-фейд кадров каждый render-кадр, дрейф, дыхание, покачивание. */
    private void drawSmoke(GuiGraphics graphics, float partialTick) {
        float t = this.ticks + partialTick;
        float pos = t / 6.0F;
        int floor = (int) Math.floor(pos);
        int frame = ((floor % SMOKE.length) + SMOKE.length) % SMOKE.length;
        int next = (frame + 1) % SMOKE.length;
        float f = pos - floor;
        float ox = (float) Math.sin(t / 9.0F) * 4.0F;
        float oy = (float) Math.cos(t / 7.0F) * 3.0F;
        float alpha = 0.80F + 0.12F * (float) Math.sin(t / 5.0F);
        float grow = 1.22F + 0.06F * (float) Math.sin(t / 11.0F);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(left + WIDTH / 2.0F + ox, top + HEIGHT / 2.0F + oy, 0.0F);
        pose.scale(grow, grow, 1.0F);
        pose.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(t / 13.0F) * 3.0F));
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha * (1.0F - f));
        graphics.blit(SMOKE[frame], -128, -112, 0, 0, 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha * f);
        graphics.blit(SMOKE[next], -128, -112, 0, 0, 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
    }

    /** Вихрь багрового дыма вокруг портрета во время реролла. */
    private void drawRerollVortex(GuiGraphics graphics) {
        int t = 40 - rerollAnim;
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(left + 58, top + 46, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(t * 14.0F));
        float scale = 1.0F + 0.25F * (float) Math.sin(t / 3.0F);
        pose.scale(scale, scale, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.75F);
        graphics.blit(SMOKE[(t / 3) % SMOKE.length], -40, -40, 64, 64, 80, 80);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
    }

    private void drawStar(GuiGraphics graphics, int x, int y, boolean filled, boolean mini) {
        String[] pattern = mini ? MINI_STAR_PIXELS : STAR_PIXELS;
        int color = filled ? (mini ? STAR_FILL : STAR_FILL) : STAR_EMPTY;
        for (int row = 0; row < pattern.length; row++) {
            for (int col = 0; col < pattern[row].length(); col++) {
                if (pattern[row].charAt(col) == '#') {
                    graphics.fill(left + x + col, top + y + row, left + x + col + 1, top + y + row + 1, color);
                }
            }
        }
    }

    /** Кнопка повышения уровня реликвии: активна и пульсирует при полном пороге опыта. */
    private void drawLevelButton(GuiGraphics graphics, RelicData relic, boolean stale, int mouseX, int mouseY) {
        boolean ready = !stale && relic.canLevelUp();
        graphics.fill(left + 175, top + 99, left + 215, top + 109, ready ? 0xFF3A2226 : 0xFF232327);
        int edge = ready ? ((ticks / 4) % 2 == 0 ? 0xFFE67878 : 0xFF963030) : 0xFF4A4A50;
        graphics.fill(left + 175, top + 99, left + 215, top + 100, edge);
        graphics.fill(left + 175, top + 108, left + 215, top + 109, edge);
        graphics.fill(left + 175, top + 99, left + 176, top + 109, edge);
        graphics.fill(left + 214, top + 99, left + 215, top + 109, edge);
        String label = relic.maxed() ? "—" : "+";
        centered(graphics, label, left + 195, top + 100,
                ready ? 0xFFE8B4B4 : 0xFF6A6A72, true);
        if (inside(mouseX, mouseY, 175, 99, 40, 10)) {
            outline(graphics, 175, 99, 40, 10);
        }
    }

    private void drawAbilityCard(GuiGraphics graphics, int index, RelicData relic, boolean stale,
            int mouseX, int mouseY) {
        boolean locked = !stale && !relic.unlocked(index);
        int x0 = 44 + index * 56;
        int y0 = 126;
        int rank = stale ? 0 : relic.rank(index);
        int stars = stale ? 0 : relic.abilityRarity(index);
        int iconColor = rank > 0 ? ICON_ON : ICON_OFF;
        String[] icon = ABILITY_ICONS[index];
        for (int row = 0; row < icon.length; row++) {
            for (int col = 0; col < icon[row].length(); col++) {
                if (icon[row].charAt(col) == '#') {
                    graphics.fill(left + x0 + 10 + col * 2, top + y0 + 2 + row * 2,
                            left + x0 + 12 + col * 2, top + y0 + 4 + row * 2, iconColor);
                }
            }
        }
        for (int i = 0; i <= RelicData.MAX_STAR; i++) {
            int shown = rerollAnim > 12 ? (int) ((ticks * 5L + i) % 8) : stars;
            drawStar(graphics, x0 + 4 + i * 4, y0 + 22, i < shown, true);
        }
        for (int i = 0; i < RelicData.MAX_RANK; i++) {
            int color = i < rank ? STAR_FILL : STAR_EMPTY;
            graphics.fill(left + x0 + 8 + i * 8, top + 174, left + x0 + 13 + i * 8, top + 179, color);
        }
        int cd = index == 2 ? AngelRingStatusClient.cooldown(2) : 0;
        if (cd > 0) {
            double cdMax = (index == 0 ? 600 : 900) * (1.0 - 0.15 * (rank - 1));
            int shade = (int) Math.round(44 * Math.min(1.0, cd / cdMax));
            graphics.fill(left + x0, top + y0, left + x0 + 36, top + y0 + shade, 0xCC08080A);
            centered(graphics, String.valueOf((cd + 19) / 20), left + x0 + 18, top + y0 + 18, 0xFFE8E8EC, true);
        }
        if (flash[index] > 0) {
            int t = flash[index];
            int e = (24 - t) / 2;
            int c = 0xFFE67878;
            graphics.fill(left + x0 - 2 - e, top + y0 - 2 - e, left + x0 + 38 + e, top + y0 - 1 - e, c);
            graphics.fill(left + x0 - 2 - e, top + y0 + 45 + e, left + x0 + 38 + e, top + y0 + 46 + e, c);
            graphics.fill(left + x0 - 2 - e, top + y0 - 2 - e, left + x0 - 1 - e, top + y0 + 46 + e, c);
            graphics.fill(left + x0 + 37 + e, top + y0 - 2 - e, left + x0 + 38 + e, top + y0 + 46 + e, c);
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(left + x0 + 18, top + y0 + 22, 0);
            float s = 1.0F + t / 24.0F * 1.2F;
            pose.scale(s, s, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.55F * t / 24.0F);
            graphics.blit(SMOKE[(24 - t) / 6 % SMOKE.length], -18, -22, 96, 96, 36, 44);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            pose.popPose();
            if (t > 16) {
                graphics.fill(left + x0, top + y0, left + x0 + 36, top + y0 + 44,
                        ((t - 16) * 8 << 24) | 0xFFFFFF);
            }
        }
        if (locked) {
            graphics.fill(left + x0 - 2, top + 124, left + x0 + 38, top + 172, 0xB408080A);
            for (int row = 0; row < LOCK_PIXELS.length; row++) {
                for (int col = 0; col < LOCK_PIXELS[row].length(); col++) {
                    if (LOCK_PIXELS[row].charAt(col) == '#') {
                        graphics.fill(left + x0 + 15 + col, top + 132 + row,
                                left + x0 + 16 + col, top + 133 + row, 0xFF8A8A92);
                    }
                }
            }
            PoseStack lockPose = graphics.pose();
            lockPose.pushPose();
            lockPose.translate(left + x0 + 18, top + 146, 0);
            lockPose.scale(0.5F, 0.5F, 1.0F);
            String req = Component.translatable("gui.fallenrelics.angel_ring.locked_until",
                    relic.unlockLevel(index)).getString();
            graphics.drawString(font, req, -font.width(req) / 2, 0, 0xFFB4B4BC, true);
            lockPose.popPose();
        }
        if (!locked && inside(mouseX, mouseY, x0 - 2, 124, 40, 48)) {
            outline(graphics, x0 - 2, 124, 40, 48);
        }
    }

    private void drawRerollButton(GuiGraphics graphics, RelicData relic, boolean stale, int mouseX, int mouseY) {
        boolean affordable = Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.experienceLevel >= RelicAbilities.REROLL_COST;
        int bg = affordable ? 0xFF2C2C32 : 0xFF232327;
        graphics.fill(left + 236, top + 120, left + 310, top + 138, bg);
        int edge = affordable ? 0xFF963030 : 0xFF4A4A50;
        graphics.fill(left + 236, top + 120, left + 310, top + 121, edge);
        graphics.fill(left + 236, top + 137, left + 310, top + 138, edge);
        graphics.fill(left + 236, top + 120, left + 237, top + 138, edge);
        graphics.fill(left + 309, top + 120, left + 310, top + 138, edge);
        int labelColor = affordable ? 0xFFE8B4B4 : 0xFF6A6A72;
        centered(graphics, Component.translatable("gui.fallenrelics.angel_ring.reroll").getString(),
                left + 273, top + 122, labelColor, true);
        centered(graphics, Component.translatable("gui.fallenrelics.angel_ring.reroll.cost").getString(),
                left + 273, top + 130, LABEL_DIM, false);
        if (inside(mouseX, mouseY, 236, 120, 74, 18)) {
            outline(graphics, 236, 120, 74, 18);
        }
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

    private void outline(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(left + x - 1, top + y - 1, left + x + w + 1, top + y, HOVER_CRIMSON);
        graphics.fill(left + x - 1, top + y + h, left + x + w + 1, top + y + h + 1, HOVER_CRIMSON);
        graphics.fill(left + x - 1, top + y, left + x, top + y + h, HOVER_CRIMSON);
        graphics.fill(left + x + w, top + y, left + x + w + 1, top + y + h, HOVER_CRIMSON);
    }

    private void plaque(GuiGraphics graphics, int row, String label, String text) {
        int y = top + 36 + row * 22;
        graphics.drawString(font, label, left + 240, y + 5, LABEL_DIM, false);
        graphics.drawString(font, text, left + 306 - font.width(text), y + 5, PLAQUE_COLOR, false);
    }

    private void centered(GuiGraphics graphics, String text, int centerX, int y, int color, boolean shadow) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, color, shadow);
    }

    private void tooltips(GuiGraphics graphics, int mouseX, int mouseY, RelicData relic, boolean stale) {
        Component tip = null;
        List<Component> body = null;
        for (int i = 0; i < RelicData.ABILITIES && tip == null; i++) {
            if (!inside(mouseX, mouseY, 42 + i * 56, 124, 40, 48)) {
                continue;
            }
            String key = i == 0 ? "aegis" : i == 1 ? "thirst" : "ascension";
            tip = Component.translatable("gui.fallenrelics.angel_ring.ability." + key);
            var list = new java.util.ArrayList<Component>();
            list.add(Component.translatable("gui.fallenrelics.angel_ring.ability." + key + ".desc"));
            if (!stale) {
                if (!relic.unlocked(i)) {
                    list.add(Component.translatable("gui.fallenrelics.angel_ring.locked_until",
                            relic.unlockLevel(i)));
                }
                list.add(Component.translatable("gui.fallenrelics.angel_ring.stars.ability", relic.abilityRarity(i)));
                list.add(Component.translatable("gui.fallenrelics.angel_ring.rank.line", relic.rank(i)));
                int next = relic.rank(i) + 1;
                if (next <= RelicData.MAX_RANK) {
                    if (next <= relic.rankCap()) {
                        list.add(Component.translatable("gui.fallenrelics.angel_ring.upgrade_click",
                                RelicData.rankCost(next)));
                    } else {
                        list.add(Component.translatable("message.fallenrelics.rank.cap"));
                    }
                }
                if (i == 2) {
                    int cd = AngelRingStatusClient.cooldown(2);
                    if (relic.unlocked(2) && relic.rank(2) > 0 && cd <= 0) {
                        list.add(Component.translatable("gui.fallenrelics.angel_ring.cast_click"));
                        list.add(Component.translatable("gui.fallenrelics.angel_ring.cast_hotkey",
                                RelicKeys.keyName()));
                    } else if (cd > 0) {
                        list.add(Component.translatable("gui.fallenrelics.angel_ring.cooldown", (cd + 19) / 20));
                    }
                } else {
                    list.add(Component.translatable("gui.fallenrelics.angel_ring.passive"));
                }
            }
            body = list;
        }
        if (tip == null && inside(mouseX, mouseY, 44, 74, 36, 10)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.stars", stale ? 0 : relic.rarity());
            body = List.of(Component.translatable("gui.fallenrelics.angel_ring.pips"));
        }
        if (tip == null && inside(mouseX, mouseY, 236, 120, 74, 18)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.reroll");
            body = List.of(
                    Component.translatable("gui.fallenrelics.angel_ring.reroll.desc"),
                    Component.translatable("gui.fallenrelics.angel_ring.reroll.cost"));
        }
        if (tip == null && inside(mouseX, mouseY, 236, 36, 74, 18)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.plaque.level");
            body = List.of(Component.translatable("gui.fallenrelics.angel_ring.plaque.level.desc"));
        }
        if (tip == null && inside(mouseX, mouseY, 236, 58, 74, 18)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.plaque.xp");
            body = List.of(Component.translatable("gui.fallenrelics.angel_ring.plaque.xp.desc"));
        }
        if (tip == null && inside(mouseX, mouseY, 236, 80, 74, 18)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.plaque.speed");
            body = List.of(Component.translatable("gui.fallenrelics.angel_ring.plaque.speed.desc"));
        }
        if (tip == null && inside(mouseX, mouseY, 175, 99, 40, 10)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.levelup.btn");
            body = List.of(
                    Component.translatable("gui.fallenrelics.angel_ring.levelup.desc"),
                    Component.literal(stale ? "--" : relic.xp() + " / " + relic.xpToNext()));
        }
        if (tip == null && inside(mouseX, mouseY, 236, 98, 74, 14)) {
            tip = Component.translatable("gui.fallenrelics.angel_ring.plaque.time");
            body = List.of(Component.literal(timeText()));
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

    /**
     * ЛКМ по карточке — улучшение за уровни, ПКМ — применение, клик по кнопке —
     * реролл. Анимация стартует в тот же кадр, что и клик (1.8.1).
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < RelicData.ABILITIES; i++) {
            if (inside((int) mouseX, (int) mouseY, 42 + i * 56, 124, 40, 48)) {
                if (button == 0) {
                    PacketDistributor.sendToServer(new RelicActionPayloads.Upgrade(i, viewId));
                    if (upgradeLooksValid(i)) {
                        flash[i] = 24;
                    }
                    AngelRingStatusClient.request(viewId);
                    return true;
                }
                if (button == 1 && i == 2) {
                    PacketDistributor.sendToServer(new RelicActionPayloads.Cast(2, viewId));
                    AngelRingStatusClient.request(viewId);
                    return true;
                }
            }
        }
        if (button == 0 && inside((int) mouseX, (int) mouseY, 175, 99, 40, 10)) {
            PacketDistributor.sendToServer(new RelicActionPayloads.LevelUp(viewId));
            if (AngelRingStatusClient.relic().canLevelUp()) {
                levelAnim = 36;
            }
            AngelRingStatusClient.request(viewId);
            return true;
        }
        if (button == 0 && inside((int) mouseX, (int) mouseY, 236, 120, 74, 18)) {
            PacketDistributor.sendToServer(new RelicActionPayloads.Reroll(viewId));
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().player
                    .experienceLevel >= RelicAbilities.REROLL_COST) {
                rerollAnim = 40;
            }
            AngelRingStatusClient.request(viewId);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** Локальная прикидка: вспышку улучшения зажигаем только когда сервер его примет. */
    private boolean upgradeLooksValid(int index) {
        if (Minecraft.getInstance().player == null) {
            return false;
        }
        RelicData relic = AngelRingStatusClient.relic();
        int next = relic.rank(index) + 1;
        return relic.unlocked(index)
                && next <= RelicData.MAX_RANK && next <= relic.rankCap()
                && Minecraft.getInstance().player.experienceLevel >= RelicData.rankCost(next);
    }

    /** Esc возвращает в тот же инвентарь, из которого открыт свиток. */
    @Override
    public void onClose() {
        if (this.returnTo != null) {
            this.minecraft.setScreen(this.returnTo);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
