package dev.agentsharik.angelring;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Серверные помощники для эффектов: только собственные частицы мода (ванильные не используются).
 * Все методы рассылают частицы клиентам; вид и размеры частиц определяют клиентские классы.
 */
public final class RelicFx {
    private RelicFx() {}

    /** Сколько тиков сфера опыта летит от игрока до кольца. Клиентская частица живёт ровно столько же. */
    public static final int ORB_FLIGHT_TICKS = 18;

    /** Искра с заданной скоростью. */
    public static void spark(ServerLevel w, double x, double y, double z, double vx, double vy, double vz) {
        w.sendParticles(ModParticles.CRIMSON_SPARK.get(), x, y, z, 0, vx, vy, vz, 1.0);
    }

    /** Клубок багрового дыма с заданной скоростью. */
    public static void smoke(ServerLevel w, double x, double y, double z, double vx, double vy, double vz) {
        w.sendParticles(ModParticles.CRIMSON_SMOKE.get(), x, y, z, 0, vx, vy, vz, 1.0);
    }

    /**
     * Сфера опыта: вылетает из {@code from} и за {@link #ORB_FLIGHT_TICKS} тиков долетает до {@code to}.
     * Скорость подобрана так, чтобы частица (без трения) прошла весь путь за время жизни.
     */
    public static void orb(ServerLevel w, Vec3 from, Vec3 to) {
        double k = 1.0 / ORB_FLIGHT_TICKS;
        w.sendParticles(ModParticles.XP_ORB.get(), from.x, from.y, from.z, 0,
                (to.x - from.x) * k, (to.y - from.y) * k, (to.z - from.z) * k, 1.0);
    }

    /** Мягкая багровая вспышка (неподвижная). */
    public static void glow(ServerLevel w, double x, double y, double z) {
        w.sendParticles(ModParticles.CRIMSON_GLOW.get(), x, y, z, 0, 0, 0, 0, 1.0);
    }

    /**
     * Тонкий луч с центром в (x, y, z): направление (dx, dy, dz) нормализуется, {@code length} — длина в блоках.
     */
    public static void beam(ServerLevel w, double x, double y, double z, double dx, double dy, double dz, double length) {
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6) {
            dx = 0;
            dy = 1;
            dz = 0;
            len = 1;
        }
        double k = length / len;
        w.sendParticles(ModParticles.CRIMSON_BEAM.get(), x, y, z, 0, dx * k, dy * k, dz * k, 1.0);
    }

    /** Луч между двумя точками. */
    public static void beamBetween(ServerLevel w, Vec3 a, Vec3 b) {
        Vec3 d = b.subtract(a);
        Vec3 mid = a.add(b).scale(0.5);
        beam(w, mid.x, mid.y, mid.z, d.x, d.y, d.z, d.length());
    }

    /** Горизонтальное кольцо из касательных лучей: выглядит как сплошная тонкая окружность. */
    public static void beamCircle(ServerLevel w, Vec3 c, double radius, int segments, double rot) {
        double step = Math.PI * 2 / segments;
        double chord = 2 * radius * Math.sin(step / 2) * 1.3;
        for (int k = 0; k < segments; k++) {
            double a = rot + k * step;
            double cos = Math.cos(a);
            double sin = Math.sin(a);
            beam(w, c.x + cos * radius, c.y, c.z + sin * radius, -sin, 0, cos, chord);
        }
    }

    /** Кольцо из искр, разлетающихся от центра (для ударных волн и вспышек). */
    public static void sparkRing(ServerLevel w, Vec3 c, double radius, int points, double speed) {
        for (int k = 0; k < points; k++) {
            double a = k * (Math.PI * 2 / points);
            double dx = Math.cos(a);
            double dz = Math.sin(a);
            spark(w, c.x + dx * radius, c.y, c.z + dz * radius, dx * speed, 0.01, dz * speed);
        }
    }
}
