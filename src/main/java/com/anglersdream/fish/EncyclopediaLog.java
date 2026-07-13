package com.anglersdream.fish;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What a single Fish Encyclopedia book has recorded: every species it has seen,
 * and a running total catch count per biome group. Lives as a data component on
 * the encyclopedia item stack itself (like {@link FishData} on a fish), so it
 * persists and syncs to the owning client with no custom networking.
 */
public record EncyclopediaLog(Set<String> caught, Map<String, Integer> groupCatches) {

    public static final EncyclopediaLog EMPTY = new EncyclopediaLog(Set.of(), Map.of());

    public static final Codec<EncyclopediaLog> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().xmap(HashSet::new, ArrayList::new)
                    .optionalFieldOf("caught", Set.of()).forGetter(EncyclopediaLog::caught),
            Codec.unboundedMap(Codec.STRING, Codec.INT)
                    .optionalFieldOf("group_catches", Map.of()).forGetter(EncyclopediaLog::groupCatches)
    ).apply(instance, EncyclopediaLog::new));

    public static final PacketCodec<ByteBuf, EncyclopediaLog> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.collection(HashSet::new, PacketCodecs.STRING), EncyclopediaLog::caught,
            PacketCodecs.map(HashMap::new, PacketCodecs.STRING, PacketCodecs.VAR_INT), EncyclopediaLog::groupCatches,
            EncyclopediaLog::new);

    public EncyclopediaLog withCatch(FishSpecies species) {
        Set<String> newCaught = new HashSet<>(caught);
        newCaught.add(species.id());
        Map<String, Integer> newCounts = new HashMap<>(groupCatches);
        newCounts.merge(species.group().name(), 1, Integer::sum);
        return new EncyclopediaLog(Set.copyOf(newCaught), Map.copyOf(newCounts));
    }

    public boolean hasCaught(FishSpecies species) {
        return caught.contains(species.id());
    }

    public int totalInGroup(BiomeGroup group) {
        return groupCatches.getOrDefault(group.name(), 0);
    }

    public int discoveredInGroup(BiomeGroup group) {
        List<FishSpecies> all = FishRegistry.forGroup(group);
        int n = 0;
        for (FishSpecies s : all) {
            if (caught.contains(s.id())) n++;
        }
        return n;
    }

    public boolean isGroupComplete(BiomeGroup group) {
        List<FishSpecies> all = FishRegistry.forGroup(group);
        return !all.isEmpty() && discoveredInGroup(group) == all.size();
    }
}
