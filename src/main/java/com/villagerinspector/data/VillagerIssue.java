package com.villagerinspector.data;

public class VillagerIssue {
    public enum Severity {
        WARNING("§e⚠", 0xFFFFFF55),
        DANGER("§c✖", 0xFFFF5555);

        private final String prefix;
        private final int color;

        Severity(String prefix, int color) {
            this.prefix = prefix;
            this.color = color;
        }

        public String getPrefix() {
            return prefix;
        }

        public int getColor() {
            return color;
        }
    }

    private final Severity severity;
    private final String description;

    public VillagerIssue(Severity severity, String description) {
        this.severity = severity;
        this.description = description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public String getFormatted() {
        return severity.getPrefix() + " " + description;
    }
}
