package org.example.model;

import java.time.LocalDate;

public class Invoice {

    private int id;
    private String invoiceNumber;
    private int paymentId;
    private LocalDate issueDate;
    private String cufe;
    private String pdfFileName;
    private String filePath;

    public Invoice() {
        this.issueDate = LocalDate.now();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public int getPaymentId() {
        return this.paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public LocalDate getIssueDate() {
        return this.issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getCufe() {
        return this.cufe;
    }

    public void setCufe(String cufe) {
        this.cufe = cufe;
    }

    public String getPdfFileName() {
        return this.pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return String.format("Factura %s | Pago #%d | Fecha: %s | Archivo: %s",
                this.invoiceNumber, this.paymentId, this.issueDate, this.filePath);
    }
}
