package com.anglersdream.mixin.client;

import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingBobberEntityRenderer.class)
public class FishingBobberEntityRendererMixin {

    /**
     * Vanilla picks which hand the fishing line is attached to by checking whether the
     * main hand holds literally Items.FISHING_ROD; any modded rod flips the check and
     * the line renders from the wrong side of the player instead of the rod tip.
     * Treat every FishingRodItem as a fishing rod so the line anchors correctly.
     */
    @Redirect(method = "render(Lnet/minecraft/entity/projectile/FishingBobberEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean anglersdream$anyRodAnchorsLine(ItemStack stack, Item item) {
        if (item == Items.FISHING_ROD) {
            return stack.getItem() instanceof FishingRodItem;
        }
        return stack.isOf(item);
    }
}
