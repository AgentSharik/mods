package dev.agentsharik.angelring;

import java.util.Optional;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

/** Ищет кольцо: надетое в слотах Curios или лежащее в слоте открытого меню. */
public final class RingLookup {
    private RingLookup() {}

    /** Надетое кольцо (слот Curios). Работает и на клиенте, и на сервере. */
    public static Optional<ItemStack> find(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(ModItems.ANGEL_RING.get()))
                .map(SlotResult::stack);
    }

    /**
     * Кольцо по номеру слота открытого меню (так окно открывается при наведении в инвентаре).
     * menuSlot < 0 означает «надетое кольцо».
     */
    public static Optional<ItemStack> resolve(Player player, AbstractContainerMenu menu, int menuSlot) {
        if (menuSlot < 0) {
            return find(player);
        }
        if (menu == null || menuSlot >= menu.slots.size()) {
            return Optional.empty();
        }
        ItemStack stack = menu.getSlot(menuSlot).getItem();
        return stack.is(ModItems.ANGEL_RING.get()) ? Optional.of(stack) : Optional.empty();
    }
}
