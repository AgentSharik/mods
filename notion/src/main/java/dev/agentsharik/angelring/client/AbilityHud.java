package dev.agentsharik.angelring.client;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.agentsharik.angelring.AngelRingMod;
import dev.agentsharik.angelring.RelicAbility;
import dev.agentsharik.angelring.RelicData;
import dev.agentsharik.angelring.client.ui.Ease;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Компактный индикатор способностей справа от хотбара: две ячейки активных способностей
 * (заливка перезарядки, секунды, клавиша, пульсирующая рамка пока способность действует, вспышка при
 * готовности) и маленькая иконка пассивной защиты, вспыхивающая при срабатывании.
 *
 * <p>Остаток перезарядки считается от gameTime + partialTick, поэтому заливка движется плавно на
 * любом FPS и совпадает с серверными тиками. В кадре ничего не создаётся: подписи секунд и клавиш
 * кэшируются и перестраиваются только когда меняется цифра.
 */
public final class AbilityHud {
    private AbilityHud() {}

    public static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, "ability_hud");
    private static final ResourceLocation ICONS =
            ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, "textures/gui/ability_icons.png");

    private static final int SLOT = 20;
    private static final int GAP = 3;
    private static final int CRIMSON = 0xC0394A;

    private static final Ease.Clock CLOCK = new Ease.Clock();
    private static float visible;
    private static final float[] readyFlash = new float[2];
    private static final float[] useFlash = new float[2];
    private static final float[] shake = new float[2];
    private static final float[] fill = new float[2];
    private static final boolean[] wasCooling = new boolean[2];
    private static float wardFlash;

    // кэш подписей
    private static final int[] secCache = {-1, -1};
    private static final FormattedCharSequence[] secText = new FormattedCharSequence[2];
    private static final String[] keyCache = new String[2];
    private static final FormattedCharSequence[] keyText = new FormattedCharSequence[2];
    private static final int[] keyWidth = new int[2];
    private static final int[] secWidth = new int[2];

    static void tick(Minecraft mc) {
        if (mc.player == null) {
            visible=0; wardFlash=0;
            for(int i=0;i<2;i++) { wasCooling[i]=false; readyFlash[i]=0; useFlash[i]=0; fill[i]=0; }
            return;
        }
        for(int id=0;id<2;id++) {
            KeyMapping key=id==0?ClientModEvents.ABILITY_1:ClientModEvents.ABILITY_2;
            String label=key.getTranslatedKeyMessage().getString();
            if(!label.equals(keyCache[id])) {
                keyCache[id]=label;
                String shortLabel=label.length()>3?label.substring(0,3):label;
                keyText[id]=FormattedCharSequence.forward(shortLabel,net.minecraft.network.chat.Style.EMPTY);
                keyWidth[id]=mc.font.width(keyText[id]);
            }
        }
    }

    static void onUsed(int id) {
        if (id >= 0 && id < 2) {
            useFlash[id] = 1f;
        }
    }

    static void onError(int id) {
        if (id >= 0 && id < 2) {
            shake[id] = 1f;
        }
    }

    static void onWardProc() {
        wardFlash = 1f;
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        CLOCK.frame();
        float dt = CLOCK.dt;
        if (mc.player == null || mc.level == null) {
            return;
        }
        RelicData data = ClientRingCache.data();
        boolean anyLearned = ClientRingCache.worn()
                && (data.abilityLevel(0) > 0 || data.abilityLevel(1) > 0 || data.abilityLevel(2) > 0);
        boolean show = anyLearned && !mc.options.hideGui && !mc.player.isSpectator();
        visible = Ease.approach(visible, show ? 1f : 0f, dt, 8f);
        if (visible <= 0.01f) {
            return;
        }
        float alpha = Ease.smoothStep(visible);
        if (alpha < 0.035f) return; // Font treats near-zero alpha as opaque
        Font font = mc.font;
        double now = mc.level.getGameTime() + (double) delta.getGameTimeDeltaPartialTick(false);

        // позиция: справа от хотбара, с учётом слота второй руки и индикатора атаки
        int w = g.guiWidth();
        int h = g.guiHeight();
        int x0 = w / 2 + 91 + 6;
        boolean offhandRight = !mc.player.getOffhandItem().isEmpty() && mc.player.getMainArm() == HumanoidArm.LEFT;
        if (offhandRight) {
            x0 += 29;
        }
        if (mc.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
            x0 += 24;
        }
        int total = 2 * SLOT + GAP + 14;
        int y0 = h - SLOT - 2;
        if (x0 + total > w - 2) {
            x0 = w - total - 2;     // узкое окно — прижимаемся к правому краю над хотбаром
            y0 = h - SLOT - 26;
        }
        y0 += Math.round((1f - Ease.outCubic(visible)) * 8f);

        RenderSystem.enableBlend();
        int x = x0;
        for (int id = 0; id < 2; id++) {
            drawSlot(g, font, data, id, x, y0, now, dt, alpha);
            x += SLOT + GAP;
        }
        drawWard(g, data, x + 1, y0 + 4, dt, alpha);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void drawSlot(GuiGraphics g, Font font, RelicData data, int id, int x, int y, double now,
                                 float dt, float alpha) {
        RelicAbility ability = RelicAbility.byId(id);
        int level = data.abilityLevel(id);
        boolean learned = level > 0;
        int stars = data.abilityStars(id);
        float totalTicks = Math.max(1f, 20f * ability.cooldownSeconds(stars, Math.max(1, level)));
        float left = learned ? Math.max(0f, (float) (data.cooldownReadyAt(id) - now)) : 0f;
        boolean cooling = left > 0f;
        float target = cooling ? Ease.clamp01(left / totalTicks) : 0f;
        fill[id] = Math.abs(fill[id] - target) > 0.2f ? target : Ease.approach(fill[id], target, dt, 20f);

        float activeTicks = 20f * ability.durationSeconds(stars, Math.max(1, level));
        double started = data.cooldownReadyAt(id) - (double) totalTicks;
        float activeLeft = learned ? (float) (started + activeTicks - now) : 0f;
        boolean active = cooling && activeLeft > 0f;

        if (wasCooling[id] && !cooling) {
            readyFlash[id] = 1f;
        }
        wasCooling[id] = cooling;
        readyFlash[id] = Math.max(0f, readyFlash[id] - dt * 1.8f);
        useFlash[id] = Math.max(0f, useFlash[id] - dt * 2.2f);
        shake[id] = Math.max(0f, shake[id] - dt * 3f);

        float sx = shake[id] > 0f ? (float) Math.sin(CLOCK.time * 70f) * 2.5f * shake[id] : 0f;
        float pop = 1f + 0.12f * Ease.smoothStep(readyFlash[id]) + 0.08f * useFlash[id];
        var pose = g.pose();
        pose.pushPose();
        pose.translate(x + SLOT / 2f + sx, y + SLOT / 2f, 0f);
        pose.scale(pop, pop, 1f);
        pose.translate(-SLOT / 2f, -SLOT / 2f, 0f);

        // подложка и рамка
        g.fill(0, 0, SLOT, SLOT, argb(0.78f * alpha, 0x120709));
        float pulse = active ? 0.55f + 0.45f * (float) Math.sin(CLOCK.time * 7f) : 0f;
        float border = learned ? (cooling ? 0.45f : 0.85f) : 0.25f;
        border = Math.max(border, pulse);
        g.renderOutline(0, 0, SLOT, SLOT, argb(border * alpha, CRIMSON));
        if (active) {
            g.renderOutline(-1, -1, SLOT + 2, SLOT + 2, argb(0.45f * pulse * alpha, 0xFF5A5A));
        }

        // иконка
        float tint = learned ? (cooling ? 0.45f : 1f) : 0.3f;
        RenderSystem.setShaderColor(tint, tint * (learned ? 1f : 0.95f), tint * (learned ? 1f : 1.1f), alpha);
        g.blit(ICONS, 2, 2, ability.id * 16, 0, 16, 16, 48, 16);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // заливка перезарядки (снизу вверх убывает)
        if (fill[id] > 0.001f) {
            int fh = Math.round((SLOT - 2) * fill[id]);
            g.fill(1, SLOT - 1 - fh, SLOT - 1, SLOT - 1, argb(0.62f * alpha, 0x000000));
            g.fill(1, SLOT - 1 - fh, SLOT - 1, SLOT - fh, argb(0.8f * alpha, CRIMSON));
        }
        // полоска «действует» под ячейкой
        if (active && activeTicks > 0f) {
            int bw = Math.round(SLOT * Ease.clamp01(activeLeft / activeTicks));
            g.fill(0, SLOT + 1, bw, SLOT + 3, argb(0.9f * alpha, 0xFF6A58));
        }
        // вспышки готовности / применения
        float flash = Math.max(readyFlash[id], useFlash[id]);
        if (flash > 0.01f) {
            g.fill(1, 1, SLOT - 1, SLOT - 1, argb(0.45f * flash * alpha, 0xFFB0A0));
        }
        // секунды
        if (cooling) {
            int sec = (int) Math.ceil(left / 20f);
            if (sec != secCache[id]) {
                secCache[id] = sec;
                secText[id] = FormattedCharSequence.forward(String.valueOf(sec), net.minecraft.network.chat.Style.EMPTY);
                secWidth[id] = font.width(secText[id]);
            }
            int tw = secWidth[id];
            pose.pushPose();
            pose.translate(SLOT / 2f, SLOT / 2f - 3f, 0f);
            pose.scale(0.85f, 0.85f, 1f);
            g.drawString(font, secText[id], -tw / 2, 0, argb(alpha, 0xFFFFFF), true);
            pose.popPose();
        }
        // клавиша
        int kw = keyWidth[id];
        pose.pushPose();
        pose.translate(SLOT - 1f, SLOT - 5f, 0f);
        pose.scale(0.5f, 0.5f, 1f);
        if (keyText[id] != null) g.drawString(font, keyText[id], -kw, 0, argb(alpha * (learned ? 1f : 0.5f), 0xE09098), true);
        pose.popPose();

        pose.popPose();
    }

    private static void drawWard(GuiGraphics g, RelicData data, int x, int y, float dt, float alpha) {
        wardFlash = Math.max(0f, wardFlash - dt * 1.6f);
        int id = RelicAbility.BLOOD_WARD.id;
        if (data.abilityLevel(id) <= 0) {
            return;
        }
        float f = Ease.smoothStep(wardFlash);
        var pose = g.pose();
        pose.pushPose();
        float s = 0.62f + 0.18f * f;
        pose.translate(x + 6f, y + 6f, 0f);
        pose.scale(s, s, 1f);
        pose.translate(-8f, -8f, 0f);
        if (f > 0.01f) {
            g.fill(-2, -2, 18, 18, argb(0.5f * f * alpha, CRIMSON));
        }
        float tint = 0.55f + 0.45f * f;
        RenderSystem.setShaderColor(tint, tint, tint, alpha * (0.6f + 0.4f * f));
        g.blit(ICONS, 0, 0, id * 16, 0, 16, 16, 48, 16);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        pose.popPose();
    }

    private static int argb(float a, int rgb) {
        int ai = (int) (Ease.clamp01(a) * 255f);
        return (ai << 24) | (rgb & 0xFFFFFF);
    }
}
