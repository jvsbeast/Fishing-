package com.anglersdream.mixin;

import com.anglersdream.AnglersDream;
import com.anglersdream.fish.FishingLogic;
import com.anglersdream.minigame.MinigameServer;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {

    @Shadow private int hookCountdown;
    @Shadow @Nullable private Entity hookedEntity;

    /**
     * Vanilla removeIfInvalid discards the bobber unless the player is holding
     * literally Items.FISHING_ROD, which instantly despawns the hook cast by any
     * modded rod. Mirror the vanilla validity check but accept every FishingRodItem
     * so tiered rods behave exactly like the vanilla rod.
     */
    @Inject(method = "removeIfInvalid", at = @At("HEAD"), cancellable = true)
    private void anglersdream$allowTieredRods(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        boolean holdingRod = player.getMainHandStack().getItem() instanceof FishingRodItem
                || player.getOffHandStack().getItem() instanceof FishingRodItem;
        FishingBobberEntity self = (FishingBobberEntity) (Object) this;
        if (!player.isRemoved() && player.isAlive() && holdingRod
                && self.squaredDistanceTo(player) <= 1024.0) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Replaces the vanilla fish loot roll with Angler's Dream generation.
     * Junk and treasure come up instantly (like trash in Stardew Valley);
     * an actual fish opens the reel-in minigame instead of being handed over.
     */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void anglersdream$customCatch(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FishingBobberEntity self = (FishingBobberEntity) (Object) this;
        if (self.getWorld().isClient) return;

        PlayerEntity player = self.getPlayerOwner();
        if (player == null) return;

        // Only take over the "reeled in a catch" case; entity hooks and empty reels stay vanilla.
        if (this.hookedEntity != null || this.hookCountdown <= 0) return;

        ServerWorld world = (ServerWorld) self.getWorld();
        List<ItemStack> loot = FishingLogic.generateCatch(world, self.getBlockPos(), player, usedItem);

        if (player instanceof ServerPlayerEntity serverPlayer) {
            Criteria.FISHING_ROD_HOOKED.trigger(serverPlayer, usedItem, self, loot);
        }

        boolean isFish = loot.size() == 1 && loot.get(0).contains(AnglersDream.FISH_DATA);
        // Creative players skip the reel-in minigame and get the fish instantly.
        if (isFish && !player.isCreative() && player instanceof ServerPlayerEntity serverPlayer) {
            MinigameServer.begin(serverPlayer, world, self.getBlockPos(), usedItem, loot.get(0));
        } else {
            MinigameServer.deliver(world, self.getX(), self.getY(), self.getZ(), player, loot);
            player.incrementStat(Stats.FISH_CAUGHT);
        }

        self.discard();
        cir.setReturnValue(1);
    }
}
