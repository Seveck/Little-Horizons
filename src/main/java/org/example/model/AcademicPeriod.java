package org.example.model;

public enum AcademicPeriod {
    FIRST("I"),
    SECOND("II"),
    THIRD("III"),
    FOURTH("IV"),
    FINAL("Final");

    private final String dbValue;

    AcademicPeriod(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public static AcademicPeriod fromDbValue(String value) {
        for (AcademicPeriod period : values()) {
            if (period.dbValue.equalsIgnoreCase(value)) {
                return period;
            }
        }
        return FIRST;
    }
}
