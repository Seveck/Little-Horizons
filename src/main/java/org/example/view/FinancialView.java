package org.example.view;

import org.example.controller.FinancialController;
import org.example.controller.StudentController;
import org.example.dao.PaymentDao.PaymentDetail;
import org.example.model.Guardian;
import org.example.model.Invoice;
import org.example.model.Payment;
import org.example.model.PaymentConcept;
import org.example.model.PaymentMethod;
import org.example.model.PaymentStatus;
import org.example.model.Student;
import org.example.util.InputValidator;

import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class FinancialView {

    private final Scanner scanner;
    private final FinancialController controller;
    private final StudentController studentController;

    // Constructor
    public FinancialView(Scanner scanner) {
        this.scanner = scanner;
        this.controller = new FinancialController();
        this.studentController = new StudentController();
    }

    // Main menu loop for Financial & Billing module
    public void showMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("     MODULO 3: GESTION FINANCIERA Y FACTURACION");
            System.out.println("==================================================");
            System.out.println("1. Listar catalogo de conceptos de pago");
            System.out.println("2. Registrar nuevo concepto de pago");
            System.out.println("3. Actualizar valor base de un concepto");
            System.out.println("4. Registrar pago y generar factura electronica (.txt)");
            System.out.println("5. Registrar cobro pendiente (cuenta por cobrar)");
            System.out.println("6. Pagar saldo pendiente y emitir factura (.txt)");
            System.out.println("7. Consultar historial de pagos de un estudiante");
            System.out.println("8. Consultar historial de pagos de un acudiente (hijos)");
            System.out.println("9. Reporte de morosos (pagos pendientes > 30 dias)");
            System.out.println("10. Consultar facturas electronicas y recaudo anual");
            System.out.println("11. Buscar factura electronica por numero");
            System.out.println("0. Volver al menu principal");
            System.out.println("Digite su opcion:");

            try {
                int option = this.scanner.nextInt();
                this.scanner.nextLine();

                switch (option) {
                    case 1:
                        this.listConceptsMenu();
                        break;
                    case 2:
                        this.registerConceptMenu();
                        break;
                    case 3:
                        this.updateConceptAmountMenu();
                        break;
                    case 4:
                        this.registerPaymentMenu();
                        break;
                    case 5:
                        this.registerPendingChargeMenu();
                        break;
                    case 6:
                        this.settlePendingPaymentMenu();
                        break;
                    case 7:
                        this.studentPaymentHistoryMenu();
                        break;
                    case 8:
                        this.guardianPaymentHistoryMenu();
                        break;
                    case 9:
                        this.delinquentReportMenu();
                        break;
                    case 10:
                        this.invoicesAndRevenueMenu();
                        break;
                    case 11:
                        this.searchInvoiceMenu();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Error, opcion invalida");
                }
            } catch (InputMismatchException e) {
                System.out.println("Error, digito invalido");
                this.scanner.nextLine();
            }
        }
    }

    // 1. List payment concepts
    private void listConceptsMenu() {
        System.out.println("\n--- CATALOGO DE CONCEPTOS DE PAGO ---");
        List<PaymentConcept> list = this.controller.listAllConcepts();
        if (list.isEmpty()) {
            System.out.println("No hay conceptos registrados.");
            return;
        }
        for (PaymentConcept pc : list) {
            System.out.println(pc.toString());
        }
    }

    // 2. Register new payment concept
    private void registerConceptMenu() {
        System.out.println("\n--- REGISTRAR NUEVO CONCEPTO DE PAGO ---");
        String code = InputValidator.readString(this.scanner, "Codigo del concepto (ej. UNI-01): ").toUpperCase();
        String name = InputValidator.readString(this.scanner, "Nombre del concepto: ");
        String desc = InputValidator.readOptionalString(this.scanner, "Descripcion: ");
        double baseAmount = InputValidator.readPositiveDouble(this.scanner, "Monto base ($): ");
        boolean recurring = InputValidator.readConfirmation(this.scanner, "¿Es un cobro recurrente (mensual)?");

        PaymentConcept pc = new PaymentConcept();
        pc.setCode(code);
        pc.setName(name);
        pc.setDescription(desc.isEmpty() ? null : desc);
        pc.setBaseAmount(baseAmount);
        pc.setRecurring(recurring);
        pc.setActive(true);

        int id = this.controller.registerConcept(pc);
        if (id > 0) {
            System.out.println("Concepto registrado con exito. ID: " + id);
        } else {
            System.out.println("Error al registrar el concepto (verifique codigo duplicado).");
        }
    }

    // 3. Update concept base amount
    private void updateConceptAmountMenu() {
        System.out.println("\n--- ACTUALIZAR VALOR BASE DE CONCEPTO ---");
        this.listConceptsMenu();
        int conceptId = InputValidator.readInt(this.scanner, "ID del concepto a actualizar: ");
        PaymentConcept concept = this.controller.findConceptById(conceptId);
        if (concept == null) {
            System.out.println("Concepto no encontrado.");
            return;
        }
        System.out.println("Concepto seleccionado: " + concept.getName() + " | Valor actual: $" + concept.getBaseAmount());
        double newAmount = InputValidator.readPositiveDouble(this.scanner, "Nuevo monto base ($): ");
        if (this.controller.updateConceptBaseAmount(conceptId, newAmount)) {
            System.out.println("Valor base actualizado correctamente.");
        } else {
            System.out.println("No se pudo actualizar el valor base.");
        }
    }

    // 4. Register confirmed payment + auto DIAN .txt invoice
    private void registerPaymentMenu() {
        System.out.println("\n--- REGISTRAR PAGO Y FACTURA ELECTRONICA ---");
        int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        System.out.println("Estudiante: " + student.getFullName());

        Guardian responsibleGuardian = this.findResponsibleGuardian(studentId);
        int guardianId = 0;
        if (responsibleGuardian != null) {
            guardianId = responsibleGuardian.getId();
            System.out.println("Acudiente responsable: " + responsibleGuardian.getFullName()
                    + " (" + responsibleGuardian.getDocumentNumber() + ")");
        }

        this.listConceptsMenu();
        int conceptId = InputValidator.readInt(this.scanner, "ID del concepto de pago: ");
        PaymentConcept concept = this.controller.findConceptById(conceptId);
        if (concept == null) {
            System.out.println("Concepto no encontrado.");
            return;
        }

        int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");
        String academicPeriod = this.askPaymentPeriod();
        double amountToCharge = concept.getBaseAmount();
        double discount = 0.0;

        System.out.println("Valor base del concepto: $" + amountToCharge);
        if (InputValidator.readConfirmation(this.scanner, "¿Ajustar valor o aplicar descuento (beca / hermanos)?")) {
            System.out.println("1. Aplicar valor de descuento ($)");
            System.out.println("2. Definir valor final personalizado para este estudiante ($)");
            int adjustOp = InputValidator.readInt(this.scanner, "Digite su opcion: ");
            if (adjustOp == 2) {
                double customTotal = InputValidator.readPositiveDouble(this.scanner, "Valor personalizado a cobrar ($): ");
                if (customTotal < amountToCharge) {
                    discount = amountToCharge - customTotal;
                } else {
                    amountToCharge = customTotal;
                    discount = 0.0;
                }
            } else {
                discount = InputValidator.readPositiveDouble(this.scanner, "Valor del descuento ($): ");
                if (discount >= amountToCharge) {
                    System.out.println("El descuento excede el monto base; se ajustara a 0.");
                    discount = 0.0;
                }
            }
        }
        double netTotal = amountToCharge - discount;
        System.out.println("Total neto a pagar: $" + netTotal);

        PaymentMethod method = this.askPaymentMethod();
        String suggestedReceipt = this.controller.generateNextReceiptNumber(year);
        System.out.println("Numero de recibo sugerido: " + suggestedReceipt);
        String customReceipt = InputValidator.readOptionalString(
                this.scanner, "Presione ENTER para usar el sugerido o escriba otro: ");
        String receiptNumber = customReceipt.isEmpty() ? suggestedReceipt : customReceipt.toUpperCase();
        String notes = InputValidator.readOptionalString(this.scanner, "Notas u observaciones: ");

        Payment payment = new Payment();
        payment.setStudentId(studentId);
        payment.setGuardianId(guardianId);
        payment.setConceptId(conceptId);
        payment.setAmountPaid(netTotal);
        payment.setDiscountApplied(discount);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(method);
        payment.setReceiptNumber(receiptNumber);
        payment.setAcademicYear(year);
        payment.setAcademicPeriod(academicPeriod);
        payment.setNotes(notes.isEmpty() ? null : notes);

        String invoicePath = this.controller.registerConfirmedPayment(payment, student, responsibleGuardian, concept);
        if (invoicePath != null) {
            System.out.println("Pago registrado con exito. Pago ID: " + payment.getId()
                    + " | Recibo: " + receiptNumber);
            System.out.println("Factura electronica (.txt) generada en: " + invoicePath);
        } else {
            System.out.println("Error al registrar el pago o generar la factura (verifique recibo duplicado).");
        }
    }

    // 5. Register pending charge
    private void registerPendingChargeMenu() {
        System.out.println("\n--- REGISTRAR COBRO PENDIENTE ---");
        int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        Guardian responsible = this.findResponsibleGuardian(studentId);
        int guardianId = (responsible != null) ? responsible.getId() : 0;

        this.listConceptsMenu();
        int conceptId = InputValidator.readInt(this.scanner, "ID del concepto: ");
        PaymentConcept concept = this.controller.findConceptById(conceptId);
        if (concept == null) {
            System.out.println("Concepto no encontrado.");
            return;
        }
        int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");
        String academicPeriod = this.askPaymentPeriod();
        double discount = InputValidator.readPositiveDouble(this.scanner, "Descuento aplicado (0 si no aplica): ");
        if (discount >= concept.getBaseAmount()) {
            discount = 0.0;
        }
        double net = concept.getBaseAmount() - discount;
        String notes = InputValidator.readOptionalString(this.scanner, "Notas u observaciones: ");

        Payment pending = new Payment();
        pending.setStudentId(studentId);
        pending.setGuardianId(guardianId);
        pending.setConceptId(conceptId);
        pending.setAmountPaid(net);
        pending.setDiscountApplied(discount);
        pending.setStatus(PaymentStatus.PENDING);
        pending.setPaymentDate(LocalDate.now());
        pending.setPaymentMethod(PaymentMethod.TRANSFER);
        pending.setAcademicYear(year);
        pending.setAcademicPeriod(academicPeriod);
        pending.setNotes(notes.isEmpty() ? null : notes);

        int id = this.controller.registerPendingCharge(pending);
        if (id > 0) {
            System.out.println("Cobro pendiente registrado. ID Pago: " + id);
        } else {
            System.out.println("Error al registrar cobro pendiente.");
        }
    }

    // 6. Pay an existing pending charge and emit invoice
    private void settlePendingPaymentMenu() {
        System.out.println("\n--- PAGAR COBRO PENDIENTE ---");
        int paymentId = InputValidator.readInt(this.scanner, "ID del pago pendiente: ");
        Payment existing = this.controller.findPaymentById(paymentId);
        if (existing == null || existing.getStatus() != PaymentStatus.PENDING) {
            System.out.println("No se encontro un cobro pendiente con ese ID.");
            return;
        }

        Student student = this.studentController.findStudentById(existing.getStudentId());
        PaymentConcept concept = this.controller.findConceptById(existing.getConceptId());
        Guardian guardian = this.findResponsibleGuardian(existing.getStudentId());

        System.out.println("Estudiante: " + student.getFullName() + " | Concepto: " + concept.getName()
                + " | Total a pagar: $" + existing.getAmountPaid());

        PaymentMethod method = this.askPaymentMethod();
        String receipt = this.controller.generateNextReceiptNumber(existing.getAcademicYear());
        String invoicePath = this.controller.settlePendingPayment(
                paymentId, receipt, method, student, guardian, concept, existing);

        if (invoicePath != null) {
            System.out.println("Cobro pagado con exito. Recibo: " + receipt);
            System.out.println("Factura electronica (.txt) generada en: " + invoicePath);
        } else {
            System.out.println("No se pudo procesar el pago pendiente.");
        }
    }

    // 7. Student payment history
    private void studentPaymentHistoryMenu() {
        System.out.println("\n--- HISTORIAL DE PAGOS POR ESTUDIANTE ---");
        int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
        List<PaymentDetail> list = this.controller.getStudentPaymentHistory(studentId);
        if (list.isEmpty()) {
            System.out.println("No hay pagos registrados para ese estudiante.");
            return;
        }
        for (PaymentDetail pd : list) {
            System.out.println(pd.toString());
        }
    }

    // 8. Guardian payment history (across siblings)
    private void guardianPaymentHistoryMenu() {
        System.out.println("\n--- HISTORIAL DE PAGOS POR ACUDIENTE ---");
        int guardianId = InputValidator.readInt(this.scanner, "ID del acudiente: ");
        Guardian g = this.studentController.findGuardianById(guardianId);
        if (g == null) {
            System.out.println("Acudiente no encontrado.");
            return;
        }
        System.out.println("Acudiente: " + g.getFullName());
        List<PaymentDetail> list = this.controller.getGuardianPaymentHistory(guardianId);
        if (list.isEmpty()) {
            System.out.println("No se encontraron pagos asociados a los estudiantes de este acudiente.");
            return;
        }
        for (PaymentDetail pd : list) {
            System.out.println(pd.toString());
        }
    }

    // 9. Delinquent students (> 30 days overdue) using Streams
    private void delinquentReportMenu() {
        System.out.println("\n--- REPORTE DE MOROSOS (MAS DE 30 DIAS PENDIENTE) ---");
        List<PaymentDetail> list = this.controller.getDelinquentPayments(30);
        if (list.isEmpty()) {
            System.out.println("No hay estudiantes con pagos pendientes mayores a 30 dias.");
            return;
        }
        for (PaymentDetail pd : list) {
            System.out.println("MOROSO -> " + pd.toString());
        }
        System.out.println("Total registros en mora: " + list.size());
    }

    // 10. List invoices and annual total revenue (Streams)
    private void invoicesAndRevenueMenu() {
        System.out.println("\n--- FACTURAS ELECTRONICAS Y RECAUDO ANUAL ---");
        int year = InputValidator.readInt(this.scanner, "Año academico para total recaudado: ");
        double collected = this.controller.calculateTotalCollectedByYear(year);
        System.out.println("Total recaudado en " + year + ": $" + String.format("%,.2f", collected));

        List<Invoice> invoices = this.controller.listAllInvoices();
        System.out.println("\nUltimas facturas emitidas (total: " + invoices.size() + "):");
        int limit = Math.min(invoices.size(), 15);
        for (int i = 0; i < limit; i++) {
            System.out.println(invoices.get(i).toString());
        }
    }

    // 11. Search electronic invoice by consecutive number
    private void searchInvoiceMenu() {
        System.out.println("\n--- BUSCAR FACTURA ELECTRONICA ---");
        String invoiceNumber = InputValidator.readString(this.scanner, "Numero de factura (ej. FE-2026-000001): ");
        Invoice invoice = this.controller.findInvoiceByNumber(invoiceNumber);
        if (invoice == null) {
            System.out.println("No se encontro ninguna factura con ese numero.");
            return;
        }
        System.out.println(invoice.toString());
        System.out.println("CUFE: " + invoice.getCufe());
        System.out.println("Archivo TXT: " + invoice.getPdfFileName());
    }

    // Helper to get the primary guardian linked to the student
    private Guardian findResponsibleGuardian(int studentId) {
        List<Guardian> guardians = this.studentController.getGuardiansByStudent(studentId);
        if (guardians.isEmpty()) {
            return null;
        }
        return guardians.get(0);
    }

    private PaymentMethod askPaymentMethod() {
        System.out.println("Metodo de pago (1. Efectivo | 2. Transferencia | 3. Tarjeta Debito | 4. Tarjeta Credito | 5. Cheque):");
        int op = InputValidator.readInt(this.scanner, "Digite su opcion: ");
        switch (op) {
            case 2:
                return PaymentMethod.TRANSFER;
            case 3:
                return PaymentMethod.DEBIT_CARD;
            case 4:
                return PaymentMethod.CREDIT_CARD;
            case 5:
                return PaymentMethod.CHECK;
            default:
                return PaymentMethod.CASH;
        }
    }

    private String askPaymentPeriod() {
        System.out.println("Periodo al que aplica el pago (1. I | 2. II | 3. III | 4. IV | 5. Anual):");
        int op = InputValidator.readInt(this.scanner, "Digite su opcion: ");
        switch (op) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            default:
                return "Anual";
        }
    }
}
