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
 * Статичный рисунок — textures/gui/relic_ui.png (512x512, используется область 448x290), числа макета ниже
 * должны совпадать с этой картинкой. Окно рисуется в «логических» координатах и уменьшается, если экран маленький;
 * любой текст автоматически ужимается, чтобы не вылезать за свою рамку.
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

    /** UI_H — высота картинки, H — высота всего окна (снизу ещё есть плашка с подсказками). */
    private static final int W = 448, UI_H = 290, H = 320;
    private static final int HAZE_W = 512, HAZE_H = 384;

    // ---- макет (совпадает с relic_ui.png) ----
    private static final int PORTRAIT_X = 38, PORTRAIT_Y = 40, PORTRAIT_SIZE = 64;
    private static final int STAR_X0 = 41, STAR_Y = 110, STAR_STEP_PX = 10;
    private static final int[] CARD_X = {38, 130, 222};
    private static final int CARD_Y = 177, CARD_W = 86, CARD_H = 78;
    private static final int[] PLAQUE_X = {346, 350, 346};
    private static final int[] PLAQUE_Y = {68, 108, 148};
    private static final int PLAQUE_W = 92, PLAQUE_H = 34;
    private static final int REROLL_X = 346, REROLL_Y = 190, REROLL_W = 92, REROLL_H = 30;

    private static final float REROLL_SPIN = 0.9f;     // секунд до начала смены звёзд
    private static final float REROLL_TOTAL = 1.5f;    // полная длительность анимации переброса
    private static final float UPGRADE_TOTAL = 1.1f;   // длительность анимации улучшения способности
    private static final float STAR_STEP = 0.07f;      // задержка между сменами соседних звёзд

    private static final int RED = 0xFFC0394A;
    private static final int TEXT = 0xFFE8DCD8;
    private static final int TEXT_DIM = 0xFFA99FA5;
    private static final int TEXT_OFF = 0xFF6A5E64;
    private static final int XP_GREEN = 0xFF80FF20;
    private static final int HINT = 0xFFD6CACE;

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
    private int rerollOldStars = 0;
    private int lastRerolls = -1;
    private int lastRingStars = -1;
    private final long[] upgradeStart = {-1L, -1L, -1L};
    private final int[] lastUpgrades = {-1, -1, -1};
    private final int[] lastUnlocked = {-1, -1, -1};
    private long levelStart = -1L;
    private int lastRingLevel = -1;

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
            e[1] = random.nextFloat() * (UI_H + 120);     // стартовая высота
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
        rerollOldStars = data(stack()).ringStars(); // до ответа сервера показываем старые звёзды
        rerollStart = Util.getMillis();
        send(RelicData.ACTION_REROLL, 0);
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
        g.blit(UI, 0, 0, 0, 0, W, UI_H, 512, 512);
        drawHeader(g, d, stack);
        drawProgress(g, d, mx, my, now);
        drawPlaques(g, d, mx, my);
        drawAbilities(g, d, mx, my, now);

        // Эффекты рисуем выше предметов (у предметов в GUI z около 150): дым закрывает иконку кольца и бейдж уровня.
        g.pose().pushPose();
        g.pose().translate(0f, 0f, 300f);
        drawRerollFx(g, now);
        for (int i = 0; i < RelicData.ABILITY_COUNT; i++) {
            drawUpgradeFx(g, i, now);
        }
        drawLevelFx(g, now);
        g.pose().popPose();

        // звёзды — поверх дыма, чтобы смена звёзд была видна
        g.pose().pushPose();
        g.pose().translate(0f, 0f, 320f);
        drawPortraitStars(g, d, now);
        g.pose().popPose();

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
            rerollOldStars = lastRingStars >= 0 ? lastRingStars : d.ringStars();
        }
        lastRerolls = d.rerolls();
        lastRingStars = rerollStart < 0 ? d.ringStars() : lastRingStars;
        for (int i = 0; i < RelicData.ABILITY_COUNT; i++) {
            int upgrades = d.abilityUpgrades().get(i);
            int unlocked = d.unlocked(i) ? 1 : 0;
            boolean upgraded = lastUpgrades[i] >= 0 && upgrades > lastUpgrades[i];
            boolean opened = lastUnlocked[i] >= 0 && unlocked > lastUnlocked[i];
            if (upgraded || opened) {
                upgradeStart[i] = now;
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, opened ? 0.8f : 1.3f));
            }
            lastUpgrades[i] = upgrades;
            lastUnlocked[i] = unlocked;
        }
        if (lastRingLevel >= 0 && d.level() > lastRingLevel) {
            levelStart = now;
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 0.9f));
        }
        lastRingLevel = d.level();
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
        float cy = UI_H / 2f;
        blob(g, HAZE, cx, cy, 1.35f + 0.04f * Mth.sin(t * 0.55f), 2.5f * Mth.sin(t * 0.30f),
                0.82f + 0.15f * Mth.sin(t * 1.3f), false);
        for (int layer = 0; layer < 2; layer++) {
            float dir = layer == 0 ? -1f : 1f;
            float pulse = 0.5f + 0.5f * Mth.sin(t * (layer == 0 ? 0.9f : 1.35f) + layer * 1.9f);
            blob(g, GLOW, cx, cy, (layer == 0 ? 1.37f : 1.47f) + 0.05f * Mth.sin(t * 0.45f + layer * 1.3f),
                    dir * 3.5f * Mth.sin(t * 0.22f + layer * 2.1f),
                    (layer == 0 ? 0.30f : 0.16f) + 0.38f * pulse, true);
        }
        float range = UI_H + 120f;
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

    private void drawHeader(GuiGraphics g, RelicData d, ItemStack stack) {
        // бейдж уровня кольца над карточкой
        text(g, Component.literal(String.valueOf(d.level())), 70, 35, 1.1f, TEXT, true, 1);

        // портрет: кольцо крупно (целый масштаб 3x, чтобы пиксели не «плыли»)
        g.pose().pushPose();
        g.pose().translate(PORTRAIT_X + 8, PORTRAIT_Y + 8, 0f);
        g.pose().scale(3f, 3f, 1f);
        g.renderItem(stack.isEmpty() ? new ItemStack(ModItems.ANGEL_RING.get()) : stack, 0, 0);
        g.pose().popPose();

        // название, подзаголовок и описание (до четырёх строк обычного размера)
        fit(g, Component.translatable("item.angelring.angel_ring").withStyle(ChatFormatting.BOLD), 116, 40, 194, 1.3f,
                0xFFF2D9D3, true, 0);
        fit(g, Component.translatable("gui.angelring.subtitle"), 116, 55, 194, 0.8f, 0xFFD0606A, false, 0);
        wrapped(g, Component.translatable("item.angelring.angel_ring.desc"), 116, 68, 194, 4, 0xFFC4BABF);

        // две плашки в шапке (96 и 94 px шириной)
        fit(g, Component.translatable("gui.angelring.chip_level", playerLevels()), 164, 112, 88, 0.9f, TEXT, false, 1);
        String xp = d.maxed() ? "MAX  (" + d.xp() + ")" : d.xp() + " / " + d.xpToNext();
        fit(g, Component.translatable("gui.angelring.chip_xp", xp), 265, 112, 86, 0.9f, TEXT, false, 1);
    }

    /** Должна ли звезда idx быть заполнена прямо сейчас: до конца анимации — старое значение, затем меняется по очереди. */
    private boolean starFilled(int idx, int realStars, long now) {
        if (rerollStart < 0) {
            return idx < realStars;
        }
        float t = (now - rerollStart) / 1000f;
        return t >= REROLL_SPIN + STAR_STEP * idx ? idx < realStars : idx < rerollOldStars;
    }

    /** Ряд из шести звёзд редкости под иконкой. Во время переброса звёзды меняются по очереди и вспыхивают. */
    private void drawPortraitStars(GuiGraphics g, RelicData d, long now) {
        int real = d.ringStars();
        float t = rerollStart < 0 ? 99f : (now - rerollStart) / 1000f;
        for (int i = 0; i < RelicData.MAX_STARS; i++) {
            int x = STAR_X0 + i * STAR_STEP_PX;
            float since = t - (REROLL_SPIN + STAR_STEP * i);
            if (rerollStart >= 0 && since >= 0f && since < 0.3f) {
                int a = (int) ((1f - since / 0.3f) * 200);
                g.fill(x - 2, STAR_Y - 2, x + 10, STAR_Y + 10, ((a / 3) << 24) | 0xFF5A4A);
                g.fill(x - 1, STAR_Y - 1, x + 9, STAR_Y + 9, ((a / 2) << 24) | 0xFFA090);
            }
            g.blit(STARS, x, STAR_Y, starFilled(i, real, now) ? 0 : 8, 0, 8, 8, 16, 8);
        }
    }

    /**
     * Шкала опыта кольца и кнопка «Повысить уровень». Опыт копится всегда (в том числе сверх порога);
     * когда его хватает на следующий уровень, кнопка загорается и пульсирует. Нажатие повышает уровень на 1,
     * вычитает порог и оставляет весь лишний опыт.
     */
    private void drawProgress(GuiGraphics g, RelicData d, float mx, float my, long now) {
        int bx = 40, by = 135, bw = 174, bh = 10;
        boolean ready = d.canLevelUp();
        float ratio = d.maxed() ? 1f : Math.min(1f, d.xp() / (float) d.xpToNext());
        int fill = Math.round(bw * ratio);
        if (fill > 0) {
            g.fillGradient(bx, by, bx + fill, by + bh, ready || d.maxed() ? 0xFFFF6A5A : 0xFFE0524A, 0xFF7A1424);
        }
        String label = d.maxed() ? Component.translatable("gui.angelring.max").getString()
                : (int) (ratio * 100f) + "%";
        text(g, Component.literal(label), bx + bw / 2f, 136, 1.0f, 0xFFFFFFFF, true, 1);

        float x = 220, y = 132, w = 88, h = 16;
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        float pulse = 0.5f + 0.5f * Mth.sin(now / 1000f * 5f);
        if (ready) {
            g.fill((int) x + 2, (int) y + 2, (int) (x + w) - 2, (int) (y + h) - 2,
                    (((int) (60 + 100 * pulse + (hover ? 50 : 0))) << 24) | 0xC0394A);
            g.renderOutline((int) x - 1, (int) y - 1, (int) w + 2, (int) h + 2,
                    (((int) (110 + 130 * pulse)) << 24) | 0xFF6A58);
        }
        Component caption = Component.translatable(d.maxed() ? "gui.angelring.level_max" : "gui.angelring.level_up");
        fit(g, caption, x + w / 2f, y + 4, w - 8, 0.85f, ready ? 0xFFFFFFFF : TEXT_OFF, ready, 1);
        hit(x, y, w, h, ready ? () -> send(RelicData.ACTION_LEVEL_UP, 0) : null, () -> levelTooltip(d, ready));
    }

    private List<Component> levelTooltip(RelicData d, boolean ready) {
        List<Component> tip = new ArrayList<>();
        if (d.maxed()) {
            tip.add(Component.translatable("gui.angelring.max_level").withStyle(ChatFormatting.GOLD));
            return tip;
        }
        tip.add(Component.translatable("gui.angelring.level_up.tip1", d.level(), d.level() + 1)
                .withStyle(ChatFormatting.GOLD));
        tip.add(Component.translatable("gui.angelring.level_up.tip2", d.xp(), d.xpToNext()).withStyle(ChatFormatting.GRAY));
        if (ready) {
            tip.add(Component.translatable("gui.angelring.level_up.ready", d.xp() - d.xpToNext())
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tip.add(Component.translatable("gui.angelring.level_up.need", d.xpToNext() - d.xp())
                    .withStyle(ChatFormatting.RED));
        }
        return tip;
    }

    private void drawPlaques(GuiGraphics g, RelicData d, float mx, float my) {
        // логотип: яркий текст с тёмной обводкой, чтобы не терялся в дыму
        outlined(g, Component.literal("F A L L E N"), 392, 24, 0.9f, 0xFFFFA090, 1);
        outlined(g, Component.literal("RELICS"), 392, 35, 1.9f, 0xFFFF6A58, 1);

        plaque(g, 0, Component.translatable("gui.angelring.stat.level"), d.level() + " / " + RelicData.MAX_LEVEL);
        plaque(g, 1, Component.translatable("gui.angelring.stat.flight"),
                "+" + Math.round((d.flightMultiplier() - 1f) * 100f) + "%");
        plaque(g, 2, Component.translatable("gui.angelring.stat.rarity"), d.ringStars() + " / " + RelicData.MAX_STARS);
        hit(PLAQUE_X[1], PLAQUE_Y[1], PLAQUE_W, PLAQUE_H, null, () -> List.of(
                Component.translatable("gui.angelring.stat.flight.tip").withStyle(ChatFormatting.GRAY)));

        // кнопка переброса редкости
        boolean afford = creative() || playerLevels() >= RelicData.REROLL_COST;
        boolean hover = mx >= REROLL_X && mx < REROLL_X + REROLL_W && my >= REROLL_Y && my < REROLL_Y + REROLL_H;
        if (hover && afford) {
            g.fill(REROLL_X + 3, REROLL_Y + 3, REROLL_X + REROLL_W - 3, REROLL_Y + REROLL_H - 3, 0x55C0394A);
        }
        fit(g, Component.translatable("gui.angelring.reroll"), REROLL_X + REROLL_W / 2f, REROLL_Y + 7, REROLL_W - 10, 1.0f,
                afford ? 0xFFFFFFFF : TEXT_OFF, afford, 1);
        fit(g, Component.translatable("gui.angelring.reroll.cost", RelicData.REROLL_COST), REROLL_X + REROLL_W / 2f,
                REROLL_Y + 19, REROLL_W - 10, 0.85f, afford ? XP_GREEN : 0xFFD05050, false, 1);
        hit(REROLL_X, REROLL_Y, REROLL_W, REROLL_H, afford ? this::doReroll : null, () -> List.of(
                Component.translatable("gui.angelring.reroll.tip1").withStyle(ChatFormatting.GOLD),
                Component.translatable("gui.angelring.reroll.tip2").withStyle(ChatFormatting.GRAY),
                Component.translatable(afford ? "gui.angelring.reroll.tip3" : "gui.angelring.reroll.need",
                        RelicData.REROLL_COST).withStyle(afford ? ChatFormatting.GREEN : ChatFormatting.RED)));

        // подпись под кнопкой — на тёмной плашке, строго в пределах колонки
        plate(g, 342, 226, 100, 44);
        wrapped(g, Component.translatable("gui.angelring.rarity_note"), 348, 231, 88, 4, HINT);
    }

    private void plaque(GuiGraphics g, int i, Component label, String value) {
        float x = PLAQUE_X[i];
        float y = PLAQUE_Y[i];
        fit(g, label, x + 8, y + 6, 76, 0.8f, TEXT_DIM, false, 0);
        fit(g, Component.literal(value), x + 8, y + 17, 76, 1.2f, TEXT, true, 0);
    }

    private void drawAbilities(GuiGraphics g, RelicData d, float mx, float my, long now) {
        Component title = Component.translatable("gui.angelring.abilities");
        text(g, title, 40, 162, 0.85f, TEXT_DIM, false, 0);
        float titleW = font.width(title) * 0.85f;
        fit(g, Component.translatable("gui.angelring.abilities_hint", ClientModEvents.ABILITY_KEY.getTranslatedKeyMessage()),
                306, 162, 306 - 40 - titleW - 12, 0.85f, 0xFFE09098, false, 2);

        long gameTime = minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        int stars = d.ringStars(); // редкость всех способностей равна редкости кольца
        for (int i = 0; i < RelicData.ABILITY_COUNT; i++) {
            RelicAbility ability = RelicAbility.byId(i);
            int cx = CARD_X[i];
            int cy = CARD_Y;
            boolean locked = !d.unlocked(i);
            int level = d.abilityLevel(i);
            boolean hover = mx >= cx && mx < cx + CARD_W && my >= cy && my < cy + CARD_H;
            if (hover) {
                selected = i;
            }
            if (selected == i) {
                g.renderOutline(cx, cy, CARD_W, CARD_H, 0x55FFFFFF);
            }

            // бейдж: клавиша активной способности (в раскладке игрока) или значок пассивной
            Component badge = ability.active ? ClientModEvents.ABILITY_KEY.getTranslatedKeyMessage()
                    : Component.translatable("gui.angelring.badge_passive");
            fit(g, badge, cx + 10, cy + 8, 10, 0.9f, locked ? TEXT_OFF : TEXT, false, 1);

            // иконка 48x48 (16x16 в три раза крупнее); закрытая — затемнена, поверх неё замок
            g.pose().pushPose();
            g.pose().translate(cx + 19, cy + 5, 0f);
            g.pose().scale(3f, 3f, 1f);
            if (locked) {
                RenderSystem.setShaderColor(0.28f, 0.26f, 0.32f, 1f);
            }
            g.blit(ICONS, 0, 0, i * 16, 0, 16, 16, 64, 16);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            g.pose().popPose();
            if (locked) {
                g.pose().pushPose();
                g.pose().translate(cx + 27, cy + 13, 0f);
                g.pose().scale(2f, 2f, 1f);
                g.blit(ICONS, 0, 0, 48, 0, 16, 16, 64, 16);
                g.pose().popPose();
            }

            fit(g, Component.translatable("gui.angelring.ability." + ability.key + ".name"),
                    cx + CARD_W / 2f, cy + 58, CARD_W - 6, 0.85f, locked ? TEXT_OFF : TEXT, false, 1);

            final int abilityId = i;
            if (locked) {
                // подпись с требованием по уровню кольца вместо звёзд
                fit(g, Component.translatable("gui.angelring.locked", ability.unlockLevel), cx + CARD_W / 2f, cy + 68,
                        CARD_W - 6, 0.8f, 0xFFD05050, false, 1);
                hit(cx, cy, CARD_W, CARD_H, null, () -> cardTooltip(ability, d));
                continue;
            }

            // уровень способности — столбик слева, мини-звёзды (= звёзды кольца) под названием
            for (int p = 0; p < RelicAbility.MAX_LEVEL; p++) {
                int py = cy + 46 - p * 8;
                g.fill(cx + 5, py, cx + 12, py + 6, p < level ? RED : 0xFF2A1A20);
            }
            for (int sIdx = 0; sIdx < RelicData.MAX_STARS; sIdx++) {
                g.blit(STARS, cx + 16 + sIdx * 9, cy + 67, starFilled(sIdx, stars, now) ? 0 : 8, 0, 8, 8, 16, 8);
            }

            // перезарядка активной способности
            long left = d.cooldownReadyAt(i) - gameTime;
            if (ability.active && left > 0) {
                g.fill(cx + 3, cy + 3, cx + CARD_W - 3, cy + CARD_H - 3, 0xB0000000);
                text(g, Component.literal(((left + 19) / 20) + "s"), cx + CARD_W / 2f, cy + 26, 1.4f, 0xFFFFFFFF, true, 1);
            }

            // кнопка «+» (улучшение за уровни опыта игрока) и цена под ней
            int cost = d.upgradeCost(i);
            boolean affordable = creative() || playerLevels() >= cost;
            boolean canBuy = cost > 0 && d.canUpgrade(i) && affordable;
            float bx = cx + 71;
            float by = cy + 4;
            if (canBuy && mx >= bx && mx < bx + 12 && my >= by && my < by + 12) {
                g.fill((int) bx + 1, (int) by + 1, (int) bx + 11, (int) by + 11, 0x66C0394A);
            }
            text(g, Component.literal(cost < 0 ? "\u2022" : "+"), bx + 6, by + 2, 1.0f,
                    cost < 0 ? RED : (canBuy ? TEXT : TEXT_OFF), false, 1);
            if (cost > 0) {
                text(g, Component.literal(String.valueOf(cost)), bx + 6, by + 17, 0.9f,
                        affordable ? XP_GREEN : 0xFFD05050, false, 1);
            }

            hit(cx, cy, CARD_W, CARD_H, null, () -> cardTooltip(ability, d));
            hit(bx, by, 12, 12, canBuy ? () -> send(RelicData.ACTION_UPGRADE, abilityId) : null,
                    () -> buyTooltip(ability, d, cost, affordable));
        }
    }

    // ------------------------------------------------------------------ анимации (рисуются на z = 300)

    /**
     * Переброс: багровый дым густеет над слотом кольца и на пике полностью скрывает иконку и бейдж уровня,
     * вокруг крутится вихрь с искрами. Звёзды при этом остаются старыми и меняются по очереди после пика.
     */
    private void drawRerollFx(GuiGraphics g, long now) {
        if (rerollStart < 0) {
            return;
        }
        float t = (now - rerollStart) / 1000f;
        float p = t / REROLL_TOTAL;
        float k = p < 0.14f ? p / 0.14f : (p > 0.62f ? Math.max(0f, (1f - p) / 0.38f) : 1f);
        float cx = PORTRAIT_X + PORTRAIT_SIZE / 2f;
        float cy = PORTRAIT_Y + PORTRAIT_SIZE / 2f;

        // плотная тёмно-красная заливка слота и бейджа уровня (границы совпадают с рамкой карточки)
        int dark = (((int) (245 * k)) << 24) | 0x16040A;
        g.fill(PORTRAIT_X + 3, PORTRAIT_Y + 3, PORTRAIT_X + PORTRAIT_SIZE - 3, PORTRAIT_Y + PORTRAIT_SIZE - 3, dark);
        g.fill(57, 34, 83, 44, dark);
        // дымные слои поверх заливки: скрывают её края и закручиваются
        for (int n = 0; n < 4; n++) {
            blob(g, HAZE, cx, cy, 0.30f + 0.08f * n + 0.02f * Mth.sin(t * 9f), -t * (170f + 90f * n), 1.0f * k, false);
        }
        for (int n = 0; n < 3; n++) {
            blob(g, GLOW, cx, cy, 0.26f + 0.09f * n, t * (240f - 110f * n), 0.75f * k, true);
        }
        for (int e = 0; e < 18; e++) {
            float angle = t * 5f + e * 0.35f;
            float radius = 40f + 10f * Mth.sin(e + t * 3f);
            int px = Math.round(cx + Mth.cos(angle) * radius);
            int py = Math.round(cy + Mth.sin(angle) * radius * 0.8f);
            g.fill(px, py, px + 2, py + 2, (((int) (220 * k)) << 24) | 0xFF4A3A);
        }
    }

    /** Повышение уровня кольца: вспышка и волна света по шкале опыта. */
    private void drawLevelFx(GuiGraphics g, long now) {
        if (levelStart < 0) {
            return;
        }
        float t = (now - levelStart) / 1000f;
        float p = t / 1.1f;
        if (p >= 1f) {
            levelStart = -1L;
            return;
        }
        float fade = 1f - p;
        g.fill(34, 130, 312, 150, (((int) (110 * fade)) << 24) | 0xFF3040);
        blob(g, GLOW, 173, 140, 0.16f + 0.5f * p, t * 90f, 0.9f * fade, true);
        blob(g, HAZE, 173, 140, 0.20f + 0.25f * p, -t * 60f, 0.5f * fade, false);
        g.renderOutline(PLAQUE_X[0] - 1, PLAQUE_Y[0] - 1, PLAQUE_W + 2, PLAQUE_H + 2, (((int) (255 * fade)) << 24) | 0xE05A4A);
    }

    /** Улучшение или открытие способности: тёмно-красная дымка сгущается над карточкой, затем следует вспышка. */
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
            blob(g, HAZE, cx, cy, 0.65f - 0.38f * q, t * 80f, 0.2f + 0.7f * q, false);
            blob(g, GLOW, cx, cy, 0.50f - 0.34f * q, -t * 140f, 0.15f + 0.5f * q, true);
        } else {
            float q = (p - 0.55f) / 0.45f;
            float fade = 1f - q;
            blob(g, GLOW, cx, cy, 0.18f + 0.44f * q, t * 60f, 0.95f * fade, true);
            blob(g, HAZE, cx, cy, 0.24f + 0.18f * q, -t * 50f, 0.6f * fade, false);
            g.fill(CARD_X[i], CARD_Y, CARD_X[i] + CARD_W, CARD_Y + CARD_H, (((int) (150 * fade * fade)) << 24) | 0xFF3040);
            g.renderOutline(CARD_X[i] - 1, CARD_Y - 1, CARD_W + 2, CARD_H + 2,
                    (((int) (255 * fade)) << 24) | 0xE05A4A);
        }
    }

    // ------------------------------------------------------------------ подсказки и тексты способностей

    private List<Component> cardTooltip(RelicAbility ability, RelicData d) {
        int id = ability.id;
        int level = d.abilityLevel(id);
        int stars = d.ringStars();
        List<Component> tip = new ArrayList<>();
        tip.add(Component.translatable("gui.angelring.ability." + ability.key + ".name")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE));
        tip.add(Component.translatable(ability.active ? "gui.angelring.type_active" : "gui.angelring.type_passive")
                .withStyle(ChatFormatting.DARK_RED));
        if (level <= 0) {
            tip.add(Component.translatable("gui.angelring.locked_long", ability.unlockLevel, d.level())
                    .withStyle(ChatFormatting.RED));
            tip.add(descText(ability, stars, 1).withStyle(ChatFormatting.DARK_GRAY));
            return tip;
        }
        tip.add(Component.translatable("gui.angelring.rarity_line", starsText(stars)).withStyle(ChatFormatting.RED));
        tip.add(Component.translatable("gui.angelring.level_of", level, RelicAbility.MAX_LEVEL).withStyle(ChatFormatting.GOLD));
        tip.add(descText(ability, stars, level).withStyle(ChatFormatting.GRAY));
        return tip;
    }

    private List<Component> buyTooltip(RelicAbility ability, RelicData d, int cost, boolean affordable) {
        List<Component> tip = new ArrayList<>();
        if (cost < 0) {
            tip.add(Component.translatable("gui.angelring.max_level").withStyle(ChatFormatting.GOLD));
            return tip;
        }
        int level = d.abilityLevel(ability.id);
        tip.add(Component.translatable("gui.angelring.improve", level + 1).withStyle(ChatFormatting.WHITE));
        tip.add(descText(ability, d.ringStars(), level + 1).withStyle(ChatFormatting.GRAY));
        tip.add(Component.translatable("gui.angelring.cost_levels", cost)
                .withStyle(affordable ? ChatFormatting.GREEN : ChatFormatting.RED));
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
            case BLOOD_WARD -> Component.translatable(key, Math.round(RelicAbility.wardChance(stars, level) * 100f),
                    num(RelicAbility.wardHeal(level)));
            case HARVEST -> Component.translatable(key, num(RelicAbility.harvestHeal(stars, level)),
                    RelicAbility.harvestAbsorbSeconds(stars),
                    Math.round(RelicAbility.harvestXpChance(stars, level) * 100f));
            case ASCENSION -> Component.translatable(key, num(RelicAbility.ascensionRadius(stars)),
                    num(RelicAbility.ascensionDamage(stars, level)),
                    num(RelicAbility.ascensionStunSeconds(stars, level)));
        };
    }

    /** Короткое описание для строки внизу окна. */
    private MutableComponent shortText(RelicAbility ability, int stars, int level) {
        String key = "gui.angelring.ability." + ability.key + ".short";
        return switch (ability) {
            case BLOOD_WARD -> Component.translatable(key, Math.round(RelicAbility.wardChance(stars, level) * 100f));
            case HARVEST -> Component.translatable(key, num(RelicAbility.harvestHeal(stars, level)));
            case ASCENSION -> Component.translatable(key, num(RelicAbility.ascensionRadius(stars)),
                    num(RelicAbility.ascensionDamage(stars, level)));
        };
    }

    private void drawFooter(GuiGraphics g, RelicData d) {
        // описание выбранной способности — внутри панели способностей, строго в её ширину
        RelicAbility ability = RelicAbility.byId(selected);
        int level = d.abilityLevel(selected);
        Component line = level > 0 ? shortText(ability, d.ringStars(), level)
                : Component.translatable("gui.angelring.locked_long", ability.unlockLevel, d.level());
        fit(g, line, 173, 258, 266, 0.9f, level > 0 ? 0xFFD6C8CC : 0xFFE07070, false, 1);

        // нижняя плашка с подсказками: выровнена по рамке окна, тёмная подложка, контрастный текст
        plate(g, 22, 292, 300, 26);
        fit(g, Component.translatable("gui.angelring.hint"), 172, 295, 290, 0.8f, HINT, false, 1);
        fit(g, Component.translatable("gui.angelring.hint2"), 172, 306, 290, 0.8f, 0xFFE09098, false, 1);
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

    /** Текст, который гарантированно не шире maxW: если не помещается, масштаб уменьшается. */
    private void fit(GuiGraphics g, Component c, float x, float y, float maxW, float preferred, int color,
                     boolean shadow, int align) {
        float w = font.width(c);
        float s = w * preferred > maxW && w > 0 ? maxW / w : preferred;
        text(g, c, x, y, s, color, shadow, align);
    }

    /** Многострочный текст: подбирает масштаб так, чтобы уместиться в maxW и maxLines строк. */
    private void wrapped(GuiGraphics g, Component c, float x, float y, float maxW, int maxLines, int color) {
        float[] scales = {0.9f, 0.85f, 0.8f, 0.75f, 0.7f};
        float use = scales[scales.length - 1];
        List<FormattedCharSequence> lines = null;
        for (float s : scales) {
            List<FormattedCharSequence> candidate = font.split(c, (int) (maxW / s));
            if (candidate.size() <= maxLines) {
                use = s;
                lines = candidate;
                break;
            }
        }
        if (lines == null) {
            lines = font.split(c, (int) (maxW / use));
        }
        float lineHeight = 9f * use + 1.4f;
        for (int i = 0; i < Math.min(maxLines, lines.size()); i++) {
            g.pose().pushPose();
            g.pose().translate(x, y + i * lineHeight, 0f);
            g.pose().scale(use, use, 1f);
            g.drawString(font, lines.get(i), 0, 0, color, false);
            g.pose().popPose();
        }
    }

    /** Текст с тёмной обводкой — читается на любом фоне. */
    private void outlined(GuiGraphics g, Component c, float x, float y, float s, int color, int align) {
        float[][] offsets = {{0.8f, 0f}, {-0.8f, 0f}, {0f, 0.8f}, {0f, -0.8f}, {0.6f, 0.6f}, {-0.6f, -0.6f}};
        for (float[] o : offsets) {
            text(g, c, x + o[0], y + o[1], s, 0xFF160406, false, align);
        }
        text(g, c, x, y, s, color, false, align);
    }

    /** Тёмная подложка с красноватой рамкой под вспомогательные надписи. */
    private void plate(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xD00E0608);
        g.renderOutline(x, y, w, h, 0xFF5A1A26);
    }
}
