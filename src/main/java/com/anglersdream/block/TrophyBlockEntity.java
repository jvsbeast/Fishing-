package com.anglersdream.block;

import com.anglersdream.AnglersDream;
import com.anglersdream.fish.Variant;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TrophyBlockEntity extends BlockEntity {

    private ItemStack fish = ItemStack.EMPTY;

    public TrophyBlockEntity(BlockPos pos, BlockState state) {
        super(AnglersDream.TROPHY_BLOCK_ENTITY, pos, state);
    }

    public ItemStack getFish() {
        return fish;
    }

    public void setFish(ItemStack stack) {
        this.fish = stack;
        markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        fish = nbt.contains("Fish")
                ? ItemStack.fromNbt(registryLookup, nbt.getCompound("Fish")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        // Always write a marker: the client discards empty block entity update packets,
        // so an NBT that only ever contains "Fish" would never sync the removal.
        nbt.putBoolean("HasFish", !fish.isEmpty());
        if (!fish.isEmpty()) {
            nbt.put("Fish", fish.encode(registryLookup));
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    /** Client-side: ambient particles for special fish variants. */
    public static void clientTick(World world, BlockPos pos, BlockState state, TrophyBlockEntity be) {
        ItemStack fish = be.getFish();
        if (fish.isEmpty()) return;

        Variant v = Variant.fromStack(fish);
        if (v == Variant.NORMAL || v.particle == null) return;

        Random r = world.getRandom();
        if (r.nextInt(4) != 0) return;

        Direction f = state.get(TrophyBlock.FACING);
        double x = pos.getX() + 0.5 - f.getOffsetX() * 0.30 + (r.nextDouble() - 0.5) * 0.7;
        double y = pos.getY() + 0.2 + r.nextDouble() * 0.7;
        double z = pos.getZ() + 0.5 - f.getOffsetZ() * 0.30 + (r.nextDouble() - 0.5) * 0.7;
        world.addParticle(v.particle, x, y, z, 0.0, 0.02, 0.0);
    }
}
