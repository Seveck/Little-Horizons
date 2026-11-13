package org.example.model;

public enum PaymentStatus {
    PAID("Pagado"),
    PENDING("Pendiente"),
    CANCELLED("Anulado");

    private final String dbValue;

    PaymentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public static PaymentStatus fromDbValue(String value) {
        for (PaymentStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PAID;
    }
}
