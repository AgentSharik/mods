package dev.agentsharik.angelring.client.ui;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/**
 * Заранее свёрстанный текст: перенос строк, подбор масштаба под ширину рамки, обрезка многоточием и
 * позиции строк считаются один раз (в init или при смене данных). В кадре остаётся только нарисовать.
 *
 * <p>Размеры задаются рамкой (та же геометрия, что и у текстуры), а не длиной строки: если перевод
 * длиннее, текст сам уменьшится, а при нехватке места и на минимальном масштабе — обрежется «…».
 * Ничего не вылезает за рамки ни на одном языке.
 */
public final class TextBox {
    public static final int LEFT = 0, CENTER = 1, RIGHT = 2;
    private static final float MIN_SCALE = 0.5f;

    private FormattedCharSequence[] lines = new FormattedCharSequence[0];
    private int[] widths = new int[0];
    private float x, y, scale = 1f;
    private int align;
    private int color;
    private boolean shadow;
    private float lineStep;

    /** Одна строка в рамке шириной maxW. (ax, y) — точка привязки с учётом align. */
    public TextBox line(Font font, Component text, float ax, float ty, float maxW, float preferredScale,
                        int rgb, boolean withShadow, int alignment) {
        int w = font.width(text);
        float s = preferredScale;
        if (w * s > maxW) {
            s = Math.max(MIN_SCALE, maxW / Math.max(1, w));
        }
        FormattedCharSequence seq;
        if (w * s > maxW + 0.01f) {
            seq = ellipsize(font, text, (int) (maxW / s));
        } else {
            seq = text.getVisualOrderText();
        }
        set(new FormattedCharSequence[] {seq}, font, ax, ty, s, rgb, withShadow, alignment);
        return this;
    }

    /**
     * Несколько строк с переносом: пробуем масштабы от крупного к мелкому, пока текст не влезет
     * в maxLines и maxH. Если не влез даже на самом мелком — последняя строка обрезается «…».
     */
    public TextBox wrap(Font font, Component text, float ax, float ty, float maxW, float maxH, int maxLines,
                        float[] scales, int rgb, boolean withShadow, int alignment) {
        List<FormattedText> split = null;
        float s = scales[scales.length - 1];
        for (float candidate : scales) {
            List<FormattedText> attempt = font.getSplitter().splitLines(text, (int) (maxW / candidate), Style.EMPTY);
            float h = attempt.size() * (font.lineHeight + 1) * candidate;
            if (attempt.size() <= maxLines && h <= maxH + 0.5f) {
                split = attempt;
                s = candidate;
                break;
            }
            split = attempt;
            s = candidate;
        }
        int fit = Math.max(1, Math.min(maxLines, (int) ((maxH + 0.5f) / ((font.lineHeight + 1) * s))));
        int count = Math.min(split.size(), fit);
        FormattedCharSequence[] out = new FormattedCharSequence[count];
        for (int i = 0; i < count; i++) {
            FormattedText part = split.get(i);
            if (i == count - 1 && split.size() > count) {
                out[i] = ellipsize(font, part, (int) (maxW / s));
            } else {
                out[i] = Language.getInstance().getVisualOrder(part);
            }
        }
        set(out, font, ax, ty, s, rgb, withShadow, alignment);
        return this;
    }

    private static FormattedCharSequence ellipsize(Font font, FormattedText text, int maxWidthPx) {
        FormattedText dots = FormattedText.of("\u2026");
        int room = Math.max(0, maxWidthPx - font.width(dots));
        FormattedText cut = font.substrByWidth(text, room);
        return Language.getInstance().getVisualOrder(FormattedText.composite(cut, dots));
    }

    private void set(FormattedCharSequence[] seqs, Font font, float ax, float ty, float s, int rgb,
                     boolean withShadow, int alignment) {
        lines = seqs;
        widths = new int[seqs.length];
        for (int i = 0; i < seqs.length; i++) {
            widths[i] = font.width(seqs[i]);
        }
        x = ax;
        y = ty;
        scale = s;
        color = rgb;
        shadow = withShadow;
        align = alignment;
        lineStep = font.lineHeight + 1;
    }

    public float height() {
        return lines.length * lineStep * scale;
    }

    public void draw(GuiGraphics g, Font font, float alpha) {
        draw(g, font, alpha, color);
    }

    /** Рисует с другим цветом (например, тусклым для неактивной кнопки). Аллокаций нет. */
    public void draw(GuiGraphics g, Font font, float alpha, int argb) {
        if (lines.length == 0) {
            return;
        }
        int a = (int) (((argb >>> 24) & 0xFF) * Ease.clamp01(alpha));
        if (a < 8) {
            return; // шрифт трактует альфу < 4 как «непрозрачный», поэтому почти невидимый текст пропускаем
        }
        int c = (a << 24) | (argb & 0xFFFFFF);
        var pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0f);
        pose.scale(scale, scale, 1f);
        for (int i = 0; i < lines.length; i++) {
            int dx = switch (align) {
                case CENTER -> -widths[i] / 2;
                case RIGHT -> -widths[i];
                default -> 0;
            };
            g.drawString(font, lines[i], dx, Math.round(i * lineStep), c, shadow);
        }
        pose.popPose();
    }

    public int color() {
        return color;
    }
}
