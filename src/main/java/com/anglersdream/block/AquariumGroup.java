package com.anglersdream.block;

import com.anglersdream.AnglersDream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A connected group of aquarium blocks acts as one tank. Membership is computed
 * on demand by flood fill over face-adjacent aquarium blocks (capped so a giant
 * glass wall can't freeze anything). Works identically on server and client
 * world views, so both sides agree on the group without extra networking.
 */
public final class AquariumGroup {

    public static final int MAX_BLOCKS = 128;
    public static final int FISH_PER_BLOCK = 4;

    private AquariumGroup() {}

    /** BFS from start over face-adjacent aquarium blocks, in deterministic order. */
    public static List<BlockPos> collect(BlockView world, BlockPos start) {
        List<BlockPos> members = new ArrayList<>();
        if (!world.getBlockState(start).isOf(AnglersDream.AQUARIUM)) return members;

        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start.toImmutable());
        seen.add(start.toImmutable());

        while (!queue.isEmpty() && members.size() < MAX_BLOCKS) {
            BlockPos pos = queue.poll();
            members.add(pos);
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.offset(dir);
                if (seen.add(next) && world.getBlockState(next).isOf(AnglersDream.AQUARIUM)) {
                    queue.add(next);
                }
            }
        }
        return members;
    }
}
