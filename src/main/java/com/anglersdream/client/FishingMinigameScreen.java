package com.anglersdream.client;

import com.anglersdream.AnglersDream;
import com.anglersdream.network.MinigameResultPayload;
import com.anglersdream.network.StartMinigamePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Stardew Valley-style reel-in minigame.
 *
 * Hold left click (or space) to lift the green catch bar; release to let it fall.
 * Keep the fish inside the bar to fill the progress column on the right — let it
 * escape and progress drains. Fill it to land the catch, empty it and the fish
 * gets away. A treasure chest sometimes appears: hold the bar on it to collect
 * bonus loot. Never lose the fish after first contact for a Perfect catch.
 */
public class FishingMinigameScreen extends Screen {

    private static final Identifier FISH_ICON = AnglersDream.id("textures/gui/fish_icon.png");
    private static final Identifier TREASURE_ICON = AnglersDream.id("textures/gui/treasure_icon.png");

    // Behavior ids (mirror StartMinigamePayload docs)
    private static final int MIXER = 0, SMOOTH = 1, DART = 2, SINKER = 3, FLOATER = 4;

    private final int difficulty;
    private int behavior;
    private final boolean hasTreasure;

    private final Random random = Random.create();

    // All positions are normalized 0..1 from the top of the track.
    private final float barSize;      // catch bar height as a fraction of the track
    private float barPos;             // top of the catch bar
    private float barVel;
    private boolean holding;

    private float fishPos = 0.5f;     // center of the fish
    private float fishTarget = 0.5f;
    private float retargetTimer;

    private float progress = 0.3f;
    private boolean touched;          // fish has been inside the bar at least once
    private boolean perfect = true;

    private float treasurePos = -1.0f;
    private float treasureAppearIn;
    private float treasureProgress;
    private boolean treasureCollected;

    private float mixerSwitchTimer;   // MIXER swaps sub-behavior periodically
    private int mixerMode = SMOOTH;

    private long lastTimeNanos;
    private boolean resultSent;

    public FishingMinigameScreen(StartMinigamePayload payload) {
        super(Text.translatable("screen.anglersdream.minigame_title"));
        this.difficulty = payload.difficulty();
        this.behavior = payload.behavior();
        this.hasTreasure = payload.treasure();
        this.barSize = 0.25f + 0.05f * MathHelper.clamp(payload.rodTier(), 0, 3);
        this.barPos = 1.0f - barSize; // start at the bottom, like Stardew
        this.treasureAppearIn = hasTreasure ? 2.0f + random.nextFloat() * 3.0f : Float.MAX_VALUE;
        this.lastTimeNanos = System.nanoTime();
    }

    @Override
    protected void init() {
        this.lastTimeNanos = System.nanoTime();
    }

    // ------------------------------------------------------------------ simulation

    private void update(float dt) {
        if (resultSent) return;

        // --- Catch bar physics ---
        float accel = holding ? -2.6f : 2.2f;
        barVel = MathHelper.clamp(barVel + accel * dt, -1.6f, 1.6f);
        barPos += barVel * dt;
        float maxPos = 1.0f - barSize;
        if (barPos <= 0.0f) {
            barPos = 0.0f;
            barVel = 0.0f;
        } else if (barPos >= maxPos) {
            barPos = maxPos;
            barVel = barVel > 0.4f ? barVel * -0.35f : 0.0f; // small Stardew-style bounce
        }

        // --- Fish movement ---
        int mode = behavior == MIXER ? mixerMode : behavior;
        if (behavior == MIXER) {
            mixerSwitchTimer -= dt;
            if (mixerSwitchTimer <= 0.0f) {
                mixerSwitchTimer = 2.0f + random.nextFloat() * 2.0f;
                mixerMode = switch (random.nextInt(4)) {
                    case 0 -> SMOOTH;
                    case 1 -> DART;
                    case 2 -> SINKER;
                    default -> FLOATER;
                };
            }
        }

        retargetTimer -= dt;
        if (retargetTimer <= 0.0f) {
            float interval = Math.max(0.35f, 1.1f - difficulty * 0.008f);
            if (mode == DART) interval *= 0.55f;
            retargetTimer = interval * (0.6f + random.nextFloat() * 0.8f);
            fishTarget = switch (mode) {
                case DART -> random.nextFloat();                                   // anywhere, instantly scary
                case SINKER -> MathHelper.clamp(fishPos + 0.1f + random.nextFloat() * 0.45f, 0.0f, 1.0f);
                case FLOATER -> MathHelper.clamp(fishPos - 0.1f - random.nextFloat() * 0.45f, 0.0f, 1.0f);
                default -> MathHelper.clamp(fishPos + (random.nextFloat() - 0.5f) * 0.7f, 0.0f, 1.0f);
            };
        }
        float fishSpeed = 0.18f + difficulty * 0.010f;
        if (mode == DART) fishSpeed *= 1.35f;
        float step = fishSpeed * dt;
        float delta = fishTarget - fishPos;
        fishPos += MathHelper.clamp(delta, -step, step);

        // --- Overlap & progress ---
        float barBottom = barPos + barSize;
        boolean overlap = fishPos >= barPos && fishPos <= barBottom;
        if (overlap) {
            touched = true;
            progress += 0.22f * dt;
        } else {
            if (touched) perfect = false;
            progress -= (0.20f + difficulty * 0.0028f) * dt;
        }

        // --- Treasure chest ---
        if (hasTreasure && !treasureCollected) {
            if (treasurePos < 0.0f) {
                treasureAppearIn -= dt;
                if (treasureAppearIn <= 0.0f) {
                    treasurePos = 0.1f + random.nextFloat() * 0.65f;
                }
            } else {
                boolean onTreasure = treasurePos >= barPos && treasurePos <= barBottom;
                if (onTreasure) {
                    treasureProgress += dt / 1.4f;
                    if (treasureProgress >= 1.0f) treasureCollected = true;
                } else {
                    treasureProgress = Math.max(0.0f, treasureProgress - dt / 2.0f);
                }
            }
        }

        // --- End conditions ---
        if (progress >= 1.0f) {
            finish(true);
        } else if (progress <= 0.0f) {
            finish(false);
        }
    }

