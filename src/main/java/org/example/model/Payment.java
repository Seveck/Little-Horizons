package org.example.model;

import java.time.LocalDateTime;

public class Payment {

    private int id;
    private String receiptNumber;
    private int studentId;
    private int conceptId;
    private int academicYear;
    private double amountPaid;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;
    private PaymentStatus status = PaymentStatus.PAID;
    private double discountApplied = 0.0;
    private String remarks;

    // Empty constructor
    public Payment() {
    }

    // Constructor with fields
    public Payment(int id, String receiptNumber, int studentId, int conceptId, int academicYear, double amountPaid,
                   PaymentMethod paymentMethod, LocalDateTime paymentDate, PaymentStatus status,
                   double discountApplied, String remarks) {
        this.id = id;
        this.receiptNumber = receiptNumber;
        this.studentId = studentId;
        this.conceptId = conceptId;
        this.academicYear = academicYear;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.status = status;
        this.discountApplied = discountApplied;
        this.remarks = remarks;
    }

    // Getters and Setters
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReceiptNumber() {
        return this.receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public int getStudentId() {
        return this.studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getConceptId() {
        return this.conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public int getAcademicYear() {
        return this.academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public double getAmountPaid() {
        return this.amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public PaymentMethod getPaymentMethod() {
        return this.paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentDate() {
        return this.paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentStatus getStatus() {
        return this.status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public double getDiscountApplied() {
        return this.discountApplied;
    }

    public void setDiscountApplied(double discountApplied) {
        this.discountApplied = discountApplied;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        String method = (this.paymentMethod != null) ? this.paymentMethod.getDbValue() : "";
        String st = (this.status != null) ? this.status.getDbValue() : "";
        return "Pago ID: " + this.id
                + " | Recibo: " + (this.receiptNumber != null ? this.receiptNumber : "N/A")
                + " | Estudiante ID: " + this.studentId
                + " | Concepto ID: " + this.conceptId
                + " | Monto: $" + this.amountPaid
                + " | Metodo: " + method
                + " | Estado: " + st
                + " | Fecha: " + this.paymentDate;
    }
}
