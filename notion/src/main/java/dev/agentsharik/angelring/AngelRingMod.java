package dev.agentsharik.angelring;

import dev.agentsharik.angelring.network.RelicActionPayload;
import dev.agentsharik.angelring.network.RelicResultPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Аддон «Кольцо ангела». Само кольцо носится в слоте аксессуаров Curios (слот "ring").
 */
@Mod(AngelRingMod.MOD_ID)
public class AngelRingMod {
    public static final String MOD_ID = "angelring";

    public AngelRingMod(IEventBus modEventBus) {
        ModComponents.COMPONENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        modEventBus.addListener(ModItems::addToCreativeTab);
        modEventBus.addListener(AngelRingMod::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // версия протокола поднята: старый клиент с новым сервером (и наоборот) не соединится
        PayloadRegistrar registrar = event.registrar("2");
        registrar.playToServer(RelicActionPayload.TYPE, RelicActionPayload.STREAM_CODEC, RelicActionPayload::handle);
        // Клиентский обработчик вызывается через лямбду: класс client.ClientResultHandler загрузится только
        // при первом пакете, то есть только на клиенте. Выделенный сервер его никогда не тронет.
        registrar.playToClient(RelicResultPayload.TYPE, RelicResultPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> dev.agentsharik.angelring.client.ClientResultHandler.handle(payload)));
    }
}
