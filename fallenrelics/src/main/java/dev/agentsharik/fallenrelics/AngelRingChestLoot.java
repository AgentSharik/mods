package dev.agentsharik.fallenrelics;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import java.util.Set;

/**
 * Артефактный способ добычи: кольцо не крафтится, а редко попадается в сундуках сокровищ —
 * как предметы мода Artifacts, на который оно стилизовано. Добавляем свой пул прямо
 * в таблицы ванильных сундуков на загрузке таблицы: без сериализаторов и датапак-JSON.
 */
public final class AngelRingChestLoot {
    private AngelRingChestLoot() {}

    /** Шанс кольца в подходящем сундуке, как у редких артефактов. */
    private static final float CHANCE = 0.05f;

    private static final Set<ResourceLocation> TREASURE_CHESTS = Set.of(
            ResourceLocation.parse("minecraft:chests/simple_dungeon"),
            ResourceLocation.parse("minecraft:chests/abandoned_mineshaft"),
            ResourceLocation.parse("minecraft:chests/buried_treasure"),
            ResourceLocation.parse("minecraft:chests/desert_pyramid"),
            ResourceLocation.parse("minecraft:chests/jungle_temple"),
            ResourceLocation.parse("minecraft:chests/stronghold_corridor"),
            ResourceLocation.parse("minecraft:chests/stronghold_crossing"),
            ResourceLocation.parse("minecraft:chests/end_city_treasure"),
            ResourceLocation.parse("minecraft:chests/ancient_city"));

    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!TREASURE_CHESTS.contains(event.getName())) {
            return;
        }
        event.getTable().addPool(LootPool.lootPool()
                .add(LootItem.lootTableItem(ModContent.ANGEL_RING.get())
                        .when(LootItemRandomChanceCondition.randomChance(CHANCE)))
                .build());
    }
}