    private void finish(boolean success) {
        if (resultSent) return;
        resultSent = true;
        ClientPlayNetworking.send(new MinigameResultPayload(
                success, success && treasureCollected, success && perfect && touched));
        if (this.client != null) this.client.setScreen(null);
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastTimeNanos) / 1_000_000_000.0f);
        lastTimeNanos = now;
        update(dt);

        int trackH = Math.min(160, this.height - 80);
        int trackW = 26;
        int trackX = this.width / 2 - 44;
        int trackY = (this.height - trackH) / 2;

        // Panel + track
        context.fill(trackX - 6, trackY - 22, trackX + trackW + 26, trackY + trackH + 8, 0xB0102028);
        context.drawBorder(trackX - 6, trackY - 22, trackW + 32, trackH + 30, 0xFF2E4A5C);
        context.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0xFF0E3A52);
        context.drawBorder(trackX, trackY, trackW, trackH, 0xFF1E5A7A);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                trackX + (trackW + 26) / 2, trackY - 16, 0xFFFFFF);

        // Catch bar
        int barPixH = Math.round(barSize * trackH);
        int barY = trackY + Math.round(barPos * (trackH - barPixH));
        boolean overlapNow = fishPos >= barPos && fishPos <= barPos + barSize;
        int barColor = overlapNow ? 0xA04FD46B : 0xA0D4C24F;
        context.fill(trackX + 2, barY, trackX + trackW - 2, barY + barPixH, barColor);
        context.drawBorder(trackX + 2, barY, trackW - 4, barPixH, 0xFFFFFFFF);

        // Treasure chest
        if (treasurePos >= 0.0f && !treasureCollected) {
            int ty = trackY + Math.round(treasurePos * (trackH - 14));
            if (treasureProgress > 0.0f) {
                int ring = Math.round(treasureProgress * 16);
                context.fill(trackX + trackW / 2 - 9, ty - 2, trackX + trackW / 2 - 9 + ring, ty, 0xFFF2C744);
            }
            context.drawTexture(TREASURE_ICON, trackX + trackW / 2 - 7, ty, 0, 0, 14, 14, 14, 14);
        }

        // Fish
        int fy = trackY + Math.round(fishPos * (trackH - 14));
        context.drawTexture(FISH_ICON, trackX + trackW / 2 - 7, fy, 0, 0, 14, 14, 14, 14);

        // Progress column
        int colX = trackX + trackW + 10;
        context.fill(colX, trackY, colX + 8, trackY + trackH, 0xFF23303A);
        int fillH = Math.round(progress * (trackH - 2));
        int fillColor = progress > 0.66f ? 0xFF4FD46B : progress > 0.33f ? 0xFFE0C24F : 0xFFD4574F;
        context.fill(colX + 1, trackY + trackH - 1 - fillH, colX + 7, trackY + trackH - 1, fillColor);
        context.drawBorder(colX, trackY, 8, trackH, 0xFF3A566B);

        // Status line
        if (perfect && touched) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("screen.anglersdream.perfect_indicator").formatted(Formatting.GOLD),
                    trackX + (trackW + 26) / 2, trackY + trackH + 12, 0xFFD700);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // No blur / darkening; the world stays visible behind the minigame.
    }

    // ------------------------------------------------------------------ input & lifecycle

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            holding = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            holding = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 32) { // space
            holding = true;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers); // ESC -> close() -> fail
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 32) {
            holding = false;
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        finish(false);
    }

    @Override
    public void removed() {
        // Safety net: any way the screen goes away without a result counts as an escape.
        if (!resultSent) {
            resultSent = true;
            ClientPlayNetworking.send(new MinigameResultPayload(false, false, false));
        }
        super.removed();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
