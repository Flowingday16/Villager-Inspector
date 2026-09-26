package com.villagerinspector.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("villagerinspector.json");
    private static ModConfig INSTANCE = null;

    public enum HudPosition {
        TOP_CENTER("gui.villagerinspector.config.hud_position.top_center"),
        TOP_LEFT("gui.villagerinspector.config.hud_position.top_left"),
        TOP_RIGHT("gui.villagerinspector.config.hud_position.top_right"),
        ABOVE_CROSSHAIR("gui.villagerinspector.config.hud_position.above_crosshair");

        private final String translationKey;

        HudPosition(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component getLabel() {
            return Component.translatable(translationKey);
        }
    }

    public enum PanelTheme {
        EMERALD("gui.villagerinspector.config.theme.emerald", 0xFF2ECC71, 0x452ECC71),
        GOLD("gui.villagerinspector.config.theme.gold", 0xFFF1C40F, 0x45F1C40F),
        DIAMOND("gui.villagerinspector.config.theme.diamond", 0xFF00D2D3, 0x4500D2D3),
        AMETHYST("gui.villagerinspector.config.theme.amethyst", 0xFF9B59B6, 0x459B59B6),
        REDSTONE("gui.villagerinspector.config.theme.redstone", 0xFFE74C3C, 0x45E74C3C),
        LAPIS("gui.villagerinspector.config.theme.lapis", 0xFF3498DB, 0x453498DB),
        COPPER("gui.villagerinspector.config.theme.copper", 0xFFE67E22, 0x45E67E22),
        NETHERITE("gui.villagerinspector.config.theme.netherite", 0xFF7F8C8D, 0x4534495E),
        WHITE("gui.villagerinspector.config.theme.white", 0xFFECF0F1, 0x45FFFFFF),
        STATUS_DYNAMIC("gui.villagerinspector.config.theme.status_dynamic", 0xFF2ECC71, 0x4000AA00);

        private final String translationKey;
        private final int borderColor;
        private final int headerBgColor;

        PanelTheme(String translationKey, int borderColor, int headerBgColor) {
            this.translationKey = translationKey;
            this.borderColor = borderColor;
            this.headerBgColor = headerBgColor;
        }

        public Component getLabel() {
            return Component.translatable(translationKey);
        }

        public int getBorderColor() {
            return borderColor;
        }

        public int getHeaderBgColor() {
            return headerBgColor;
        }
    }

    public boolean showBed = true;
    public boolean showWorkstation = true;
    public boolean showTrades = true;
    public boolean showBreeding = true;
    public boolean showHealth = true;
    public boolean showIssues = true;
    public boolean showCoordinates = true;
    public HudPosition hudPosition = HudPosition.TOP_CENTER;
    public PanelTheme panelTheme = PanelTheme.EMERALD;
    public int backgroundOpacityPercent = 90;

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                if (INSTANCE != null) {
                    if (INSTANCE.panelTheme == null) {
                        INSTANCE.panelTheme = PanelTheme.EMERALD;
                    }
                    return;
                }
            } catch (Throwable ignored) {
            }
        }
        INSTANCE = new ModConfig();
        save();
    }

    public static void save() {
        if (INSTANCE == null) {
            INSTANCE = new ModConfig();
        }
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Throwable ignored) {
        }
    }

    public static void resetDefaults() {
        INSTANCE = new ModConfig();
        save();
    }
}
