package com.anglersdream.client;

import com.anglersdream.AnglersDream;
import com.anglersdream.block.TrophyBlockEntity;
import com.anglersdream.network.StartMinigamePayload;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.ActionResult;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.List;

public class AnglersDreamClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(AnglersDream.TROPHY_BLOCK_ENTITY, TrophyBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(AnglersDream.AQUARIUM_BLOCK_ENTITY, AquariumBlockEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(AnglersDream.AQUARIUM, RenderLayer.getTranslucent());

        // Right-clicking any aquarium block opens the shared management screen. The
        // block consumes the interaction server-side; the screen reads live synced
        // block entity state, so no open packet is needed.
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient && !player.isSneaking()
                    && world.getBlockState(hitResult.getBlockPos()).isOf(AnglersDream.AQUARIUM)) {
                MinecraftClient.getInstance().setScreen(new AquariumScreen(hitResult.getBlockPos()));
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register(AnglersDreamClient::renderTrophyTooltip);

        ClientPlayNetworking.registerGlobalReceiver(StartMinigamePayload.ID, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreen(new FishingMinigameScreen(payload))));

        registerCastPredicate(AnglersDream.REINFORCED_ROD);
        registerCastPredicate(AnglersDream.PRISMATIC_ROD);
        registerCastPredicate(AnglersDream.POSEIDONS_ROD);
        registerCastPredicate(AnglersDream.CELESTIAL_ROD);

        // Opening the encyclopedia is purely a client-side read of the held stack's
        // component data — it's already synced like any other item data, no packet needed.
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (world.isClient && stack.isOf(AnglersDream.FISH_ENCYCLOPEDIA)) {
                MinecraftClient.getInstance().setScreen(new FishEncyclopediaScreen(stack));
                return TypedActionResult.success(stack, true);
            }
            return TypedActionResult.pass(stack);
        });
    }

    /**
     * When the crosshair rests on a trophy stand with a mounted fish, draw the fish's
     * full item tooltip (name, rarity, variant, length, weight) just below the crosshair.
     */
    private static void renderTrophyTooltip(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;
        if (!(client.crosshairTarget instanceof BlockHitResult hit)
                || client.crosshairTarget.getType() != HitResult.Type.BLOCK) return;
        if (!(client.world.getBlockEntity(hit.getBlockPos()) instanceof TrophyBlockEntity be)) return;

        ItemStack fish = be.getFish();
        if (fish.isEmpty()) return;

        // getTooltip returns the styled name plus every tooltip line the item shows.
        List<Text> lines = fish.getTooltip(
                Item.TooltipContext.create(client.world), client.player, TooltipType.BASIC);
        if (lines.isEmpty()) return;

        TextRenderer tr = client.textRenderer;
        int cx = context.getScaledWindowWidth() / 2;

        // Anchor to the bottom of the screen, clearing the hotbar (22px tall + 4px
        // margin) plus the exp bar row above it, instead of floating over the crosshair.
        int boxHeight = lines.size() * 10 + 6;
        int boxBottom = context.getScaledWindowHeight() - 29;
        int y = boxBottom - boxHeight + 4;

        int w = 0;
        for (Text line : lines) w = Math.max(w, tr.getWidth(line));
        context.fill(cx - w / 2 - 5, y - 4, cx + w / 2 + 5, y + lines.size() * 10 + 2, 0xA0100E18);

        for (Text line : lines) {
            context.drawTextWithShadow(tr, line, cx - tr.getWidth(line) / 2, y, 0xFFFFFFFF);
            y += 10;
        }
    }

    /** Mirrors vanilla's "cast" model predicate so custom rods swap to their cast texture. */
    private static void registerCastPredicate(Item rod) {
        ModelPredicateProviderRegistry.register(rod, Identifier.ofVanilla("cast"), (stack, world, entity, seed) -> {
            if (entity == null) return 0.0f;
            boolean mainHand = entity.getMainHandStack() == stack;
            boolean offHand = entity.getOffHandStack() == stack;
            if (entity.getMainHandStack().getItem() instanceof FishingRodItem) {
                offHand = false;
            }
            return (mainHand || offHand) && entity instanceof PlayerEntity player && player.fishHook != null
                    ? 1.0f : 0.0f;
        });
    }
}
