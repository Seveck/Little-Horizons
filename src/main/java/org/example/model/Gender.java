package org.example.model;

public enum Gender {
    MALE("M"),
    FEMALE("F"),
    OTHER("Otro");

    private final String dbValue;

    Gender(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public static Gender fromDbValue(String value) {
        for (Gender g : values()) {
            if (g.dbValue.equalsIgnoreCase(value)) {
                return g;
            }
        }
        return OTHER;
    }
}
