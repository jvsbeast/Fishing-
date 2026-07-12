package com.anglersdream.network;

import com.anglersdream.AnglersDream;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * Server -> client: a fish is on the line, open the reel-in minigame.
 *
 * @param difficulty 10..95, drives fish speed and progress drain
 * @param behavior   0 mixer, 1 smooth, 2 dart, 3 sinker, 4 floater
 * @param rodTier    0 vanilla .. 3 Poseidon's — larger catch bar for better rods
 * @param treasure   whether a treasure chest will appear mid-game
 */
public record StartMinigamePayload(int difficulty, int behavior, int rodTier, boolean treasure)
        implements CustomPayload {

    public static final CustomPayload.Id<StartMinigamePayload> ID =
            new CustomPayload.Id<>(AnglersDream.id("start_minigame"));

    public static final PacketCodec<ByteBuf, StartMinigamePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, StartMinigamePayload::difficulty,
            PacketCodecs.VAR_INT, StartMinigamePayload::behavior,
            PacketCodecs.VAR_INT, StartMinigamePayload::rodTier,
            PacketCodecs.BOOL, StartMinigamePayload::treasure,
            StartMinigamePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
