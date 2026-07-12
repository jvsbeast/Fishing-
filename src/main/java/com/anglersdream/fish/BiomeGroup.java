package com.anglersdream.fish;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public enum BiomeGroup {
    OCEAN,
    WARM_OCEAN,
    FROZEN,
    RIVER,
    SWAMP,
    JUNGLE,
    DESERT,
    MOUNTAIN,
    MUSHROOM,
    CAVES;

    public static BiomeGroup of(World world, BlockPos pos) {
        RegistryEntry<Biome> b = world.getBiome(pos);

        if (b.matchesKey(BiomeKeys.WARM_OCEAN)
                || b.matchesKey(BiomeKeys.LUKEWARM_OCEAN)
                || b.matchesKey(BiomeKeys.DEEP_LUKEWARM_OCEAN)) return WARM_OCEAN;

        if (b.matchesKey(BiomeKeys.FROZEN_OCEAN)
                || b.matchesKey(BiomeKeys.DEEP_FROZEN_OCEAN)
                || b.matchesKey(BiomeKeys.COLD_OCEAN)
                || b.matchesKey(BiomeKeys.DEEP_COLD_OCEAN)
                || b.matchesKey(BiomeKeys.FROZEN_RIVER)
                || b.matchesKey(BiomeKeys.SNOWY_PLAINS)
                || b.matchesKey(BiomeKeys.SNOWY_TAIGA)
                || b.matchesKey(BiomeKeys.SNOWY_BEACH)
                || b.matchesKey(BiomeKeys.ICE_SPIKES)
                || b.matchesKey(BiomeKeys.FROZEN_PEAKS)
                || b.matchesKey(BiomeKeys.SNOWY_SLOPES)) return FROZEN;

        if (b.isIn(BiomeTags.IS_OCEAN)
                || b.isIn(BiomeTags.IS_DEEP_OCEAN)
                || b.isIn(BiomeTags.IS_BEACH)) return OCEAN;

        if (b.matchesKey(BiomeKeys.SWAMP)
                || b.matchesKey(BiomeKeys.MANGROVE_SWAMP)) return SWAMP;

        if (b.isIn(BiomeTags.IS_JUNGLE)) return JUNGLE;

        if (b.matchesKey(BiomeKeys.DESERT)
                || b.isIn(BiomeTags.IS_BADLANDS)) return DESERT;

        if (b.matchesKey(BiomeKeys.MUSHROOM_FIELDS)) return MUSHROOM;

        if (b.matchesKey(BiomeKeys.LUSH_CAVES)
                || b.matchesKey(BiomeKeys.DRIPSTONE_CAVES)
                || b.matchesKey(BiomeKeys.DEEP_DARK)) return CAVES;

        if (b.isIn(BiomeTags.IS_MOUNTAIN)
                || b.isIn(BiomeTags.IS_HILL)) return MOUNTAIN;

        return RIVER;
    }
}
