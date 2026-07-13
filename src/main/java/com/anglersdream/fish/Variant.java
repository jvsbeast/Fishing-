package com.anglersdream.fish;

import com.anglersdream.AnglersDream;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

public enum Variant implements StringIdentifiable {
    NORMAL("normal", 0.0, null),
    SHINY("shiny", 0.012, null),               // enchant glint only, no particles
    GOLDEN("golden", 0.003, ParticleTypes.WAX_ON),
    PRISMATIC("prismatic", 0.0006, ParticleTypes.NAUTILUS);

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

    /**
     * The ambient particle for this variant, or null for NORMAL. Prismatic fish emit
     * rainbow dust: every particle rolls a random hue, so together they shimmer
     * through the whole spectrum.
     */
    public ParticleEffect createParticle(Random random) {
        if (this == PRISMATIC) {
            int rgb = MathHelper.hsvToRgb(random.nextFloat(), 0.85f, 1.0f);
            return new DustParticleEffect(new Vector3f(
                    ((rgb >> 16) & 0xFF) / 255.0f,
                    ((rgb >> 8) & 0xFF) / 255.0f,
                    (rgb & 0xFF) / 255.0f), 1.0f);
        }
        return particle;
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

    /**
     * Rolls a variant. {@code multiplier} scales the chance of every special variant,
     * but with square-root diminishing returns so even fully stacked gear (Celestial
     * Rod x Prismatic Lure x Luck of the Sea III, ~25x) only multiplies odds ~5x —
     * a prismatic catch stays rare no matter the setup (~1 in 330 at best).
     */
    public static Variant roll(Random random, double multiplier) {
        double m = Math.sqrt(Math.max(1.0, multiplier));
        double r = random.nextDouble();
        double p = PRISMATIC.baseChance * m;
        if (r < p) return PRISMATIC;
        double g = p + GOLDEN.baseChance * m;
        if (r < g) return GOLDEN;
        double s = g + SHINY.baseChance * m;
        if (r < s) return SHINY;
        return NORMAL;
    }
}
