package com.anglersdream.fish;

/**
 * Definition of a fish species. Sizes are in centimetres, weights in kilograms.
 */
public record FishSpecies(
        String id,
        BiomeGroup group,
        Rarity rarity,
        float minSize,
        float maxSize,
        float minWeight,
        float maxWeight
) {}
