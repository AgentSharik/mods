package dev.agentsharik.angelring.client.ui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Пул 2D-частиц для GUI без аллокаций: все данные лежат в предвыделенных массивах фиксированного
 * размера (structure of arrays). Новая частица занимает свободную ячейку, а если свободных нет — самую старую
 * (кольцевой буфер). Сборщик мусора при работе эффектов не задействуется.
 */
public final class ParticlePool {
    private final int capacity;
    private final float[] x, y, vx, vy, life, maxLife, size;
    private final int[] color;
    private final boolean[] alive;
    private int cursor;
    private int aliveCount;
    /** Дешёвый собственный ГПСЧ (xorshift): без создания Random и без блокировок. */
    private int seed = 0x9E3779B9;

    public ParticlePool(int capacity) {
        this.capacity = capacity;
        x = new float[capacity];
        y = new float[capacity];
        vx = new float[capacity];
        vy = new float[capacity];
        life = new float[capacity];
        maxLife = new float[capacity];
        size = new float[capacity];
        color = new int[capacity];
        alive = new boolean[capacity];
    }

    public float rand() {
        seed ^= seed << 13;
        seed ^= seed >>> 17;
        seed ^= seed << 5;
        return (seed >>> 8) / (float) (1 << 24);
    }

    public int aliveCount() {
        return aliveCount;
    }

    public void spawn(float px, float py, float pvx, float pvy, float seconds, float psize, int rgb) {
        int slot = -1;
        for (int n = 0; n < capacity; n++) {
            int i = (cursor + n) % capacity;
            if (!alive[i]) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            slot = cursor; // пул заполнен — переиспользуем самую старую
        } else {
            aliveCount++;
        }
        cursor = (slot + 1) % capacity;
        alive[slot] = true;
        x[slot] = px;
        y[slot] = py;
        vx[slot] = pvx;
        vy[slot] = pvy;
        life[slot] = seconds;
        maxLife[slot] = seconds;
        size[slot] = psize;
        color[slot] = rgb & 0xFFFFFF;
    }

    /** Вспышка частиц из контура прямоугольника наружу (отклик на клик по кнопке). */
    public void burstRect(float rx, float ry, float rw, float rh, int count, int rgbA, int rgbB) {
        float cx = rx + rw / 2f;
        float cy = ry + rh / 2f;
        float perimeter = 2f * (rw + rh);
        for (int n = 0; n < count; n++) {
            float p = rand() * perimeter;
            float px, py;
            if (p < rw) { px = rx + p; py = ry; }
            else if (p < rw + rh) { px = rx + rw; py = ry + (p - rw); }
            else if (p < 2f * rw + rh) { px = rx + (p - rw - rh); py = ry + rh; }
            else { px = rx; py = ry + (p - 2f * rw - rh); }
            float dx = px - cx;
            float dy = py - cy;
            float len = (float) Math.sqrt(dx * dx + dy * dy) + 0.001f;
            float speed = 18f + rand() * 34f;
            spawn(px, py, dx / len * speed, dy / len * speed - 6f, 0.35f + rand() * 0.35f,
                    1f + rand() * 1.5f, rand() < 0.5f ? rgbA : rgbB);
        }
    }

    public void update(float dt) {
        if (aliveCount == 0 || dt <= 0f) {
            return;
        }
        float drag = (float) Math.exp(-3.5f * dt);
        for (int i = 0; i < capacity; i++) {
            if (!alive[i]) {
                continue;
            }
            life[i] -= dt;
            if (life[i] <= 0f) {
                alive[i] = false;
                aliveCount--;
                continue;
            }
            vx[i] *= drag;
            vy[i] = vy[i] * drag + 14f * dt; // чуть-чуть оседают
            x[i] += vx[i] * dt;
            y[i] += vy[i] * dt;
        }
    }

    /** Квадратики с затуханием (рисуется в текущей трансформации). alpha — общая прозрачность экрана. */
    public void render(GuiGraphics g, float alpha) {
        if (aliveCount == 0) {
            return;
        }
        for (int i = 0; i < capacity; i++) {
            if (!alive[i]) {
                continue;
            }
            float k = life[i] / maxLife[i];
            int a = (int) (255f * alpha * Ease.smoothStep(k * 1.6f));
            if (a < 6) {
                continue;
            }
            int s = Math.max(1, Math.round(size[i] * (0.6f + 0.4f * k)));
            int px = Math.round(x[i]);
            int py = Math.round(y[i]);
            g.fill(px, py, px + s, py + s, (a << 24) | color[i]);
        }
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            alive[i] = false;
        }
        aliveCount = 0;
    }
}
