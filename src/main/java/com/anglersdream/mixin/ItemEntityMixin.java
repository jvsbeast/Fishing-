package com.anglersdream.mixin;

import com.anglersdream.fish.Variant;
import com.anglersdream.item.FishItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    /** Client-side sparkle around dropped special-variant fish. */
    @Inject(method = "tick", at = @At("TAIL"))
    private void anglersdream$variantParticles(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        World world = self.getWorld();
        if (!world.isClient) return;

        ItemStack stack = self.getStack();
        if (!(stack.getItem() instanceof FishItem)) return;

        Variant v = Variant.fromStack(stack);
        if (v == Variant.NORMAL || v.particle == null) return;

        Random r = world.getRandom();
        if (r.nextInt(5) != 0) return;

        world.addParticle(v.particle,
                self.getX() + (r.nextDouble() - 0.5) * 0.6,
                self.getY() + 0.3 + r.nextDouble() * 0.4,
                self.getZ() + (r.nextDouble() - 0.5) * 0.6,
                0.0, 0.02, 0.0);
    }
}
