package com.anglersdream.client;

import com.anglersdream.block.TrophyBlock;
import com.anglersdream.block.TrophyBlockEntity;
import com.anglersdream.item.FishItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class TrophyBlockEntityRenderer implements BlockEntityRenderer<TrophyBlockEntity> {

    public TrophyBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(TrophyBlockEntity be, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack fish = be.getFish();
        if (fish.isEmpty()) return;

        Direction facing = be.getCachedState().get(TrophyBlock.FACING);

        // "True size" scaling: a FIXED-mode item sprite is ~0.5 blocks wide,
        // so scale = (length in metres) / 0.5 makes the sprite span the fish's real length.
        // Minimum keeps even tiny fish larger than the 6px medallion behind them;
        // maximum accommodates trophy-class giants.
        float sizeCm = FishItem.getSizeCm(fish);
        float scale = sizeCm > 0
                ? MathHelper.clamp((sizeCm / 100.0f) / 0.5f, 0.8f, 8.0f)
                : 1.0f;

        matrices.push();
        // Sit the sprite just in front of the plaque face (plaque hugs the wall behind it).
        matrices.translate(
                0.5 - facing.getOffsetX() * 0.40,
                0.5,
                0.5 - facing.getOffsetZ() * 0.40);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
        matrices.scale(scale, scale, 1.0f);

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                fish, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV,
                matrices, vertexConsumers, be.getWorld(), 0);
        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(TrophyBlockEntity blockEntity) {
        return true; // Legendary fish can be much larger than one block.
    }
}
