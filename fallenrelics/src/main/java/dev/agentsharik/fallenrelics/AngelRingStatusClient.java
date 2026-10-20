package dev.agentsharik.fallenrelics;

import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client-side cache of the latest angel ring snapshot. The relic scroll screen
 * asks the server for a fresh snapshot once a second while it is open; between
 * answers the screen reads these fields. Only ever touched on the client.
 */
public final class AngelRingStatusClient {
    private static boolean granted;
    private static boolean flying;
    private static boolean buffering;
    private static int bufferTicks;
    private static long flightTicks;
    private static RelicData relic = RelicData.EMPTY;
    private static long receivedAt;

    private AngelRingStatusClient() {}

    public static void apply(AngelRingStatusPayload payload) {
        granted = payload.granted();
        flying = payload.flying();
        buffering = payload.buffering();
        bufferTicks = payload.bufferTicks();
        flightTicks = payload.flightTicks();
        relic = payload.relic();
        receivedAt = System.currentTimeMillis();
    }

    /** Asks the server for a fresh snapshot; safe to call any time on the client. */
    public static void request() {
        PacketDistributor.sendToServer(AngelRingStatusRequestPayload.INSTANCE);
    }

    public static boolean granted() {
        return granted;
    }

    public static boolean flying() {
        return flying;
    }

    public static boolean buffering() {
        return buffering;
    }

    public static int bufferTicks() {
        return bufferTicks;
    }

    public static long flightTicks() {
        return flightTicks;
    }

    public static RelicData relic() {
        return relic;
    }

    /** True when no snapshot arrived for five seconds (server side unreachable). */
    public static boolean stale() {
        return System.currentTimeMillis() - receivedAt > 5000L;
    }
}
