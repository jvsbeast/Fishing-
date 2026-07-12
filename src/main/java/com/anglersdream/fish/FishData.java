package com.anglersdream.fish;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/**
 * Per-catch data attached to fish item stacks via a custom data component
 * (1.20.5+ replaced item NBT with components).
 */
public record FishData(float sizeCm, float weightKg, Variant variant) {

    public static final Codec<FishData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("size_cm").forGetter(FishData::sizeCm),
            Codec.FLOAT.fieldOf("weight_kg").forGetter(FishData::weightKg),
            Variant.CODEC.optionalFieldOf("variant", Variant.NORMAL).forGetter(FishData::variant)
    ).apply(instance, FishData::new));

    public static final PacketCodec<ByteBuf, FishData> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, FishData::sizeCm,
            PacketCodecs.FLOAT, FishData::weightKg,
            Variant.PACKET_CODEC, FishData::variant,
            FishData::new);
}
