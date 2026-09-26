package com.villagerinspector.client.gui;

import com.villagerinspector.config.ModConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VillagerConfigScreen extends Screen {
    private final Screen parent;

    public VillagerConfigScreen(Screen parent) {
        super(Component.translatable("gui.villagerinspector.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        ModConfig config = ModConfig.get();

        int col1X = this.width / 2 - 165;
        int col2X = this.width / 2 + 5;
        int btnWidth = 160;
        int btnHeight = 20;

        int y0 = 60;
        int y1 = y0 + 26;
        int y2 = y1 + 26;
        int y3 = y2 + 26;
        int y4 = y3 + 26;

        // Row 0
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.showBed).create(
                col1X, y0, btnWidth, btnHeight,
                Component.translatable("gui.villagerinspector.config.show_bed"),
                (btn, val) -> { config.showBed = val; ModConfig.save(); }
            )
        );
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.showWorkstation).create(
                col2X, y0, btnWidth, btnHeight,
                Component.translatable("gui.villagerinspector.config.show_workstation"),
                (btn, val) -> { config.showWorkstation = val; ModConfig.save(); }
            )
        );

        // Row 1
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.showTrades).create(
                col1X, y1, btnWidth, btnHeight,
                Component.translatable("gui.villagerinspector.config.show_trades"),
                (btn, val) -> { config.showTrades = val; ModConfig.save(); }
            )
        );
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.showBreeding).create(
                col2X, y1, btnWidth, btnHeight,
                Component.translatable("gui.villagerinspector.config.show_breeding"),
                (btn, val) -> { config.showBreeding = val; ModConfig.save(); }
            )
        );

        // Row 2
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.showHealth).create(
                col1X, y2, btnWidth, btnHeight,
                Component.translatable("gui.villagerinspector.config.show_health"),
                (btn, val) -> { config.showHealth = val; ModConfig.save(); }
            )
        );
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.showIssues).create(
                col2X, y2, btnWidth, btnHeight,
                Component.translatable("gui.villagerinspector.config.show_issues"),
                (btn, val) -> { config.showIssues = val; ModConfig.save(); }
            )
        );

        // Row 3
        this.addRenderableWidget(
            CycleButton.onOffBuilder(config.showCoordinates).create(
                col1X, y3, btnWidth, btnHeight,
                Component.translatable("gui.villagerinspector.config.show_coordinates"),
                (btn, val) -> { config.showCoordinates = val; ModConfig.save(); }
            )
        );
        this.addRenderableWidget(
            CycleButton.builder(ModConfig.HudPosition::getLabel, config.hudPosition)
                .withValues(ModConfig.HudPosition.values())
                .create(
                    col2X, y3, btnWidth, btnHeight,
                    Component.translatable("gui.villagerinspector.config.hud_position"),
                    (btn, val) -> { config.hudPosition = val; ModConfig.save(); }
                )
        );

        // Row 4
        this.addRenderableWidget(
            CycleButton.builder(v -> Component.literal("%" + v), config.backgroundOpacityPercent)
                .withValues(50, 70, 85, 95, 100)
                .create(
                    col1X, y4, btnWidth, btnHeight,
                    Component.translatable("gui.villagerinspector.config.background_opacity"),
                    (btn, val) -> { config.backgroundOpacityPercent = val; ModConfig.save(); }
                )
        );
        this.addRenderableWidget(
            CycleButton.builder(ModConfig.PanelTheme::getLabel, config.panelTheme)
                .withValues(ModConfig.PanelTheme.values())
                .create(
                    col2X, y4, btnWidth, btnHeight,
                    Component.translatable("gui.villagerinspector.config.panel_theme"),
                    (btn, val) -> { config.panelTheme = val; ModConfig.save(); }
                )
        );

        // Bottom Action Buttons
        int bottomY = Math.max(y4 + 36, this.height - 36);
        this.addRenderableWidget(
            Button.builder(Component.translatable("gui.villagerinspector.config.reset_defaults"), btn -> {
                ModConfig.resetDefaults();
                this.init();
            }).bounds(col1X, bottomY, btnWidth, btnHeight).build()
        );

        this.addRenderableWidget(
            Button.builder(Component.translatable("gui.villagerinspector.config.done"), btn -> this.onClose())
                .bounds(col2X, bottomY, btnWidth, btnHeight).build()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        graphics.fill(0, 0, this.width, this.height, 0xD0101018);
        graphics.drawCenteredString(this.font, Component.translatable("gui.villagerinspector.config.title"), this.width / 2, 18, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("gui.villagerinspector.config.subtitle"), this.width / 2, 34, 0xFFAAAAAA);
        super.render(graphics, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void onClose() {
        ModConfig.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
