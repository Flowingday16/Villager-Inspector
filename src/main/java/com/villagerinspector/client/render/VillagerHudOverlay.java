package com.villagerinspector.client.render;

import com.villagerinspector.client.VillagerClientCache;
import com.villagerinspector.config.ModConfig;
import com.villagerinspector.data.OverallStatus;
import com.villagerinspector.data.VillagerIssue;
import com.villagerinspector.data.VillagerStatusData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class VillagerHudOverlay {
    private static final ItemStack BED_ITEM = new ItemStack(Items.RED_BED);
    private static final ItemStack WORKSTATION_ITEM = new ItemStack(Items.LECTERN);
    private static final ItemStack TRADE_ITEM = new ItemStack(Items.EMERALD);
    private static final ItemStack FOOD_ITEM = new ItemStack(Items.BREAD);

    public static void render(GuiGraphics graphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.font == null) {
            return;
        }

        VillagerStatusData data = VillagerClientCache.getCurrentStatus();
        if (data == null) {
            return;
        }

        ModConfig config = ModConfig.get();
        Font font = client.font;
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        // 1. Prepare Header Texts
        String statusSymbol = data.getOverallStatus().getSymbol();
        String title = data.getOverallStatus().getColorCode() + statusSymbol + " §f" + data.getProfessionName();
        if (data.isBaby()) {
            title += " §f" + Component.translatable("hud.villagerinspector.baby").getString();
        } else if (data.getLevel() > 0) {
            title += " §f" + Component.translatable("hud.villagerinspector.level", data.getLevel()).getString();
        }

        String healthText = "";
        int healthWidth = 0;
        if (config.showHealth) {
            healthText = String.format("§c❤ §f%.0f/%.0f", data.getHealth(), data.getMaxHealth());
            healthWidth = font.width(healthText);
        }

        BlockPos vPos = data.getVillagerPos();

        // 2. Prepare Row 1 Texts (Bed & Workstation)
        String bedFullString = "";
        if (config.showBed) {
            String bedText;
            if (data.hasBed() && data.getBedPos() != null && config.showCoordinates) {
                int dist = (int) Math.sqrt(data.getBedPos().distSqr(vPos));
                bedText = "§a" + data.getBedPos().getX() + "," + data.getBedPos().getY() + "," + data.getBedPos().getZ() + " §7(" + dist + "m)";
            } else if (data.hasBed()) {
                bedText = "§a" + Component.translatable("hud.villagerinspector.bed.linked").getString();
            } else {
                bedText = "§c" + Component.translatable("hud.villagerinspector.bed.none").getString();
            }
            bedFullString = Component.translatable("hud.villagerinspector.bed").getString() + ": " + bedText;
        }

        String wsFullString = "";
        if (config.showWorkstation) {
            String wsText;
            if (data.isNoneOrNitwit()) {
                wsText = "§7" + Component.translatable("hud.villagerinspector.workstation.not_needed").getString();
            } else if (data.getWorkstationPos() != null && config.showCoordinates) {
                int dist = (int) Math.sqrt(data.getWorkstationPos().distSqr(vPos));
                wsText = "§a" + data.getWorkstationPos().getX() + "," + data.getWorkstationPos().getY() + "," + data.getWorkstationPos().getZ() + " §7(" + dist + "m)";
            } else if (data.hasWorkstation()) {
                wsText = "§e" + Component.translatable("hud.villagerinspector.workstation.linked_far").getString();
            } else {
                wsText = "§c" + Component.translatable("hud.villagerinspector.workstation.none").getString();
            }
            wsFullString = Component.translatable("hud.villagerinspector.workstation").getString() + ": " + wsText;
        }

        // 3. Prepare Row 2 Texts (Trades & Breeding)
        String tradeFullString = "";
        if (config.showTrades) {
            String tradeText;
            if (data.getTotalTradesCount() == 0) {
                tradeText = "§7" + Component.translatable("hud.villagerinspector.trades.none").getString();
            } else if (data.getLockedTradesCount() > 0) {
                tradeText = "§e" + Component.translatable("hud.villagerinspector.trades.active", (data.getTotalTradesCount() - data.getLockedTradesCount()), data.getTotalTradesCount()).getString();
            } else {
                tradeText = "§a" + Component.translatable("hud.villagerinspector.trades.ready", data.getTotalTradesCount(), data.getTotalTradesCount()).getString();
            }
            tradeFullString = Component.translatable("hud.villagerinspector.trades").getString() + ": " + tradeText;
        }

        String breedFullString = "";
        if (config.showBreeding) {
            String breedText;
            if (data.isBaby()) {
                breedText = "§b" + Component.translatable("hud.villagerinspector.breeding.growth", Math.abs(data.getAgeTicks()) / 20).getString();
            } else if (data.canBreed()) {
                breedText = "§a" + Component.translatable("hud.villagerinspector.breeding.ready", data.getFoodPoints()).getString();
            } else {
                breedText = "§e" + Component.translatable("hud.villagerinspector.breeding.food", data.getFoodPoints()).getString();
            }
            breedFullString = Component.translatable("hud.villagerinspector.breeding").getString() + ": " + breedText;
        }

        // 4. Calculate Dynamic Sizing (Guarantees zero text overflow)
        int col1Width = Math.max(
            config.showBed ? font.width(bedFullString) : 0,
            config.showTrades ? font.width(tradeFullString) : 0
        );

        int col2Width = Math.max(
            config.showWorkstation ? font.width(wsFullString) : 0,
            config.showBreeding ? font.width(breedFullString) : 0
        );

        boolean hasCol1 = config.showBed || config.showTrades;
        boolean hasCol2 = config.showWorkstation || config.showBreeding;
        boolean hasRow1 = config.showBed || config.showWorkstation;
        boolean hasRow2 = config.showTrades || config.showBreeding;
        boolean hasIssues = config.showIssues && !data.getIssues().isEmpty();

        int contentWidth = 0;
        int col2Offset = 0;
        if (hasCol1 && hasCol2) {
            col2Offset = 25 + col1Width + 14;
            contentWidth = col2Offset + 19 + col2Width + 10;
        } else if (hasCol1) {
            contentWidth = 25 + col1Width + 10;
        } else if (hasCol2) {
            contentWidth = 25 + col2Width + 10;
        }

        int headerWidth = font.width(title) + (config.showHealth ? healthWidth + 24 : 16);
        int boxWidth = Math.max(260, Math.max(headerWidth, contentWidth));

        if (hasIssues) {
            for (VillagerIssue issue : data.getIssues()) {
                boxWidth = Math.max(boxWidth, font.width(issue.getFormatted()) + 20);
            }
        }

        int totalHeight = 18; // Header
        if (hasRow1) totalHeight += 20;
        if (hasRow2) totalHeight += 20;
        if (hasIssues) totalHeight += 6 + (data.getIssues().size() * 11);
        totalHeight += 4; // bottom padding

        int x;
        int y;

        switch (config.hudPosition) {
            case TOP_LEFT -> {
                x = 10;
                y = 10;
            }
            case TOP_RIGHT -> {
                x = screenWidth - boxWidth - 10;
                y = 10;
            }
            case ABOVE_CROSSHAIR -> {
                x = (screenWidth - boxWidth) / 2;
                y = Math.max(8, (screenHeight / 2) - totalHeight - 24);
            }
            default -> { // TOP_CENTER
                x = (screenWidth - boxWidth) / 2;
                y = 8;
            }
        }

        // 5. Opacity & Color Theme
        int alpha = (int) (255 * (Math.max(20, Math.min(100, config.backgroundOpacityPercent)) / 100.0f));
        int bgFill = (alpha << 24) | 0x121218;

        int borderColor;
        int headerBg;
        if (config.panelTheme == ModConfig.PanelTheme.STATUS_DYNAMIC) {
            borderColor = data.getOverallStatus().getTextColor();
            headerBg = data.getOverallStatus().getBackgroundColor();
        } else {
            borderColor = config.panelTheme.getBorderColor();
            headerBg = config.panelTheme.getHeaderBgColor();
        }

        // Border & Background
        graphics.fill(x - 1, y - 1, x + boxWidth + 1, y + totalHeight + 1, borderColor);
        graphics.fill(x, y, x + boxWidth, y + totalHeight, bgFill);

        // Header strip
        graphics.fill(x, y, x + boxWidth, y + 16, headerBg);

        // Header texts
        graphics.drawString(font, title, x + 6, y + 4, 0xFFFFFFFF, true);
        if (config.showHealth) {
            graphics.drawString(font, healthText, x + boxWidth - healthWidth - 6, y + 4, 0xFFFFFFFF, true);
        }

        int currentContentY = y + 20;

        // 6. Render Row 1
        if (hasRow1) {
            if (config.showBed) {
                graphics.renderFakeItem(BED_ITEM, x + 6, currentContentY);
                graphics.drawString(font, bedFullString, x + 25, currentContentY + 4, 0xFFE0E0E0, true);
            }
            if (config.showWorkstation) {
                int wsX = hasCol1 ? (x + col2Offset) : (x + 6);
                graphics.renderFakeItem(WORKSTATION_ITEM, wsX, currentContentY);
                graphics.drawString(font, wsFullString, wsX + 19, currentContentY + 4, 0xFFE0E0E0, true);
            }
            currentContentY += 20;
        }

        // 7. Render Row 2
        if (hasRow2) {
            if (config.showTrades) {
                graphics.renderFakeItem(TRADE_ITEM, x + 6, currentContentY);
                graphics.drawString(font, tradeFullString, x + 25, currentContentY + 4, 0xFFE0E0E0, true);
            }
            if (config.showBreeding) {
                int brX = hasCol1 ? (x + col2Offset) : (x + 6);
                graphics.renderFakeItem(FOOD_ITEM, brX, currentContentY);
                graphics.drawString(font, breedFullString, brX + 19, currentContentY + 4, 0xFFE0E0E0, true);
            }
            currentContentY += 20;
        }

        // 8. Render Issues list
        if (hasIssues) {
            int sepY = currentContentY;
            graphics.fill(x + 6, sepY, x + boxWidth - 6, sepY + 1, 0xFF3A3A4A);

            int curIssueY = sepY + 4;
            for (VillagerIssue issue : data.getIssues()) {
                graphics.drawString(font, issue.getFormatted(), x + 6, curIssueY, issue.getSeverity().getColor(), true);
                curIssueY += 11;
            }
        }
    }
}
