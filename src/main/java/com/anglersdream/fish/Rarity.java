package com.anglersdream.fish;

import net.minecraft.util.Formatting;

public enum Rarity {
    COMMON(90.0, Formatting.WHITE),
    UNCOMMON(38.0, Formatting.GREEN),
    RARE(12.0, Formatting.AQUA),
    EPIC(3.2, Formatting.LIGHT_PURPLE),
    LEGENDARY(0.8, Formatting.GOLD);

    public final double weight;
    public final Formatting color;

    Rarity(double weight, Formatting color) {
        this.weight = weight;
        this.color = color;
    }

    public String translationKey() {
        return "rarity.anglersdream." + name().toLowerCase();
    }
}
