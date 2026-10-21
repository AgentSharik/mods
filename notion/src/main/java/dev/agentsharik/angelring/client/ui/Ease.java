package dev.agentsharik.angelring.client.ui;

import net.minecraft.Util;

/** Easing-функции и независимое от FPS сглаживание. Без аллокаций. */
public final class Ease {
    private Ease() {}

    public static float clamp01(float x) {
        return x < 0f ? 0f : (x > 1f ? 1f : x);
    }

    public static float outCubic(float t) {
        float u = 1f - clamp01(t);
        return 1f - u * u * u;
    }

    public static float inOutCubic(float t) {
        t = clamp01(t);
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }

    public static float smoothStep(float t) {
        t = clamp01(t);
        return t * t * (3f - 2f * t);
    }

    public static float smoothStep(float edge0, float edge1, float x) {
        return smoothStep((x - edge0) / (edge1 - edge0));
    }

    /** Лёгкий «перелёт» за 1.0 и возврат — для появления звёзд и окна. */
    public static float outBack(float t) {
        t = clamp01(t);
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /**
     * Экспоненциальное приближение к цели: одинаково выглядит на 30 и на 240 FPS, потому что зависит
     * только от реального времени кадра dt. rate — «скорость» (чем больше, тем быстрее).
     */
    public static float approach(float current, float target, float dt, float rate) {
        float k = (float) Math.exp(-rate * dt);
        float v = target + (current - target) * k;
        return Math.abs(v - target) < 0.0005f ? target : v;
    }

    /** Измеритель времени кадра (Delta Time). Один объект на экран, ничего не создаёт в кадре. */
    public static final class Clock {
        private long last = -1L;
        /** Секунды с момента первого кадра. */
        public float time;
        /** Длительность прошлого кадра в секундах (не больше 0.1 — чтобы после фриза не было рывка). */
        public float dt;

        public void frame() {
            long now = Util.getNanos();
            dt = last < 0L ? 0f : Math.min(0.1f, (now - last) / 1.0e9f);
            last = now;
            time += dt;
        }
    }
}
