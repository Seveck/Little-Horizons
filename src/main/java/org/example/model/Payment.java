package org.example.model;

import java.time.LocalDate;

public class Payment {

    private int id;
    private int studentId;
    private int guardianId;
    private int conceptId;
    private double amountPaid;
    private double discountApplied;
    private PaymentStatus status;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String receiptNumber;
    private int academicYear;
    private String academicPeriod;
    private String notes;
    private String studentName;
    private String guardianName;
    private String conceptName;

    public Payment() {
        this.paymentDate = LocalDate.now();
        this.status = PaymentStatus.PAID;
        this.academicPeriod = "Anual";
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return this.studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getGuardianId() {
        return this.guardianId;
    }

    public void setGuardianId(int guardianId) {
        this.guardianId = guardianId;
    }

    public int getConceptId() {
        return this.conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public double getAmountPaid() {
        return this.amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public double getDiscountApplied() {
        return this.discountApplied;
    }

    public void setDiscountApplied(double discountApplied) {
        this.discountApplied = discountApplied;
    }

    public PaymentStatus getStatus() {
        return this.status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDate getPaymentDate() {
        return this.paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return this.paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReceiptNumber() {
        return this.receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public int getAcademicYear() {
        return this.academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public String getAcademicPeriod() {
        return this.academicPeriod;
    }

    public void setAcademicPeriod(String academicPeriod) {
        this.academicPeriod = academicPeriod;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getGuardianName() {
        return this.guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getConceptName() {
        return this.conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "N/A";
        String method = (this.paymentMethod != null) ? this.paymentMethod.getDbValue() : "N/A";
        String rec = (this.receiptNumber != null && !this.receiptNumber.isEmpty()) ? this.receiptNumber : "-";
        return String.format("Pago #%d | Rec: %s | Est: %s | Concepto: %s | Monto: $%,.2f | Desc: $%,.2f | %s | %s | %s",
                this.id, rec,
                this.studentName != null ? this.studentName : String.valueOf(this.studentId),
                this.conceptName != null ? this.conceptName : String.valueOf(this.conceptId),
                this.amountPaid, this.discountApplied, this.paymentDate, method, st);
    }
}
