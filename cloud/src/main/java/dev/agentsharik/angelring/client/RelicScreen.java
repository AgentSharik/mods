package dev.agentsharik.angelring.client;

import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Окно кольца (тёмно-красный стиль): составная рама с колоннами, шапка с карточкой, шкала опыта,
 * панель из трёх способностей, справа плашки характеристик. На заднем плане — пульсирующая дымка
 * неправильной формы со светящимися «жилами» и летящими искрами.
 *
 * Статичный рисунок лежит в textures/gui/relic_ui.png (512x256, используется область 340x214),
 * числа макета ниже должны совпадать с этой картинкой. Окно рисуется в «логических» координатах
 * и автоматически уменьшается, если экран маленький; клики пересчитываются обратно.
 */
public class RelicScreen extends Screen {
    private static final ResourceLocation UI = tex("textures/gui/relic_ui.png");
    private static final ResourceLocation HAZE = tex("textures/gui/relic_haze.png");
    private static final ResourceLocation GLOW = tex("textures/gui/relic_haze_glow.png");
    private static final ResourceLocation ICONS = tex("textures/gui/ability_icons.png");

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, path);
    }

    private static final int W = 340, H = 214;
    private static final int HAZE_W = 512, HAZE_H = 384;

    // ---- макет (совпадает с relic_ui.png) ----
    private static final int PORTRAIT_X = 34, PORTRAIT_Y = 32;
    private static final int[] CARD_X = {35, 105, 175};
    private static final int CARD_Y = 134, CARD_W = 62, CARD_H = 48;
    private static final int[] PLAQUE_X = {272, 276, 272};
    private static final int[] PLAQUE_Y = {60, 90, 120};
    private static final int PLAQUE_W = 62, PLAQUE_H = 26;

    private static final int RED = 0xFFC0394A;
    private static final int TEXT = 0xFFE8DCD8;
    private static final int TEXT_DIM = 0xFF8F868C;
    private static final int TEXT_OFF = 0xFF55494E;

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

    // ------------------------------------------------------------------ жизненный цикл

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

    // ------------------------------------------------------------------ ввод

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
        // фон мира не затемняем и не размываем: дымка лежит прямо поверх игры, как в Relics
        hits.clear();
        ItemStack stack = stack();
        RelicData d = data(stack);
        float t = (Util.getMillis() % 1_000_000L) / 1000f;
        float mx = (mouseX - ox) / scale;
        float my = (mouseY - oy) / scale;

        g.pose().pushPose();
        g.pose().translate(ox, oy, 0f);
        g.pose().scale(scale, scale, 1f);

        drawHaze(g, t);
        g.blit(UI, 0, 0, 0, 0, W, H, 512, 256);
        drawHeader(g, d, stack);
        drawProgress(g, d, mx, my, t);
        drawPlaques(g, d, mx, my);
        drawAbilities(g, d, mx, my);
        drawFooter(g);

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

    /** Дымка: тёмная основа пульсирует и колышется, светящиеся жилы переливаются, снизу вверх летят искры. */
    private void drawHaze(GuiGraphics g, float t) {
        float cx = W / 2f;
        float cy = H / 2f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // основа
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0f);
        g.pose().mulPose(Axis.ZP.rotationDegrees(2.5f * Mth.sin(t * 0.30f)));
        float s1 = 1.0f + 0.03f * Mth.sin(t * 0.55f);
        g.pose().scale(s1, s1, 1f);
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.82f + 0.15f * Mth.sin(t * 1.3f));
        g.blit(HAZE, -HAZE_W / 2, -HAZE_H / 2, 0, 0, HAZE_W, HAZE_H, HAZE_W, HAZE_H);
        g.pose().popPose();

        // светящиеся жилы — аддитивное смешивание, два слоя в противофазе
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        for (int layer = 0; layer < 2; layer++) {
            float dir = layer == 0 ? -1f : 1f;
            g.pose().pushPose();
            g.pose().translate(cx, cy, 0f);
            g.pose().mulPose(Axis.ZP.rotationDegrees(dir * 3.5f * Mth.sin(t * 0.22f + layer * 2.1f)));
            float s = (layer == 0 ? 1.02f : 1.10f) + 0.035f * Mth.sin(t * 0.45f + layer * 1.3f);
            g.pose().scale(s, s, 1f);
            float pulse = 0.5f + 0.5f * Mth.sin(t * (layer == 0 ? 0.9f : 1.35f) + layer * 1.9f);
            RenderSystem.setShaderColor(1f, 1f, 1f, (layer == 0 ? 0.30f : 0.16f) + 0.38f * pulse);
            g.blit(GLOW, -HAZE_W / 2, -HAZE_H / 2, 0, 0, HAZE_W, HAZE_H, HAZE_W, HAZE_H);
            g.pose().popPose();
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        // искры
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

    private void drawHeader(GuiGraphics g, RelicData d, ItemStack stack) {
        // бейдж уровня над карточкой
        text(g, Component.literal(String.valueOf(d.level())), 59, 28, 1.0f, TEXT, true, 1);

        // портрет: кольцо крупно
        g.pose().pushPose();
        g.pose().translate(PORTRAIT_X + 5, PORTRAIT_Y + 5, 0f);
        g.pose().scale(2.5f, 2.5f, 1f);
        g.renderItem(stack.isEmpty() ? new ItemStack(ModItems.ANGEL_RING.get()) : stack, 0, 0);
        g.pose().popPose();

        // пипсы ранга под карточкой
        for (int i = 0; i < RelicData.MAX_RANK; i++) {
            int x = 40 + i * 8;
            g.fill(x, 86, x + 6, 90, i < d.rank() ? RED : 0xFF2A1A20);
        }

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
        text(g, Component.translatable("gui.angelring.chip_points", d.points()), 130, 84, 0.8f, TEXT, false, 1);
        String xp = d.maxed() ? "MAX" : d.xp() + " / " + d.xpToNext();
        text(g, Component.translatable("gui.angelring.chip_xp", xp), 206, 84, 0.8f, TEXT, false, 1);
    }

    private void drawProgress(GuiGraphics g, RelicData d, float mx, float my, float t) {
        int bx = 34, by = 103, bw = 204, bh = 8;
        float ratio = d.maxed() ? 1f : Math.min(1f, d.xp() / (float) d.xpToNext());
        int fill = Math.round(bw * ratio);
        if (fill > 0) {
            if (d.maxed()) {
                int glow = (int) (200 + 55 * (0.5f + 0.5f * Mth.sin(t * 3f)));
                g.fillGradient(bx, by, bx + fill, by + bh, 0xFF000000 | (glow << 16) | (90 << 8) | 70, 0xFFB02030);
            } else {
                g.fillGradient(bx, by, bx + fill, by + bh, 0xFFE0524A, 0xFF7A1424);
            }
        }
        if (d.canRankUp()) {
            // на максимальном уровне поверх шкалы лежит кнопка «Повысить ранг»
            float x = 76, y = 100, w = 120, h = 14;
            boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
            g.fillGradient((int) x, (int) y, (int) (x + w), (int) (y + h), hover ? 0xFFB02A3C : 0xFF8A1E2E, 0xFF4A0C16);
            g.renderOutline((int) x, (int) y, (int) w, (int) h, 0xFFE0A0A0);
            text(g, Component.translatable("gui.angelring.rank_up"), x + w / 2f, y + 3, 1.0f, 0xFFFFFFFF, true, 1);
            hit(x, y, w, h, () -> send(RelicData.ACTION_RANK_UP, 0), () -> List.of(
                    Component.translatable("gui.angelring.rank_up.tip1").withStyle(ChatFormatting.GOLD),
                    Component.translatable("gui.angelring.rank_up.tip2").withStyle(ChatFormatting.GRAY)));
        } else {
            String label = d.maxed() ? Component.translatable("gui.angelring.max").getString()
                    : (int) (ratio * 100f) + "%";
            text(g, Component.literal(label), 136, 103, 0.9f, 0xFFFFFFFF, true, 1);
        }
    }

    private void drawPlaques(GuiGraphics g, RelicData d, float mx, float my) {
        // заголовок
        text(g, Component.literal("F A L L E N"), 304, 22, 0.6f, 0xFFB05058, false, 1);
        text(g, Component.literal("RELICS"), 304, 31, 1.5f, 0xFFE05A4A, true, 1);

        // 1: скорость полёта (+)
        plaque(g, 0, Component.translatable("gui.angelring.stat.speed"),
                "+" + Math.round((d.speedMultiplier() - 1f) * 100f) + "%");
        statButton(g, 0, d.points() > 0 && d.speed() < d.maxStat(), mx, my,
                () -> send(RelicData.ACTION_STAT_SPEED, 0),
                Component.translatable("gui.angelring.stat.speed.tip", d.speed(), d.maxStat()));

        // 2: мастерство (+)
        plaque(g, 1, Component.translatable("gui.angelring.stat.mastery"), "+" + Math.round((d.xpMultiplier() - 1f) * 100f) + "%");
        statButton(g, 1, d.points() > 0 && d.mastery() < d.maxStat(), mx, my,
                () -> send(RelicData.ACTION_STAT_MASTERY, 0),
                Component.translatable("gui.angelring.stat.mastery.tip", d.mastery(), d.maxStat()));

        // 3: ранг
        plaque(g, 2, Component.translatable("gui.angelring.stat.rank"), d.rank() + " / " + RelicData.MAX_RANK);

        // подпись под плашками
        int learned = 0;
        for (int id = 0; id < RelicData.ABILITY_COUNT; id++) {
            if (d.abilityLevel(id) > 0) {
                learned++;
            }
        }
        text(g, Component.translatable("gui.angelring.learned", learned, RelicData.ABILITY_COUNT), 304, 154, 0.6f, TEXT_DIM, false, 1);
        text(g, Component.translatable("gui.angelring.flight_note"), 304, 164, 0.55f, 0xFF6F676C, false, 1);
    }

    private void plaque(GuiGraphics g, int i, Component label, String value) {
        float x = PLAQUE_X[i];
        float y = PLAQUE_Y[i];
        text(g, label, x + 6, y + 5, 0.6f, TEXT_DIM, false, 0);
        text(g, Component.literal(value), x + 6, y + 13, 1.0f, TEXT, true, 0);
    }

    private void statButton(GuiGraphics g, int i, boolean enabled, float mx, float my, Runnable action, Component tip) {
        float x = PLAQUE_X[i] + 46;
        float y = PLAQUE_Y[i] + 7;
        boolean hover = enabled && mx >= x && mx < x + 12 && my >= y && my < y + 12;
        if (hover) {
            g.fill((int) x + 1, (int) y + 1, (int) x + 11, (int) y + 11, 0x66C0394A);
        }
        text(g, Component.literal("+"), x + 6, y + 2, 1.0f, enabled ? TEXT : TEXT_OFF, false, 1);
        hit(x, y, 12, 12, enabled ? action : null, () -> List.of(
                tip,
                Component.translatable(enabled ? "gui.angelring.cost_one" : "gui.angelring.unavailable")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY)));
    }

    private void drawAbilities(GuiGraphics g, RelicData d, float mx, float my) {
        text(g, Component.translatable("gui.angelring.abilities"), 34, 124, 0.6f, TEXT_DIM, false, 0);
        text(g, Component.translatable("gui.angelring.equipped", d.equippedCount(), RelicData.SLOTS), 238, 124, 0.6f,
                d.equippedCount() > 0 ? 0xFFD08088 : TEXT_DIM, false, 2);

        long now = minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        for (int i = 0; i < RelicData.ABILITY_COUNT; i++) {
            RelicAbility ability = RelicAbility.byId(i);
            int cx = CARD_X[i];
            int cy = CARD_Y;
            int level = d.abilityLevel(i);
            int slot = d.equippedSlot(i);
            boolean hover = mx >= cx && mx < cx + CARD_W && my >= cy && my < cy + CARD_H;
            if (hover) {
                selected = i;
            }

            // рамка: экипирована — красная, выбрана — светлая
            if (slot >= 0) {
                g.renderOutline(cx - 1, cy - 1, CARD_W + 2, CARD_H + 2, RED);
            }
            if (selected == i) {
                g.renderOutline(cx, cy, CARD_W, CARD_H, 0x55FFFFFF);
            }

            // номер слота
            if (slot >= 0) {
                text(g, Component.literal(String.valueOf(slot + 1)), cx + 8, cy + 4, 0.9f, TEXT, true, 1);
            }

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

            // название и пипсы уровня
            text(g, Component.translatable("gui.angelring.ability." + ability.key + ".name"),
                    cx + CARD_W / 2f, cy + 38, 0.7f, level > 0 ? TEXT : TEXT_OFF, false, 1);
            for (int p = 0; p < RelicAbility.MAX_LEVEL; p++) {
                int px = cx + 18 + p * 7;
                g.fill(px, cy + 44, px + 5, cy + 47, p < level ? RED : 0xFF2A1A20);
            }

            // перезарядка
            long left = d.cooldownReadyAt(i) - now;
            if (level > 0 && left > 0) {
                g.fill(cx + 2, cy + 2, cx + CARD_W - 2, cy + CARD_H - 2, 0xB0000000);
                text(g, Component.literal(((left + 19) / 20) + "s"), cx + CARD_W / 2f, cy + 16, 1.2f, 0xFFFFFFFF, true, 1);
            }
            // пассивная способность
            if (ability.passive && level > 0 && left <= 0) {
                text(g, Component.translatable("gui.angelring.passive"), cx + CARD_W / 2f, cy + 30, 0.5f, 0xFFB05058, false, 1);
            }

            // кнопка «+» (изучить / улучшить)
            final int abilityId = i;
            int cost = d.abilityCost(i);
            boolean canBuy = cost > 0 && d.points() >= cost;
            float bx = cx + 48;
            float by = cy + 3;
            boolean plusHover = canBuy && mx >= bx && mx < bx + 12 && my >= by && my < by + 12;
            if (plusHover) {
                g.fill((int) bx + 1, (int) by + 1, (int) bx + 11, (int) by + 11, 0x66C0394A);
            }
            text(g, Component.literal(cost < 0 ? "\u2605" : "+"), bx + 6, by + 2, 1.0f,
                    cost < 0 ? RED : (canBuy ? TEXT : TEXT_OFF), false, 1);

            // клик по карточке — экипировать / снять; клик по «+» — изучить / улучшить
            hit(cx, cy, CARD_W, CARD_H, level > 0 ? () -> send(RelicData.ACTION_ABILITY_EQUIP, abilityId) : null,
                    () -> cardTooltip(ability, d));
            hit(bx, by, 12, 12, canBuy ? () -> send(RelicData.ACTION_ABILITY_LEARN, abilityId) : null,
                    () -> buyTooltip(ability, d, cost, canBuy));
        }
    }

    private List<Component> cardTooltip(RelicAbility ability, RelicData d) {
        int level = d.abilityLevel(ability.id);
        List<Component> tip = new ArrayList<>();
        tip.add(Component.translatable("gui.angelring.ability." + ability.key + ".name").withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE));
        tip.add(Component.translatable("gui.angelring.level_of", level, RelicAbility.MAX_LEVEL).withStyle(ChatFormatting.GOLD));
        tip.add(abilityDesc(ability, Math.max(1, level)).withStyle(ChatFormatting.GRAY));
        if (level > 0) {
            tip.add(Component.translatable(d.equippedSlot(ability.id) >= 0 ? "gui.angelring.click_unequip" : "gui.angelring.click_equip")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tip.add(Component.translatable("gui.angelring.not_learned").withStyle(ChatFormatting.DARK_RED));
        }
        return tip;
    }

    private List<Component> buyTooltip(RelicAbility ability, RelicData d, int cost, boolean canBuy) {
        List<Component> tip = new ArrayList<>();
        if (cost < 0) {
            tip.add(Component.translatable("gui.angelring.max_level").withStyle(ChatFormatting.GOLD));
            return tip;
        }
        int level = d.abilityLevel(ability.id);
        tip.add(Component.translatable(level == 0 ? "gui.angelring.learn" : "gui.angelring.improve", level + 1)
                .withStyle(ChatFormatting.WHITE));
        tip.add(abilityDesc(ability, level + 1).withStyle(ChatFormatting.GRAY));
        tip.add(Component.translatable("gui.angelring.cost", cost)
                .withStyle(canBuy ? ChatFormatting.GREEN : ChatFormatting.RED));
        return tip;
    }

    /** Описание способности с цифрами для заданного уровня. */
    private net.minecraft.network.chat.MutableComponent abilityDesc(RelicAbility ability, int level) {
        String key = "gui.angelring.ability." + ability.key + ".desc";
        return switch (ability) {
            case DASH -> Component.translatable(key, ability.cooldownSeconds(level));
            case WARD -> Component.translatable(key, ability.absorbHealth(level), ability.cooldownSeconds(level));
            case FEATHER -> Component.translatable(key, ability.fallReductionPercent(level));
        };
    }

    private void drawFooter(GuiGraphics g) {
        RelicData d = data(stack());
        RelicAbility ability = RelicAbility.byId(selected);
        int level = d.abilityLevel(selected);
        Component name = Component.translatable("gui.angelring.ability." + ability.key + ".name");
        Component line = Component.empty().append(name).append(" \u2014 ").append(abilityDesc(ability, Math.max(1, level)));
        text(g, line, 136, 185, 0.55f, 0xFFA59AA0, false, 1);

        Component k1 = ClientModEvents.ABILITY_1.getTranslatedKeyMessage();
        Component k2 = ClientModEvents.ABILITY_2.getTranslatedKeyMessage();
        text(g, Component.translatable("gui.angelring.hint", k1, k2), 30, 205, 0.55f, 0xFF6F676C, false, 0);
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
