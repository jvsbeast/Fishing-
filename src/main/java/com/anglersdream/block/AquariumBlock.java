package com.anglersdream.block;

import com.anglersdream.AnglersDream;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A glass tank block. Face-adjacent aquarium blocks form one connected tank
 * (see {@link AquariumGroup}); right-clicking any member opens the shared
 * management screen (opened client-side in AnglersDreamClient). FILLED is the
 * per-block water state — Fill/Drain in the screen toggles the whole group.
 */
public class AquariumBlock extends Block implements BlockEntityProvider {

    public static final MapCodec<AquariumBlock> CODEC = createCodec(AquariumBlock::new);
    public static final BooleanProperty FILLED = BooleanProperty.of("filled");

    public AquariumBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FILLED, false));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FILLED);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // The client-side UseBlockCallback opens the management screen; consuming the
        // interaction here stops buckets and blocks in hand from firing against the tank.
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof AquariumBlockEntity be) {
                ItemScatterer.spawn(world, pos, be.getFish());
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AquariumBlockEntity(pos, state);
    }

    public static boolean isFilled(BlockState state) {
        return state.isOf(AnglersDream.AQUARIUM) && state.get(FILLED);
    }
}
