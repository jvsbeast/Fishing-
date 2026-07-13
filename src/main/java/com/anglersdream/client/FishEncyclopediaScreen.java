package com.anglersdream.client;

import com.anglersdream.AnglersDream;
import com.anglersdream.fish.BiomeGroup;
import com.anglersdream.fish.EncyclopediaLog;
import com.anglersdream.fish.FishRegistry;
import com.anglersdream.fish.FishSpecies;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only "bestiary" of every fish species, one themed page per biome group.
 * Undiscovered species show as a dark silhouette of their real shape (plus their
 * rarity), caught ones show their true item icon and name. A page gets a gold
 * border once every species in that biome has been caught.
 */
public class FishEncyclopediaScreen extends Screen {

    private record PageTheme(String displayName, String code, int bg, int accent) {}

    private static final Map<BiomeGroup, PageTheme> THEMES = new EnumMap<>(BiomeGroup.class);
    static {
        THEMES.put(BiomeGroup.OCEAN, new PageTheme("Ocean", "OCN", 0xF01B3A5C, 0xFF4FA8E8));
        THEMES.put(BiomeGroup.WARM_OCEAN, new PageTheme("Warm Ocean", "WOC", 0xF00E5C56, 0xFFF28A5C));
        THEMES.put(BiomeGroup.FROZEN, new PageTheme("Frozen", "FRZ", 0xF0274156, 0xFFBFE8F5));
        THEMES.put(BiomeGroup.RIVER, new PageTheme("River", "RIV", 0xF02D4A2E, 0xFF8FD45C));
        THEMES.put(BiomeGroup.SWAMP, new PageTheme("Swamp", "SWP", 0xF033361F, 0xFF9CAF3F));
        THEMES.put(BiomeGroup.JUNGLE, new PageTheme("Jungle", "JUN", 0xF0183D1F, 0xFF4FD45C));
        THEMES.put(BiomeGroup.DESERT, new PageTheme("Desert", "DSR", 0xF05C4A26, 0xFFE0B25C));
        THEMES.put(BiomeGroup.MOUNTAIN, new PageTheme("Mountain", "MTN", 0xF03C3F45, 0xFFC8D4E0));
        THEMES.put(BiomeGroup.MUSHROOM, new PageTheme("Mushroom Fields", "MSH", 0xF04A1F38, 0xFFE05C8A));
        THEMES.put(BiomeGroup.CAVES, new PageTheme("Caves", "CAV", 0xF0141118, 0xFF9F6BE8));
    }

    private static final BiomeGroup[] ORDER = {
            BiomeGroup.OCEAN, BiomeGroup.WARM_OCEAN, BiomeGroup.FROZEN, BiomeGroup.RIVER,
            BiomeGroup.SWAMP, BiomeGroup.JUNGLE, BiomeGroup.DESERT, BiomeGroup.MOUNTAIN,
            BiomeGroup.MUSHROOM, BiomeGroup.CAVES,
    };

    private static final int GOLD = 0xFFFFD700;
    private static final int PAGE_W = 320;
    private static final int PAGE_H = 210;
    private static final int TAB_H = 20;

    private final EncyclopediaLog log;
    private BiomeGroup selected = BiomeGroup.OCEAN;

    public FishEncyclopediaScreen(ItemStack stack) {
        super(Text.translatable("screen.anglersdream.encyclopedia_title"));
        EncyclopediaLog data = stack.get(AnglersDream.ENCYCLOPEDIA_LOG);
        this.log = data != null ? data : EncyclopediaLog.EMPTY;
    }

    private int blockX() {
        return (this.width - PAGE_W) / 2;
    }

    private int blockY() {
        return (this.height - (TAB_H + 2 + PAGE_H)) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int bx = blockX();
        int by = blockY();
        int pageY = by + TAB_H + 2;

        renderTabs(context, mouseX, mouseY, bx, by);
        renderPage(context, bx, pageY, selected);
    }

    private void renderTabs(DrawContext context, int mouseX, int mouseY, int bx, int by) {
        int tabW = PAGE_W / ORDER.length;
        for (int i = 0; i < ORDER.length; i++) {
            BiomeGroup group = ORDER[i];
            PageTheme theme = THEMES.get(group);
            int x = bx + i * tabW;
            boolean active = group == selected;
            boolean hovered = mouseX >= x && mouseX < x + tabW && mouseY >= by && mouseY < by + TAB_H;

            int fill = active ? (theme.accent() & 0x00FFFFFF) | 0xE0000000 : 0xC0202028;
            if (!active && hovered) fill = 0xC0303040;
            context.fill(x, by, x + tabW - 1, by + TAB_H, fill);
            if (log.isGroupComplete(group)) {
                context.fill(x, by, x + tabW - 1, by + 2, GOLD);
            }

            int textColor = active ? 0xFF101010 : 0xFFD0D0D0;
            context.drawCenteredTextWithShadow(this.textRenderer, theme.code(),
                    x + tabW / 2, by + 6, textColor);
        }
    }

