package com.anglersdream.block;

import com.anglersdream.AnglersDream;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

/**
 * A glass tank block. Face-adjacent aquarium blocks form one connected tank
 * (see {@link AquariumGroup}); right-clicking any member opens the shared
 * management screen (opened client-side in AnglersDreamClient).
 *
 * Six connection properties track which neighbors are also aquariums; the
 * multipart blockstate uses them to draw each of the block's twelve frame
 * edges only where the tank does not continue, so borders merge into one
 * outline like connected-texture glass. Faces between two aquariums are
 * culled entirely via isSideInvisible, exactly like vanilla glass.
 */
public class AquariumBlock extends Block implements BlockEntityProvider {

    public static final MapCodec<AquariumBlock> CODEC = createCodec(AquariumBlock::new);
    public static final BooleanProperty FILLED = BooleanProperty.of("filled");

    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;

    public AquariumBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FILLED, false)
                .with(NORTH, false).with(SOUTH, false)
                .with(EAST, false).with(WEST, false)
                .with(UP, false).with(DOWN, false));
    }

    private static BooleanProperty connection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FILLED, NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = getDefaultState();
        for (Direction dir : Direction.values()) {
            state = state.with(connection(dir),
                    ctx.getWorld().getBlockState(ctx.getBlockPos().offset(dir)).isOf(this));
        }
        return state;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                   WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return state.with(connection(direction), neighborState.isOf(this));
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        // No pane of glass between two joined tank blocks, like vanilla glass.
        return stateFrom.isOf(this) || super.isSideInvisible(state, stateFrom, direction);
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
