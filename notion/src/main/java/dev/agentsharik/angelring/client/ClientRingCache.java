package dev.agentsharik.angelring.client;

import dev.agentsharik.angelring.ModComponents;
import dev.agentsharik.angelring.RelicData;
import dev.agentsharik.angelring.RingLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * Надетое кольцо и его данные на клиенте. Обновляется один раз за клиентский тик (20 раз в секунду),
 * чтобы HUD не искал кольцо в слотах Curios каждый кадр (на 144 FPS это в 7 раз реже).
 */
public final class ClientRingCache {
    private ClientRingCache() {}

    private static boolean worn;
    private static RelicData data = RelicData.EMPTY;

    static void refresh(Minecraft mc) {
        if (mc.player == null) {
            worn = false;
            data = RelicData.EMPTY;
            return;
        }
        ItemStack stack = RingLookup.find(mc.player).orElse(ItemStack.EMPTY);
        worn = !stack.isEmpty();
        data = worn ? stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY) : RelicData.EMPTY;
    }

    public static boolean worn() {
        return worn;
    }

    public static RelicData data() {
        return data;
    }
}
