#!/usr/bin/env python3
"""Generates FishRegistry.java, lang file, item models, recipes and pixel-art textures
for the Angler's Dream Fabric mod."""
import json, os, random
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.abspath(__file__))
ASSETS = f"{ROOT}/src/main/resources/assets/anglersdream"
DATA = f"{ROOT}/src/main/resources/data/anglersdream"
JAVA = f"{ROOT}/src/main/java/com/anglersdream"

# id, display name, group, rarity, min_cm, max_cm, min_kg, max_kg, body, fin
SPECIES = [
    # OCEAN
    ("herring", "Herring", "OCEAN", "COMMON", 15, 30, 0.08, 0.4, "#9fb4c4", "#5c7285"),
    ("mackerel", "Mackerel", "OCEAN", "COMMON", 25, 45, 0.3, 1.4, "#4f7d9e", "#2c4a63"),
    ("sea_bass", "Sea Bass", "OCEAN", "UNCOMMON", 35, 80, 1.0, 7.0, "#8a9aa8", "#4a5a68"),
    ("bluefin_tuna", "Bluefin Tuna", "OCEAN", "RARE", 120, 300, 60, 380, "#2f4f6f", "#16283d"),
    ("swordfish", "Swordfish", "OCEAN", "EPIC", 170, 340, 80, 500, "#5a6d8c", "#33415c"),
    ("leviathan_ray", "Leviathan Ray", "OCEAN", "LEGENDARY", 280, 560, 350, 1300, "#3d3a5c", "#6a5acd"),
    # WARM_OCEAN
    ("parrotfish", "Parrotfish", "WARM_OCEAN", "COMMON", 20, 45, 0.5, 2.0, "#3fbf9f", "#e06fa5"),
    ("butterflyfish", "Butterflyfish", "WARM_OCEAN", "COMMON", 10, 20, 0.1, 0.4, "#f2c744", "#2b2b2b"),
    ("lionfish", "Lionfish", "WARM_OCEAN", "UNCOMMON", 25, 40, 0.7, 1.6, "#c9543a", "#f0e0d0"),
    ("mahi_mahi", "Mahi-Mahi", "WARM_OCEAN", "RARE", 80, 160, 8, 30, "#4fc94f", "#f2d044"),
    ("sailfish", "Sailfish", "WARM_OCEAN", "EPIC", 180, 320, 50, 95, "#3a6ea5", "#7fd4f2"),
    ("sunken_emperor", "Sunken Emperor", "WARM_OCEAN", "LEGENDARY", 200, 420, 150, 600, "#7a5c2e", "#e8c355"),
    # FROZEN
    ("arctic_cod", "Arctic Cod", "FROZEN", "COMMON", 20, 40, 0.4, 2.0, "#a8b8bf", "#6c8089"),
    ("icefin", "Icefin", "FROZEN", "COMMON", 15, 35, 0.3, 1.2, "#cfe8f0", "#8fc4d8"),
    ("arctic_char", "Arctic Char", "FROZEN", "UNCOMMON", 35, 75, 1.0, 9.0, "#c96a6a", "#8a4444"),
    ("greenland_halibut", "Greenland Halibut", "FROZEN", "RARE", 60, 130, 5, 45, "#6b705c", "#414536"),
    ("frostjaw_pike", "Frostjaw Pike", "FROZEN", "EPIC", 90, 180, 10, 35, "#7fb2c9", "#3f6b82"),
    ("glacier_wraithfin", "Glacier Wraithfin", "FROZEN", "LEGENDARY", 180, 380, 90, 450, "#dff2fa", "#8fd0ea"),
    # RIVER
    ("minnow", "Minnow", "RIVER", "COMMON", 5, 10, 0.01, 0.05, "#b8c4a8", "#7d8a6c"),
    ("river_perch", "River Perch", "RIVER", "COMMON", 15, 35, 0.2, 1.5, "#8aa34f", "#57682c"),
    ("brown_trout", "Brown Trout", "RIVER", "UNCOMMON", 30, 70, 0.8, 6.0, "#a3824f", "#6b542c"),
    ("zander", "Zander", "RIVER", "RARE", 50, 110, 3, 15, "#9aa8b0", "#5c6a72"),
    ("golden_sturgeon", "Golden Sturgeon", "RIVER", "EPIC", 120, 280, 30, 200, "#c9a83a", "#8a6f1e"),
    ("river_king_salmon", "River King Salmon", "RIVER", "LEGENDARY", 110, 200, 25, 90, "#d96a4f", "#9c3f2c"),
    # SWAMP
    ("mudskipper", "Mudskipper", "SWAMP", "COMMON", 10, 25, 0.1, 0.4, "#8a7d5c", "#5c5138"),
    ("bullhead_catfish", "Bullhead Catfish", "SWAMP", "COMMON", 20, 45, 0.5, 2.5, "#5c5244", "#38312a"),
    ("snakehead", "Snakehead", "SWAMP", "UNCOMMON", 40, 90, 1.5, 8.0, "#4f5c3a", "#2c3520"),
    ("alligator_gar", "Alligator Gar", "SWAMP", "RARE", 120, 260, 40, 140, "#6b7d4f", "#41522c"),
    ("bogmaw", "Bogmaw", "SWAMP", "EPIC", 140, 260, 60, 180, "#3f4a2e", "#7d9455"),
    ("elder_lungfish", "Elder Lungfish", "SWAMP", "LEGENDARY", 150, 300, 50, 250, "#5c4a6b", "#8f74a8"),
    # JUNGLE
    ("neon_tetra", "Neon Tetra", "JUNGLE", "COMMON", 3, 5, 0.005, 0.02, "#44d4f2", "#f24444"),
    ("emerald_cichlid", "Emerald Cichlid", "JUNGLE", "COMMON", 10, 25, 0.1, 0.8, "#3fbf6f", "#1e8a44"),
    ("piranha", "Piranha", "JUNGLE", "UNCOMMON", 15, 40, 0.5, 3.0, "#8a8a9c", "#d94f4f"),
    ("peacock_bass", "Peacock Bass", "JUNGLE", "RARE", 50, 100, 3, 12, "#c9a83a", "#3f7d3f"),
    ("arapaima", "Arapaima", "JUNGLE", "EPIC", 180, 320, 90, 220, "#6b7d6b", "#c94f5c"),
    ("feathered_serpentfish", "Feathered Serpentfish", "JUNGLE", "LEGENDARY", 220, 450, 120, 500, "#2ea86b", "#f2c744"),
    # DESERT
    ("desert_pupfish", "Desert Pupfish", "DESERT", "COMMON", 4, 8, 0.01, 0.05, "#7fa8d4", "#4f78a4"),
    ("sandskimmer", "Sandskimmer", "DESERT", "COMMON", 15, 30, 0.2, 1.0, "#d4bf8a", "#a8945c"),
    ("nile_perch", "Nile Perch", "DESERT", "UNCOMMON", 60, 140, 5, 60, "#a8b0a0", "#6c756a"),
    ("tigerfish", "Tigerfish", "DESERT", "RARE", 50, 110, 5, 30, "#d4d4c4", "#3a3a2e"),
    ("mirage_eel", "Mirage Eel", "DESERT", "EPIC", 120, 260, 15, 70, "#e0d4a8", "#b09a5c"),
    ("pharaohs_goldscale", "Pharaoh's Goldscale", "DESERT", "LEGENDARY", 160, 340, 80, 400, "#e8b83a", "#2e6ba8"),
    # MOUNTAIN
    ("stone_loach", "Stone Loach", "MOUNTAIN", "COMMON", 8, 16, 0.05, 0.2, "#8f8a80", "#5c584f"),
    ("alpine_dace", "Alpine Dace", "MOUNTAIN", "COMMON", 12, 25, 0.1, 0.5, "#a8bfd4", "#6c8aa8"),
    ("golden_trout", "Golden Trout", "MOUNTAIN", "UNCOMMON", 25, 60, 0.7, 4.0, "#e0a83a", "#a86c1e"),
    ("cutthroat_trout", "Cutthroat Trout", "MOUNTAIN", "RARE", 35, 80, 1.5, 8.0, "#bf8a5c", "#d94444"),
    ("thunderfin", "Thunderfin", "MOUNTAIN", "EPIC", 90, 190, 15, 70, "#5c6b8f", "#f2e044"),
    ("skyplume_koi", "Skyplume Koi", "MOUNTAIN", "LEGENDARY", 120, 260, 30, 160, "#f0f0f0", "#e05c3a"),
    # MUSHROOM
    ("sporegill", "Sporegill", "MUSHROOM", "UNCOMMON", 20, 40, 0.5, 2.0, "#c46a8a", "#f0d4e0"),
    ("shroomfin", "Shroomfin", "MUSHROOM", "RARE", 40, 90, 3, 14, "#a83a3a", "#e8dcc4"),
    ("mycelial_ancient", "Mycelial Ancient", "MUSHROOM", "LEGENDARY", 130, 280, 60, 300, "#8a5c9c", "#d4a8e0"),
    # CAVES
    ("glowtail", "Glowtail", "CAVES", "UNCOMMON", 15, 35, 0.3, 1.5, "#3a4a5c", "#7ff2c4"),
    ("cave_angler", "Cave Angler", "CAVES", "RARE", 40, 100, 3, 20, "#2e2e3a", "#f2e07f"),
    ("crystal_lanternfish", "Crystal Lanternfish", "CAVES", "LEGENDARY", 110, 240, 40, 220, "#a4c4e8", "#e8bff2"),
]

