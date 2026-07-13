package com.anglersdream.fish;

import com.anglersdream.AnglersDream;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Updates every Fish Encyclopedia the player is carrying when they land a catch.
 * The book must be somewhere in the player's inventory (main, hotbar, or offhand)
 * at the moment of the catch to record it.
 */
public final class EncyclopediaLogHelper {

    private EncyclopediaLogHelper() {}

    public static void recordCatch(ServerPlayerEntity player, FishSpecies species) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() != AnglersDream.FISH_ENCYCLOPEDIA) continue;

            EncyclopediaLog current = stack.get(AnglersDream.ENCYCLOPEDIA_LOG);
            if (current == null) current = EncyclopediaLog.EMPTY;
            stack.set(AnglersDream.ENCYCLOPEDIA_LOG, current.withCatch(species));
        }
    }
}
