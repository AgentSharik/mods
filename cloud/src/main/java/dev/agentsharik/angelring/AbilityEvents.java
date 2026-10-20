package dev.agentsharik.angelring;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

/** Пассивные способности кольца. */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID)
public final class AbilityEvents {
    private AbilityEvents() {}

    /** «Лёгкость пера»: снижает урон от падения, если способность изучена и стоит в слоте. */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        RingLookup.find(player).ifPresent(stack -> {
            RelicData data = stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
            int level = data.equippedLevel(RelicAbility.FEATHER);
            if (level > 0) {
                float keep = 1f - RelicAbility.FEATHER.fallReductionPercent(level) / 100f;
                event.setDamageMultiplier(event.getDamageMultiplier() * Math.max(0f, keep));
            }
        });
    }
}