RODS = [("reinforced_rod", "Reinforced Rod"),
        ("prismatic_rod", "Prismatic Rod"),
        ("poseidons_rod", "Poseidon's Rod"),
        ("celestial_rod", "Celestial Rod")]
BAITS = [("worm_bait", "Worm Bait", "#c98a7a"),
         ("chum_bait", "Chum Bait", "#8a5c46"),
         ("lucky_bait", "Lucky Bait", "#5cbf5c"),
         ("glimmer_bait", "Glimmer Bait", "#8ff2c4"),
         ("royal_bait", "Royal Bait", "#e8c355"),
         ("prismatic_lure", "Prismatic Lure", "#c48af2")]


def hex_to_rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def shade(rgb, f):
    return tuple(max(0, min(255, int(c * f))) for c in rgb)


# ---------------------------------------------------------------- FishRegistry.java
def gen_registry():
    lines = [
        "package com.anglersdream.fish;",
        "",
        "import java.util.ArrayList;",
        "import java.util.EnumMap;",
        "import java.util.List;",
        "import java.util.Map;",
        "",
        "/** All fish species in the mod. Generated data - tweak freely. */",
        "public final class FishRegistry {",
        "",
        "    public static final List<FishSpecies> ALL = new ArrayList<>();",
        "    private static final Map<BiomeGroup, List<FishSpecies>> BY_GROUP = new EnumMap<>(BiomeGroup.class);",
        "",
        "    static {",
    ]
    for sid, _name, group, rarity, mn, mx, wmn, wmx, *_ in SPECIES:
        lines.append(
            f'        add(new FishSpecies("{sid}", BiomeGroup.{group}, Rarity.{rarity}, '
            f'{mn}f, {mx}f, {wmn}f, {wmx}f));')
    lines += [
        "    }",
        "",
        "    private static void add(FishSpecies s) {",
        "        ALL.add(s);",
        "        BY_GROUP.computeIfAbsent(s.group(), g -> new ArrayList<>()).add(s);",
        "    }",
        "",
        "    public static List<FishSpecies> forGroup(BiomeGroup group) {",
        "        return BY_GROUP.getOrDefault(group, BY_GROUP.get(BiomeGroup.RIVER));",
        "    }",
        "",
        "    private FishRegistry() {}",
        "}",
        "",
    ]
    with open(f"{JAVA}/fish/FishRegistry.java", "w") as f:
        f.write("\n".join(lines))


# ---------------------------------------------------------------- lang
def gen_lang():
    lang = {
        "itemgroup.anglersdream": "Angler's Dream",
        "block.anglersdream.trophy_stand": "Trophy Stand",
        "block.anglersdream.aquarium": "Aquarium",
        "screen.anglersdream.aquarium_title": "Aquarium",
        "screen.anglersdream.aquarium_blocks": "%s blocks",
        "screen.anglersdream.aquarium_fish": "Fish: %s / %s",
        "screen.anglersdream.aquarium_water_full": "Water: Filled",
        "screen.anglersdream.aquarium_water_empty": "Water: Empty",
        "screen.anglersdream.aquarium_water_partial": "Water: %s / %s",
        "screen.anglersdream.aquarium_fill": "Fill",
        "screen.anglersdream.aquarium_drain": "Drain",
        "screen.anglersdream.aquarium_tank_label": "In the tank (click: remove · right-click: show/hide)",
        "screen.anglersdream.aquarium_inv_label": "Your fish (click to add)",
        "screen.anglersdream.aquarium_no_water": "Fill the tank with a water bucket so your fish can swim",
        "screen.anglersdream.aquarium_hidden": "Hidden from display",
        "rarity.anglersdream.common": "Common",
        "rarity.anglersdream.uncommon": "Uncommon",
        "rarity.anglersdream.rare": "Rare",
        "rarity.anglersdream.epic": "Epic",
        "rarity.anglersdream.legendary": "\u2605 Legendary",
        "variant.anglersdream.shiny": "Shiny",
        "variant.anglersdream.golden": "Golden",
        "variant.anglersdream.prismatic": "Prismatic",
        "tooltip.anglersdream.length": "Length: %s cm",
        "tooltip.anglersdream.weight": "Weight: %s kg",
        "tooltip.anglersdream.luck": "+%s Rarity Luck",
        "tooltip.anglersdream.size_bonus": "+%s%% Fish Size",
        "tooltip.anglersdream.variant_bonus": "\u00d7%s Variant Chance",
        "tooltip.anglersdream.bait_hint": "Consumed automatically when fishing",
        "message.anglersdream.rare_catch": "Incredible catch: %s!",
        "message.anglersdream.escaped": "It got away...",
        "message.anglersdream.perfect": "\u2728 Perfect catch! \u2728",
        "screen.anglersdream.minigame_title": "Reel it in!",
        "screen.anglersdream.perfect_indicator": "Perfect!",
        "screen.anglersdream.encyclopedia_title": "Fish Encyclopedia",
        "item.anglersdream.fish_encyclopedia": "Fish Encyclopedia",
        "tooltip.anglersdream.encyclopedia_hint": "Right-click to open your catch log",
    }
    for sid, name, *_ in SPECIES:
        lang[f"item.anglersdream.{sid}"] = name
    for rid, name in RODS:
        lang[f"item.anglersdream.{rid}"] = name
    for bid, name, _ in BAITS:
        lang[f"item.anglersdream.{bid}"] = name
    with open(f"{ASSETS}/lang/en_us.json", "w") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)


