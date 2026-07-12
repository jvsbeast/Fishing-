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
import org.lwjgl.glfw.GLFW;

/**
 * Stardew Valley-style reel-in minigame.
 *
 * Hold left click, right click or space to lift the green catch bar; release to
 * let it fall. Keep the fish inside the bar to fill the progress column; let it
 * escape and progress drains. Fill the column to land the catch, empty it and
 * the fish gets away. A treasure chest sometimes appears mid-fight: hold the bar
 * on it to collect bonus loot. Never lose the fish after first contact for a
 * Perfect catch.
 *
 * The simulation runs at a fixed 20 ticks per second in {@link #tick()} so it is
 * frame-rate independent; {@link #render} only interpolates between the last two
 * ticks. A short grace period after the hook is set lets the player reach the
 * fish before any progress can drain.
 */
public class FishingMinigameScreen extends Screen {

    private static final Identifier FISH_ICON = AnglersDream.id("textures/gui/fish_icon.png");
    private static final Identifier TREASURE_ICON = AnglersDream.id("textures/gui/treasure_icon.png");

    // Behavior ids (mirror StartMinigamePayload docs)
    private static final int MIXER = 0, SMOOTH = 1, DART = 2, SINKER = 3, FLOATER = 4;

    private static final float DT = 1.0f / 20.0f;   // fixed tick step
    private static final float GRACE_SECONDS = 1.5f;
    private static final float HARD_TIMEOUT_SECONDS = 55.0f; // < server's 60s window

    private final int difficulty;
    private final int behavior;
    private final boolean hasTreasure;

    private final Random random = Random.create();

    // All positions are normalized 0..1 from the top of the track.
    private final float barSize;      // catch bar height as a fraction of the track
    private float barPos;             // top of the catch bar
    private float prevBarPos;
    private float barVel;
    private boolean holding;

    private float fishPos = 0.5f;     // center of the fish
    private float prevFishPos = 0.5f;
    private float fishVel;
    private float fishTarget = 0.5f;
    private float retargetTimer = 0.6f;
    private float dartPause;          // darters lunge, then rest

    private float progress = 0.35f;
    private float grace = GRACE_SECONDS;
    private float elapsed;
    private boolean touched;          // fish has been inside the bar at least once
    private boolean perfect = true;

    private float treasurePos = -1.0f;
    private float prevTreasurePos = -1.0f;
    private float treasureAppearIn;
    private float treasureProgress;
    private boolean treasureCollected;

    private float mixerSwitchTimer;   // MIXER swaps sub-behavior periodically
    private int mixerMode = SMOOTH;

    private boolean resultSent;

    public FishingMinigameScreen(StartMinigamePayload payload) {
        super(Text.translatable("screen.anglersdream.minigame_title"));
        this.difficulty = payload.difficulty();
        this.behavior = payload.behavior();
        this.hasTreasure = payload.treasure();
        this.barSize = 0.24f + 0.05f * MathHelper.clamp(payload.rodTier(), 0, 4);
        this.barPos = 0.5f - barSize / 2.0f;  // start centered on the fish, not at the bottom
        this.prevBarPos = barPos;
        this.treasureAppearIn = hasTreasure ? 2.5f + random.nextFloat() * 3.0f : Float.MAX_VALUE;
    }

    // ------------------------------------------------------------------ simulation

    @Override
    public void tick() {
        super.tick();
        if (resultSent) return;

        prevBarPos = barPos;
        prevFishPos = fishPos;
        prevTreasurePos = treasurePos;

        elapsed += DT;
        if (elapsed >= HARD_TIMEOUT_SECONDS) {
            finish(false);
            return;
        }

        tickBar();
        tickFish();
        tickProgress();
        tickTreasure();

        if (progress >= 1.0f) {
            finish(true);
        } else if (progress <= 0.0f) {
            finish(false);
        }
    }

    private void tickBar() {
        float accel = holding ? -3.4f : 2.8f;
        barVel = MathHelper.clamp(barVel + accel * DT, -1.15f, 1.15f);
        barPos += barVel * DT;
        float maxPos = 1.0f - barSize;
        if (barPos <= 0.0f) {
            barPos = 0.0f;
            barVel = 0.0f;
        } else if (barPos >= maxPos) {
            barPos = maxPos;
            barVel = barVel > 0.45f ? barVel * -0.3f : 0.0f; // small Stardew-style bounce
        }
    }

