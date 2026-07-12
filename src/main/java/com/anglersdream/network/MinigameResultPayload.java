package com.anglersdream.network;

import com.anglersdream.AnglersDream;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Client -> server: outcome of the reel-in minigame. */
public record MinigameResultPayload(boolean success, boolean treasureCollected, boolean perfect)
        implements CustomPayload {

    public static final CustomPayload.Id<MinigameResultPayload> ID =
            new CustomPayload.Id<>(AnglersDream.id("minigame_result"));

    public static final PacketCodec<ByteBuf, MinigameResultPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, MinigameResultPayload::success,
            PacketCodecs.BOOL, MinigameResultPayload::treasureCollected,
            PacketCodecs.BOOL, MinigameResultPayload::perfect,
            MinigameResultPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
