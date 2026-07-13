package com.anglersdream.minigame;

import com.anglersdream.AnglersDream;
import com.anglersdream.fish.FishData;
import com.anglersdream.fish.FishSpecies;
import com.anglersdream.fish.FishingLogic;
import com.anglersdream.fish.Rarity;
import com.anglersdream.fish.Variant;
import com.anglersdream.item.FishItem;
import com.anglersdream.item.TieredRodItem;
import com.anglersdream.network.MinigameResultPayload;
import com.anglersdream.network.StartMinigamePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server side of the Stardew-style reel-in minigame. Holds the pre-rolled catch
 * while the client plays, then delivers (or discards) it based on the result.
 */
public final class MinigameServer {

    private record Pending(ItemStack fish, ItemStack rod, BlockPos pos, long gameTime) {}

    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final long TIMEOUT_TICKS = 1200; // 60s safety window

    private MinigameServer() {}

    /** Called from the bobber mixin when a custom fish is on the line. */
    public static void begin(ServerPlayerEntity player, ServerWorld world, BlockPos pos,
                             ItemStack rod, ItemStack fish) {
        FishSpecies species = ((FishItem) fish.getItem()).species;
        FishData data = fish.get(AnglersDream.FISH_DATA);

        float sizeFrac = 0.5f;
        Variant variant = Variant.NORMAL;
        if (data != null) {
            sizeFrac = MathHelper.clamp(
                    (data.sizeCm() - species.minSize()) / Math.max(1.0f, species.maxSize() - species.minSize()),
                    0.0f, 1.0f);
            variant = data.variant();
        }
        int variantBonus = switch (variant) {
            case SHINY -> 5;
            case GOLDEN -> 10;
            case PRISMATIC -> 20;
            default -> 0;
        };
        int difficulty = MathHelper.clamp(
                12 + species.rarity().ordinal() * 14 + Math.round(sizeFrac * 22) + variantBonus, 10, 95);

        int behavior = switch (species.group()) {
            case JUNGLE, CAVES -> 2;              // dart
            case SWAMP -> 3;                      // sinker
            case MOUNTAIN -> 4;                   // floater
            case OCEAN, WARM_OCEAN, FROZEN -> 1;  // smooth
            default -> 0;                         // mixer
        };
        if (species.rarity() == Rarity.LEGENDARY) behavior = 2; // legendaries always dart

        int rodTier = rod.getItem() instanceof TieredRodItem tiered ? tiered.rarityLuck : 0;
        boolean treasure = world.getRandom().nextDouble() < 0.12 + 0.04 * rodTier;

        PENDING.put(player.getUuid(), new Pending(fish, rod.copy(), pos, world.getTime()));
        ServerPlayNetworking.send(player, new StartMinigamePayload(difficulty, behavior, rodTier, treasure));
        world.playSound(null, pos, SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, SoundCategory.NEUTRAL, 0.6f, 1.0f);
    }

    /** Called from the C2S packet receiver (already on the server thread). */
    public static void handleResult(ServerPlayerEntity player, MinigameResultPayload result) {
        Pending pending = PENDING.remove(player.getUuid());
        if (pending == null) return;

        ServerWorld world = (ServerWorld) player.getWorld();
        if (world.getTime() - pending.gameTime() > TIMEOUT_TICKS) return;

        if (!result.success()) {
            player.sendMessage(Text.translatable("message.anglersdream.escaped"), true);
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_FISHING_BOBBER_SPLASH,
                    SoundCategory.PLAYERS, 0.8f, 0.8f);
            return;
        }

        ItemStack fish = pending.fish();

        if (result.perfect() && fish.getItem() instanceof FishItem fishItem) {
            FishData d = fish.get(AnglersDream.FISH_DATA);
            if (d != null) {
                // Cap matches the trophy-class giant ceiling so a perfect catch never shrinks one.
                float size = Math.min(fishItem.species.maxSize() * 2.5f, d.sizeCm() * 1.1f);
                size = Math.round(size * 10.0f) / 10.0f;
                float weight = Math.round(d.weightKg() * 1.15f * 100.0f) / 100.0f;
                fish.set(AnglersDream.FISH_DATA, new FishData(size, weight, d.variant()));
            }
            player.sendMessage(Text.translatable("message.anglersdream.perfect"), true);
        }

        List<ItemStack> loot = new ArrayList<>();
        loot.add(fish);
        if (result.treasureCollected()) {
            loot.addAll(FishingLogic.treasureLoot(world, pending.pos(), pending.rod(), 1));
        }

        BlockPos pos = pending.pos();
        deliver(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, player, loot);
        player.incrementStat(Stats.FISH_CAUGHT);
        celebrate(world, player, fish);
    }

    /** Tosses loot toward the player with vanilla fishing velocity, plus XP orbs. */
    public static void deliver(ServerWorld world, double x, double y, double z,
                               PlayerEntity player, List<ItemStack> loot) {
        for (ItemStack stack : loot) {
            ItemEntity itemEntity = new ItemEntity(world, x, y, z, stack);
            double dx = player.getX() - x;
            double dy = player.getY() - y;
            double dz = player.getZ() - z;
            itemEntity.setVelocity(dx * 0.1,
                    dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08,
                    dz * 0.1);
            world.spawnEntity(itemEntity);

            world.spawnEntity(new ExperienceOrbEntity(world,
                    player.getX(), player.getY() + 0.5, player.getZ() + 0.5,
                    world.getRandom().nextInt(6) + 1));
        }
    }

    /** Actionbar fanfare for epic+ rarities and prismatic variants. */
    public static void celebrate(ServerWorld world, PlayerEntity player, ItemStack fish) {
        if (!(fish.getItem() instanceof FishItem fishItem)) return;
        boolean big = fishItem.species.rarity().ordinal() >= Rarity.EPIC.ordinal()
                || Variant.fromStack(fish) == Variant.PRISMATIC;
        if (big) {
            player.sendMessage(Text.translatable("message.anglersdream.rare_catch", fish.getName()), true);
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP,
                    SoundCategory.PLAYERS, 0.6f, 1.4f);
        }
    }
}
