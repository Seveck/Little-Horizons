package org.example.model;

public class PaymentConcept {

    private int id;
    private String code;
    private String name;
    private String description;
    private double baseAmount;
    private boolean recurring;
    private boolean active;

    public PaymentConcept() {
        this.active = true;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBaseAmount() {
        return this.baseAmount;
    }

    public void setBaseAmount(double baseAmount) {
        this.baseAmount = baseAmount;
    }

    public boolean isRecurring() {
        return this.recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | [%s] %s | Base: $%,.2f | Recurrente: %s",
                this.id, this.code, this.name, this.baseAmount, this.recurring ? "Si" : "No");
    }
}
