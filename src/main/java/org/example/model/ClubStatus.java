package org.example.model;

public enum ClubStatus {
    ACTIVE("Activo"),
    INACTIVE("Inactivo");

    private final String dbValue;

    ClubStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public static ClubStatus fromDbValue(String value) {
        for (ClubStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
