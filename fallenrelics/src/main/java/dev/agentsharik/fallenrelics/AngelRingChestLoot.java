package dev.agentsharik.fallenrelics;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.GlobalLootModifierSerializer;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

/**
 * Артефактный способ добычи: кольцо не крафтится, а редко попадается в сундуках сокровищ —
 * как предметы мода Artifacts, на который оно стилизовано.
 */
public final class AngelRingChestLoot extends LootModifier {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, FallenRelicsMod.MOD_ID);

    public static final MapCodec<AngelRingChestLoot> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
            .and(ExtraCodecs.POSITIVE_FLOAT.fieldOf("chance").forGetter(modifier -> modifier.chance))
            .apply(inst, AngelRingChestLoot::new));

    public static final DeferredHolder<GlobalLootModifierSerializer<?>, AngelRingChestLoot.Serializer> SERIALIZER =
            SERIALIZERS.register("angel_ring_chests", AngelRingChestLoot.Serializer::new);

    private final float chance;

    public AngelRingChestLoot(LootItemCondition[] conditionsIn, float chance) {
        super(conditionsIn);
        this.chance = chance;
    }

    @Override
    protected List<ItemStack> doLoot(LootContext context, List<ItemStack> generatedLoot) {
        if (context.getRandom().nextFloat() < chance) {
            generatedLoot.add(new ItemStack(ModContent.ANGEL_RING.get()));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends LootModifier> codec() {
        return CODEC;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AngelRingChestLoot> {
        @Override
        public MapCodec<AngelRingChestLoot> codec() {
            return CODEC;
        }
    }
}
