package dev.agentsharik.angelring;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Серверная логика: применение действий из окна и использование способностей. */
public final class RelicActions {
    private RelicActions() {}

    public static void perform(ServerPlayer player, ItemStack stack, int action, int arg) {
        RelicData old = stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
        if (action == RelicData.ACTION_USE) {
            useAbility(player, stack, old, arg);
            return;
        }
        RelicData updated = old.act(action, arg);
        if (updated != old) {
            stack.set(ModComponents.RELIC_DATA.get(), updated);
            if (updated.rank() > old.rank()) {
                player.displayClientMessage(Component.translatable("message.angelring.rank_up", updated.rank()), true);
            }
        }
    }

    private static void useAbility(ServerPlayer player, ItemStack stack, RelicData data, int slot) {
        if (slot < 0 || slot >= RelicData.SLOTS) {
            return;
        }
        RelicAbility ability = RelicAbility.byId(data.equipped().get(slot));
        if (ability == null || ability.passive) {
            return;
        }
        int level = data.abilityLevel(ability.id);
        long now = player.level().getGameTime();
        if (level <= 0 || now < data.cooldownReadyAt(ability.id)) {
            return; // не изучена или ещё перезаряжается
        }

        ServerLevel world = player.serverLevel();
        switch (ability) {
            case DASH -> {
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(look.scale(ability.dashPower(level)));
                player.fallDistance = 0f;
                player.hurtMarked = true;
                player.connection.send(new ClientboundSetEntityMotionPacket(player));
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.7f, 1.5f);
                world.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1.0, player.getZ(),
                        14, 0.3, 0.3, 0.3, 0.03);
            }
            case WARD -> {
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 30, level - 1, false, true));
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.8f);
                world.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
                        12, 0.4, 0.6, 0.4, 0.02);
            }
            default -> {
                return;
            }
        }

        RelicData next = data.withCooldown(ability.id, now + 20L * ability.cooldownSeconds(level))
                .addXp(RelicData.XP_PER_ABILITY_USE);
        stack.set(ModComponents.RELIC_DATA.get(), next);
        if (next.level() > data.level()) {
            player.displayClientMessage(Component.translatable("message.angelring.level_up", next.level()), true);
        }
    }
}
