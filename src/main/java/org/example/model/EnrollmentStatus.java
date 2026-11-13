package org.example.model;

public enum EnrollmentStatus {
    ENROLLED("Matriculado"),
    PROMOTED("Promovido"),
    FAILED("Reprobado"),
    WITHDRAWN("Retirado");

    private final String dbValue;

    EnrollmentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public static EnrollmentStatus fromDbValue(String value) {
        for (EnrollmentStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return ENROLLED;
    }
}
