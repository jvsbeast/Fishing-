package com.anglersdream;

import com.anglersdream.block.AquariumActions;
import com.anglersdream.block.AquariumBlock;
import com.anglersdream.block.AquariumBlockEntity;
import com.anglersdream.block.TrophyBlock;
import com.anglersdream.block.TrophyBlockEntity;
import com.anglersdream.fish.EncyclopediaLog;
import com.anglersdream.fish.FishData;
import com.anglersdream.fish.FishRegistry;
import com.anglersdream.fish.FishSpecies;
import com.anglersdream.item.BaitItem;
import com.anglersdream.item.FishEncyclopediaItem;
import com.anglersdream.item.FishItem;
import com.anglersdream.item.TieredRodItem;
import com.anglersdream.minigame.MinigameServer;
import com.anglersdream.network.AquariumActionPayload;
import com.anglersdream.network.MinigameResultPayload;
import com.anglersdream.network.StartMinigamePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnglersDream implements ModInitializer {

    public static final String MOD_ID = "anglersdream";

    /** 1.20.5+ data component carrying size / weight / variant for every caught fish. */
    public static final ComponentType<FishData> FISH_DATA = Registry.register(
            Registries.DATA_COMPONENT_TYPE, id("fish_data"),
            ComponentType.<FishData>builder()
                    .codec(FishData.CODEC)
                    .packetCodec(FishData.PACKET_CODEC)
                    .build());

    /** Carries a Fish Encyclopedia's discovered species and per-biome catch counts. */
    public static final ComponentType<EncyclopediaLog> ENCYCLOPEDIA_LOG = Registry.register(
            Registries.DATA_COMPONENT_TYPE, id("encyclopedia_log"),
            ComponentType.<EncyclopediaLog>builder()
                    .codec(EncyclopediaLog.CODEC)
                    .packetCodec(EncyclopediaLog.PACKET_CODEC)
                    .build());

    public static final Map<FishSpecies, Item> FISH_ITEMS = new LinkedHashMap<>();

    private static final FoodComponent RAW_FISH_FOOD = new FoodComponent.Builder()
            .nutrition(2).saturationModifier(0.1f).build();

    // Rods: rarity luck / size bonus / variant multiplier
    public static final Item REINFORCED_ROD = new TieredRodItem(1, 0.15, 1.0,
            new Item.Settings().maxDamage(128));
    public static final Item PRISMATIC_ROD = new TieredRodItem(2, 0.35, 1.5,
            new Item.Settings().maxDamage(384));
    public static final Item POSEIDONS_ROD = new TieredRodItem(3, 0.60, 2.5,
            new Item.Settings().maxDamage(1024));
    public static final Item CELESTIAL_ROD = new TieredRodItem(4, 0.90, 4.0,
            new Item.Settings().maxDamage(2048));

    // Baits: rarity luck / size bonus / variant multiplier.
    // Each mid-tier bait specializes in one or two axes rather than boosting everything —
    // Chum is size-only, Lucky is luck-only, Glimmer is variant-only, Royal is luck+size.
    // Prismatic Lure alone boosts all three, and is the strongest bait overall.
    public static final Item WORM_BAIT = new BaitItem(1, 0.08, 1.0, new Item.Settings());
    public static final Item CHUM_BAIT = new BaitItem(0, 0.55, 1.0, new Item.Settings());
    public static final Item LUCKY_BAIT = new BaitItem(4, 0.0, 1.0, new Item.Settings());
    public static final Item GLIMMER_BAIT = new BaitItem(0, 0.0, 3.0, new Item.Settings());
    public static final Item ROYAL_BAIT = new BaitItem(3, 0.35, 1.0, new Item.Settings());
    public static final Item PRISMATIC_LURE = new BaitItem(5, 0.75, 4.0, new Item.Settings());

    public static final Block TROPHY_STAND = new TrophyBlock(AbstractBlock.Settings.create()
            .strength(1.0f)
            .nonOpaque()
            .sounds(BlockSoundGroup.WOOD));
    public static final Item TROPHY_STAND_ITEM = new BlockItem(TROPHY_STAND, new Item.Settings());

    public static final Item FISH_ENCYCLOPEDIA = new FishEncyclopediaItem(new Item.Settings().maxCount(1));

    public static final Block AQUARIUM = new AquariumBlock(AbstractBlock.Settings.create()
            .strength(0.6f)
            .nonOpaque()
            .sounds(BlockSoundGroup.GLASS)
            // Mirror vanilla glass so light passes and mobs don't spawn/suffocate on it.
            .solidBlock(Blocks::never)
            .suffocates(Blocks::never)
            .blockVision(Blocks::never)
            .allowsSpawning(Blocks::never));
    public static final Item AQUARIUM_ITEM = new BlockItem(AQUARIUM, new Item.Settings());

    public static BlockEntityType<TrophyBlockEntity> TROPHY_BLOCK_ENTITY;
    public static BlockEntityType<AquariumBlockEntity> AQUARIUM_BLOCK_ENTITY;

    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY =
            RegistryKey.of(RegistryKeys.ITEM_GROUP, id("main"));

    @Override
    public void onInitialize() {
        // Reel-in minigame networking
        PayloadTypeRegistry.playS2C().register(StartMinigamePayload.ID, StartMinigamePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MinigameResultPayload.ID, MinigameResultPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MinigameResultPayload.ID,
                (payload, context) -> MinigameServer.handleResult(context.player(), payload));

        // Aquarium management networking
        PayloadTypeRegistry.playC2S().register(AquariumActionPayload.ID, AquariumActionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AquariumActionPayload.ID,
                (payload, context) -> AquariumActions.handle(context.player(), payload));

        // Fish items: one item per species so each gets its own sprite.
        for (FishSpecies species : FishRegistry.ALL) {
            Item item = new FishItem(species, new Item.Settings().maxCount(16).food(RAW_FISH_FOOD));
            Registry.register(Registries.ITEM, id(species.id()), item);
            FISH_ITEMS.put(species, item);
        }

        Registry.register(Registries.ITEM, id("reinforced_rod"), REINFORCED_ROD);
        Registry.register(Registries.ITEM, id("prismatic_rod"), PRISMATIC_ROD);
        Registry.register(Registries.ITEM, id("poseidons_rod"), POSEIDONS_ROD);
        Registry.register(Registries.ITEM, id("celestial_rod"), CELESTIAL_ROD);

        Registry.register(Registries.ITEM, id("worm_bait"), WORM_BAIT);
        Registry.register(Registries.ITEM, id("chum_bait"), CHUM_BAIT);
        Registry.register(Registries.ITEM, id("lucky_bait"), LUCKY_BAIT);
        Registry.register(Registries.ITEM, id("glimmer_bait"), GLIMMER_BAIT);
        Registry.register(Registries.ITEM, id("royal_bait"), ROYAL_BAIT);
        Registry.register(Registries.ITEM, id("prismatic_lure"), PRISMATIC_LURE);

        Registry.register(Registries.BLOCK, id("trophy_stand"), TROPHY_STAND);
        Registry.register(Registries.ITEM, id("trophy_stand"), TROPHY_STAND_ITEM);

        Registry.register(Registries.ITEM, id("fish_encyclopedia"), FISH_ENCYCLOPEDIA);

        TROPHY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("trophy_stand"),
                FabricBlockEntityTypeBuilder.create(TrophyBlockEntity::new, TROPHY_STAND).build());

        Registry.register(Registries.BLOCK, id("aquarium"), AQUARIUM);
        Registry.register(Registries.ITEM, id("aquarium"), AQUARIUM_ITEM);
        AQUARIUM_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("aquarium"),
                FabricBlockEntityTypeBuilder.create(AquariumBlockEntity::new, AQUARIUM).build());

        ItemGroup group = FabricItemGroup.builder()
                .icon(() -> new ItemStack(CELESTIAL_ROD))
                .displayName(Text.translatable("itemgroup.anglersdream"))
                .build();
        Registry.register(Registries.ITEM_GROUP, ITEM_GROUP_KEY, group);

        ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP_KEY).register(entries -> {
            entries.add(REINFORCED_ROD);
            entries.add(PRISMATIC_ROD);
            entries.add(POSEIDONS_ROD);
            entries.add(CELESTIAL_ROD);
            entries.add(WORM_BAIT);
            entries.add(CHUM_BAIT);
            entries.add(LUCKY_BAIT);
            entries.add(GLIMMER_BAIT);
            entries.add(ROYAL_BAIT);
            entries.add(PRISMATIC_LURE);
            entries.add(TROPHY_STAND_ITEM);
            entries.add(AQUARIUM_ITEM);
            entries.add(FISH_ENCYCLOPEDIA);
            for (Item fish : FISH_ITEMS.values()) {
                entries.add(fish);
            }
        });
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
