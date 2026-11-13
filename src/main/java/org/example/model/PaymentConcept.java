package org.example.model;

public class PaymentConcept {

    private int id;
    private String code;
    private String name;
    private String description;
    private double baseAmount;
    private boolean periodic = false;

    // Empty constructor
    public PaymentConcept() {
    }

    // Constructor with fields
    public PaymentConcept(int id, String code, String name, String description,
                          double baseAmount, boolean periodic) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.baseAmount = baseAmount;
        this.periodic = periodic;
    }

    // Getters and Setters
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

    public boolean isPeriodic() {
        return this.periodic;
    }

    public void setPeriodic(boolean periodic) {
        this.periodic = periodic;
    }

    @Override
    public String toString() {
        String per = this.periodic ? "Si" : "No";
        return "ID: " + this.id
                + " | Codigo: " + this.code
                + " | Concepto: " + this.name
                + " | Monto Base: $" + this.baseAmount
                + " | Periodico: " + per;
    }
}
