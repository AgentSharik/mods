package dev.agentsharik.angelring.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import dev.agentsharik.angelring.AngelRingMod;
import dev.agentsharik.angelring.ModComponents;
import dev.agentsharik.angelring.ModItems;
import dev.agentsharik.angelring.RelicAbility;
import dev.agentsharik.angelring.RelicData;
import dev.agentsharik.angelring.RingLookup;
import dev.agentsharik.angelring.network.RelicActionPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Окно кольца (чёрно-серо-красный стиль): составная рама с колоннами, карточка кольца со звёздами редкости,
 * шкала опыта кольца, три способности (две активные и одна пассивная), справа плашки и кнопка переброса.
 * На заднем плане — пульсирующая дымка неправильной формы со светящимися «жилами» и искрами.
 *
 * Статичный рисунок — textures/gui/relic_ui.png (512x256, используется область 340x214), числа макета ниже
 * должны совпадать с этой картинкой. Окно рисуется в «логических» координатах и уменьшается, если экран маленький.
 */
public class RelicScreen extends Screen {
    private static final ResourceLocation UI = tex("textures/gui/relic_ui.png");
    private static final ResourceLocation HAZE = tex("textures/gui/relic_haze.png");
    private static final ResourceLocation GLOW = tex("textures/gui/relic_haze_glow.png");
    private static final ResourceLocation ICONS = tex("textures/gui/ability_icons.png");
    private static final ResourceLocation STARS = tex("textures/gui/rarity_stars.png");

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, path);
    }

    private static final int W = 340, H = 214;
    private static final int HAZE_W = 512, HAZE_H = 384;

    // ---- макет (совпадает с relic_ui.png) ----
    private static final int PORTRAIT_X = 34, PORTRAIT_Y = 32;
    private static final int STAR_X0 = 35, STAR_Y = 86;
    private static final int[] CARD_X = {35, 105, 175};
    private static final int CARD_Y = 134, CARD_W = 62, CARD_H = 48;
    private static final int[] PLAQUE_X = {272, 276, 272};
    private static final int[] PLAQUE_Y = {60, 90, 120};
    private static final int REROLL_X = 272, REROLL_Y = 152, REROLL_W = 62, REROLL_H = 20;

    private static final float REROLL_SPIN = 0.9f;     // секунд «пересборки» звёзд
    private static final float REROLL_TOTAL = 1.4f;    // полная длительность анимации переброса
    private static final float UPGRADE_TOTAL = 1.1f;   // длительность анимации улучшения способности

    private static final int RED = 0xFFC0394A;
    private static final int TEXT = 0xFFE8DCD8;
    private static final int TEXT_DIM = 0xFF8F868C;
    private static final int TEXT_OFF = 0xFF55494E;
    private static final int XP_GREEN = 0xFF80FF20;

    private record Hit(float x, float y, float w, float h, Runnable action, Supplier<List<Component>> tooltip) {}

    private final Screen previous;
    private final AbstractContainerMenu menu;
    private final int menuSlot;
    private final List<Hit> hits = new ArrayList<>();
    private final float[][] embers = new float[34][6];

    private float scale = 1f;
    private float ox;
    private float oy;
    private int selected = 0;

    // состояние анимаций
    private long rerollStart = -1L;
    private int lastRerolls = -1;
    private final long[] upgradeStart = {-1L, -1L, -1L};
    private final int[] lastLevels = {-1, -1, -1};

    /**
     * @param previous экран, который нужно вернуть после закрытия (инвентарь), либо null
     * @param menu     меню, в котором лежит кольцо (для menuSlot >= 0), либо null
     * @param menuSlot номер слота меню; -1 — надетое кольцо
     */
    public RelicScreen(Screen previous, AbstractContainerMenu menu, int menuSlot) {
        super(Component.translatable("item.angelring.angel_ring"));
        this.previous = previous;
        this.menu = menu;
        this.menuSlot = menuSlot;
        Random random = new Random(1337L);
        for (float[] e : embers) {
            e[0] = random.nextFloat() * (W + 120) - 60;   // x
            e[1] = random.nextFloat() * (H + 120);        // стартовая высота
            e[2] = 6f + random.nextFloat() * 14f;         // скорость подъёма
            e[3] = 0.4f + random.nextFloat() * 0.9f;      // скорость покачивания
            e[4] = random.nextFloat() * 6.28f;            // фаза
            e[5] = 0.8f + random.nextFloat() * 1.8f;      // скорость мерцания
        }
    }

    // ------------------------------------------------------------------ данные

    private ItemStack stack() {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return ItemStack.EMPTY;
        }
        Optional<ItemStack> found = RingLookup.resolve(player, menu, menuSlot);
        return found.orElse(ItemStack.EMPTY);
    }

    private RelicData data(ItemStack stack) {
        return stack.isEmpty() ? RelicData.EMPTY : stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
    }

    private int playerLevels() {
        return minecraft.player == null ? 0 : minecraft.player.experienceLevel;
    }

    private boolean creative() {
        return minecraft.player != null && minecraft.player.isCreative();
    }

    // ------------------------------------------------------------------ жизненный цикл и ввод

    @Override
    protected void init() {
        scale = Math.min(1f, Math.min((width - 8f) / W, (height - 8f) / H));
        ox = (width - W * scale) / 2f;
        oy = (height - H * scale) / 2f;
    }

    @Override
    public void tick() {
        super.tick();
        if (stack().isEmpty()) {
            onClose(); // кольцо убрали из слота — закрываем окно
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(previous);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void send(int action, int arg) {
        PacketDistributor.sendToServer(new RelicActionPayload(action, arg, menuSlot));
    }

    private void doReroll() {
        send(RelicData.ACTION_REROLL, 0);
        rerollStart = Util.getMillis();
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 0.8f));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            float lx = (float) ((mouseX - ox) / scale);
            float ly = (float) ((mouseY - oy) / scale);
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (h.action() != null && inside(lx, ly, h)) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    h.action().run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static boolean inside(float x, float y, Hit h) {
        return x >= h.x() && x < h.x() + h.w() && y >= h.y() && y < h.y() + h.h();
    }

    private void hit(float x, float y, float w, float h, Runnable action, Supplier<List<Component>> tooltip) {
        hits.add(new Hit(x, y, w, h, action, tooltip));
    }

    // ------------------------------------------------------------------ отрисовка

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // фон мира не затемняем и не размываем: дымка лежит прямо поверх игры
        hits.clear();
        ItemStack stack = stack();
        RelicData d = data(stack);
        long now = Util.getMillis();
        float t = (now % 1_000_000L) / 1000f;
        float mx = (mouseX - ox) / scale;
        float my = (mouseY - oy) / scale;
        trackChanges(d, now);

        g.pose().pushPose();
        g.pose().translate(ox, oy, 0f);
        g.pose().scale(scale, scale, 1f);

        drawHaze(g, t);
        g.blit(UI, 0, 0, 0, 0, W, H, 512, 256);
        drawHeader(g, d, stack, now);
        drawProgress(g, d);
        drawPlaques(g, d, mx, my);
        drawAbilities(g, d, mx, my, now);
        drawRerollFx(g, now);
        for (int i = 0; i < RelicData.ABILITY_COUNT; i++) {
            drawUpgradeFx(g, i, now);
        }
        drawFooter(g, d);

        g.pose().popPose();

        // подсказка рисуется в экранных координатах
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (h.tooltip() != null && inside(mx, my, h)) {
                g.renderComponentTooltip(font, h.tooltip().get(), mouseX, mouseY);
                break;
            }
        }
    }

    /** Замечаем, что сервер подтвердил улучшение или реролл, и запускаем анимации. */
    private void trackChanges(RelicData d, long now) {
        if (lastRerolls >= 0 && d.rerolls() != lastRerolls && rerollStart < 0) {
            rerollStart = now;
        }
        lastRerolls = d.rerolls();
        for (int i = 0; i < RelicData.ABILITY_COUNT; i++) {
            int level = d.abilityLevel(i);
            if (lastLevels[i] >= 0 && level > lastLevels[i]) {
                upgradeStart[i] = now;
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 1.3f));
            }
            lastLevels[i] = level;
        }
        if (rerollStart >= 0 && (now - rerollStart) / 1000f > REROLL_TOTAL) {
            rerollStart = -1L;
        }
    }

    /** Рисует большую текстуру дымки с центром в (cx, cy): поворот, масштаб, прозрачность, обычное или аддитивное смешивание. */
    private void blob(GuiGraphics g, ResourceLocation texture, float cx, float cy, float s, float degrees,
                      float alpha, boolean additive) {
        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0f);
        g.pose().mulPose(Axis.ZP.rotationDegrees(degrees));
        g.pose().scale(s, s, 1f);
        RenderSystem.setShaderColor(1f, 1f, 1f, Mth.clamp(alpha, 0f, 1f));
        g.blit(texture, -HAZE_W / 2, -HAZE_H / 2, 0, 0, HAZE_W, HAZE_H, HAZE_W, HAZE_H);
        g.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /** Фоновая дымка: основа пульсирует и колышется, светящиеся жилы переливаются, снизу вверх летят искры. */
    private void drawHaze(GuiGraphics g, float t) {
        float cx = W / 2f;
        float cy = H / 2f;
        blob(g, HAZE, cx, cy, 1.0f + 0.03f * Mth.sin(t * 0.55f), 2.5f * Mth.sin(t * 0.30f),
                0.82f + 0.15f * Mth.sin(t * 1.3f), false);
        for (int layer = 0; layer < 2; layer++) {
            float dir = layer == 0 ? -1f : 1f;
            float pulse = 0.5f + 0.5f * Mth.sin(t * (layer == 0 ? 0.9f : 1.35f) + layer * 1.9f);
            blob(g, GLOW, cx, cy, (layer == 0 ? 1.02f : 1.10f) + 0.035f * Mth.sin(t * 0.45f + layer * 1.3f),
                    dir * 3.5f * Mth.sin(t * 0.22f + layer * 2.1f),
                    (layer == 0 ? 0.30f : 0.16f) + 0.38f * pulse, true);
        }
        float range = H + 120f;
        for (float[] e : embers) {
            float y = (e[1] - t * e[2]) % range;
            if (y < 0) {
                y += range;
            }
            float x = e[0] + 7f * Mth.sin(t * e[3] + e[4]);
            int a = (int) (50 + 170 * (0.5f + 0.5f * Mth.sin(t * e[5] + e[4])));
            int px = Math.round(x);
            int py = Math.round(y - 60f);
            g.fill(px, py, px + 1, py + 1, (a << 24) | 0xFF5A3C);
        }
    }

    private void drawHeader(GuiGraphics g, RelicData d, ItemStack stack, long now) {
        // бейдж уровня кольца над карточкой
        text(g, Component.literal(String.valueOf(d.level())), 59, 28, 1.0f, TEXT, true, 1);

        // портрет: кольцо крупно
        g.pose().pushPose();
        g.pose().translate(PORTRAIT_X + 5, PORTRAIT_Y + 5, 0f);
        g.pose().scale(2.5f, 2.5f, 1f);
        g.renderItem(stack.isEmpty() ? new ItemStack(ModItems.ANGEL_RING.get()) : stack, 0, 0);
        g.pose().popPose();

        drawPortraitStars(g, d, now);

        // название, подзаголовок и описание
        text(g, Component.translatable("item.angelring.angel_ring").withStyle(ChatFormatting.BOLD), 94, 32, 1.1f,
                0xFFF2D9D3, true, 0);
        text(g, Component.translatable("gui.angelring.subtitle"), 94, 44, 0.6f, 0xFFB05058, false, 0);
        float y = 54f;
        for (FormattedCharSequence line : font.split(Component.translatable("item.angelring.angel_ring.desc"),
                (int) (146 / 0.7f))) {
            g.pose().pushPose();
            g.pose().translate(94, y, 0f);
            g.pose().scale(0.7f, 0.7f, 1f);
            g.drawString(font, line, 0, 0, 0xFFB9AFB4, false);
            g.pose().popPose();
            y += 7.5f;
        }

        // две плашки в шапке
        text(g, Component.translatable("gui.angelring.chip_level", playerLevels()), 130, 84, 0.8f, TEXT, false, 1);
        String xp = d.maxed() ? "MAX" : d.xp() + " / " + d.xpToNext();
        text(g, Component.translatable("gui.angelring.chip_xp", xp), 206, 84, 0.8f, TEXT, false, 1);
    }

    /** Ряд из шести звёзд редкости под иконкой. Во время переброса звёзды «пересобираются» и вспыхивают. */
    private void drawPortraitStars(GuiGraphics g, RelicData d, long now) {
        int real = d.ringStars();
        float t = rerollStart < 0 ? 99f : (now - rerollStart) / 1000f;
        for (int i = 0; i < RelicData.MAX_STARS; i++) {
            boolean filled;
            float flash = 0f;
            if (t < REROLL_SPIN) {
                filled = ((now / 70L) + i * 5L) % 3L != 0L; // мерцание, пока идёт «пересборка»
            } else {
                filled = i < real;
                float since = t - (REROLL_SPIN + 0.05f * i);
                if (since >= 0f && since < 0.3f) {
                    flash = 1f - since / 0.3f;
                }
            }
            int x = STAR_X0 + i * 8;
            if (flash > 0f) {
                int a = (int) (flash * 200);
                g.fill(x - 2, STAR_Y - 2, x + 10, STAR_Y + 10, ((a / 3) << 24) | 0xFF5A4A);
                g.fill(x - 1, STAR_Y - 1, x + 9, STAR_Y + 9, ((a / 2) << 24) | 0xFFA090);
            }
            g.blit(STARS, x, STAR_Y, filled ? 0 : 8, 0, 8, 8, 16, 8);
        }
    }

    /** Шкала опыта кольца (опыт копится вместе с опытом игрока). */
    private void drawProgress(GuiGraphics g, RelicData d) {
        int bx = 34, by = 103, bw = 204, bh = 8;
        float ratio = d.maxed() ? 1f : Math.min(1f, d.xp() / (float) d.xpToNext());
        int fill = Math.round(bw * ratio);
        if (fill > 0) {
            g.fillGradient(bx, by, bx + fill, by + bh, d.maxed() ? 0xFFFF6A5A : 0xFFE0524A, 0xFF7A1424);
        }
        String label = d.maxed() ? Component.translatable("gui.angelring.max").getString() : (int) (ratio * 100f) + "%";
        text(g, Component.literal(label), 136, 103, 0.9f, 0xFFFFFFFF, true, 1);
    }

    private void drawPlaques(GuiGraphics g, RelicData d, float mx, float my) {
        text(g, Component.literal("F A L L E N"), 304, 22, 0.6f, 0xFFB05058, false, 1);
        text(g, Component.literal("RELICS"), 304, 31, 1.5f, 0xFFE05A4A, true, 1);

        plaque(g, 0, Component.translatable("gui.angelring.stat.level"), d.level() + " / " + RelicData.MAX_LEVEL);
        plaque(g, 1, Component.translatable("gui.angelring.stat.flight"),
                "+" + Math.round((d.flightMultiplier() - 1f) * 100f) + "%");
        plaque(g, 2, Component.translatable("gui.angelring.stat.rarity"), d.ringStars() + " / " + RelicData.MAX_STARS);
        hit(PLAQUE_X[1], PLAQUE_Y[1], 62, 26, null, () -> List.of(
                Component.translatable("gui.angelring.stat.flight.tip").withStyle(ChatFormatting.GRAY)));

        // кнопка переброса редкости
        boolean afford = creative() || playerLevels() >= RelicData.REROLL_COST;
        boolean hover = mx >= REROLL_X && mx < REROLL_X + REROLL_W && my >= REROLL_Y && my < REROLL_Y + REROLL_H;
        if (hover && afford) {
            g.fill(REROLL_X + 2, REROLL_Y + 2, REROLL_X + REROLL_W - 2, REROLL_Y + REROLL_H - 2, 0x55C0394A);
        }
        text(g, Component.translatable("gui.angelring.reroll"), REROLL_X + REROLL_W / 2f, REROLL_Y + 4, 0.85f,
                afford ? 0xFFFFFFFF : TEXT_OFF, afford, 1);
        text(g, Component.translatable("gui.angelring.reroll.cost", RelicData.REROLL_COST), REROLL_X + REROLL_W / 2f,
                REROLL_Y + 12, 0.6f, afford ? XP_GREEN : 0xFFB04040, false, 1);
        hit(REROLL_X, REROLL_Y, REROLL_W, REROLL_H, afford ? this::doReroll : null, () -> List.of(
                Component.translatable("gui.angelring.reroll.tip1").withStyle(ChatFormatting.GOLD),
                Component.translatable("gui.angelring.reroll.tip2").withStyle(ChatFormatting.GRAY),
                Component.translatable(afford ? "gui.angelring.reroll.tip3" : "gui.angelring.reroll.need",
                        RelicData.REROLL_COST).withStyle(afford ? ChatFormatting.GREEN : ChatFormatting.RED)));

        text(g, Component.translatable("gui.angelring.rarity_note"), 304, 177, 0.55f, 0xFF6F676C, false, 1);
    }

    private void plaque(GuiGraphics g, int i, Component label, String value) {
        float x = PLAQUE_X[i];
        float y = PLAQUE_Y[i];
        text(g, label, x + 6, y + 5, 0.6f, TEXT_DIM, false, 0);
        text(g, Component.literal(value), x + 6, y + 13, 1.0f, TEXT, true, 0);
    }

    private void drawAbilities(GuiGraphics g, RelicData d, float mx, float my, long now) {
        text(g, Component.translatable("gui.angelring.abilities"), 34, 124, 0.6f, TEXT_DIM, false, 0);
        text(g, Component.translatable("gui.angelring.abilities_hint",
                        ClientModEvents.ABILITY_1.getTranslatedKeyMessage(), ClientModEvents.ABILITY_2.getTranslatedKeyMessage()),
                238, 124, 0.6f, 0xFFD08088, false, 2);

        long gameTime = minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        for (int i = 0; i < RelicData.ABILITY_COUNT; i++) {
            RelicAbility ability = RelicAbility.byId(i);
            int cx = CARD_X[i];
            int cy = CARD_Y;
            int level = d.abilityLevel(i);
            int stars = d.abilityStars(i);
            boolean hover = mx >= cx && mx < cx + CARD_W && my >= cy && my < cy + CARD_H;
            if (hover) {
                selected = i;
            }
            if (selected == i) {
                g.renderOutline(cx, cy, CARD_W, CARD_H, 0x55FFFFFF);
            }

            // бейдж: клавиша активной способности или значок пассивной
            Component badge = ability.active
                    ? (i == 0 ? ClientModEvents.ABILITY_1 : ClientModEvents.ABILITY_2).getTranslatedKeyMessage()
                    : Component.translatable("gui.angelring.badge_passive");
            text(g, badge, cx + 8, cy + 5, 0.8f, level > 0 ? TEXT : TEXT_OFF, false, 1);

            // иконка 32x32 (16x16 в два раза крупнее); неизученная — затемнена
            g.pose().pushPose();
            g.pose().translate(cx + 15, cy + 4, 0f);
            g.pose().scale(2f, 2f, 1f);
            if (level <= 0) {
                RenderSystem.setShaderColor(0.32f, 0.30f, 0.36f, 1f);
            }
            g.blit(ICONS, 0, 0, i * 16, 0, 16, 16, 48, 16);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            g.pose().popPose();

            // уровень способности — столбик слева, редкость — мини-звёзды под названием
            for (int p = 0; p < RelicAbility.MAX_LEVEL; p++) {
                int py = cy + 33 - p * 6;
                g.fill(cx + 4, py, cx + 9, py + 4, p < level ? RED : 0xFF2A1A20);
            }
            text(g, Component.translatable("gui.angelring.ability." + ability.key + ".name"),
                    cx + CARD_W / 2f, cy + 37, 0.65f, level > 0 ? TEXT : TEXT_OFF, false, 1);
            for (int s = 0; s < RelicData.MAX_STARS; s++) {
                g.pose().pushPose();
                g.pose().translate(cx + 16 + s * 5, cy + 43, 0f);
                g.pose().scale(0.5f, 0.5f, 1f);
                g.blit(STARS, 0, 0, s < stars ? 0 : 8, 0, 8, 8, 16, 8);
                g.pose().popPose();
            }

            // перезарядка активной способности
            long left = d.cooldownReadyAt(i) - gameTime;
            if (ability.active && level > 0 && left > 0) {
                g.fill(cx + 2, cy + 2, cx + CARD_W - 2, cy + CARD_H - 2, 0xB0000000);
                text(g, Component.literal(((left + 19) / 20) + "s"), cx + CARD_W / 2f, cy + 16, 1.2f, 0xFFFFFFFF, true, 1);
            }

            // кнопка «+» (изучить / улучшить за уровни опыта игрока) и цена под ней
            final int abilityId = i;
            int cost = d.upgradeCost(i);
            boolean unlocked = d.canUpgrade(i);
            boolean affordable = creative() || playerLevels() >= cost;
            boolean canBuy = cost > 0 && unlocked && affordable;
            float bx = cx + 48;
            float by = cy + 3;
            if (canBuy && mx >= bx && mx < bx + 12 && my >= by && my < by + 12) {
                g.fill((int) bx + 1, (int) by + 1, (int) bx + 11, (int) by + 11, 0x66C0394A);
            }
            text(g, Component.literal(cost < 0 ? "\u2022" : "+"), bx + 6, by + 2, 1.0f,
                    cost < 0 ? RED : (canBuy ? TEXT : TEXT_OFF), false, 1);
            if (cost > 0) {
                text(g, Component.literal(String.valueOf(cost)), bx + 6, by + 15, 0.8f,
                        !unlocked ? TEXT_OFF : (affordable ? XP_GREEN : 0xFFB04040), false, 1);
            }

            hit(cx, cy, CARD_W, CARD_H, null, () -> cardTooltip(ability, d));
            hit(bx, by, 12, 12, canBuy ? () -> send(RelicData.ACTION_UPGRADE, abilityId) : null,
                    () -> buyTooltip(ability, d, cost, unlocked, affordable));
        }
    }

    // ------------------------------------------------------------------ анимации

    /** Переброс: багровый вихрь крутится вокруг кольца, звёзды ниже пересобираются и вспыхивают. */
    private void drawRerollFx(GuiGraphics g, long now) {
        if (rerollStart < 0) {
            return;
        }
        float t = (now - rerollStart) / 1000f;
        float p = t / REROLL_TOTAL;
        float k = p < 0.12f ? p / 0.12f : (p > 0.65f ? Math.max(0f, (1f - p) / 0.35f) : 1f);
        float cx = PORTRAIT_X + 25f;
        float cy = PORTRAIT_Y + 25f;
        for (int n = 0; n < 2; n++) {
            blob(g, HAZE, cx, cy, 0.24f + 0.06f * n + 0.02f * Mth.sin(t * 9f), -t * (170f + 90f * n), 0.85f * k, false);
        }
        for (int n = 0; n < 3; n++) {
            blob(g, GLOW, cx, cy, 0.20f + 0.07f * n, t * (240f - 110f * n), 0.75f * k, true);
        }
        for (int e = 0; e < 18; e++) {
            float angle = t * 5f + e * 0.35f;
            float radius = 30f + 9f * Mth.sin(e + t * 3f);
            int px = Math.round(cx + Mth.cos(angle) * radius);
            int py = Math.round(cy + Mth.sin(angle) * radius * 0.8f);
            g.fill(px, py, px + 2, py + 2, (((int) (220 * k)) << 24) | 0xFF4A3A);
        }
    }

    /** Улучшение способности: тёмно-красная дымка сгущается над карточкой, затем следует вспышка. */
    private void drawUpgradeFx(GuiGraphics g, int i, long now) {
        if (upgradeStart[i] < 0) {
            return;
        }
        float t = (now - upgradeStart[i]) / 1000f;
        float p = t / UPGRADE_TOTAL;
        if (p >= 1f) {
            upgradeStart[i] = -1L;
            return;
        }
        float cx = CARD_X[i] + CARD_W / 2f;
        float cy = CARD_Y + CARD_H / 2f;
        if (p < 0.55f) {
            float q = p / 0.55f;
            blob(g, HAZE, cx, cy, 0.50f - 0.30f * q, t * 80f, 0.2f + 0.7f * q, false);
            blob(g, GLOW, cx, cy, 0.38f - 0.26f * q, -t * 140f, 0.15f + 0.5f * q, true);
        } else {
            float q = (p - 0.55f) / 0.45f;
            float fade = 1f - q;
            blob(g, GLOW, cx, cy, 0.14f + 0.34f * q, t * 60f, 0.95f * fade, true);
            blob(g, HAZE, cx, cy, 0.18f + 0.14f * q, -t * 50f, 0.6f * fade, false);
            g.fill(CARD_X[i], CARD_Y, CARD_X[i] + CARD_W, CARD_Y + CARD_H, (((int) (150 * fade * fade)) << 24) | 0xFF3040);
            g.renderOutline(CARD_X[i] - 1, CARD_Y - 1, CARD_W + 2, CARD_H + 2,
                    (((int) (255 * fade)) << 24) | 0xE05A4A);
        }
    }

    // ------------------------------------------------------------------ подсказки и тексты способностей

    private List<Component> cardTooltip(RelicAbility ability, RelicData d) {
        int id = ability.id;
        int level = d.abilityLevel(id);
        int stars = d.abilityStars(id);
        List<Component> tip = new ArrayList<>();
        tip.add(Component.translatable("gui.angelring.ability." + ability.key + ".name")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE));
        tip.add(Component.translatable(ability.active ? "gui.angelring.type_active" : "gui.angelring.type_passive")
                .withStyle(ChatFormatting.DARK_RED));
        tip.add(Component.translatable("gui.angelring.rarity_line", starsText(stars)).withStyle(ChatFormatting.RED));
        tip.add(Component.translatable("gui.angelring.level_of", level, RelicAbility.MAX_LEVEL).withStyle(ChatFormatting.GOLD));
        tip.add(descText(ability, stars, Math.max(1, level)).withStyle(ChatFormatting.GRAY));
        if (level <= 0) {
            tip.add(Component.translatable("gui.angelring.not_learned").withStyle(ChatFormatting.DARK_RED));
        }
        return tip;
    }

    private List<Component> buyTooltip(RelicAbility ability, RelicData d, int cost, boolean unlocked, boolean affordable) {
        List<Component> tip = new ArrayList<>();
        if (cost < 0) {
            tip.add(Component.translatable("gui.angelring.max_level").withStyle(ChatFormatting.GOLD));
            return tip;
        }
        int level = d.abilityLevel(ability.id);
        tip.add(Component.translatable(level == 0 ? "gui.angelring.learn" : "gui.angelring.improve", level + 1)
                .withStyle(ChatFormatting.WHITE));
        tip.add(descText(ability, d.abilityStars(ability.id), level + 1).withStyle(ChatFormatting.GRAY));
        if (!unlocked) {
            tip.add(Component.translatable("gui.angelring.need_ring_level", (level) * 3).withStyle(ChatFormatting.RED));
        } else {
            tip.add(Component.translatable("gui.angelring.cost_levels", cost)
                    .withStyle(affordable ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        return tip;
    }

    private static String starsText(int n) {
        return "\u2605".repeat(n) + "\u2606".repeat(RelicData.MAX_STARS - n);
    }

    private static String num(float v) {
        return Math.abs(v - Math.round(v)) < 0.05f ? String.valueOf(Math.round(v)) : String.format(Locale.ROOT, "%.1f", v);
    }

    /** Полное описание способности с цифрами для заданной редкости и уровня. */
    private MutableComponent descText(RelicAbility ability, int stars, int level) {
        String key = "gui.angelring.ability." + ability.key + ".desc";
        return switch (ability) {
            case BONDS -> Component.translatable(key, num(RelicAbility.bondsRadius(stars)),
                    num(RelicAbility.bondsSeconds(stars, level)));
            case SANCTUARY -> Component.translatable(key, num(RelicAbility.sanctuarySeconds(stars, level)),
                    num(RelicAbility.sanctuaryRadius(stars)));
            case BLOOD_WARD -> Component.translatable(key, Math.round(RelicAbility.wardChance(stars, level) * 100f),
                    num(RelicAbility.wardHeal(level)));
        };
    }

    /** Короткое описание для строки внизу окна. */
    private MutableComponent shortText(RelicAbility ability, int stars, int level) {
        String key = "gui.angelring.ability." + ability.key + ".short";
        return switch (ability) {
            case BONDS -> Component.translatable(key, num(RelicAbility.bondsRadius(stars)),
                    num(RelicAbility.bondsSeconds(stars, level)));
            case SANCTUARY -> Component.translatable(key, num(RelicAbility.sanctuarySeconds(stars, level)));
            case BLOOD_WARD -> Component.translatable(key, Math.round(RelicAbility.wardChance(stars, level) * 100f));
        };
    }

    private void drawFooter(GuiGraphics g, RelicData d) {
        RelicAbility ability = RelicAbility.byId(selected);
        int level = d.abilityLevel(selected);
        Component name = Component.translatable("gui.angelring.ability." + ability.key + ".name");
        Component line = Component.empty().append(name).append(" \u2014 ")
                .append(shortText(ability, d.abilityStars(selected), Math.max(1, level)));
        text(g, line, 136, 185, 0.55f, 0xFFA59AA0, false, 1);
        text(g, Component.translatable("gui.angelring.hint"), 30, 205, 0.55f, 0xFF6F676C, false, 0);
    }

    // ------------------------------------------------------------------ текст

    /** align: 0 — слева, 1 — по центру, 2 — справа. */
    private void text(GuiGraphics g, Component c, float x, float y, float s, int color, boolean shadow, int align) {
        float w = font.width(c) * s;
        float px = align == 1 ? x - w / 2f : align == 2 ? x - w : x;
        g.pose().pushPose();
        g.pose().translate(px, y, 0f);
        g.pose().scale(s, s, 1f);
        g.drawString(font, c, 0, 0, color, shadow);
        g.pose().popPose();
    }
}
