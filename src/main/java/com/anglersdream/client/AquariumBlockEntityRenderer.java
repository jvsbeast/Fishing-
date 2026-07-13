package com.anglersdream.client;

import com.anglersdream.block.AquariumBlock;
import com.anglersdream.block.AquariumBlockEntity;
import com.anglersdream.block.AquariumGroup;
import com.anglersdream.fish.Variant;
import com.anglersdream.item.FishItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.List;

/**
 * Animates each displayed fish swimming through the connected tank. The group's
 * member cells are flood-filled client-side and cached briefly; a fish wanders
 * from its current cell only to face-adjacent member cells, so it can never cut
 * a corner out of an L-shaped tank. Scale preserves the fish's true size, capped
 * by the tank's smallest dimension so it stays inside the glass.
 */
public class AquariumBlockEntityRenderer implements BlockEntityRenderer<AquariumBlockEntity> {

    /** Per-slot swim state; stored in the BE's transient clientAnim array. */
    private static final class Swim {
        Vec3d pos;
        Vec3d target;
        BlockPos cell;
        float yaw;
        long lastNanos = System.nanoTime();
        long lastParticleTick = Long.MIN_VALUE;
        ItemStack tracked = ItemStack.EMPTY;
    }

    /** Transient per-BE group cache so we don't flood fill every frame. */
    private static final class GroupCache {
        List<BlockPos> cells;
        long expiresAt;
        float maxScale;
    }

    private final Random random = Random.create();

    public AquariumBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AquariumBlockEntity be, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (be.getWorld() == null || !AquariumBlock.isFilled(be.getCachedState())) return;

        GroupCache group = groupCache(be);
        if (group.cells.isEmpty()) return;

        for (int slot = 0; slot < AquariumGroup.FISH_PER_BLOCK; slot++) {
            ItemStack stack = be.getFish(slot);
            if (stack.isEmpty() || !be.isDisplayed(slot)) continue;
            renderFish(be, group, slot, stack, matrices, vertexConsumers, light);
        }
    }

    private void renderFish(AquariumBlockEntity be, GroupCache group, int slot, ItemStack stack,
                            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        Swim swim = swimState(be, slot, stack);
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - swim.lastNanos) / 1_000_000_000.0f);
        swim.lastNanos = now;

        if (swim.target == null || swim.pos.squaredDistanceTo(swim.target) < 0.01) {
            pickNewTarget(swim, group.cells);
        }

        Vec3d delta = swim.target.subtract(swim.pos);
        double dist = delta.length();
        if (dist > 1.0e-4) {
            double speed = 0.45;
            Vec3d step = delta.multiply(Math.min(1.0, speed * dt / dist));
            swim.pos = swim.pos.add(step);
            float targetYaw = (float) -Math.toDegrees(Math.atan2(delta.z, delta.x));
            swim.yaw += MathHelper.wrapDegrees(targetYaw - swim.yaw) * Math.min(1.0f, dt * 4.0f);
            swim.cell = BlockPos.ofFloored(swim.pos);
        }

        float sizeCm = FishItem.getSizeCm(stack);
        float scale = sizeCm > 0 ? (sizeCm / 100.0f) / 0.5f : 0.5f;
        scale = MathHelper.clamp(scale, 0.25f, group.maxScale);

        float bob = (float) Math.sin((now / 1_000_000_000.0) * 1.7 + slot * 2.1) * 0.03f;

        matrices.push();
        BlockPos origin = be.getPos();
        matrices.translate(
                swim.pos.x - origin.getX(),
                swim.pos.y - origin.getY() + bob,
                swim.pos.z - origin.getZ());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(swim.yaw));
        matrices.scale(scale, scale, scale);

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                stack, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV,
                matrices, vertexConsumers, be.getWorld(), slot);
        matrices.pop();

        // Special-variant fish keep their ambient particles while on display,
        // trailing from wherever the fish currently swims. Gated to one roll per
        // game tick so the rate is frame-rate independent.
        Variant variant = Variant.fromStack(stack);
        if (variant != Variant.NORMAL && be.getWorld() != null) {
            long tick = be.getWorld().getTime();
            if (tick != swim.lastParticleTick) {
                swim.lastParticleTick = tick;
                if (random.nextInt(4) == 0) {
                    var particle = variant.createParticle(random);
                    if (particle != null) {
                        be.getWorld().addParticle(particle,
                                swim.pos.x + (random.nextDouble() - 0.5) * 0.3,
                                swim.pos.y + bob + (random.nextDouble() - 0.5) * 0.3,
                                swim.pos.z + (random.nextDouble() - 0.5) * 0.3,
                                0.0, 0.01, 0.0);
                    }
                }
            }
        }
    }

    private void pickNewTarget(Swim swim, List<BlockPos> cells) {
        // Candidates: stay in the current cell or move to a face-adjacent member cell.
        BlockPos current = swim.cell != null && cells.contains(swim.cell) ? swim.cell : cells.get(0);
        BlockPos next = current;
        int options = 1;
        for (BlockPos cell : cells) {
            if (cell.getManhattanDistance(current) == 1 && random.nextInt(++options) == 0) {
                next = cell;
            }
        }
        if (random.nextInt(3) == 0) next = current; // sometimes just drift in place
        swim.cell = current;
        swim.target = new Vec3d(
                next.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.45,
                next.getY() + 0.35 + random.nextDouble() * 0.35,
                next.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.45);
    }

    private Swim swimState(AquariumBlockEntity be, int slot, ItemStack stack) {
        // clientAnim[last index] holds the group cache; the rest are per-slot swim states.
        if (!(be.clientAnim[slot] instanceof Swim swim) || !ItemStack.areEqual(swim.tracked, stack)) {
            Swim swim2 = new Swim();
            BlockPos p = be.getPos();
            swim2.pos = new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
            swim2.cell = p;
            swim2.tracked = stack.copy();
            be.clientAnim[slot] = swim2;
            return swim2;
        }
        return swim;
    }

    private GroupCache groupCache(AquariumBlockEntity be) {
        GroupCache cache = be.clientGroupCache instanceof GroupCache g ? g : null;
        long now = System.nanoTime();
        if (cache == null || now > cache.expiresAt) {
            cache = new GroupCache();
            cache.cells = AquariumGroup.collect(be.getWorld(), be.getPos());
            cache.expiresAt = now + 3_000_000_000L;
            cache.maxScale = maxScale(cache.cells);
            be.clientGroupCache = cache;
        }
        return cache;
    }

    private static float maxScale(List<BlockPos> cells) {
        if (cells.isEmpty()) return 0.9f;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos c : cells) {
            minX = Math.min(minX, c.getX()); maxX = Math.max(maxX, c.getX());
            minY = Math.min(minY, c.getY()); maxY = Math.max(maxY, c.getY());
            minZ = Math.min(minZ, c.getZ()); maxZ = Math.max(maxZ, c.getZ());
        }
        int minDim = Math.min(maxX - minX + 1, Math.min(maxY - minY + 1, maxZ - minZ + 1));
        return 0.92f * minDim;
    }

    @Override
    public boolean rendersOutsideBoundingBox(AquariumBlockEntity blockEntity) {
        return true; // fish roam the whole connected tank, not just this block
    }
}
