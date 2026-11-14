package org.example.controller;

import org.example.dao.InvoiceDao;
import org.example.dao.PaymentConceptDao;
import org.example.dao.PaymentDao;
import org.example.dao.PaymentDao.PaymentDetail;
import org.example.model.Guardian;
import org.example.model.Invoice;
import org.example.model.Payment;
import org.example.model.PaymentConcept;
import org.example.model.PaymentMethod;
import org.example.model.PaymentStatus;
import org.example.model.Student;
import org.example.util.TxtInvoiceGenerator.InvoiceDetails;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class FinancialController {

    private final PaymentConceptDao conceptDao;
    private final PaymentDao paymentDao;
    private final InvoiceDao invoiceDao;

    // Constructor
    public FinancialController() {
        this.conceptDao = new PaymentConceptDao();
        this.paymentDao = new PaymentDao();
        this.invoiceDao = new InvoiceDao();
    }

    // Register a new payment concept
    public int registerConcept(PaymentConcept concept) {
        return this.conceptDao.insert(concept);
    }

    // Update base amount for a concept
    public boolean updateConceptBaseAmount(int conceptId, double newAmount) {
        return this.conceptDao.updateBaseAmount(conceptId, newAmount);
    }

    // List all payment concepts
    public List<PaymentConcept> listAllConcepts() {
        return this.conceptDao.findAll();
    }

    // Find concept by id
    public PaymentConcept findConceptById(int conceptId) {
        return this.conceptDao.findById(conceptId);
    }

    // Suggest next unique receipt number
    public String generateNextReceiptNumber(int academicYear) {
        return this.paymentDao.generateNextReceiptNumber(academicYear);
    }

    // Register a confirmed payment and auto-generate its DIAN .txt invoice
    public String registerConfirmedPayment(Payment payment, Student student, Guardian guardian, PaymentConcept concept) {
        InvoiceDetails details = this.buildInvoiceDetails(payment, student, guardian, concept);
        return this.paymentDao.registerPaymentWithInvoice(payment, details);
    }

    // Register a pending charge without generating an invoice yet
    public int registerPendingCharge(Payment payment) {
        return this.paymentDao.insertPendingPayment(payment);
    }

    // Settle a pending payment and generate its DIAN .txt invoice
    public String settlePendingPayment(int paymentId, String receiptNumber, PaymentMethod method,
                                       Student student, Guardian guardian, PaymentConcept concept, Payment payment) {
        payment.setReceiptNumber(receiptNumber);
        payment.setPaymentMethod(method);
        InvoiceDetails details = this.buildInvoiceDetails(payment, student, guardian, concept);
        return this.paymentDao.settlePendingPayment(paymentId, receiptNumber, method, details);
    }

    // Find payment by id
    public Payment findPaymentById(int paymentId) {
        return this.paymentDao.findById(paymentId);
    }

    // Get payment history of a single student
    public List<PaymentDetail> getStudentPaymentHistory(int studentId) {
        return this.paymentDao.findPaymentDetailsByStudent(studentId);
    }

    // Get consolidated payment history for all students of a guardian
    public List<PaymentDetail> getGuardianPaymentHistory(int guardianId) {
        return this.paymentDao.findPaymentDetailsByGuardian(guardianId);
    }

    // Filter delinquent payments pending for more than minDaysOverdue days using Java Stream API
    public List<PaymentDetail> getDelinquentPayments(int minDaysOverdue) {
        LocalDate today = LocalDate.now();
        return this.paymentDao.findAllPaymentDetails().stream()
                .filter(p -> p.status == PaymentStatus.PENDING && p.paymentDate != null)
                .filter(p -> ChronoUnit.DAYS.between(p.paymentDate.toLocalDate(), today) > minDaysOverdue)
                .collect(Collectors.toList());
    }

    // Calculate total revenue collected in a given academic year using Java Stream API
    public double calculateTotalCollectedByYear(int academicYear) {
        return this.paymentDao.findAllPaymentDetails().stream()
                .filter(p -> p.status == PaymentStatus.PAID && p.academicYear == academicYear)
                .mapToDouble(p -> p.amountPaid)
                .sum();
    }

    // List all generated invoices
    public List<Invoice> listAllInvoices() {
        return this.invoiceDao.findAll();
    }

    // Find invoice associated with a payment
    public Invoice findInvoiceByPaymentId(int paymentId) {
        return this.invoiceDao.findByPaymentId(paymentId);
    }

    // Build InvoiceDetails object for TxtInvoiceGenerator
    private InvoiceDetails buildInvoiceDetails(Payment payment, Student student, Guardian guardian, PaymentConcept concept) {
        InvoiceDetails details = new InvoiceDetails();
        details.studentName = student.getFullName();
        details.studentDocument = student.getDocumentType().getDbValue() + " " + student.getDocumentNumber();

        if (guardian != null) {
            details.guardianName = guardian.getFullName();
            details.guardianDocument = guardian.getDocumentType().getDbValue() + " " + guardian.getDocumentNumber();
            details.guardianPhone = guardian.getPhone();
            details.guardianEmail = guardian.getEmail();
            details.guardianAddress = guardian.getAddress();
        } else {
            details.guardianName = student.getFullName();
            details.guardianDocument = student.getDocumentNumber();
            details.guardianPhone = (student.getPhone() != null) ? student.getPhone() : "N/A";
            details.guardianEmail = (student.getEmail() != null) ? student.getEmail() : "N/A";
            details.guardianAddress = student.getAddress();
        }

        details.conceptCode = concept.getCode();
        details.conceptName = concept.getName();
        details.baseAmount = payment.getAmountPaid() + payment.getDiscountApplied();
        details.discount = payment.getDiscountApplied();
        details.netTotal = payment.getAmountPaid();
        details.paymentMethod = payment.getPaymentMethod().getDbValue();
        details.academicYear = payment.getAcademicYear();
        details.remarks = payment.getRemarks();
        return details;
    }
}