# ---------------------------------------------------------------- models
def gen_models():
    for sid, *_ in SPECIES:
        with open(f"{ASSETS}/models/item/{sid}.json", "w") as f:
            json.dump({"parent": "minecraft:item/generated",
                       "textures": {"layer0": f"anglersdream:item/{sid}"}}, f, indent=2)
    for rid, _ in RODS:
        with open(f"{ASSETS}/models/item/{rid}.json", "w") as f:
            json.dump({
                "parent": "minecraft:item/handheld_rod",
                "textures": {"layer0": f"anglersdream:item/{rid}"},
                "overrides": [{"predicate": {"cast": 1},
                               "model": f"anglersdream:item/{rid}_cast"}],
            }, f, indent=2)
        with open(f"{ASSETS}/models/item/{rid}_cast.json", "w") as f:
            json.dump({"parent": "minecraft:item/handheld_rod",
                       "textures": {"layer0": f"anglersdream:item/{rid}_cast"}}, f, indent=2)
    for bid, _, _ in BAITS:
        with open(f"{ASSETS}/models/item/{bid}.json", "w") as f:
            json.dump({"parent": "minecraft:item/generated",
                       "textures": {"layer0": f"anglersdream:item/{bid}"}}, f, indent=2)

    # trophy stand block model (small wall medallion the mounted fish covers,
    # base = facing north)
    block_model = {
        "textures": {
            "particle": "anglersdream:block/trophy_stand",
            "plaque": "anglersdream:block/trophy_stand",
        },
        "elements": [{
            "from": [5, 5, 15],
            "to": [11, 11, 16],
            "faces": {
                "north": {"texture": "#plaque"},
                "south": {"texture": "#plaque"},
                "east": {"texture": "#plaque"},
                "west": {"texture": "#plaque"},
                "up": {"texture": "#plaque"},
                "down": {"texture": "#plaque"},
            },
        }],
    }
    with open(f"{ASSETS}/models/block/trophy_stand.json", "w") as f:
        json.dump(block_model, f, indent=2)
    with open(f"{ASSETS}/blockstates/trophy_stand.json", "w") as f:
        json.dump({"variants": {
            "facing=north": {"model": "anglersdream:block/trophy_stand"},
            "facing=east": {"model": "anglersdream:block/trophy_stand", "y": 90},
            "facing=south": {"model": "anglersdream:block/trophy_stand", "y": 180},
            "facing=west": {"model": "anglersdream:block/trophy_stand", "y": 270},
        }}, f, indent=2)
    with open(f"{ASSETS}/models/item/trophy_stand.json", "w") as f:
        json.dump({"parent": "minecraft:item/generated",
                   "textures": {"layer0": "anglersdream:item/trophy_stand"}}, f, indent=2)

    # aquarium: connected-texture-style tank. The core model is six nearly-clear
    # glass faces (culled between adjacent tanks like vanilla glass); each of the
    # block's twelve frame edges is its own multipart entry, drawn only when the
    # tank does not continue in either direction flanking that edge, so borders
    # merge into one outline across any build.
    all_faces = lambda tex, cull: {
        face: ({"texture": tex, "cullface": face} if cull else {"texture": tex})
        for face in ("north", "south", "east", "west", "up", "down")
    }

    def wm(name, obj):
        with open(f"{ASSETS}/models/block/{name}.json", "w") as f:
            json.dump(obj, f, indent=2)

    # top-north horizontal beam; blockstate rotations place all 8 horizontal edges
    wm("aquarium_edge_h", {
        "textures": {"particle": "anglersdream:block/aquarium_frame",
                     "edge": "anglersdream:block/aquarium_edge"},
        "elements": [{"from": [0, 15, 0], "to": [16, 16, 1],
                      "faces": all_faces("#edge", False)}],
    })
    # north-west vertical beam; y-rotations place all 4 vertical edges
    wm("aquarium_edge_v", {
        "textures": {"particle": "anglersdream:block/aquarium_frame",
                     "edge": "anglersdream:block/aquarium_edge"},
        "elements": [{"from": [0, 0, 0], "to": [1, 16, 1],
                      "faces": all_faces("#edge", False)}],
    })
    # Water is six full-extent face planes, each inset 0.1px along its own
    # normal only: adjacent blocks' water surfaces stay perfectly coplanar and
    # continuous across seams (a cube inset on all axes leaves slit lines at
    # every block boundary). Each face carries cullface, and isSideInvisible
    # merges same-fill neighbors, so interior panes vanish — one body of water.
    inset = 0.1
    water_planes = []
    for face, box in (
        ("up",    [[0, 0, 0], [16, 16 - inset, 16]]),
        ("down",  [[0, inset, 0], [16, 16, 16]]),
        ("north", [[0, 0, inset], [16, 16, 16]]),
        ("south", [[0, 0, 0], [16, 16, 16 - inset]]),
        ("west",  [[inset, 0, 0], [16, 16, 16]]),
        ("east",  [[0, 0, 0], [16 - inset, 16, 16]]),
    ):
        water_planes.append({
            "from": box[0], "to": box[1],
            "faces": {face: {"texture": "#water", "cullface": face}},
        })
    wm("aquarium_water", {
        "textures": {"particle": "anglersdream:block/aquarium_water",
                     "water": "anglersdream:block/aquarium_water"},
        "elements": water_planes,
    })
    # standalone framed cube for the inventory/hand model
    wm("aquarium_item", {
        "parent": "minecraft:block/block",
        "textures": {"particle": "anglersdream:block/aquarium_frame",
                     "frame": "anglersdream:block/aquarium_frame"},
        "elements": [{"from": [0, 0, 0], "to": [16, 16, 16],
                      "faces": all_faces("#frame", False)}],
    })

    edge = "anglersdream:block/aquarium_edge_h"
    vert = "anglersdream:block/aquarium_edge_v"
    multipart = [
        # no glass face model at all: the block is fully clear except the borders
        {"when": {"filled": "true"}, "apply": {"model": "anglersdream:block/aquarium_water"}},
        # horizontal edges, top then bottom (x=180 flips top-north to bottom-south)
        {"when": {"up": "false", "north": "false"}, "apply": {"model": edge}},
        {"when": {"up": "false", "east": "false"}, "apply": {"model": edge, "y": 90}},
        {"when": {"up": "false", "south": "false"}, "apply": {"model": edge, "y": 180}},
        {"when": {"up": "false", "west": "false"}, "apply": {"model": edge, "y": 270}},
        {"when": {"down": "false", "south": "false"}, "apply": {"model": edge, "x": 180}},
        {"when": {"down": "false", "west": "false"}, "apply": {"model": edge, "x": 180, "y": 90}},
        {"when": {"down": "false", "north": "false"}, "apply": {"model": edge, "x": 180, "y": 180}},
        {"when": {"down": "false", "east": "false"}, "apply": {"model": edge, "x": 180, "y": 270}},
        # vertical corner edges
        {"when": {"north": "false", "west": "false"}, "apply": {"model": vert}},
        {"when": {"north": "false", "east": "false"}, "apply": {"model": vert, "y": 90}},
        {"when": {"south": "false", "east": "false"}, "apply": {"model": vert, "y": 180}},
        {"when": {"south": "false", "west": "false"}, "apply": {"model": vert, "y": 270}},
    ]
    with open(f"{ASSETS}/blockstates/aquarium.json", "w") as f:
        json.dump({"multipart": multipart}, f, indent=2)
    with open(f"{ASSETS}/models/item/aquarium.json", "w") as f:
        json.dump({"parent": "anglersdream:block/aquarium_item"}, f, indent=2)
    with open(f"{ASSETS}/models/item/fish_encyclopedia.json", "w") as f:
        json.dump({"parent": "minecraft:item/generated",
                   "textures": {"layer0": "anglersdream:item/fish_encyclopedia"}}, f, indent=2)


# ---------------------------------------------------------------- recipes + loot
def gen_data():
    def w(name, obj):
        with open(f"{DATA}/recipe/{name}.json", "w") as f:
            json.dump(obj, f, indent=2)

    w("trophy_stand", {
        "type": "minecraft:crafting_shaped",
        "pattern": ["SSS", "SPS", "SSS"],
        "key": {"S": {"item": "minecraft:stick"}, "P": {"tag": "minecraft:planks"}},
        "result": {"id": "anglersdream:trophy_stand", "count": 1},
    })
    w("reinforced_rod", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "minecraft:fishing_rod"},
                        {"item": "minecraft:iron_ingot"},
                        {"item": "minecraft:iron_ingot"}],
        "result": {"id": "anglersdream:reinforced_rod"},
    })
    # Every upgraded rod starts from a plain vanilla rod; the extra ingredients
    # scale in rarity with the rod tier.
    w("prismatic_rod", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "minecraft:fishing_rod"},
                        {"item": "minecraft:prismarine_crystals"},
                        {"item": "minecraft:prismarine_crystals"},
                        {"item": "minecraft:diamond"}],
        "result": {"id": "anglersdream:prismatic_rod"},
    })
    w("poseidons_rod", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "minecraft:fishing_rod"},
                        {"item": "minecraft:heart_of_the_sea"},
                        {"item": "minecraft:nautilus_shell"},
                        {"item": "minecraft:diamond"}],
        "result": {"id": "anglersdream:poseidons_rod"},
    })
    w("celestial_rod", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "minecraft:fishing_rod"},
                        {"item": "minecraft:nether_star"},
                        {"item": "minecraft:dragon_breath"},
                        {"item": "minecraft:echo_shard"}],
        "result": {"id": "anglersdream:celestial_rod"},
    })
    # Worm Bait is the base for every other bait; higher tiers need progressively
    # more of it plus rarer specialty ingredients matching what they boost.
    w("worm_bait", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "minecraft:rotten_flesh"}],
        "result": {"id": "anglersdream:worm_bait", "count": 4},
    })
    w("chum_bait", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "anglersdream:worm_bait"},
                        {"item": "minecraft:pufferfish"},
                        {"item": "minecraft:bone_meal"}],
        "result": {"id": "anglersdream:chum_bait", "count": 2},
    })
    w("lucky_bait", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "anglersdream:worm_bait"},
                        {"item": "anglersdream:worm_bait"},
                        {"item": "minecraft:rabbit_foot"}],
        "result": {"id": "anglersdream:lucky_bait", "count": 2},
    })
    w("glimmer_bait", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "anglersdream:worm_bait"},
                        {"item": "anglersdream:worm_bait"},
                        {"item": "minecraft:glow_ink_sac"},
                        {"item": "minecraft:phantom_membrane"}],
        "result": {"id": "anglersdream:glimmer_bait", "count": 2},
    })
    w("royal_bait", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "anglersdream:worm_bait"},
                        {"item": "anglersdream:worm_bait"},
                        {"item": "anglersdream:worm_bait"},
                        {"item": "minecraft:gold_nugget"},
                        {"item": "minecraft:gold_nugget"}],
        "result": {"id": "anglersdream:royal_bait", "count": 2},
    })
    w("prismatic_lure", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "anglersdream:worm_bait"},
                        {"item": "anglersdream:worm_bait"},
                        {"item": "anglersdream:worm_bait"},
                        {"item": "anglersdream:worm_bait"},
                        {"item": "minecraft:diamond"},
                        {"item": "minecraft:obsidian"},
                        {"item": "minecraft:gold_ingot"}],
        "result": {"id": "anglersdream:prismatic_lure", "count": 2},
    })
    w("fish_encyclopedia", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "minecraft:book"},
                        {"item": "minecraft:fishing_rod"},
                        {"item": "minecraft:ink_sac"}],
        "result": {"id": "anglersdream:fish_encyclopedia"},
    })

    # Any fish (all mod species + vanilla) counts for aquarium crafting and storage.
    tags_dir = f"{DATA}/tags/item"
    os.makedirs(tags_dir, exist_ok=True)
    with open(f"{tags_dir}/fishes.json", "w") as f:
        json.dump({
            "replace": False,
            "values": [f"anglersdream:{sid}" for sid, *_ in SPECIES]
                      + ["minecraft:cod", "minecraft:salmon",
                         "minecraft:tropical_fish", "minecraft:pufferfish"],
        }, f, indent=2)

    w("aquarium", {
        "type": "minecraft:crafting_shaped",
        "pattern": ["GGG", "GFG", "GGG"],
        "key": {"G": {"item": "minecraft:glass_pane"}, "F": {"tag": "anglersdream:fishes"}},
        "result": {"id": "anglersdream:aquarium", "count": 1},
    })

    for block in ("trophy_stand", "aquarium"):
        with open(f"{DATA}/loot_table/blocks/{block}.json", "w") as f:
            json.dump({
                "type": "minecraft:block",
                "pools": [{
                    "rolls": 1,
                    "entries": [{"type": "minecraft:item", "name": f"anglersdream:{block}"}],
                    "conditions": [{"condition": "minecraft:survives_explosion"}],
                }],
            }, f, indent=2)


