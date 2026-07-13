package com.anglersdream.network;

import com.anglersdream.AnglersDream;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

/**
 * Client -> server: an action taken in the aquarium management screen.
 *
 * @param clicked the aquarium block whose screen is open (group anchor)
 * @param action  DEPOSIT / WITHDRAW / TOGGLE_DISPLAY / FILL / DRAIN
 * @param target  member block the action applies to (DEPOSIT/FILL/DRAIN ignore it)
 * @param slot    aquarium slot for WITHDRAW/TOGGLE_DISPLAY, player inventory
 *                index for DEPOSIT, unused otherwise
 */
public record AquariumActionPayload(BlockPos clicked, int action, BlockPos target, int slot)
        implements CustomPayload {

    public static final int DEPOSIT = 0;
    public static final int WITHDRAW = 1;
    public static final int TOGGLE_DISPLAY = 2;
    public static final int FILL = 3;
    public static final int DRAIN = 4;

    public static final CustomPayload.Id<AquariumActionPayload> ID =
            new CustomPayload.Id<>(AnglersDream.id("aquarium_action"));

    public static final PacketCodec<RegistryByteBuf, AquariumActionPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, AquariumActionPayload::clicked,
            PacketCodecs.VAR_INT, AquariumActionPayload::action,
            BlockPos.PACKET_CODEC, AquariumActionPayload::target,
            PacketCodecs.VAR_INT, AquariumActionPayload::slot,
            AquariumActionPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
