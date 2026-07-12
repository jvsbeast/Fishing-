package com.anglersdream.fish;

import com.anglersdream.AnglersDream;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.random.Random;

public enum Variant implements StringIdentifiable {
    NORMAL("normal", 0.0, null),
    SHINY("shiny", 0.050, ParticleTypes.GLOW),
    GOLDEN("golden", 0.015, ParticleTypes.WAX_ON),
    PRISMATIC("prismatic", 0.004, ParticleTypes.NAUTILUS);

    public static final Codec<Variant> CODEC = StringIdentifiable.createCodec(Variant::values);
    public static final PacketCodec<ByteBuf, Variant> PACKET_CODEC =
            PacketCodecs.STRING.xmap(Variant::byName, v -> v.id);

    public final String id;
    public final double baseChance;
    public final SimpleParticleType particle;

    Variant(String id, double baseChance, SimpleParticleType particle) {
        this.id = id;
        this.baseChance = baseChance;
        this.particle = particle;
    }

    @Override
    public String asString() {
        return id;
    }

    public String translationKey() {
        return "variant.anglersdream." + id;
    }

    public static Variant byName(String name) {
        for (Variant v : values()) {
            if (v.id.equals(name)) return v;
        }
        return NORMAL;
    }

    public static Variant fromStack(ItemStack stack) {
        FishData data = stack.get(AnglersDream.FISH_DATA);
        return data == null ? NORMAL : data.variant();
    }

    /** Rolls a variant. {@code multiplier} scales the chance of every special variant. */
    public static Variant roll(Random random, double multiplier) {
        double r = random.nextDouble();
        double p = PRISMATIC.baseChance * multiplier;
        if (r < p) return PRISMATIC;
        double g = p + GOLDEN.baseChance * multiplier;
        if (r < g) return GOLDEN;
        double s = g + SHINY.baseChance * multiplier;
        if (r < s) return SHINY;
        return NORMAL;
    }
}
