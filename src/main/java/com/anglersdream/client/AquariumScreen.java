package com.anglersdream.client;

import com.anglersdream.block.AquariumActions;
import com.anglersdream.block.AquariumBlock;
import com.anglersdream.block.AquariumBlockEntity;
import com.anglersdream.block.AquariumGroup;
import com.anglersdream.network.AquariumActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Management window for a connected aquarium. Reads live state from the client
 * world's block entities every frame (they sync automatically after each server
 * action), so there is no bespoke refresh packet: click a fish below to add it,
 * left-click a tank fish to take it out, right-click one to show/hide it in the
 * tank, and use Fill / Drain with a bucket in your inventory.
 */
public class AquariumScreen extends Screen {

    private record TankEntry(BlockPos member, int slot, ItemStack stack, boolean displayed) {}

    private record InvEntry(int invIndex, ItemStack stack) {}

    private static final int PANEL_W = 260;
    private static final int CELL = 20;
    private static final int COLS = 12;

    private final BlockPos clicked;

    private final List<TankEntry> tankEntries = new ArrayList<>();
    private final List<InvEntry> invEntries = new ArrayList<>();
    private int groupSize;
    private int filledCount;
    private int capacity;

    public AquariumScreen(BlockPos clicked) {
        super(Text.translatable("screen.anglersdream.aquarium_title"));
        this.clicked = clicked;
    }

