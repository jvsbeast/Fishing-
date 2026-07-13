package com.anglersdream.fish;

import com.anglersdream.AnglersDream;
import com.anglersdream.item.BaitItem;
import com.anglersdream.item.TieredRodItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.List;

public final class FishingLogic {

    private FishingLogic() {}

    /**
     * Generates the loot for a successful reel-in at the bobber position.
     * Handles junk/treasure (delegated to vanilla loot tables) and custom fish generation.
     */
    public static List<ItemStack> generateCatch(ServerWorld world, BlockPos pos, PlayerEntity player, ItemStack rod) {
        Random random = world.getRandom();

        // 1.21: enchantments are data-driven; this sums the rod's fishing-luck effect (Luck of the Sea).
        int enchantLuck = EnchantmentHelper.getFishingLuckBonus(world, rod, player);

        int rodLuck = 0;
        double rodSize = 0.0;
        double rodVariant = 1.0;
        if (rod.getItem() instanceof TieredRodItem tiered) {
            rodLuck = tiered.rarityLuck;
            rodSize = tiered.sizeBonus;
            rodVariant = tiered.variantMultiplier;
        }

        BaitItem bait = consumeBait(player);
        int baitLuck = 0;
        double baitSize = 0.0;
        double baitVariant = 1.0;
        if (bait != null) {
            baitLuck = bait.rarityLuck;
            baitSize = bait.sizeBonus;
            baitVariant = bait.variantMultiplier;
        }

        // --- Junk / treasure rolls (vanilla loot tables keep enchanted books etc. obtainable) ---
        double treasureChance = 0.04 + 0.02 * enchantLuck + 0.01 * rodLuck;
        double junkChance = Math.max(0.01, 0.10 - 0.02 * enchantLuck - 0.02 * baitLuck);
        double roll = random.nextDouble();
        if (roll < treasureChance) {
            return vanillaLoot(world, pos, rod, LootTables.FISHING_TREASURE_GAMEPLAY, enchantLuck + rodLuck);
        }
        if (roll < treasureChance + junkChance) {
            return vanillaLoot(world, pos, rod, LootTables.FISHING_JUNK_GAMEPLAY, enchantLuck);
        }

        // --- Custom fish ---
        int rarityLuck = enchantLuck + rodLuck + baitLuck;
        BiomeGroup group = BiomeGroup.of(world, pos);
        FishSpecies species = pickSpecies(group, rarityLuck, random);

        // Size roll: heavily skewed toward small fish; size bonuses flatten the curve.
        double sizeBonus = rodSize + baitSize;
        double exponent = 2.6 / (1.0 + sizeBonus);
        double t = Math.pow(random.nextDouble(), exponent);
        float size = (float) (species.minSize() + (species.maxSize() - species.minSize()) * t);

        // Weight grows super-linearly with length, with a little noise.
        double wt = Math.pow(t, 1.4) * (0.85 + random.nextDouble() * 0.3);
        float weight = (float) Math.max(species.minWeight(),
                species.minWeight() + (species.maxWeight() - species.minWeight()) * wt);

        // Trophy-class giants: a rare roll lets a fish overshoot its species' normal
        // maximum, up to 2.5x length (and correspondingly heavier). Size gear makes
        // giants a little more frequent and a little bigger.
        if (random.nextDouble() < 0.02 + 0.02 * sizeBonus) {
            double giant = 1.15 + random.nextDouble() * (0.6 + 0.5 * sizeBonus);
            size = (float) Math.min(species.maxSize() * 2.5, size * giant);
            weight = (float) Math.min(species.maxWeight() * 4.0, weight * Math.pow(giant, 1.8));
        }

        Variant variant = Variant.roll(random, rodVariant * baitVariant * (1.0 + 0.10 * rarityLuck));

        ItemStack stack = new ItemStack(AnglersDream.FISH_ITEMS.get(species));
        stack.set(AnglersDream.FISH_DATA, new FishData(
                Math.round(size * 10.0f) / 10.0f,
                Math.round(weight * 100.0f) / 100.0f,
                variant));

        return List.of(stack);
    }

    /** One roll of the vanilla treasure table (used for minigame treasure chests). */
    public static List<ItemStack> treasureLoot(ServerWorld world, BlockPos pos, ItemStack rod, int luck) {
        return vanillaLoot(world, pos, rod, LootTables.FISHING_TREASURE_GAMEPLAY, luck);
    }

    private static List<ItemStack> vanillaLoot(ServerWorld world, BlockPos pos, ItemStack rod,
                                               RegistryKey<LootTable> table, int luck) {
        LootContextParameterSet params = new LootContextParameterSet.Builder(world)
                .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                .add(LootContextParameters.TOOL, rod)
                .luck(luck)
                .build(LootContextTypes.FISHING);
        return world.getServer().getReloadableRegistries().getLootTable(table).generateLoot(params);
    }

    private static FishSpecies pickSpecies(BiomeGroup group, int rarityLuck, Random random) {
        List<FishSpecies> list = FishRegistry.forGroup(group);
        double luckFactor = 1.0 + 0.12 * rarityLuck;

        double total = 0.0;
        double[] weights = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            FishSpecies s = list.get(i);
            // Higher tiers benefit more from luck: weight * luckFactor^tier
            weights[i] = s.rarity().weight * Math.pow(luckFactor, s.rarity().ordinal());
            total += weights[i];
        }

        double r = random.nextDouble() * total;
        for (int i = 0; i < weights.length; i++) {
            r -= weights[i];
            if (r <= 0) return list.get(i);
        }
        return list.get(list.size() - 1);
    }

    private static BaitItem consumeBait(PlayerEntity player) {
        ItemStack off = player.getOffHandStack();
        if (off.getItem() instanceof BaitItem b) {
            if (!player.getAbilities().creativeMode) off.decrement(1);
            return b;
        }
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (s.getItem() instanceof BaitItem b) {
                if (!player.getAbilities().creativeMode) s.decrement(1);
                return b;
            }
        }
        return null;
    }
}
