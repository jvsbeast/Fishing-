package com.anglersdream.block;

import com.anglersdream.AnglersDream;
import com.anglersdream.network.AquariumActionPayload;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Server-side execution of aquarium screen actions. Every action re-validates
 * against the live world (distance, block type, stack contents) — the client
 * request is a suggestion, never trusted.
 */
public final class AquariumActions {

    /** Any fish counts for storage and crafting: all mod species plus vanilla fish. */
    public static final TagKey<net.minecraft.item.Item> FISHES =
            TagKey.of(RegistryKeys.ITEM, AnglersDream.id("fishes"));

    private AquariumActions() {}

    public static void handle(ServerPlayerEntity player, AquariumActionPayload payload) {
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos clicked = payload.clicked();

        if (!player.getBlockPos().isWithinDistance(clicked, 10.0)) return;
        if (!world.getBlockState(clicked).isOf(AnglersDream.AQUARIUM)) return;

        switch (payload.action()) {
            case AquariumActionPayload.DEPOSIT -> deposit(player, world, clicked, payload.slot());
            case AquariumActionPayload.WITHDRAW -> withdraw(player, world, clicked, payload.target(), payload.slot());
            case AquariumActionPayload.TOGGLE_DISPLAY -> toggleDisplay(world, clicked, payload.target(), payload.slot());
            case AquariumActionPayload.FILL -> setWater(player, world, clicked, true);
            case AquariumActionPayload.DRAIN -> setWater(player, world, clicked, false);
            default -> { }
        }
    }

    private static void deposit(ServerPlayerEntity player, ServerWorld world, BlockPos clicked, int invIndex) {
        if (invIndex < 0 || invIndex >= player.getInventory().size()) return;
        ItemStack stack = player.getInventory().getStack(invIndex);
        if (stack.isEmpty() || !stack.isIn(FISHES)) return;

        for (BlockPos member : AquariumGroup.collect(world, clicked)) {
            if (world.getBlockEntity(member) instanceof AquariumBlockEntity be) {
                int slot = be.firstFreeSlot();
                if (slot >= 0) {
                    be.setFish(slot, stack.copyWithCount(1), true);
                    stack.decrement(1);
                    world.playSound(null, member, SoundEvents.ENTITY_FISHING_BOBBER_SPLASH,
                            SoundCategory.BLOCKS, 0.5f, 1.3f);
                    return;
                }
            }
        }
    }

    private static void withdraw(ServerPlayerEntity player, ServerWorld world, BlockPos clicked,
                                 BlockPos target, int slot) {
        if (!isGroupMember(world, clicked, target)) return;
        if (!(world.getBlockEntity(target) instanceof AquariumBlockEntity be)) return;
        if (slot < 0 || slot >= AquariumGroup.FISH_PER_BLOCK) return;

        ItemStack stack = be.getFish(slot);
        if (stack.isEmpty()) return;

        be.setFish(slot, ItemStack.EMPTY, false);
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }

    private static void toggleDisplay(ServerWorld world, BlockPos clicked, BlockPos target, int slot) {
        if (!isGroupMember(world, clicked, target)) return;
        if (world.getBlockEntity(target) instanceof AquariumBlockEntity be
                && slot >= 0 && slot < AquariumGroup.FISH_PER_BLOCK) {
            be.toggleDisplayed(slot);
        }
    }

    private static void setWater(ServerPlayerEntity player, ServerWorld world, BlockPos clicked, boolean fill) {
        // One bucket handles the whole connected tank; the empty/full bucket is returned.
        if (!player.isCreative()) {
            var needed = fill ? Items.WATER_BUCKET : Items.BUCKET;
            var returned = fill ? Items.BUCKET : Items.WATER_BUCKET;
            int found = -1;
            for (int i = 0; i < player.getInventory().size(); i++) {
                if (player.getInventory().getStack(i).isOf(needed)) {
                    found = i;
                    break;
                }
            }
            if (found < 0) return;
            player.getInventory().getStack(found).decrement(1);
            ItemStack back = new ItemStack(returned);
            if (!player.getInventory().insertStack(back)) {
                player.dropItem(back, false);
            }
        }

        List<BlockPos> members = AquariumGroup.collect(world, clicked);
        for (BlockPos member : members) {
            BlockState state = world.getBlockState(member);
            if (state.get(AquariumBlock.FILLED) != fill) {
                world.setBlockState(member, state.with(AquariumBlock.FILLED, fill));
            }
        }
        world.playSound(null, clicked,
                fill ? SoundEvents.ITEM_BUCKET_EMPTY : SoundEvents.ITEM_BUCKET_FILL,
                SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    private static boolean isGroupMember(ServerWorld world, BlockPos clicked, BlockPos target) {
        return AquariumGroup.collect(world, clicked).contains(target);
    }
}
