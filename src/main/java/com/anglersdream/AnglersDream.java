package com.anglersdream;

import com.anglersdream.block.TrophyBlock;
import com.anglersdream.block.TrophyBlockEntity;
import com.anglersdream.fish.FishData;
import com.anglersdream.fish.FishRegistry;
import com.anglersdream.fish.FishSpecies;
import com.anglersdream.item.BaitItem;
import com.anglersdream.item.FishItem;
import com.anglersdream.item.TieredRodItem;
import com.anglersdream.minigame.MinigameServer;
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

    // Baits: rarity luck / size bonus / variant multiplier
    public static final Item WORM_BAIT = new BaitItem(1, 0.10, 1.0, new Item.Settings());
    public static final Item GLOW_BAIT = new BaitItem(2, 0.25, 1.25, new Item.Settings());
    public static final Item ROYAL_BAIT = new BaitItem(3, 0.45, 1.5, new Item.Settings());
    public static final Item PRISMATIC_LURE = new BaitItem(4, 0.70, 3.0, new Item.Settings());

    public static final Block TROPHY_STAND = new TrophyBlock(AbstractBlock.Settings.create()
            .strength(1.0f)
            .nonOpaque()
            .sounds(BlockSoundGroup.WOOD));
    public static final Item TROPHY_STAND_ITEM = new BlockItem(TROPHY_STAND, new Item.Settings());

    public static BlockEntityType<TrophyBlockEntity> TROPHY_BLOCK_ENTITY;

    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY =
            RegistryKey.of(RegistryKeys.ITEM_GROUP, id("main"));

    @Override
    public void onInitialize() {
        // Reel-in minigame networking
        PayloadTypeRegistry.playS2C().register(StartMinigamePayload.ID, StartMinigamePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MinigameResultPayload.ID, MinigameResultPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MinigameResultPayload.ID,
                (payload, context) -> MinigameServer.handleResult(context.player(), payload));

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
        Registry.register(Registries.ITEM, id("glow_bait"), GLOW_BAIT);
        Registry.register(Registries.ITEM, id("royal_bait"), ROYAL_BAIT);
        Registry.register(Registries.ITEM, id("prismatic_lure"), PRISMATIC_LURE);

        Registry.register(Registries.BLOCK, id("trophy_stand"), TROPHY_STAND);
        Registry.register(Registries.ITEM, id("trophy_stand"), TROPHY_STAND_ITEM);

        TROPHY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("trophy_stand"),
                FabricBlockEntityTypeBuilder.create(TrophyBlockEntity::new, TROPHY_STAND).build());

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
            entries.add(GLOW_BAIT);
            entries.add(ROYAL_BAIT);
            entries.add(PRISMATIC_LURE);
            entries.add(TROPHY_STAND_ITEM);
            for (Item fish : FISH_ITEMS.values()) {
                entries.add(fish);
            }
        });
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
