package com.villagerinspector.data;

public enum OverallStatus {
    OPTIMAL("§a", "✔", 0xFF55FF55, 0x4000AA00),
    WARNING("§e", "⚠", 0xFFFFFF55, 0x40AAAA00),
    DANGER("§c", "✖", 0xFFFF5555, 0x40AA0000);

    private final String colorCode;
    private final String symbol;
    private final int textColor;
    private final int backgroundColor;

    OverallStatus(String colorCode, String symbol, int textColor, int backgroundColor) {
        this.colorCode = colorCode;
        this.symbol = symbol;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }
}
