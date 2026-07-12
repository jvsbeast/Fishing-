package com.anglersdream.item;

import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class TieredRodItem extends FishingRodItem {

    public final int rarityLuck;
    public final double sizeBonus;
    public final double variantMultiplier;

    public TieredRodItem(int rarityLuck, double sizeBonus, double variantMultiplier, Settings settings) {
        super(settings);
        this.rarityLuck = rarityLuck;
        this.sizeBonus = sizeBonus;
        this.variantMultiplier = variantMultiplier;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.anglersdream.luck", rarityLuck).formatted(Formatting.AQUA));
        tooltip.add(Text.translatable("tooltip.anglersdream.size_bonus",
                Math.round(sizeBonus * 100)).formatted(Formatting.GREEN));
        if (variantMultiplier > 1.0) {
            tooltip.add(Text.translatable("tooltip.anglersdream.variant_bonus",
                    String.format(java.util.Locale.ROOT, "%.1f", variantMultiplier)).formatted(Formatting.LIGHT_PURPLE));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }
}