    private void renderPage(DrawContext context, int px, int py, BiomeGroup group) {
        PageTheme theme = THEMES.get(group);
        boolean complete = log.isGroupComplete(group);
        int borderColor = complete ? GOLD : theme.accent();
        int borderThickness = complete ? 3 : 1;

        context.fill(px, py, px + PAGE_W, py + PAGE_H, theme.bg());
        for (int t = 0; t < borderThickness; t++) {
            context.drawBorder(px - t, py - t, PAGE_W + t * 2, PAGE_H + t * 2, borderColor);
        }

        // Header: title + discovery / catch counters.
        context.drawTextWithShadow(this.textRenderer, theme.displayName(), px + 10, py + 8, 0xFFFFFFFF);
        List<FishSpecies> species = FishRegistry.forGroup(group);
        int discovered = log.discoveredInGroup(group);
        int total = log.totalInGroup(group);
        Text discoveredLine = Text.literal(discovered + " / " + species.size() + " discovered")
                .formatted(complete ? Formatting.GOLD : Formatting.GRAY);
        Text totalLine = Text.literal("Caught in " + theme.displayName() + ": " + total)
                .formatted(Formatting.GRAY);
        context.drawTextWithShadow(this.textRenderer, discoveredLine, px + 10, py + 19, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, totalLine,
                px + PAGE_W - 10 - this.textRenderer.getWidth(totalLine), py + 19, 0xFFFFFFFF);

        // Fish grid: up to 3 columns.
        int gridX = px + 8;
        int gridY = py + 34;
        int gridW = PAGE_W - 16;
        int cols = 3;
        int cellW = gridW / cols;
        int cellH = 86;

        for (int i = 0; i < species.size(); i++) {
            FishSpecies fs = species.get(i);
            int col = i % cols;
            int row = i / cols;
            drawFishCard(context, fs, gridX + col * cellW, gridY + row * cellH, cellW - 4, cellH - 4);
        }
    }

    private void drawFishCard(DrawContext context, FishSpecies species, int x, int y, int w, int h) {
        boolean caught = log.hasCaught(species);
        int rarityColor = 0xFF000000 | (species.rarity().color.getColorValue() != null
                ? species.rarity().color.getColorValue() : 0xFFFFFF);

        context.fill(x, y, x + w, y + h, 0x50000000);
        context.drawBorder(x, y, w, h, caught ? rarityColor : 0x40FFFFFF);

        int iconX = x + w / 2 - 16;
        int iconY = y + 6;
        if (caught) {
            Item fishItem = AnglersDream.FISH_ITEMS.get(species);
            if (fishItem != null) {
                context.getMatrices().push();
                context.getMatrices().translate(iconX, iconY, 0);
                context.getMatrices().scale(2.0f, 2.0f, 1.0f);
                context.drawItem(new ItemStack(fishItem), 0, 0);
                context.getMatrices().pop();
            }
        } else {
            Identifier silhouette = AnglersDream.id("textures/gui/silhouette/" + species.id() + ".png");
            context.drawTexture(silhouette, iconX, iconY, 0, 0, 32, 32, 32, 32);
        }

        String nameText = caught ? displayName(species) : "???";
        int nameColor = caught ? 0xFFFFFFFF : 0xFF808080;
        int nameY = y + 42;
        if (this.textRenderer.getWidth(nameText) > w - 4) {
            nameText = this.textRenderer.trimToWidth(nameText, w - 10) + "..";
        }
        context.drawCenteredTextWithShadow(this.textRenderer, nameText, x + w / 2, nameY, nameColor);

        Text rarityText = Text.translatable(species.rarity().translationKey()).formatted(species.rarity().color);
        context.drawCenteredTextWithShadow(this.textRenderer, rarityText, x + w / 2, nameY + 10, 0xFFFFFFFF);
    }

    private static String displayName(FishSpecies species) {
        String id = species.id();
        StringBuilder sb = new StringBuilder();
        boolean upperNext = true;
        for (char c : id.toCharArray()) {
            if (c == '_') {
                sb.append(' ');
                upperNext = true;
            } else {
                sb.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            }
        }
        return sb.toString();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int bx = blockX();
        int by = blockY();
        int tabW = PAGE_W / ORDER.length;
        if (mouseY >= by && mouseY < by + TAB_H) {
            for (int i = 0; i < ORDER.length; i++) {
                int x = bx + i * tabW;
                if (mouseX >= x && mouseX < x + tabW) {
                    selected = ORDER[i];
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}
