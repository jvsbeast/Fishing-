package com.anglersdream.block;

import com.anglersdream.AnglersDream;
import com.anglersdream.item.FishItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

/**
 * A wall-mounted plaque. Right-click with a fish to mount it (rendered at true size),
 * right-click again to take it back.
 */
public class TrophyBlock extends HorizontalFacingBlock implements BlockEntityProvider {

    public static final MapCodec<TrophyBlock> CODEC = createCodec(TrophyBlock::new);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(1, 1, 14, 15, 15, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(1, 1, 0, 15, 15, 2);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0, 1, 1, 2, 15, 15);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(14, 1, 1, 16, 15, 15);

    public TrophyBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction side = ctx.getSide();
        BlockState state;
        if (side.getAxis().isHorizontal()) {
            state = getDefaultState().with(FACING, side);
        } else {
            state = getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
        }
        return state.canPlaceAt(ctx.getWorld(), ctx.getBlockPos()) ? state : null;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos wallPos = pos.offset(facing.getOpposite());
        return world.getBlockState(wallPos).isSideSolidFullSquare(world, wallPos, facing);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                   WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.get(FACING).getOpposite() && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    /** 1.20.5+ split block interaction: item-in-hand path (mounting a fish). */
    @Override
    protected ItemActionResult onUseWithItem(ItemStack held, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TrophyBlockEntity be)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (be.getFish().isEmpty() && held.getItem() instanceof FishItem) {
            if (!world.isClient) {
                ItemStack copy = held.copyWithCount(1);
                be.setFish(copy);
                if (!player.getAbilities().creativeMode) held.decrement(1);
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ItemActionResult.success(world.isClient);
        }
        // Fall through to onUse so a filled plaque can be emptied even with an item in hand.
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Empty-hand / default path (retrieving the mounted fish). */
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TrophyBlockEntity be)) return ActionResult.PASS;

        if (!be.getFish().isEmpty()) {
            if (!world.isClient) {
                ItemStack fish = be.getFish();
                be.setFish(ItemStack.EMPTY);
                if (!player.getInventory().insertStack(fish)) {
                    player.dropItem(fish, false);
                }
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof TrophyBlockEntity be && !be.getFish().isEmpty()) {
                ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, be.getFish());
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TrophyBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient || type != AnglersDream.TROPHY_BLOCK_ENTITY) return null;
        return (w, p, s, be) -> TrophyBlockEntity.clientTick(w, p, s, (TrophyBlockEntity) be);
    }
}
