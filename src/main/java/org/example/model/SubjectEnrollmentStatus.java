package org.example.model;

public enum SubjectEnrollmentStatus {
    ACTIVE("Activa"),
    WITHDRAWN("Retirada"),
    COMPLETED("Completada");

    private final String dbValue;

    SubjectEnrollmentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public static SubjectEnrollmentStatus fromDbValue(String value) {
        for (SubjectEnrollmentStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
