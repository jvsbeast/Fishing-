package com.anglersdream.client;

import com.anglersdream.AnglersDream;
import com.anglersdream.network.StartMinigamePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class AnglersDreamClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(AnglersDream.TROPHY_BLOCK_ENTITY, TrophyBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(StartMinigamePayload.ID, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreen(new FishingMinigameScreen(payload))));

        registerCastPredicate(AnglersDream.REINFORCED_ROD);
        registerCastPredicate(AnglersDream.PRISMATIC_ROD);
        registerCastPredicate(AnglersDream.POSEIDONS_ROD);
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
