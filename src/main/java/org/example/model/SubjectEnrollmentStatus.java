package org.example.model;

public enum SubjectEnrollmentStatus {
    IN_PROGRESS("Cursando"),
    PASSED("Aprobada"),
    FAILED("Reprobada"),
    CANCELLED("Cancelada");

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
        return IN_PROGRESS;
    }
}
