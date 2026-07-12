package com.anglersdream.fish;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** All fish species in the mod. Generated data - tweak freely. */
public final class FishRegistry {

    public static final List<FishSpecies> ALL = new ArrayList<>();
    private static final Map<BiomeGroup, List<FishSpecies>> BY_GROUP = new EnumMap<>(BiomeGroup.class);

    static {
        add(new FishSpecies("herring", BiomeGroup.OCEAN, Rarity.COMMON, 15f, 30f, 0.08f, 0.4f));
        add(new FishSpecies("mackerel", BiomeGroup.OCEAN, Rarity.COMMON, 25f, 45f, 0.3f, 1.4f));
        add(new FishSpecies("sea_bass", BiomeGroup.OCEAN, Rarity.UNCOMMON, 35f, 80f, 1.0f, 7.0f));
        add(new FishSpecies("bluefin_tuna", BiomeGroup.OCEAN, Rarity.RARE, 120f, 300f, 60f, 380f));
        add(new FishSpecies("swordfish", BiomeGroup.OCEAN, Rarity.EPIC, 170f, 340f, 80f, 500f));
        add(new FishSpecies("leviathan_ray", BiomeGroup.OCEAN, Rarity.LEGENDARY, 280f, 560f, 350f, 1300f));
        add(new FishSpecies("parrotfish", BiomeGroup.WARM_OCEAN, Rarity.COMMON, 20f, 45f, 0.5f, 2.0f));
        add(new FishSpecies("butterflyfish", BiomeGroup.WARM_OCEAN, Rarity.COMMON, 10f, 20f, 0.1f, 0.4f));
        add(new FishSpecies("lionfish", BiomeGroup.WARM_OCEAN, Rarity.UNCOMMON, 25f, 40f, 0.7f, 1.6f));
        add(new FishSpecies("mahi_mahi", BiomeGroup.WARM_OCEAN, Rarity.RARE, 80f, 160f, 8f, 30f));
        add(new FishSpecies("sailfish", BiomeGroup.WARM_OCEAN, Rarity.EPIC, 180f, 320f, 50f, 95f));
        add(new FishSpecies("sunken_emperor", BiomeGroup.WARM_OCEAN, Rarity.LEGENDARY, 200f, 420f, 150f, 600f));
        add(new FishSpecies("arctic_cod", BiomeGroup.FROZEN, Rarity.COMMON, 20f, 40f, 0.4f, 2.0f));
        add(new FishSpecies("icefin", BiomeGroup.FROZEN, Rarity.COMMON, 15f, 35f, 0.3f, 1.2f));
        add(new FishSpecies("arctic_char", BiomeGroup.FROZEN, Rarity.UNCOMMON, 35f, 75f, 1.0f, 9.0f));
        add(new FishSpecies("greenland_halibut", BiomeGroup.FROZEN, Rarity.RARE, 60f, 130f, 5f, 45f));
        add(new FishSpecies("frostjaw_pike", BiomeGroup.FROZEN, Rarity.EPIC, 90f, 180f, 10f, 35f));
        add(new FishSpecies("glacier_wraithfin", BiomeGroup.FROZEN, Rarity.LEGENDARY, 180f, 380f, 90f, 450f));
        add(new FishSpecies("minnow", BiomeGroup.RIVER, Rarity.COMMON, 5f, 10f, 0.01f, 0.05f));
        add(new FishSpecies("river_perch", BiomeGroup.RIVER, Rarity.COMMON, 15f, 35f, 0.2f, 1.5f));
        add(new FishSpecies("brown_trout", BiomeGroup.RIVER, Rarity.UNCOMMON, 30f, 70f, 0.8f, 6.0f));
        add(new FishSpecies("zander", BiomeGroup.RIVER, Rarity.RARE, 50f, 110f, 3f, 15f));
        add(new FishSpecies("golden_sturgeon", BiomeGroup.RIVER, Rarity.EPIC, 120f, 280f, 30f, 200f));
        add(new FishSpecies("river_king_salmon", BiomeGroup.RIVER, Rarity.LEGENDARY, 110f, 200f, 25f, 90f));
        add(new FishSpecies("mudskipper", BiomeGroup.SWAMP, Rarity.COMMON, 10f, 25f, 0.1f, 0.4f));
        add(new FishSpecies("bullhead_catfish", BiomeGroup.SWAMP, Rarity.COMMON, 20f, 45f, 0.5f, 2.5f));
        add(new FishSpecies("snakehead", BiomeGroup.SWAMP, Rarity.UNCOMMON, 40f, 90f, 1.5f, 8.0f));
        add(new FishSpecies("alligator_gar", BiomeGroup.SWAMP, Rarity.RARE, 120f, 260f, 40f, 140f));
        add(new FishSpecies("bogmaw", BiomeGroup.SWAMP, Rarity.EPIC, 140f, 260f, 60f, 180f));
        add(new FishSpecies("elder_lungfish", BiomeGroup.SWAMP, Rarity.LEGENDARY, 150f, 300f, 50f, 250f));
        add(new FishSpecies("neon_tetra", BiomeGroup.JUNGLE, Rarity.COMMON, 3f, 5f, 0.005f, 0.02f));
        add(new FishSpecies("emerald_cichlid", BiomeGroup.JUNGLE, Rarity.COMMON, 10f, 25f, 0.1f, 0.8f));
        add(new FishSpecies("piranha", BiomeGroup.JUNGLE, Rarity.UNCOMMON, 15f, 40f, 0.5f, 3.0f));
        add(new FishSpecies("peacock_bass", BiomeGroup.JUNGLE, Rarity.RARE, 50f, 100f, 3f, 12f));
        add(new FishSpecies("arapaima", BiomeGroup.JUNGLE, Rarity.EPIC, 180f, 320f, 90f, 220f));
        add(new FishSpecies("feathered_serpentfish", BiomeGroup.JUNGLE, Rarity.LEGENDARY, 220f, 450f, 120f, 500f));
        add(new FishSpecies("desert_pupfish", BiomeGroup.DESERT, Rarity.COMMON, 4f, 8f, 0.01f, 0.05f));
        add(new FishSpecies("sandskimmer", BiomeGroup.DESERT, Rarity.COMMON, 15f, 30f, 0.2f, 1.0f));
        add(new FishSpecies("nile_perch", BiomeGroup.DESERT, Rarity.UNCOMMON, 60f, 140f, 5f, 60f));
        add(new FishSpecies("tigerfish", BiomeGroup.DESERT, Rarity.RARE, 50f, 110f, 5f, 30f));
        add(new FishSpecies("mirage_eel", BiomeGroup.DESERT, Rarity.EPIC, 120f, 260f, 15f, 70f));
        add(new FishSpecies("pharaohs_goldscale", BiomeGroup.DESERT, Rarity.LEGENDARY, 160f, 340f, 80f, 400f));
        add(new FishSpecies("stone_loach", BiomeGroup.MOUNTAIN, Rarity.COMMON, 8f, 16f, 0.05f, 0.2f));
        add(new FishSpecies("alpine_dace", BiomeGroup.MOUNTAIN, Rarity.COMMON, 12f, 25f, 0.1f, 0.5f));
        add(new FishSpecies("golden_trout", BiomeGroup.MOUNTAIN, Rarity.UNCOMMON, 25f, 60f, 0.7f, 4.0f));
        add(new FishSpecies("cutthroat_trout", BiomeGroup.MOUNTAIN, Rarity.RARE, 35f, 80f, 1.5f, 8.0f));
        add(new FishSpecies("thunderfin", BiomeGroup.MOUNTAIN, Rarity.EPIC, 90f, 190f, 15f, 70f));
        add(new FishSpecies("skyplume_koi", BiomeGroup.MOUNTAIN, Rarity.LEGENDARY, 120f, 260f, 30f, 160f));
        add(new FishSpecies("sporegill", BiomeGroup.MUSHROOM, Rarity.UNCOMMON, 20f, 40f, 0.5f, 2.0f));
        add(new FishSpecies("shroomfin", BiomeGroup.MUSHROOM, Rarity.RARE, 40f, 90f, 3f, 14f));
        add(new FishSpecies("mycelial_ancient", BiomeGroup.MUSHROOM, Rarity.LEGENDARY, 130f, 280f, 60f, 300f));
        add(new FishSpecies("glowtail", BiomeGroup.CAVES, Rarity.UNCOMMON, 15f, 35f, 0.3f, 1.5f));
        add(new FishSpecies("cave_angler", BiomeGroup.CAVES, Rarity.RARE, 40f, 100f, 3f, 20f));
        add(new FishSpecies("crystal_lanternfish", BiomeGroup.CAVES, Rarity.LEGENDARY, 110f, 240f, 40f, 220f));
    }

    private static void add(FishSpecies s) {
        ALL.add(s);
        BY_GROUP.computeIfAbsent(s.group(), g -> new ArrayList<>()).add(s);
    }

    public static List<FishSpecies> forGroup(BiomeGroup group) {
        return BY_GROUP.getOrDefault(group, BY_GROUP.get(BiomeGroup.RIVER));
    }

    private FishRegistry() {}
}
