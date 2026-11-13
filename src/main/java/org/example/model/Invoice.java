package org.example.model;

import java.time.LocalDateTime;

public class Invoice {

    private int id;
    private int paymentId;
    private String invoiceNumber;
    private String cufe;
    private String fileName; // Maps to nombre_archivo_pdf in MySQL
    private String filePath;
    private LocalDateTime issueDate;
    private double subtotal;
    private double iva = 0.0;
    private double total;

    // Empty constructor
    public Invoice() {
    }

    // Constructor with fields
    public Invoice(int id, int paymentId, String invoiceNumber, String cufe, String fileName,
                   String filePath, LocalDateTime issueDate, double subtotal, double iva, double total) {
        this.id = id;
        this.paymentId = paymentId;
        this.invoiceNumber = invoiceNumber;
        this.cufe = cufe;
        this.fileName = fileName;
        this.filePath = filePath;
        this.issueDate = issueDate;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
    }

    // Getters and Setters
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPaymentId() {
        return this.paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCufe() {
        return this.cufe;
    }

    public void setCufe(String cufe) {
        this.cufe = cufe;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getIssueDate() {
        return this.issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public double getSubtotal() {
        return this.subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getIva() {
        return this.iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public double getTotal() {
        return this.total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Factura ID: " + this.id
                + " | Numero: " + this.invoiceNumber
                + " | Pago ID: " + this.paymentId
                + " | Total: $" + this.total
                + " | Archivo: " + this.filePath;
    }
}