    private void tickFish() {
        int mode = behavior == MIXER ? mixerMode : behavior;
        if (behavior == MIXER) {
            mixerSwitchTimer -= DT;
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

        if (dartPause > 0.0f) {
            dartPause -= DT;
        } else {
            retargetTimer -= DT;
        }
        if (retargetTimer <= 0.0f) {
            float interval = Math.max(0.5f, 1.4f - difficulty * 0.008f);
            if (mode == DART) {
                interval *= 0.6f;
                dartPause = 0.25f + random.nextFloat() * 0.3f; // rest between lunges
            }
            retargetTimer = interval * (0.6f + random.nextFloat() * 0.8f);
            fishTarget = switch (mode) {
                case DART -> random.nextFloat();
                case SINKER -> MathHelper.clamp(fishPos + 0.12f + random.nextFloat() * 0.4f, 0.0f, 1.0f);
                case FLOATER -> MathHelper.clamp(fishPos - 0.12f - random.nextFloat() * 0.4f, 0.0f, 1.0f);
                default -> MathHelper.clamp(fishPos + (random.nextFloat() - 0.5f) * 0.65f, 0.0f, 1.0f);
            };
        }

        // Ease toward the target: accelerate, brake near it. Feels organic and fair.
        float maxSpeed = 0.15f + difficulty * 0.0048f;
        if (mode == DART && dartPause <= 0.0f) maxSpeed *= 1.45f;
        float delta = fishTarget - fishPos;
        float desired = MathHelper.clamp(delta * 4.0f, -maxSpeed, maxSpeed);
        fishVel += MathHelper.clamp(desired - fishVel, -2.2f * DT, 2.2f * DT);
        // Keep the icon's center just inside the track so it never hides behind the border.
        fishPos = MathHelper.clamp(fishPos + fishVel * DT, 0.03f, 0.97f);
    }

    private void tickProgress() {
        boolean overlap = fishInsideBar();
        if (overlap) {
            touched = true;
            grace = 0.0f;                     // contact ends the grace period early
            progress += 0.28f * DT;
        } else {
            if (touched) perfect = false;
            if (grace > 0.0f) {
                grace -= DT;                  // no drain while the player reaches the fish
            } else {
                progress -= (0.13f + difficulty * 0.0024f) * DT;
            }
        }
        progress = MathHelper.clamp(progress, 0.0f, 1.0f);
    }

    private void tickTreasure() {
        if (!hasTreasure || treasureCollected) return;
        if (treasurePos < 0.0f) {
            treasureAppearIn -= DT;
            if (treasureAppearIn <= 0.0f) {
                treasurePos = 0.1f + random.nextFloat() * 0.65f;
                prevTreasurePos = treasurePos;
            }
        } else {
            boolean onTreasure = treasurePos >= barPos && treasurePos <= barPos + barSize;
            if (onTreasure) {
                treasureProgress += DT / 1.3f;
                if (treasureProgress >= 1.0f) treasureCollected = true;
            } else {
                treasureProgress = Math.max(0.0f, treasureProgress - DT / 2.0f);
            }
        }
    }

    private boolean fishInsideBar() {
        // Small tolerance so the icon visibly touching the bar counts as caught;
        // render and simulation share one linear track mapping, so this matches
        // exactly what the player sees.
        return fishPos >= barPos - 0.03f && fishPos <= barPos + barSize + 0.03f;
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
        super.render(context, mouseX, mouseY, delta);

        float barLerp = MathHelper.lerp(delta, prevBarPos, barPos);
        float fishLerp = MathHelper.lerp(delta, prevFishPos, fishPos);

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

        // One linear mapping for everything on the track: normalized p -> trackY + p * trackH.
        // The bar spans [barPos, barPos + barSize], so its bottom lands exactly on the
        // track floor when barPos = 1 - barSize; icons draw centered on their position.

        // Catch bar
        int barPixH = Math.round(barSize * trackH);
        int barY = trackY + Math.round(barLerp * trackH);
        if (barY + barPixH > trackY + trackH) barY = trackY + trackH - barPixH; // rounding guard
        int barColor = fishInsideBar() ? 0xA04FD46B : 0xA0D4C24F;
        context.fill(trackX + 2, barY, trackX + trackW - 2, barY + barPixH, barColor);
        context.drawBorder(trackX + 2, barY, trackW - 4, barPixH, 0xFFFFFFFF);

        // Treasure chest
        if (treasurePos >= 0.0f && !treasureCollected) {
            float tLerp = MathHelper.lerp(delta, Math.max(0.0f, prevTreasurePos), treasurePos);
            int ty = trackY + Math.round(tLerp * trackH) - 7;
            if (treasureProgress > 0.0f) {
                int ring = Math.round(treasureProgress * 16);
                context.fill(trackX + trackW / 2 - 9, ty - 2, trackX + trackW / 2 - 9 + ring, ty, 0xFFF2C744);
            }
            context.drawTexture(TREASURE_ICON, trackX + trackW / 2 - 7, ty, 0, 0, 14, 14, 14, 14);
        }

        // Fish (icon centered on fishPos)
        int fy = trackY + Math.round(fishLerp * trackH) - 7;
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
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // No blur / darkening; the world stays visible behind the minigame.
    }

    // ------------------------------------------------------------------ input & lifecycle

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            holding = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            holding = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            holding = true;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers); // ESC -> close() -> fail
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
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
