package dev.agentsharik.angelring;

import java.util.Optional;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

/** Ищет надетое кольцо в слотах Curios. Работает и на клиенте, и на сервере. */
public final class RingLookup {
    private RingLookup() {}

    public static Optional<ItemStack> find(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(ModItems.ANGEL_RING.get()))
                .map(SlotResult::stack);
    }
}
