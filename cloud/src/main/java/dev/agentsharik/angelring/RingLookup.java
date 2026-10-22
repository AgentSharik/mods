package dev.agentsharik.angelring;

import java.util.Optional;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

/** Ищет кольца: надетые в слотах Curios или лежащие в слоте открытого меню. */
public final class RingLookup {
    private RingLookup() {}

    /** Какое из трёх колец надето. */
    public enum Kind { ANGEL, AWAKENED, GLIDING }

    public record Worn(Kind kind, ItemStack stack) {}

    /** Надетое «Кольцо ангела» (слот Curios). Работает и на клиенте, и на сервере. */
    public static Optional<ItemStack> find(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(ModItems.ANGEL_RING.get()))
                .map(SlotResult::stack);
    }

    /** Любое надетое кольцо цепочки: ангела, пробуждённое или кольцо планирования (в таком приоритете). */
    public static Optional<Worn> findWorn(Player player) {
        return CuriosApi.getCuriosInventory(player).flatMap(handler -> {
            Optional<SlotResult> angel = handler.findFirstCurio(ModItems.ANGEL_RING.get());
            if (angel.isPresent()) {
                return Optional.of(new Worn(Kind.ANGEL, angel.get().stack()));
            }
            Optional<SlotResult> awakened = handler.findFirstCurio(ModItems.AWAKENED_RING.get());
            if (awakened.isPresent()) {
                return Optional.of(new Worn(Kind.AWAKENED, awakened.get().stack()));
            }
            Optional<SlotResult> gliding = handler.findFirstCurio(ModItems.GLIDING_RING.get());
            return gliding.map(result -> new Worn(Kind.GLIDING, result.stack()));
        });
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
