package org.example.model;

public enum GradeLevel {
    PREESCOLAR("Preescolar"),
    PRIMARIA("Primaria"),
    SECUNDARIA("Secundaria"),
    MEDIA("Media");

    private final String dbValue;

    GradeLevel(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public static GradeLevel fromDbValue(String value) {
        for (GradeLevel level : values()) {
            if (level.dbValue.equalsIgnoreCase(value)) {
                return level;
            }
        }
        return PRIMARIA;
    }
}
