# Angler's Dream — a fishing overhaul for Minecraft (Fabric, 1.21.1)

54 biome-specific fish species with rarities, special variants, realistic sizes & weights,
a Stardew Valley-style reel-in minigame, wall-mounted trophy stands, tiered fishing rods,
and craftable baits.

## Features

**Fish (54 species)**
- Every catch is generated for the biome you're fishing in: temperate oceans, warm oceans,
  frozen biomes, rivers/plains, swamps, jungles, deserts, mountains, mushroom fields, and caves.
- Five rarity tiers per biome group: Common → Uncommon → Rare → Epic → ★ Legendary
  (e.g. Leviathan Ray, Pharaoh's Goldscale, Skyplume Koi, Feathered Serpentfish).
- Each fish rolls an individual **length (cm)** and **weight (kg)** shown in its tooltip.
  The distribution is heavily skewed, so large, heavy specimens are genuinely rare.
- **Trophy-class giants:** a rare roll (~2%, slightly more with size gear) overshoots the
  species' normal maximum — up to 2.5x length and far heavier.
- **Variants:** every species can roll Shiny (1.2%), Golden (0.3%), or Prismatic (0.06%).
  Gear multiplies these odds with square-root diminishing returns, so even a fully
  stacked setup only ~5x's them — a prismatic catch stays rare (~1 in 330 at best).
  Variant fish glint and get a colored name prefix; Golden and Prismatic also emit
  ambient particles (prismatic = rainbow dust) as drops and on trophy stands.

**Reel-In Minigame (Stardew Valley style)**
- When a fish bites and you reel in, a minigame opens: hold **left click** (or **space**)
  to lift the green catch bar, release to let it fall. Keep the fish inside the bar to
  fill the progress column; let it escape and progress drains. Fill it to land the catch.
- Junk and treasure still come up instantly — only real fish put up a fight.
- Difficulty scales with rarity, size and variant; species have movement personalities by
  biome (jungle/cave fish *dart*, swamp fish *sink*, mountain fish *float*, ocean fish are
  *smooth*, legendaries always dart). Better rods give you a larger catch bar.
- **Treasure chests** sometimes appear mid-fight — hold the bar on one to collect a roll
  of the vanilla treasure table on top of your fish.
- **Perfect catch:** never lose the fish after first contact and it lands ~10% longer and
  ~15% heavier, with an action-bar fanfare.
- Press ESC (or fail) and the fish gets away — bait is still spent, as nature intended.

**Trophy Stand**
- Craftable wall medallion (8 sticks around any plank). Right-click with a fish to mount it,
  right-click again to take it back; breaking the stand drops the fish.
- The medallion itself is small — the mounted fish covers it. Fish render at their
  **true size**: a 30 cm perch is a modest wall piece, a giant Leviathan Ray dominates it.
- Look at a mounted fish and its full stats (name, rarity, variant, length, weight)
  appear under the crosshair.

**Rods** (all enchantable, work with Lure / Luck of the Sea; every recipe starts
from a plain vanilla fishing rod plus rarer ingredients as the tiers climb)
- Reinforced Rod — fishing rod + 2 iron ingots (+1 luck, +15% size, 128 durability)
- Prismatic Rod — fishing rod + 2 prismarine crystals + diamond (+2 luck, +35% size, ×1.5 variants, 384 durability)
- Poseidon's Rod — fishing rod + heart of the sea + nautilus shell + diamond (+3 luck, +60% size, ×2.5 variants, 1024 durability)
- Celestial Rod — fishing rod + nether star + dragon's breath + echo shard (+4 luck, +90% size, ×4 variants, 2048 durability)

**Baits** (consumed automatically on each catch; offhand is checked first. Worm
Bait is the base ingredient for every other bait, so each recipe needs more of
it as the tier climbs. Each specializes in what it boosts rather than
improving everything at once.)
- Worm Bait — 1 rotten flesh (weak all-rounder: +1 luck, +8% size)
- Chum Bait — worm bait + pufferfish + bone meal (**size only**: +55% size)
- Lucky Bait — 2 worm bait + rabbit's foot (**luck only**: +4 luck)
- Glimmer Bait — 2 worm bait + glow ink sac + phantom membrane (**variant only**: ×3 variants)
- Royal Bait — 3 worm bait + 2 gold nuggets (luck + size, no variant: +3 luck, +35% size)
- Prismatic Lure — 4 worm bait + diamond + obsidian + gold ingot (the best bait overall: +5 luck, +75% size, ×4 variants)

**Fish Encyclopedia**
- Craftable catch log: book + fishing rod + ink sac. Right-click to open it.
- One themed page per biome group, colored and bordered to match that biome's water
  (icy pale blue for Frozen, deep magenta for Mushroom Fields, glowing purple for Caves...).
  Undiscovered species show as a dark silhouette of their real shape plus their rarity;
  catching one fills in its true icon and name for good.
- Each page tracks total fish caught in that biome, not just species discovered.
- Catch every species in a biome and its page border turns gold.
- The log lives on the book itself (a data component, like a written book's pages) and
  updates automatically for any copy of the encyclopedia in your inventory when you land
  a catch — it doesn't need to be in your hand.

Vanilla junk/treasure fishing loot (enchanted books, saddles, etc.) is preserved.
Epic+, Legendary and Prismatic catches trigger a level-up chime and an action-bar announcement.

## Building

Requires **JDK 21** and **Gradle 8.8+** (8.10 recommended).

```bash
cd anglers-dream
gradle build        # or: gradle wrapper --gradle-version 8.10 && ./gradlew build
```

The jar lands in `build/libs/`. At runtime you need **Fabric Loader 0.15+** and **Fabric API**
for Minecraft **1.21.1**.

> Note: this project was authored against the 1.21.1 yarn mappings without access to the
> Fabric maven, so a stray method name may need a small touch-up — `gradle build` will
> point straight at it. The sources pass a full javac syntax check.
> The trophy renderer's rotation/offset constants (in `TrophyBlockEntityRenderer`) are the
> most likely place to want a visual tweak.

## Tuning

- Fish stats live in a custom **data component** (`anglersdream:fish_data`) per 1.20.5+
  conventions — see `fish/FishData.java`.
- Species list, sizes, weights: `fish/FishRegistry.java` (one line per species).
- Rarity weights & colors: `fish/Rarity.java`.
- Variant chances & particles: `fish/Variant.java`.
- Size skew, junk/treasure rates, luck math: `fish/FishingLogic.java`.
- Rod/bait stats: constants at the top of `AnglersDream.java`.
- Minigame feel (bar physics, fish speed, progress rates, treasure odds): client tuning in
  `client/FishingMinigameScreen.java`, difficulty/behavior/treasure rolls in
  `minigame/MinigameServer.java`.
- All textures are generated placeholders (see `gen_assets.py` mentality) — drop your own
  16×16 PNGs into `assets/anglersdream/textures/item/` to reskin any species.

## Project layout

```
src/main/java/com/anglersdream/
  AnglersDream.java            registration & creative tab
  fish/                        species data, biome mapping, catch generation
  item/                        FishItem, TieredRodItem, BaitItem
  block/                       TrophyBlock + block entity
  client/                      trophy renderer, rod cast predicates
  mixin/                       fishing bobber override, item-entity particles
  network/                     minigame start (S2C) / result (C2S) payloads
  minigame/                    server-side pending-catch manager & loot delivery
src/main/resources/            models, textures, lang, recipes, loot tables
```
