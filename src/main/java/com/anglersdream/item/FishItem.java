package com.anglersdream.item;

import com.anglersdream.AnglersDream;
import com.anglersdream.fish.FishData;
import com.anglersdream.fish.FishSpecies;
import com.anglersdream.fish.Variant;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Locale;

public class FishItem extends Item {

    public final FishSpecies species;

    public FishItem(FishSpecies species, Settings settings) {
        super(settings);
        this.species = species;
    }

    public static float getSizeCm(ItemStack stack) {
        FishData data = stack.get(AnglersDream.FISH_DATA);
        return data == null ? -1.0f : data.sizeCm();
    }

    public static float getWeightKg(ItemStack stack) {
        FishData data = stack.get(AnglersDream.FISH_DATA);
        return data == null ? -1.0f : data.weightKg();
    }

    @Override
    public Text getName(ItemStack stack) {
        Variant v = Variant.fromStack(stack);
        MutableText name = Text.translatable(this.getTranslationKey(stack));
        if (v != Variant.NORMAL) {
            name = Text.translatable(v.translationKey()).append(" ").append(name);
        }
        return name.formatted(species.rarity().color);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return Variant.fromStack(stack) != Variant.NORMAL || super.hasGlint(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(species.rarity().translationKey()).formatted(species.rarity().color));

        Variant v = Variant.fromStack(stack);
        if (v != Variant.NORMAL) {
            Formatting color = switch (v) {
                case SHINY -> Formatting.AQUA;
                case GOLDEN -> Formatting.GOLD;
                case PRISMATIC -> Formatting.LIGHT_PURPLE;
                default -> Formatting.GRAY;
            };
            tooltip.add(Text.literal("\u2726 ").append(Text.translatable(v.translationKey())).formatted(color));
        }

        float size = getSizeCm(stack);
        float weight = getWeightKg(stack);
        if (size > 0) {
            tooltip.add(Text.translatable("tooltip.anglersdream.length",
                    String.format(Locale.ROOT, "%.1f", size)).formatted(Formatting.GRAY));
        }
        if (weight > 0) {
            tooltip.add(Text.translatable("tooltip.anglersdream.weight",
                    String.format(Locale.ROOT, "%.2f", weight)).formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }
}