    @Override
    protected void init() {
        int px = panelX();
        int py = panelY();
        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("screen.anglersdream.aquarium_fill"),
                        b -> send(AquariumActionPayload.FILL, clicked, 0))
                .dimensions(px + PANEL_W - 96, py + 6, 44, 16).build());
        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("screen.anglersdream.aquarium_drain"),
                        b -> send(AquariumActionPayload.DRAIN, clicked, 0))
                .dimensions(px + PANEL_W - 48, py + 6, 44, 16).build());
    }

    private int panelX() {
        return (this.width - PANEL_W) / 2;
    }

    private int panelY() {
        return Math.max(10, this.height / 2 - 90);
    }

    /** Re-read everything from the client world; BE sync keeps this current. */
    private void refresh() {
        tankEntries.clear();
        invEntries.clear();
        groupSize = 0;
        filledCount = 0;

        if (this.client == null || this.client.world == null || this.client.player == null) return;

        List<BlockPos> members = AquariumGroup.collect(this.client.world, clicked);
        groupSize = members.size();
        capacity = groupSize * AquariumGroup.FISH_PER_BLOCK;
        for (BlockPos member : members) {
            if (AquariumBlock.isFilled(this.client.world.getBlockState(member))) filledCount++;
            if (this.client.world.getBlockEntity(member) instanceof AquariumBlockEntity be) {
                for (int slot = 0; slot < AquariumGroup.FISH_PER_BLOCK; slot++) {
                    ItemStack stack = be.getFish(slot);
                    if (!stack.isEmpty()) {
                        tankEntries.add(new TankEntry(member, slot, stack, be.isDisplayed(slot)));
                    }
                }
            }
        }

        var inv = this.client.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isIn(AquariumActions.FISHES)) {
                invEntries.add(new InvEntry(i, stack));
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        refresh();

        if (groupSize == 0) {
            close();
            return;
        }

        int px = panelX();
        int py = panelY();
        int tankRows = Math.max(1, (Math.min(tankEntries.size() + 1, capacity) + COLS - 1) / COLS);
        tankRows = Math.min(tankRows, 4);
        int tankGridH = tankRows * CELL;
        int invRows = Math.max(1, Math.min(3, (invEntries.size() + COLS - 1) / COLS));
        int invGridH = invRows * CELL;
        int panelH = 30 + 12 + tankGridH + 16 + invGridH + 20;

        context.fill(px - 6, py - 6, px + PANEL_W + 6, py + panelH + 6, 0xC0101820);
        context.drawBorder(px - 6, py - 6, PANEL_W + 12, panelH + 12, 0xFF2E5A6C);

        // Header
        context.drawTextWithShadow(this.textRenderer, this.title, px, py + 2, 0xFFFFFFFF);
        Text waterLine = filledCount == groupSize
                ? Text.translatable("screen.anglersdream.aquarium_water_full").formatted(Formatting.AQUA)
                : filledCount == 0
                ? Text.translatable("screen.anglersdream.aquarium_water_empty").formatted(Formatting.GRAY)
                : Text.translatable("screen.anglersdream.aquarium_water_partial", filledCount, groupSize)
                        .formatted(Formatting.YELLOW);
        Text info = Text.translatable("screen.anglersdream.aquarium_blocks", groupSize)
                .append(" · ").append(waterLine).append(" · ")
                .append(Text.translatable("screen.anglersdream.aquarium_fish", tankEntries.size(), capacity));
        context.drawTextWithShadow(this.textRenderer, info, px, py + 14, 0xFFB0C4CC);

        // Tank grid
        int gridY = py + 30;
        context.drawTextWithShadow(this.textRenderer,
                Text.translatable("screen.anglersdream.aquarium_tank_label").formatted(Formatting.GRAY),
                px, gridY, 0xFF909CA4);
        gridY += 12;
        ItemStack hovered = ItemStack.EMPTY;
        boolean hoveredHidden = false;
        for (int i = 0; i < tankEntries.size() && i < COLS * 4; i++) {
            TankEntry entry = tankEntries.get(i);
            int x = px + (i % COLS) * CELL;
            int y = gridY + (i / COLS) * CELL;
            boolean hover = mouseX >= x && mouseX < x + CELL - 2 && mouseY >= y && mouseY < y + CELL - 2;
            context.fill(x, y, x + CELL - 2, y + CELL - 2, hover ? 0x803A6A80 : 0x60203038);
            context.drawItem(entry.stack(), x + 1, y + 1);
            if (!entry.displayed()) {
                context.fill(x, y, x + CELL - 2, y + CELL - 2, 0x90101014);
            }
            if (hover) {
                hovered = entry.stack();
                hoveredHidden = !entry.displayed();
            }
        }

        // Inventory grid
        int invY = gridY + tankGridH + 4;
        context.drawTextWithShadow(this.textRenderer,
                Text.translatable("screen.anglersdream.aquarium_inv_label").formatted(Formatting.GRAY),
                px, invY, 0xFF909CA4);
        invY += 12;
        for (int i = 0; i < invEntries.size() && i < COLS * 3; i++) {
            InvEntry entry = invEntries.get(i);
            int x = px + (i % COLS) * CELL;
            int y = invY + (i / COLS) * CELL;
            boolean hover = mouseX >= x && mouseX < x + CELL - 2 && mouseY >= y && mouseY < y + CELL - 2;
            context.fill(x, y, x + CELL - 2, y + CELL - 2, hover ? 0x805A8040 : 0x60283024);
            context.drawItem(entry.stack(), x + 1, y + 1);
            context.drawTextWithShadow(this.textRenderer, String.valueOf(entry.stack().getCount()),
                    x + 11, y + 10, 0xFFFFFFFF);
            if (hover) hovered = entry.stack();
        }

        if (filledCount < groupSize) {
            context.drawTextWithShadow(this.textRenderer,
                    Text.translatable("screen.anglersdream.aquarium_no_water").formatted(Formatting.YELLOW),
                    px, invY + invGridH + 4, 0xFFE0C24F);
        }

        if (!hovered.isEmpty() && this.client != null && this.client.world != null) {
            List<Text> tip = new ArrayList<>(hovered.getTooltip(
                    Item.TooltipContext.create(this.client.world), this.client.player, TooltipType.BASIC));
            if (hoveredHidden) {
                tip.add(Text.translatable("screen.anglersdream.aquarium_hidden")
                        .formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            }
            context.drawTooltip(this.textRenderer, tip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        int px = panelX();
        int gridY = panelY() + 42;
        int tankRows = Math.max(1, (Math.min(tankEntries.size() + 1, capacity) + COLS - 1) / COLS);
        tankRows = Math.min(tankRows, 4);

        // Tank entries: left-click withdraws, right-click toggles display.
        for (int i = 0; i < tankEntries.size() && i < COLS * 4; i++) {
            int x = px + (i % COLS) * CELL;
            int y = gridY + (i / COLS) * CELL;
            if (mouseX >= x && mouseX < x + CELL - 2 && mouseY >= y && mouseY < y + CELL - 2) {
                TankEntry entry = tankEntries.get(i);
                int action = button == 1
                        ? AquariumActionPayload.TOGGLE_DISPLAY
                        : AquariumActionPayload.WITHDRAW;
                ClientPlayNetworking.send(new AquariumActionPayload(clicked, action, entry.member(), entry.slot()));
                return true;
            }
        }

        // Inventory entries: click deposits one.
        int invY = gridY + tankRows * CELL + 16;
        for (int i = 0; i < invEntries.size() && i < COLS * 3; i++) {
            int x = px + (i % COLS) * CELL;
            int y = invY + (i / COLS) * CELL;
            if (mouseX >= x && mouseX < x + CELL - 2 && mouseY >= y && mouseY < y + CELL - 2) {
                send(AquariumActionPayload.DEPOSIT, clicked, invEntries.get(i).invIndex());
                return true;
            }
        }
        return false;
    }

    private void send(int action, BlockPos target, int slot) {
        ClientPlayNetworking.send(new AquariumActionPayload(clicked, action, target, slot));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
