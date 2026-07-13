package com.anglersdream.block;

import com.anglersdream.AnglersDream;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Per-block fish storage for an aquarium. Each block holds up to
 * {@link AquariumGroup#FISH_PER_BLOCK} fish stacks plus a "displayed" flag per
 * slot; the renderer animates the displayed ones swimming through the whole
 * connected tank. Client-side animation state lives here transiently so it is
 * garbage-collected with the block entity.
 */
public class AquariumBlockEntity extends BlockEntity {

    private final DefaultedList<ItemStack> fish =
            DefaultedList.ofSize(AquariumGroup.FISH_PER_BLOCK, ItemStack.EMPTY);
    private final boolean[] displayed = new boolean[AquariumGroup.FISH_PER_BLOCK];

    /** Client-only swim animation state, one entry per slot; managed by the renderer. */
    public transient Object[] clientAnim = new Object[AquariumGroup.FISH_PER_BLOCK];

    /** Client-only cached group membership; managed by the renderer. */
    public transient Object clientGroupCache;

    public AquariumBlockEntity(BlockPos pos, BlockState state) {
        super(AnglersDream.AQUARIUM_BLOCK_ENTITY, pos, state);
    }

    public DefaultedList<ItemStack> getFish() {
        return fish;
    }

    public ItemStack getFish(int slot) {
        return fish.get(slot);
    }

    public boolean isDisplayed(int slot) {
        return displayed[slot];
    }

    public void setFish(int slot, ItemStack stack, boolean display) {
        fish.set(slot, stack);
        displayed[slot] = display && !stack.isEmpty();
        sync();
    }

    public void toggleDisplayed(int slot) {
        if (!fish.get(slot).isEmpty()) {
            displayed[slot] = !displayed[slot];
            sync();
        }
    }

    public int firstFreeSlot() {
        for (int i = 0; i < fish.size(); i++) {
            if (fish.get(i).isEmpty()) return i;
        }
        return -1;
    }

    private void sync() {
        markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        fish.clear();
        Inventories.readNbt(nbt, fish, registryLookup);
        int mask = nbt.getInt("Displayed");
        for (int i = 0; i < displayed.length; i++) {
            displayed[i] = (mask & (1 << i)) != 0 && !fish.get(i).isEmpty();
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, fish, registryLookup);
        int mask = 0;
        for (int i = 0; i < displayed.length; i++) {
            if (displayed[i]) mask |= 1 << i;
        }
        // Always written: the client drops empty update packets, so removals
        // would never sync if the NBT could end up empty (same as TrophyBlockEntity).
        nbt.putInt("Displayed", mask);
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
}
