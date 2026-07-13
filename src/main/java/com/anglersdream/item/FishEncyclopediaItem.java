package com.anglersdream.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * A personal fish-catch logbook. Its {@link com.anglersdream.fish.EncyclopediaLog}
 * data component is filled in as species are caught; opening the screen is handled
 * entirely client-side (see AnglersDreamClient) since the component already synced
 * with the stack like any other item data.
 */
public class FishEncyclopediaItem extends Item {

    public FishEncyclopediaItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.anglersdream.encyclopedia_hint")
                .formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