# ---------------------------------------------------------------- textures
#
# Every species has its own hand-tuned 16x16 sprite: a silhouette (oval, pike,
# eel, ray, flatfish, billfish, serpent...) plus species-specific markings and
# details, drawn with the palette from SPECIES. Fish face right; tail at left.

WHITE = (245, 245, 245)
INK = (20, 20, 25)
GOLD = (238, 200, 80)
RED = (214, 64, 54)


class Sprite:
    """16x16 canvas with a body mask so patterns stay inside the silhouette."""

    def __init__(self, body_hex, fin_hex):
        self.img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        self.d = ImageDraw.Draw(self.img)
        self.mask = Image.new("1", (16, 16), 0)
        self.md = ImageDraw.Draw(self.mask)
        self.body = hex_to_rgb(body_hex)
        self.fin = hex_to_rgb(fin_hex)
        self.dark = shade(self.body, 0.55)
        self.light = shade(self.body, 1.4)
        self.fdark = shade(self.fin, 0.6)
        self.flight = shade(self.fin, 1.35)
        self.box = (3, 5, 13, 11)

    # ---- silhouettes ----
    def ellipse(self, box):
        self.d.ellipse(box, fill=self.body, outline=self.dark)
        self.md.ellipse(box, fill=1)
        self.box = tuple(box)

    def poly(self, pts):
        self.d.polygon(pts, fill=self.body, outline=self.dark)
        self.md.polygon(pts, fill=1)
        xs = [p[0] for p in pts]
        ys = [p[1] for p in pts]
        self.box = (min(xs), min(ys), max(xs), max(ys))

    def snake(self, pts, r=1):
        """Eel/serpent body: overlapping discs along a path."""
        for x, y in pts:
            self.d.ellipse([x - r, y - r, x + r, y + r], fill=self.body)
            self.md.ellipse([x - r, y - r, x + r, y + r], fill=1)
        xs = [p[0] for p in pts]
        ys = [p[1] for p in pts]
        self.box = (min(xs) - r, min(ys) - r, max(xs) + r, max(ys) + r)

    # ---- pixel helpers ----
    def px(self, x, y, c):
        """Paint only inside the body silhouette."""
        if 0 <= x < 16 and 0 <= y < 16 and self.mask.getpixel((x, y)):
            self.d.point((x, y), fill=c)

    def raw(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            self.d.point((x, y), fill=c)

    # ---- patterns (mask-constrained) ----
    def vbar(self, x, c, top=0, bottom=0):
        for y in range(self.box[1] + 1 + top, self.box[3] - bottom):
            self.px(x, y, c)

    def hstripe(self, y, c, pad=1):
        for x in range(self.box[0] + pad, self.box[2] - pad + 1):
            self.px(x, y, c)

    def belly(self, c=None):
        c = c or self.light
        x0, y0, x1, y1 = self.box
        cy = (y0 + y1) // 2
        for y in range(cy + 1, y1):
            for x in range(x0 + 2, x1 - 1):
                self.px(x, y, c)

    def back(self, c):
        x0, y0, x1, y1 = self.box
        cy = (y0 + y1) // 2
        for y in range(y0 + 1, cy):
            for x in range(x0 + 2, x1 - 1):
                self.px(x, y, c)

    def dots(self, pts, c):
        for x, y in pts:
            self.px(x, y, c)

    def mottle(self, seed, n, c):
        rng = random.Random(seed)
        x0, y0, x1, y1 = self.box
        for _ in range(n):
            self.px(rng.randint(x0 + 1, x1 - 1), rng.randint(y0 + 1, y1 - 1), c)

    # ---- fins ----
    def tail_fan(self, x, cy, c=None, spread=3):
        c = c or self.fin
        self.d.polygon([(x, cy), (x - 3, cy - spread), (x - 3, cy + spread)],
                       fill=c, outline=self.fdark)

    def tail_fork(self, x, cy, c=None):
        c = c or self.fin
        self.d.polygon([(x, cy), (x - 3, cy - 3), (x - 1, cy)], fill=c)
        self.d.polygon([(x, cy), (x - 3, cy + 3), (x - 1, cy)], fill=c)

    def tail_crescent(self, x, cy, c=None):
        c = c or self.fin
        self.d.line([(x, cy), (x - 3, cy - 4)], fill=c)
        self.d.line([(x - 1, cy), (x - 3, cy - 3)], fill=c)
        self.d.line([(x, cy), (x - 3, cy + 4)], fill=c)
        self.d.line([(x - 1, cy), (x - 3, cy + 3)], fill=c)

    def dorsal(self, x0, x1, ybase, ytop, c=None):
        c = c or self.fin
        self.d.polygon([(x0, ybase), ((x0 + x1) // 2, ytop), (x1, ybase)], fill=c)

    def spines(self, xs, ybase, h=2, c=None):
        c = c or self.fin
        for x in xs:
            self.d.line([(x, ybase), (x, ybase - h)], fill=c)

    def sail(self, x0, x1, ybase, ytop, c=None):
        c = c or self.fin
        self.d.polygon([(x0, ybase), (x0 + 1, ytop + 1), ((x0 + x1) // 2, ytop),
                        (x1 - 1, ytop + 1), (x1, ybase)], fill=c)

    def pect(self, x, y, c=None):
        c = c or self.fin
        self.d.polygon([(x, y), (x + 2, y + 2), (x - 1, y + 2)], fill=c)

    # ---- face / extras ----
    def eye(self, x, y, ring=None):
        if ring:
            for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
                self.raw(x + dx, y + dy, ring)
        self.raw(x, y, WHITE)
        self.raw(x + 1, y, INK)

    def teeth(self, pts):
        for x, y in pts:
            self.raw(x, y, WHITE)

    def barbels(self, pts, c=None):
        c = c or self.dark
        for (x0, y0), (x1, y1) in pts:
            self.d.line([(x0, y0), (x1, y1)], fill=c)

    def bill(self, x, y, ln, c=None):
        self.d.line([(x, y), (x + ln, y - 1)], fill=c or self.dark)


SPRITES = {}


def sprite(sid):
    def reg(fn):
        SPRITES[sid] = fn
        return fn
    return reg


# ------------------------------- OCEAN -------------------------------
@sprite("herring")
def _(c):
    c.ellipse([3, 6, 13, 10])
    c.belly()
    c.hstripe(8, c.dark)
    c.dorsal(7, 10, 6, 4)
    c.tail_fork(3, 8)
    c.pect(9, 9)
    c.eye(11, 7)


@sprite("mackerel")
def _(c):
    c.ellipse([3, 6, 13, 10])
    c.belly()
    for x in (5, 7, 9, 11):          # wavy tiger-striped back
        c.px(x, 6, c.dark)
        c.px(x + 1, 7, c.dark)
    c.dorsal(7, 10, 6, 4)
    c.tail_fork(3, 8)
    c.eye(11, 7)


@sprite("sea_bass")
def _(c):
    c.ellipse([3, 5, 13, 11])
    c.belly()
    c.hstripe(8, c.dark)
    c.spines((6, 8, 10), 5, 2)
    c.tail_fan(3, 8)
    c.pect(9, 9)
    c.eye(11, 7)


@sprite("bluefin_tuna")
def _(c):
    c.poly([(3, 8), (6, 5), (11, 5), (14, 8), (11, 11), (6, 11)])
    c.back(shade(c.body, 0.75))
    c.belly((200, 210, 218))
    for x in (8, 9, 10, 11):         # yellow finlets
        c.raw(x, 4, GOLD)
    c.tail_crescent(3, 8)
    c.pect(10, 9)
    c.eye(11, 7)


@sprite("swordfish")
def _(c):
    c.poly([(4, 8), (7, 6), (12, 6), (13, 8), (12, 10), (7, 10)])
    c.belly()
    c.bill(13, 8, 2, c.dark)
    c.raw(15, 7, c.dark)
    c.dorsal(7, 10, 6, 2)            # tall dorsal
    c.tail_crescent(4, 8)
    c.eye(11, 7)


@sprite("leviathan_ray")
def _(c):
    c.poly([(4, 8), (8, 4), (13, 8), (8, 12)])
    c.dots([(7, 7), (9, 7), (8, 9), (10, 8), (6, 8)], c.fin)   # violet spots
    c.d.line([(4, 8), (1, 11)], fill=c.dark)                    # tail whip
    c.raw(1, 12, c.fin)
    c.eye(10, 7)


# ---------------------------- WARM_OCEAN -----------------------------
@sprite("parrotfish")
def _(c):
    c.ellipse([4, 5, 13, 11])
    c.hstripe(6, c.fin)
    c.hstripe(10, c.fin)
    c.raw(13, 8, c.flight)           # beak
    c.raw(13, 9, c.flight)
    c.tail_fan(4, 8)
    c.pect(10, 9)
    c.eye(11, 7)


@sprite("butterflyfish")
def _(c):
    c.ellipse([4, 5, 12, 11])
    c.vbar(10, c.fin)                # black band through the eye
    c.vbar(11, c.fin)
    c.px(6, 6, c.fin)                # false eyespot near tail
    c.px(6, 7, c.fin)
    c.dorsal(6, 10, 5, 3)
    c.tail_fan(4, 8, spread=2)
    c.eye(11, 7)


@sprite("lionfish")
def _(c):
    c.ellipse([4, 6, 12, 10])
    for x in (5, 7, 9, 11):
        c.vbar(x, c.fin)
    for x0, y0, x1, y1 in ((5, 6, 3, 3), (7, 6, 6, 2), (9, 6, 9, 2), (11, 6, 12, 3)):
        c.d.line([(x0, y0), (x1, y1)], fill=c.fdark)            # venomous rays
    c.tail_fan(4, 8, spread=2)
    c.eye(10, 7)


@sprite("mahi_mahi")
def _(c):
    c.poly([(3, 8), (6, 5), (12, 5), (13, 6), (13, 10), (6, 11)])
    c.belly(c.fin)                   # golden belly
    for x in range(6, 13):           # long low dorsal along the whole back
        c.raw(x, 4, shade(c.body, 0.7))
    c.tail_fork(3, 8, c.fin)
    c.eye(11, 7)


@sprite("sailfish")
def _(c):
    c.poly([(3, 8), (6, 6), (12, 6), (13, 8), (12, 10), (6, 10)])
    c.belly()
    c.sail(5, 11, 5, 2)              # huge sail
    c.bill(13, 8, 2, c.dark)
    c.raw(15, 7, c.dark)
    c.tail_crescent(3, 8)
    c.eye(11, 7)


@sprite("sunken_emperor")
def _(c):
    c.ellipse([4, 5, 13, 11])
    c.hstripe(6, c.fin)
    c.hstripe(10, c.fin)
    for x in (11, 12, 13):           # golden crown spikes
        c.raw(x, 4, GOLD)
    c.raw(12, 3, GOLD)
    c.tail_fan(4, 8, GOLD)
    c.mottle("emperor", 4, c.dark)   # barnacled hide
    c.eye(11, 7, ring=GOLD)


# ------------------------------ FROZEN -------------------------------
@sprite("arctic_cod")
def _(c):
    c.ellipse([3, 6, 13, 10])
    c.belly()
    c.mottle("cod", 8, c.dark)
    c.raw(13, 10, c.dark)            # chin barbel
    for x0, x1 in ((5, 6), (7, 8), (9, 10)):                    # triple dorsal
        c.dorsal(x0, x1 + 1, 6, 5)
    c.tail_fan(3, 8, spread=2)
    c.eye(11, 7)


@sprite("icefin")
def _(c):
    c.ellipse([4, 6, 12, 9])
    c.dots([(6, 7), (8, 8), (10, 7)], WHITE)                    # ice glints
    c.dorsal(6, 9, 6, 4, c.flight)
    c.tail_fan(4, 7, c.flight, spread=2)
    c.pect(9, 8, c.flight)
    c.eye(10, 7)


@sprite("arctic_char")
def _(c):
    c.ellipse([3, 6, 13, 10])
    c.back(shade(c.body, 0.8))
    c.belly(c.flight)
    c.dots([(5, 7), (7, 6), (9, 7), (11, 6)], WHITE)            # pale spots
    c.tail_fork(3, 8)
    c.dorsal(7, 10, 6, 4)
    c.eye(11, 7)


@sprite("greenland_halibut")
def _(c):
    c.ellipse([2, 7, 13, 12])        # flatfish lying on its side
    c.mottle("halibut", 10, c.dark)
    c.hstripe(8, shade(c.body, 0.8))
    c.eye(10, 8)
    c.eye(8, 8)                      # both eyes on the up-side
    c.tail_fan(2, 9, spread=2)


@sprite("frostjaw_pike")
def _(c):
    c.poly([(3, 8), (5, 6), (10, 6), (14, 7), (14, 9), (10, 10), (5, 10)])
    c.belly()
    for x in (6, 8, 10):
        c.px(x, 7, c.flight)         # icy flecks
    c.teeth([(13, 9), (14, 9)])      # frost jaw
    c.dorsal(5, 8, 6, 5)             # dorsal set far back
    c.tail_fan(3, 8, spread=2)
    c.eye(12, 7)


@sprite("glacier_wraithfin")
def _(c):
    c.snake([(3, 10), (5, 9), (7, 8), (9, 7), (11, 7), (13, 8)])
    for x, y in ((5, 7), (7, 6), (9, 5), (11, 5)):
        c.raw(x, y, c.fin)           # trailing spectral fin ribbon
    c.dots([(6, 9), (9, 7), (11, 7)], WHITE)
    c.d.line([(3, 11), (1, 13)], fill=c.fin)                    # wispy tail
    c.d.line([(3, 10), (1, 10)], fill=c.fin)
    c.eye(12, 7)


# ------------------------------- RIVER -------------------------------
@sprite("minnow")
def _(c):
    c.ellipse([5, 7, 11, 9])
    c.hstripe(8, c.light, pad=1)
    c.tail_fork(5, 8)
    c.eye(9, 8)


@sprite("river_perch")
def _(c):
    c.ellipse([3, 5, 13, 11])
    c.belly()
    for x in (5, 7, 9, 11):
        c.vbar(x, c.dark)
    c.spines((6, 8, 10), 5, 2)
    c.tail_fan(3, 8)
    c.eye(11, 7)


@sprite("brown_trout")
def _(c):
    c.ellipse([3, 6, 13, 10])
    c.belly((222, 205, 160))
    c.dots([(5, 7), (8, 6), (11, 7)], c.dark)
    c.dots([(6, 8), (9, 7), (10, 8)], RED)                      # red spots
    c.raw(5, 5, c.body)              # adipose fin nub
    c.tail_fan(3, 8, spread=2)
    c.eye(11, 7)


@sprite("zander")
def _(c):
    c.poly([(3, 8), (5, 6), (11, 6), (14, 8), (11, 10), (5, 10)])
    c.belly()
    for x in (5, 7, 9):
        c.vbar(x, c.dark)
    c.spines((6, 8), 6, 2)
    c.teeth([(13, 9)])
    c.tail_fork(3, 8)
    c.eye(12, 7, ring=c.light)       # glassy nocturnal eye


@sprite("golden_sturgeon")
def _(c):
    c.poly([(3, 9), (6, 7), (11, 7), (15, 8), (11, 10), (6, 10)])
    for x in (5, 7, 9, 11):          # bony scutes along the back
        c.raw(x, 7, c.light)
    c.barbels([((12, 10), (12, 12)), ((13, 10), (13, 12))])
    c.d.polygon([(3, 9), (1, 5), (3, 7)], fill=c.fin)           # upturned shark tail
    c.d.polygon([(3, 9), (1, 10), (2, 10)], fill=c.fdark)
    c.eye(12, 8)


@sprite("river_king_salmon")
def _(c):
    c.ellipse([3, 5, 13, 10])
    c.belly()
    c.dots([(6, 6), (8, 7), (10, 6)], c.dark)
    c.raw(14, 8, c.dark)             # hooked kype jaw
    c.raw(14, 9, c.dark)
    c.raw(13, 9, c.dark)
    c.dorsal(7, 10, 5, 3)            # humped back
    c.tail_fan(3, 7)
    c.eye(11, 6)


# ------------------------------- SWAMP -------------------------------
@sprite("mudskipper")
def _(c):
    c.ellipse([3, 8, 11, 12])
    c.ellipse([8, 6, 13, 11])        # bulbous head
    c.belly()
    c.raw(11, 5, c.body)             # periscope eyes on top
    c.eye(11, 5)
    c.d.line([(6, 12), (6, 14)], fill=c.fdark)                  # propped pectorals
    c.d.line([(9, 12), (9, 14)], fill=c.fdark)
    c.tail_fan(3, 10, spread=2)
    c.mottle("skipper", 5, c.dark)


@sprite("bullhead_catfish")
def _(c):
    c.ellipse([3, 7, 10, 11])
    c.ellipse([8, 6, 14, 11])        # broad flat head
    c.belly()
    c.barbels([((14, 8), (15, 7)), ((14, 9), (15, 10)), ((13, 10), (14, 12))])
    c.tail_fan(3, 9, spread=2)
    c.eye(11, 7)


@sprite("snakehead")
def _(c):
    c.poly([(3, 8), (4, 6), (12, 6), (14, 8), (12, 10), (4, 10)])
    for x in range(5, 13):           # continuous low dorsal
        c.raw(x, 5, c.fin)
    for x in (5, 8, 11):             # camo blotches
        c.px(x, 8, c.dark)
        c.px(x + 1, 9, c.dark)
    c.tail_fan(3, 8, spread=2)
    c.eye(12, 7)


@sprite("alligator_gar")
def _(c):
    c.poly([(3, 8), (5, 7), (10, 7), (15, 8), (10, 9), (5, 9)])
    c.teeth([(12, 8), (14, 8)])      # toothy snout
    for x in (5, 7, 9):              # armored diamond scales
        c.raw(x, 7, c.light)
        c.raw(x + 1, 8, c.light)
    c.dorsal(4, 6, 7, 6)
    c.tail_fan(3, 8, spread=2)
    c.eye(11, 7)


@sprite("bogmaw")
def _(c):
    c.ellipse([4, 5, 13, 11])
    c.d.polygon([(11, 7), (15, 6), (15, 10), (11, 9)], fill=INK)  # gaping maw
    c.teeth([(12, 7), (14, 7), (13, 9)])
    c.d.line([(12, 10), (12, 13)], fill=c.fin)                    # algae drips
    c.d.line([(14, 10), (14, 12)], fill=c.fin)
    c.mottle("bog", 6, c.fin)
    c.tail_fan(4, 8, spread=2)
    c.eye(10, 5)


@sprite("elder_lungfish")
def _(c):
    c.snake([(3, 9), (5, 8), (7, 8), (9, 8), (11, 8), (13, 9)])
    for x in range(5, 12):           # long ribbon fins above and below
        c.raw(x, 6, c.fin)
        c.raw(x, 10, c.fin)
    c.dots([(6, 8), (9, 7), (11, 8)], c.light)                  # ancient pale spots
    c.d.line([(14, 10), (15, 11)], fill=c.dark)                 # whisker
    c.eye(13, 8, ring=c.light)


# ------------------------------- JUNGLE ------------------------------
@sprite("neon_tetra")
def _(c):
    c.ellipse([5, 7, 11, 9])
    c.hstripe(7, (110, 240, 255), pad=0)                        # electric blue stripe
    for x in (5, 6, 7):              # red rear half
        c.px(x, 8, c.fin)
        c.px(x, 9, c.fin)
    c.tail_fork(5, 8, c.fin)
    c.eye(10, 8)


@sprite("emerald_cichlid")
def _(c):
    c.ellipse([4, 5, 12, 11])
    for x in (6, 8, 10):
        c.vbar(x, c.fin)
    c.spines((6, 8, 10), 5, 2)
    c.tail_fan(4, 8, spread=2)
    c.eye(10, 7, ring=GOLD)


@sprite("piranha")
def _(c):
    c.ellipse([4, 5, 12, 11])
    c.belly(c.fin)                   # red throat and belly
    c.d.polygon([(10, 9), (13, 9), (12, 11)], fill=c.dark)      # underbite jaw
    c.teeth([(11, 9), (12, 9)])
    c.dorsal(6, 9, 5, 4)
    c.tail_fan(4, 8, spread=2)
    c.eye(10, 6)


@sprite("peacock_bass")
def _(c):
    c.ellipse([3, 5, 13, 11])
    c.belly(c.flight)
    for x in (6, 8, 10):
        c.vbar(x, c.dark)
    c.raw(4, 8, INK)                 # ringed eyespot on the tail base
    for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
        c.raw(4 + dx, 8 + dy, GOLD)
    c.spines((7, 9), 5, 2)
    c.tail_fan(3, 8)
    c.eye(11, 7)


@sprite("arapaima")
def _(c):
    c.poly([(3, 8), (5, 6), (11, 6), (14, 8), (11, 10), (5, 10)])
    for x, y in ((9, 7), (11, 8), (10, 9), (12, 8), (12, 9)):   # red-flecked rear scales
        c.px(x, y, c.fin)
    c.dorsal(4, 7, 6, 5)             # fins set far back near the tail
    c.d.polygon([(4, 10), (5, 12), (7, 10)], fill=c.fin)
    c.tail_fan(3, 8, spread=2)
    c.eye(12, 7)


@sprite("feathered_serpentfish")
def _(c):
    c.snake([(3, 10), (5, 9), (7, 8), (9, 7), (11, 7), (13, 8)])
    for x, y in ((13, 5), (12, 4), (14, 4)):                    # golden quetzal crest
        c.raw(x, y, c.fin)
    c.raw(13, 6, c.fin)
    for x, y in ((5, 7), (7, 6), (9, 5)):                       # plume ridge
        c.raw(x, y, c.fin)
    c.d.line([(3, 10), (1, 8)], fill=c.fin)                     # plumed tail
    c.d.line([(3, 11), (1, 12)], fill=c.fin)
    c.eye(12, 7, ring=RED)


# ------------------------------- DESERT ------------------------------
@sprite("desert_pupfish")
def _(c):
    c.ellipse([5, 7, 11, 10])
    c.vbar(7, c.fin)
    c.vbar(9, c.fin)
    c.tail_fan(5, 8, spread=2)
    c.eye(9, 8)


@sprite("sandskimmer")
def _(c):
    c.ellipse([3, 8, 13, 11])
    c.d.polygon([(7, 8), (9, 4), (11, 8)], fill=c.fin)          # wing-like pectoral
    c.mottle("skimmer", 6, c.fin)
    c.tail_fan(3, 9, spread=2)
    c.eye(11, 8)


@sprite("nile_perch")
def _(c):
    c.ellipse([3, 5, 13, 10])
    c.back(shade(c.body, 0.8))
    c.belly()
    c.d.line([(12, 9), (14, 9)], fill=c.dark)                   # big mouth
    c.spines((6, 8), 5, 2)
    c.tail_fan(3, 7)
    c.eye(11, 6, ring=GOLD)


@sprite("tigerfish")
def _(c):
    c.poly([(3, 8), (5, 6), (12, 6), (14, 8), (12, 10), (5, 10)])
    c.hstripe(7, c.fin)
    c.hstripe(9, c.fin)
    c.teeth([(13, 7), (14, 8), (13, 9)])                        # notorious fangs
    c.tail_fork(3, 8, RED)
    c.eye(11, 7)


@sprite("mirage_eel")
def _(c):
    c.snake([(3, 10), (5, 9), (7, 9), (9, 8), (11, 8), (13, 9)])
    for x in (5, 8, 11):             # heat-shimmer dashes
        c.raw(x, 6, c.fin)
        c.px(x, 8, c.light)
    for x in range(5, 12, 2):        # thin ribbon fin
        c.raw(x, 7, c.fin)
    c.eye(13, 8)


@sprite("pharaohs_goldscale")
def _(c):
    c.ellipse([3, 5, 13, 11])
    c.vbar(11, c.fin)                # lapis nemes headband
    c.vbar(5, c.fin)                 # lapis banding toward the tail
    c.vbar(7, c.fin)
    c.raw(12, 3, GOLD)               # crown spike
    c.raw(12, 4, GOLD)
    c.raw(11, 9, c.fin)              # kohl line under the eye
    c.tail_fan(3, 8, c.fin)
    c.eye(11, 7, ring=GOLD)


# ------------------------------ MOUNTAIN -----------------------------
@sprite("stone_loach")
def _(c):
    c.ellipse([3, 8, 13, 11])
    c.mottle("loach", 10, c.dark)
    c.mottle("loach2", 5, c.light)
    c.barbels([((13, 9), (15, 9)), ((13, 10), (15, 11))])
    c.tail_fan(3, 9, spread=2)
    c.eye(11, 9)


@sprite("alpine_dace")
def _(c):
    c.ellipse([3, 6, 12, 9])
    c.hstripe(6, shade(c.body, 0.8))
    c.belly()
    c.dorsal(6, 9, 6, 5)
    c.tail_fork(3, 7)
    c.eye(10, 7)


@sprite("golden_trout")
def _(c):
    c.ellipse([3, 6, 13, 10])
    c.hstripe(8, RED)                # crimson lateral band
    for x in (5, 7, 9, 11):          # parr marks
        c.px(x, 7, c.dark)
    c.dots([(6, 6), (10, 6)], c.dark)
    c.tail_fan(3, 8, spread=2)
    c.eye(11, 7)


@sprite("cutthroat_trout")
def _(c):
    c.ellipse([3, 6, 13, 10])
    c.belly()
    c.raw(12, 9, c.fin)              # signature red throat slash
    c.raw(13, 9, c.fin)
    c.dots([(5, 7), (7, 6), (9, 7), (11, 6)], c.dark)
    c.tail_fan(3, 8, spread=2)
    c.eye(11, 7)


@sprite("thunderfin")
def _(c):
    c.ellipse([4, 5, 12, 10])
    for x, y in ((6, 6), (7, 7), (8, 6), (9, 7), (10, 6)):      # lightning flank
        c.px(x, y, c.fin)
    c.spines((6, 8, 10), 5, 3)       # jagged storm dorsal
    c.tail_crescent(4, 8)
    c.eye(10, 7, ring=c.fin)


@sprite("skyplume_koi")
def _(c):
    c.ellipse([3, 6, 12, 10])
    for x, y in ((6, 7), (7, 7), (7, 8), (10, 7), (11, 8)):     # red koi patches
        c.px(x, y, c.fin)
    c.d.line([(3, 8), (0, 5)], fill=c.flight)                   # flowing plume tail
    c.d.line([(3, 8), (0, 8)], fill=c.fin)
    c.d.line([(3, 9), (0, 12)], fill=c.flight)
    c.raw(13, 9, c.dark)             # whisker
    c.eye(11, 7)


# ------------------------------ MUSHROOM -----------------------------
@sprite("sporegill")
def _(c):
    c.ellipse([4, 6, 12, 10])
    c.dots([(6, 7), (8, 8), (10, 7)], c.flight)                 # spore speckles
    for x, y in ((5, 4), (9, 3), (13, 5)):                      # drifting spores
        c.raw(x, y, c.fin)
    for x in (5, 7, 9, 11):          # gill frills along the belly
        c.raw(x, 11, c.fin)
    c.tail_fan(4, 8, spread=2)
    c.eye(10, 7)


@sprite("shroomfin")
def _(c):
    c.ellipse([4, 6, 12, 11])
    c.belly(c.fin)                   # cream stem-belly
    c.d.polygon([(5, 6), (7, 3), (10, 3), (12, 6)], fill=c.body)  # mushroom cap dorsal
    c.raw(7, 4, WHITE)               # cap spots
    c.raw(9, 5, WHITE)
    c.raw(6, 5, WHITE)
    c.tail_fan(4, 9, c.fin, spread=2)
    c.eye(10, 8)


@sprite("mycelial_ancient")
def _(c):
    c.ellipse([3, 5, 13, 11])
    for x in range(5, 12, 2):        # mycelium web threads
        c.px(x, 7, c.fin)
        c.px(x + 1, 9, c.fin)
    c.hstripe(8, shade(c.fin, 0.85))
    for x, y in ((4, 3), (8, 2), (12, 3)):                      # drifting spores
        c.raw(x, y, c.fin)
    c.tail_fan(3, 8, spread=2)
    c.eye(11, 7, ring=(140, 255, 220))                          # eldritch glow eye


# ------------------------------- CAVES -------------------------------
@sprite("glowtail")
def _(c):
    c.ellipse([3, 6, 12, 10])
    for x, glow in ((3, 1.0), (4, 0.8), (5, 0.55)):             # bioluminescent tail fade
        col = tuple(int(a + (b - a) * glow) for a, b in zip(c.body, c.fin))
        c.vbar(x, col, top=-1, bottom=-1)
    c.tail_fan(3, 8)
    c.raw(1, 7, c.fin)               # drifting glow motes
    c.raw(2, 10, c.fin)
    c.eye(10, 7)


@sprite("cave_angler")
def _(c):
    c.ellipse([4, 5, 12, 11])
    c.d.line([(10, 4), (10, 2)], fill=c.dark)                   # illicium stalk
    c.raw(11, 2, c.fin)              # glowing esca
    c.raw(12, 2, WHITE)
    c.d.line([(9, 9), (13, 9)], fill=INK)                       # huge mouth
    c.teeth([(10, 8), (12, 8), (11, 10)])
    c.tail_fan(4, 8, spread=2)
    c.eye(9, 6)


@sprite("crystal_lanternfish")
def _(c):
    c.ellipse([3, 5, 13, 11])
    for x, y in ((6, 6), (8, 7), (10, 6), (7, 9)):              # crystal facets
        c.px(x, y, WHITE)
    for x in (5, 7, 9, 11):          # photophore row along the belly
        c.px(x, 10, c.fin)
    c.spines((6, 8, 10), 5, 2, WHITE)                           # crystalline dorsal
    c.tail_fan(3, 8, spread=2)
    c.eye(11, 7, ring=c.fin)


def draw_species(sid, body_hex, fin_hex):
    c = Sprite(body_hex, fin_hex)
    SPRITES[sid](c)
    return c.img


# Rod sprites: the base is a faithful vanilla-fishing-rod silhouette (diagonal
# wooden pole, wrapped grip, line off the tip) and each tier layers themed
# detailing on top of it.

ROD_WOOD = (125, 82, 40)
ROD_WOOD_D = (86, 55, 26)
ROD_GRIP = (66, 44, 24)
ROD_LINE = (216, 216, 220)


def pole_xy(i):
    """Pole runs from the handle at (1,14) to the tip at (11,4)."""
    return 1 + i, 14 - i


def draw_rod_base(d, cast):
    for i in range(11):
        x, y = pole_xy(i)
        d.point((x, y), fill=ROD_WOOD)
        d.point((x + 1, y), fill=ROD_WOOD_D)
    for i in range(3):  # wrapped grip at the handle
        x, y = pole_xy(i)
        d.point((x, y), fill=ROD_GRIP)
        d.point((x + 1, y), fill=shade(ROD_GRIP, 0.7))
    if cast:
        d.point((13, 4), fill=ROD_LINE)
        for y in range(5, 12):
            d.point((14, y), fill=ROD_LINE)
        d.point((13, 12), fill=(240, 240, 240))  # bobber
        d.point((14, 12), fill=(214, 64, 54))
        d.point((13, 13), fill=(214, 64, 54))
        d.point((14, 13), fill=(150, 40, 34))
    else:
        d.point((13, 4), fill=ROD_LINE)
        d.point((14, 5), fill=ROD_LINE)
        d.point((14, 6), fill=ROD_LINE)
        d.point((13, 7), fill=ROD_LINE)


def rod_reinforced(d):
    """Iron-banded workhorse: riveted steel bands and a capped tip."""
    silver = (168, 174, 186)
    silver_l = (218, 224, 234)
    for i in (3, 4, 7):
        x, y = pole_xy(i)
        d.point((x, y), fill=silver)
        d.point((x + 1, y), fill=shade(silver, 0.65))
    rx, ry = pole_xy(4)
    d.point((rx, ry - 1), fill=silver_l)  # rivet glint
    for i in (9, 10):  # steel tip cap
        x, y = pole_xy(i)
        d.point((x, y), fill=silver)
        d.point((x + 1, y), fill=shade(silver, 0.65))
    d.point((11, 3), fill=silver_l)


def rod_prismatic(d):
    """Prismarine crystal rod: gem studs and a glowing crystal tip."""
    teal = (86, 224, 207)
    teal_d = (44, 150, 140)
    glow = (208, 255, 246)
    for i in (3, 6):
        x, y = pole_xy(i)
        d.point((x, y), fill=teal)
        d.point((x + 1, y), fill=teal_d)
    # crystal tip
    d.point((11, 4), fill=teal)
    d.point((12, 4), fill=teal_d)
    d.point((11, 3), fill=glow)
    d.point((12, 3), fill=teal)
    d.point((12, 2), fill=glow)
    d.point((10, 3), fill=teal_d)


def rod_poseidons(d):
    """Sea-god rod: golden trident head and a heart-of-the-sea orb."""
    gold = (240, 200, 72)
    gold_d = (172, 130, 32)
    sea = (44, 122, 202)
    sea_l = (128, 204, 244)
    for i in (3, 4):
        x, y = pole_xy(i)
        d.point((x, y), fill=gold)
        d.point((x + 1, y), fill=gold_d)
    # heart of the sea below the head
    d.point((9, 5), fill=sea)
    d.point((10, 5), fill=sea_l)
    d.point((9, 6), fill=shade(sea, 0.7))
    d.point((10, 6), fill=sea)
    # golden trident head
    for x in (10, 11, 12, 13, 14):
        d.point((x, 3), fill=gold_d)   # crossbar
    for x in (10, 12, 14):
        d.point((x, 2), fill=gold)     # prongs
    d.point((12, 1), fill=gold)        # center prong reaches highest
    d.point((11, 4), fill=gold)
    d.point((12, 4), fill=gold_d)


def rod_celestial(d):
    """Heaven-forged rod: ender-violet windings and a radiant star tip."""
    violet = (155, 89, 208)
    violet_d = (98, 52, 140)
    star_w = (255, 255, 255)
    star_g = (255, 224, 120)
    for i in (3, 4, 7):
        x, y = pole_xy(i)
        d.point((x, y), fill=violet)
        d.point((x + 1, y), fill=violet_d)
    # four-pointed star on the tip
    cx, cy = 12, 3
    d.point((cx, cy), fill=star_w)
    d.point((cx - 1, cy), fill=star_g)
    d.point((cx + 1, cy), fill=star_g)
    d.point((cx, cy - 1), fill=star_g)
    d.point((cx, cy + 1), fill=star_g)
    d.point((cx - 1, cy - 1), fill=violet)
    d.point((cx + 1, cy + 1), fill=violet_d)
    # drifting stardust
    d.point((9, 1), fill=star_w)
    d.point((15, 2), fill=star_g)
    d.point((7, 4), fill=violet)


ROD_SPRITES = {
    "reinforced_rod": rod_reinforced,
    "prismatic_rod": rod_prismatic,
    "poseidons_rod": rod_poseidons,
    "celestial_rod": rod_celestial,
}


def draw_rod(rid, cast=False):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    draw_rod_base(d, cast)
    ROD_SPRITES[rid](d)
    return img


def draw_bait(hex_color):
    c = hex_to_rgb(hex_color)
    dark = shade(c, 0.6)
    light = shade(c, 1.35)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([4, 7, 9, 12], fill=c, outline=dark)
    d.ellipse([8, 4, 12, 8], fill=c, outline=dark)
    d.ellipse([7, 9, 12, 13], fill=dark)
    d.point((6, 8), fill=light)
    d.point((10, 5), fill=light)
    return img


def draw_aquarium_frame():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    frame = (94, 134, 150, 255)
    frame_l = (168, 214, 228, 255)
    rivet = (58, 86, 100, 255)
    d.rectangle([0, 0, 15, 15], fill=(205, 232, 242, 36))   # faint glass
    d.rectangle([0, 0, 15, 15], outline=frame)
    d.rectangle([1, 1, 14, 14], outline=frame_l)
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        d.point((x, y), fill=rivet)
    d.line([(3, 6), (6, 3)], fill=(255, 255, 255, 90))       # glass glint
    d.line([(4, 8), (8, 4)], fill=(255, 255, 255, 55))
    return img


def draw_aquarium_water():
    # one flat translucent blue — no highlights, no speckles, just water
    return Image.new("RGBA", (16, 16), (47, 118, 190, 112))


def draw_aquarium_edge():
    # deliberately a single flat color: edge beams overlap at the block corners,
    # and identical coplanar texels make that overlap invisible
    return Image.new("RGBA", (16, 16), (94, 134, 150, 255))


def draw_trophy_block():
    wood = hex_to_rgb("#8b6b43")
    dark = shade(wood, 0.6)
    light = shade(wood, 1.25)
    img = Image.new("RGBA", (16, 16), wood + (255,))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=dark + (255,))
    d.rectangle([1, 1, 14, 14], outline=light + (255,))
    for y in (4, 8, 12):
        for x in range(3, 13, 2):
            d.point((x, y), fill=shade(wood, 0.85) + (255,))
    return img


def draw_trophy_item():
    img = draw_trophy_block()
    d = ImageDraw.Draw(img)
    gold = (232, 195, 85, 255)
    for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        d.point((x, y), fill=gold)
    return img


def draw_gui_fish():
    body = (26, 42, 58)
    light = (58, 86, 110)
    eye = (240, 240, 245)
    img = Image.new("RGBA", (14, 14), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([2, 4, 10, 10], fill=body, outline=light)
    d.polygon([(2, 7), (0, 4), (0, 10)], fill=body)
    d.polygon([(4, 4), (7, 1), (9, 4)], fill=light)
    d.point((8, 6), fill=eye)
    return img


def draw_gui_treasure():
    wood = (138, 96, 44)
    dark = (92, 62, 26)
    gold = (242, 199, 68)
    img = Image.new("RGBA", (14, 14), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([1, 4, 12, 12], fill=wood, outline=dark)
    d.rectangle([1, 2, 12, 5], fill=dark)
    d.rectangle([6, 5, 8, 8], fill=gold)
    d.point((7, 7), fill=dark)
    return img


def draw_fish_encyclopedia():
    cover = (58, 42, 84)
    cover_d = (38, 27, 58)
    page = (232, 218, 182)
    gold = (232, 195, 85)
    ink = (90, 70, 40)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 1, 13, 14], fill=cover, outline=cover_d)
    d.rectangle([4, 3, 12, 13], fill=page)
    d.line([(2, 1), (2, 14)], fill=gold)
    d.line([(3, 1), (3, 14)], fill=cover_d)
    # a tiny fish glyph stamped on the open page (body + tail, facing right)
    d.ellipse([6, 7, 10, 9], fill=ink)
    d.polygon([(6, 8), (4, 6), (4, 10)], fill=ink)
    d.point((9, 8), fill=gold)
    return img


def gen_silhouettes():
    """Pre-baked dark silhouettes of every species' final texture, normalized to
    32x32 so the encyclopedia screen can draw them all at one fixed size. Built
    from whatever is on disk after gen_textures() runs, so it works for both
    hand-made and procedurally generated art."""
    tex_item = f"{ASSETS}/textures/item"
    sil_dir = f"{ASSETS}/textures/gui/silhouette"
    os.makedirs(sil_dir, exist_ok=True)
    ink = (16, 18, 24)
    for sid, *_ in SPECIES:
        img = Image.open(f"{tex_item}/{sid}.png").convert("RGBA")
        if img.size != (32, 32):
            img = img.resize((32, 32), Image.NEAREST)
        px = img.load()
        out = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        opx = out.load()
        for y in range(32):
            for x in range(32):
                a = px[x, y][3]
                if a > 20:
                    opx[x, y] = (ink[0], ink[1], ink[2], min(255, int(a * 0.92)))
        out.save(f"{sil_dir}/{sid}.png")


# Species whose textures are hand-made 32x32 art checked into the repo.
# gen_textures never regenerates these — only the remaining species fall back
# to the procedural sprite engine above.
CUSTOM_ART = {
    "alligator_gar", "alpine_dace", "arapaima", "arctic_char", "arctic_cod",
    "bluefin_tuna", "bogmaw", "brown_trout", "bullhead_catfish", "butterflyfish",
    "cave_angler", "crystal_lanternfish", "cutthroat_trout", "desert_pupfish",
    "elder_lungfish", "emerald_cichlid", "feathered_serpentfish", "frostjaw_pike",
    "glacier_wraithfin", "glowtail", "golden_sturgeon", "golden_trout",
    "greenland_halibut", "herring", "icefin", "leviathan_ray", "lionfish",
    "mackerel", "mahi_mahi", "mirage_eel", "mudskipper", "mycelial_ancient",
    "neon_tetra", "nile_perch", "parrotfish", "peacock_bass", "pharaohs_goldscale",
    "piranha", "river_king_salmon", "river_perch", "sailfish", "sandskimmer",
    "sea_bass", "shroomfin", "skyplume_koi", "snakehead", "sporegill",
    "stone_loach", "sunken_emperor", "swordfish", "thunderfin", "tigerfish",
    "zander",
}


def gen_textures():
    tex_item = f"{ASSETS}/textures/item"
    tex_block = f"{ASSETS}/textures/block"
    for sid, _n, _g, _r, _a, _b, _c, _d2, body, fin in SPECIES:
        if sid in CUSTOM_ART:
            continue
        draw_species(sid, body, fin).save(f"{tex_item}/{sid}.png")
    for rid, _n in RODS:
        draw_rod(rid, cast=False).save(f"{tex_item}/{rid}.png")
        draw_rod(rid, cast=True).save(f"{tex_item}/{rid}_cast.png")
    for bid, _n, c in BAITS:
        draw_bait(c).save(f"{tex_item}/{bid}.png")
    draw_trophy_block().save(f"{tex_block}/trophy_stand.png")
    draw_aquarium_frame().save(f"{tex_block}/aquarium_frame.png")
    draw_aquarium_water().save(f"{tex_block}/aquarium_water.png")
    draw_aquarium_edge().save(f"{tex_block}/aquarium_edge.png")
    gui_dir = f"{ASSETS}/textures/gui"
    os.makedirs(gui_dir, exist_ok=True)
    draw_gui_fish().save(f"{gui_dir}/fish_icon.png")
    draw_gui_treasure().save(f"{gui_dir}/treasure_icon.png")
    draw_trophy_item().save(f"{tex_item}/trophy_stand.png")
    draw_fish_encyclopedia().save(f"{tex_item}/fish_encyclopedia.png")
    # mod icon: an upscaled legendary fish
    icon = draw_species("pharaohs_goldscale", "#e8b83a", "#2e6ba8").resize((128, 128), Image.NEAREST)
    icon.save(f"{ASSETS}/icon.png")
    gen_silhouettes()


if __name__ == "__main__":
    import sys
    missing = [sid for sid, *_ in SPECIES if sid not in SPRITES]
    if missing:
        raise SystemExit(f"species without a sprite design: {missing}")
    if "--textures" not in sys.argv:
        gen_registry()
        gen_lang()
        gen_models()
        gen_data()
    gen_textures()
    print("species:", len(SPECIES))
    print("assets generated OK")
