package dev.agentsharik.angelring;

import dev.agentsharik.angelring.network.RelicActionPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Аддон «Кольцо ангела». Кольца носятся в слоте аксессуаров Curios (слот "ring").
 */
@Mod(AngelRingMod.MOD_ID)
public class AngelRingMod {
    public static final String MOD_ID = "angelring";

    public AngelRingMod(IEventBus modEventBus) {
        ModComponents.COMPONENTS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(ModItems::addToCreativeTab);
        modEventBus.addListener(AngelRingMod::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(RelicActionPayload.TYPE, RelicActionPayload.STREAM_CODEC, RelicActionPayload::handle);
    }
}
