package dev.agentsharik.angelring;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

/** Серверная логика: повышение уровня, улучшения за уровни игрока, реролл редкости и применение ульты. */
public final class RelicActions {
    private RelicActions() {}

    public static void perform(ServerPlayer player, ItemStack stack, int action, int arg) {
        RelicData data = stack.getOrDefault(ModComponents.RELIC_DATA.get(), RelicData.EMPTY);
        switch (action) {
            case RelicData.ACTION_LEVEL_UP -> levelUp(player, stack, data);
            case RelicData.ACTION_UPGRADE -> upgrade(player, stack, data, arg);
            case RelicData.ACTION_REROLL -> reroll(player, stack, data);
            case RelicData.ACTION_USE -> use(player, stack, data, arg);
            default -> { }
        }
    }

    /**
     * Повышение уровня кольца: опыт вычитается на величину порога, весь остаток сохраняется, порог растёт.
     * Если опыта не хватает или уровень максимальный, ничего не происходит.
     */
    private static void levelUp(ServerPlayer player, ItemStack stack, RelicData data) {
        RelicData next = data.levelUp();
        if (next == data) {
            return;
        }
        stack.set(ModComponents.RELIC_DATA.get(), next);
        ServerLevel world = player.serverLevel();
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9f, 0.8f);
        world.sendParticles(ModParticles.CRIMSON_SPARK.get(), player.getX(), player.getY() + 1.0, player.getZ(),
                24, 0.5, 0.8, 0.5, 0.15);
        player.displayClientMessage(Component.translatable("message.angelring.level_up", next.level()), true);
        for (RelicAbility ability : RelicAbility.values()) {
            if (ability.unlockLevel == next.level()) {
                player.sendSystemMessage(Component.translatable("message.angelring.ability_unlocked",
                        Component.translatable("gui.angelring.ability." + ability.key + ".name")));
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 0.7f);
            }
        }
    }

    /** Улучшение способности стоит уровни опыта игрока; креативу уровни не нужны (удобно для проверки). */
    private static void upgrade(ServerPlayer player, ItemStack stack, RelicData data, int id) {
        int cost = data.upgradeCost(id);
        if (cost < 0 || !data.canUpgrade(id)) {
            return; // максимум или способность ещё закрыта
        }
        boolean free = player.isCreative();
        if (!free && player.experienceLevel < cost) {
            return;
        }
        if (!free) {
            player.giveExperienceLevels(-cost);
        }
        stack.set(ModComponents.RELIC_DATA.get(), data.upgrade(id));
        ServerLevel world = player.serverLevel();
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
    }

    private static void reroll(ServerPlayer player, ItemStack stack, RelicData data) {
        boolean free = player.isCreative();
        if (!free && player.experienceLevel < RelicData.REROLL_COST) {
            return;
        }
        if (!free) {
            player.giveExperienceLevels(-RelicData.REROLL_COST);
        }
        stack.set(ModComponents.RELIC_DATA.get(), data.reroll(player.getRandom()));
        ServerLevel world = player.serverLevel();
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 0.7f);
        world.sendParticles(ModParticles.CRIMSON_SPARK.get(), player.getX(), player.getY() + 1.0, player.getZ(),
                16, 0.4, 0.6, 0.4, 0.05);
    }

    /** Применение активной способности (она у кольца одна — «Багровое вознесение», открывается на 10 уровне). */
    private static void use(ServerPlayer player, ItemStack stack, RelicData data, int id) {
        RelicAbility ability = RelicAbility.byId(id);
        if (ability == null || !ability.active) {
            return;
        }
        int level = data.abilityLevel(id);
        long now = player.level().getGameTime();
        if (level <= 0 || now < data.cooldownReadyAt(id)) {
            return; // закрыта или ещё перезаряжается
        }
        int stars = data.abilityStars(id);
        AscensionEffects.start(player, stars, level);
        long ready = now + 20L * ability.cooldownSeconds(stars, level);
        stack.set(ModComponents.RELIC_DATA.get(), data.withCooldown(id, ready));
    }
}
