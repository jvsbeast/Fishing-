#!/usr/bin/env python3
"""Generates FishRegistry.java, lang file, item models, recipes and pixel-art textures
for the Angler's Dream Fabric mod."""
import json, os, random
from PIL import Image, ImageDraw

ROOT = "/home/claude/anglers-dream"
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

RODS = [("reinforced_rod", "Reinforced Rod", "#c8c8d0"),
        ("prismatic_rod", "Prismatic Rod", "#5fd4c4"),
        ("poseidons_rod", "Poseidon's Rod", "#4a90e8")]
BAITS = [("worm_bait", "Worm Bait", "#c98a7a"),
         ("glow_bait", "Glow Bait", "#8ff2c4"),
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
    }
    for sid, name, *_ in SPECIES:
        lang[f"item.anglersdream.{sid}"] = name
    for rid, name, _ in RODS:
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
    for rid, _, _ in RODS:
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

    # trophy stand block model (thin plaque against the wall, base = facing north)
    block_model = {
        "textures": {
            "particle": "anglersdream:block/trophy_stand",
            "plaque": "anglersdream:block/trophy_stand",
        },
        "elements": [{
            "from": [1, 1, 15],
            "to": [15, 15, 16],
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
    w("prismatic_rod", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "anglersdream:reinforced_rod"},
                        {"item": "minecraft:prismarine_crystals"},
                        {"item": "minecraft:prismarine_crystals"},
                        {"item": "minecraft:prismarine_shard"}],
        "result": {"id": "anglersdream:prismatic_rod"},
    })
    w("poseidons_rod", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "anglersdream:prismatic_rod"},
                        {"item": "minecraft:heart_of_the_sea"},
                        {"item": "minecraft:nautilus_shell"},
                        {"item": "minecraft:diamond"}],
        "result": {"id": "anglersdream:poseidons_rod"},
    })
    w("worm_bait", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "minecraft:dirt"}, {"item": "minecraft:wheat_seeds"}],
        "result": {"id": "anglersdream:worm_bait", "count": 3},
    })
    w("glow_bait", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "anglersdream:worm_bait"}, {"item": "minecraft:glow_ink_sac"}],
        "result": {"id": "anglersdream:glow_bait", "count": 2},
    })
    w("royal_bait", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "anglersdream:worm_bait"},
                        {"item": "minecraft:gold_nugget"},
                        {"item": "minecraft:gold_nugget"}],
        "result": {"id": "anglersdream:royal_bait", "count": 2},
    })
    w("prismatic_lure", {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "anglersdream:glow_bait"},
                        {"item": "minecraft:amethyst_shard"},
                        {"item": "minecraft:prismarine_crystals"}],
        "result": {"id": "anglersdream:prismatic_lure", "count": 2},
    })

    with open(f"{DATA}/loot_table/blocks/trophy_stand.json", "w") as f:
        json.dump({
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "entries": [{"type": "minecraft:item", "name": "anglersdream:trophy_stand"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
        }, f, indent=2)


# ---------------------------------------------------------------- textures
def draw_fish(body_hex, fin_hex, seed):
    rng = random.Random(seed)
    body = hex_to_rgb(body_hex)
    fin = hex_to_rgb(fin_hex)
    dark = shade(body, 0.55)
    belly = shade(body, 1.35)

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    x0 = rng.choice([4, 5])
    x1 = rng.choice([12, 13])
    y0 = rng.choice([5, 6])
    y1 = rng.choice([10, 11])

    # body
    d.ellipse([x0, y0, x1, y1], fill=body, outline=dark)
    # belly highlight
    d.ellipse([x0 + 2, (y0 + y1) // 2, x1 - 2, y1 - 1], fill=belly)
    # tail
    d.polygon([(x0, (y0 + y1) // 2), (x0 - 3, y0 - 1), (x0 - 3, y1 + 1)], fill=fin, outline=shade(fin, 0.6))
    # dorsal fin
    d.polygon([(x0 + 3, y0), ((x0 + x1) // 2, y0 - 3), (x1 - 3, y0)], fill=fin)
    # pectoral fin
    d.polygon([((x0 + x1) // 2, y1 - 1), ((x0 + x1) // 2 + 2, y1 + 2), ((x0 + x1) // 2 - 1, y1 + 1)], fill=fin)
    # eye
    ex = x1 - 3
    ey = y0 + 2
    d.point((ex, ey), fill=(255, 255, 255, 255))
    d.point((ex + 1, ey), fill=(20, 20, 25, 255))
    # a couple of scale flecks
    for _ in range(3):
        sx = rng.randint(x0 + 2, x1 - 4)
        sy = rng.randint(y0 + 1, y1 - 2)
        d.point((sx, sy), fill=dark)
    return img


def draw_rod(tip_hex, cast=False):
    tip = hex_to_rgb(tip_hex)
    wood = (117, 76, 36, 255)
    wood_d = (84, 53, 24, 255)
    line = (200, 200, 205, 255)

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # diagonal rod from bottom-left to top-right
    for i in range(12):
        x = 2 + i
        y = 13 - i
        d.point((x, y), fill=wood)
        d.point((x, y + 1), fill=wood_d)
    # colored tip section
    for i in range(9, 12):
        d.point((2 + i, 13 - i), fill=tip + (255,))
    # reel
    d.rectangle([3, 11, 5, 13], fill=(60, 60, 65, 255))
    # fishing line
    if cast:
        for y in range(2, 12):
            d.point((14, y), fill=line)
        d.point((14, 12), fill=(220, 60, 60, 255))  # bobber
        d.point((13, 12), fill=(240, 240, 240, 255))
    else:
        d.point((13, 3), fill=line)
        d.point((14, 4), fill=line)
        d.point((14, 5), fill=line)
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


def gen_textures():
    tex_item = f"{ASSETS}/textures/item"
    tex_block = f"{ASSETS}/textures/block"
    for i, (sid, _n, _g, _r, _a, _b, _c, _d2, body, fin) in enumerate(SPECIES):
        draw_fish(body, fin, seed=i * 7919 + 13).save(f"{tex_item}/{sid}.png")
    for rid, _n, tip in RODS:
        draw_rod(tip, cast=False).save(f"{tex_item}/{rid}.png")
        draw_rod(tip, cast=True).save(f"{tex_item}/{rid}_cast.png")
    for bid, _n, c in BAITS:
        draw_bait(c).save(f"{tex_item}/{bid}.png")
    draw_trophy_block().save(f"{tex_block}/trophy_stand.png")
    gui_dir = f"{ASSETS}/textures/gui"
    os.makedirs(gui_dir, exist_ok=True)
    draw_gui_fish().save(f"{gui_dir}/fish_icon.png")
    draw_gui_treasure().save(f"{gui_dir}/treasure_icon.png")
    draw_trophy_item().save(f"{tex_item}/trophy_stand.png")
    # mod icon: an upscaled legendary fish
    icon = draw_fish("#e8b83a", "#2e6ba8", seed=42).resize((128, 128), Image.NEAREST)
    icon.save(f"{ASSETS}/icon.png")


if __name__ == "__main__":
    gen_registry()
    gen_lang()
    gen_models()
    gen_data()
    gen_textures()
    print("species:", len(SPECIES))
    print("assets generated OK")
