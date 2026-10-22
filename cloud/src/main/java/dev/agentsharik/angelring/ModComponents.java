package dev.agentsharik.angelring;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModComponents {
    private ModComponents() {}

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AngelRingMod.MOD_ID);

    /** Прокачка «Кольца ангела». */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RelicData>> RELIC_DATA =
            COMPONENTS.register("relic_data", () -> DataComponentType.<RelicData>builder()
                    .persistent(RelicData.CODEC)
                    .networkSynchronized(RelicData.STREAM_CODEC)
                    .build());

    /** Заряд полёта «Пробуждённого кольца». */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FlightState>> FLIGHT_STATE =
            COMPONENTS.register("flight_state", () -> DataComponentType.<FlightState>builder()
                    .persistent(FlightState.CODEC)
                    .networkSynchronized(FlightState.STREAM_CODEC)
                    .build());
}
