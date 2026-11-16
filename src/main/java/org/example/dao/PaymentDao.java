package org.example.dao;

import org.example.model.Payment;
import org.example.model.PaymentMethod;
import org.example.model.PaymentStatus;
import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;
import org.example.util.TxtInvoiceGenerator;
import org.example.util.TxtInvoiceGenerator.InvoiceDetails;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentDao {

    // Helper class with joined details for history and delinquent reports
    public static class PaymentDetail {
        public int paymentId;
        public String receiptNumber;
        public int studentId;
        public String studentName;
        public int guardianId;
        public String guardianName;
        public int conceptId;
        public String conceptName;
        public int academicYear;
        public String academicPeriod;
        public double amountPaid;
        public double discountApplied;
        public PaymentMethod method;
        public LocalDate paymentDate;
        public PaymentStatus status;
        public String notes;

        @Override
        public String toString() {
            String st = (this.status != null) ? this.status.getDbValue() : "";
            String m = (this.method != null) ? this.method.getDbValue() : "";
            String rec = (this.receiptNumber != null && !this.receiptNumber.isEmpty()) ? this.receiptNumber : "-";
            return String.format("Pago #%d | Rec: %s | Est: %s | Concepto: %s | Monto: $%,.2f | Fecha: %s | %s | %s",
                    this.paymentId, rec, this.studentName, this.conceptName,
                    this.amountPaid, this.paymentDate, m, st);
        }
    }

    // Insert a pending payment charge (without invoice yet)
    public int insertPendingPayment(Payment payment) {
        String sql = "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, descuento_aplicado, "
                + "estado_pago, fecha_pago, metodo_pago, numero_recibo, año_academico, periodo_academico, notas) "
                + "VALUES (?, ?, ?, ?, ?, 'Pendiente', ?, ?, NULL, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getStudentId());
            if (payment.getGuardianId() > 0) {
                ps.setInt(2, payment.getGuardianId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, payment.getConceptId());
            ps.setDouble(4, payment.getAmountPaid());
            ps.setDouble(5, payment.getDiscountApplied());
            ps.setDate(6, Date.valueOf(payment.getPaymentDate()));
            ps.setString(7, payment.getPaymentMethod().getDbValue());
            ps.setInt(8, payment.getAcademicYear());
            ps.setString(9, payment.getAcademicPeriod() != null ? payment.getAcademicPeriod() : "Anual");
            ps.setString(10, payment.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    payment.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "insertPendingPayment", e);
        }
        return -1;
    }

    // Register a confirmed payment, insert DIAN invoice row, and generate the TXT file transactionally
    public String registerPaymentWithInvoice(Payment payment, InvoiceDetails details) {
        String insertPaymentSql = "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, descuento_aplicado, "
                + "estado_pago, fecha_pago, metodo_pago, numero_recibo, año_academico, periodo_academico, notas) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String countInvoicesSql = "SELECT COUNT(*) + 1 FROM facturas";
        String insertInvoiceSql = "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, "
                + "nombre_archivo_pdf, ruta_archivo) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            int paymentId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertPaymentSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, payment.getStudentId());
                if (payment.getGuardianId() > 0) {
                    ps.setInt(2, payment.getGuardianId());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setInt(3, payment.getConceptId());
                ps.setDouble(4, payment.getAmountPaid());
                ps.setDouble(5, payment.getDiscountApplied());
                ps.setString(6, PaymentStatus.PAID.getDbValue());
                ps.setDate(7, Date.valueOf(payment.getPaymentDate()));
                ps.setString(8, payment.getPaymentMethod().getDbValue());
                ps.setString(9, payment.getReceiptNumber());
                ps.setInt(10, payment.getAcademicYear());
                ps.setString(11, payment.getAcademicPeriod() != null ? payment.getAcademicPeriod() : "Anual");
                ps.setString(12, payment.getNotes());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        paymentId = rs.getInt(1);
                        payment.setId(paymentId);
                    }
                }
            }

            if (paymentId <= 0) {
                conn.rollback();
                return null;
            }

            String filePath = this.createInvoiceForPayment(conn, paymentId, payment.getAcademicYear(),
                    payment.getPaymentDate(), details, countInvoicesSql, insertInvoiceSql);
            conn.commit();
            return filePath;
        } catch (SQLException | IOException e) {
            ErrorLogger.log(PaymentDao.class, "registerPaymentWithInvoice", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ErrorLogger.log(PaymentDao.class, "registerPaymentWithInvoice.rollback", ex);
                }
            }
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ErrorLogger.log(PaymentDao.class, "registerPaymentWithInvoice.finally", ex);
                }
            }
        }
    }

    // Settle an existing pending payment and generate its TXT invoice transactionally
    public String settlePendingPayment(int paymentId, String receiptNumber, PaymentMethod method, InvoiceDetails details) {
        String updateSql = "UPDATE pagos SET estado_pago = 'Pagado', fecha_pago = ?, metodo_pago = ?, numero_recibo = ? "
                + "WHERE id_pago = ? AND estado_pago = 'Pendiente'";
        String countInvoicesSql = "SELECT COUNT(*) + 1 FROM facturas";
        String insertInvoiceSql = "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, "
                + "nombre_archivo_pdf, ruta_archivo) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            LocalDate today = LocalDate.now();
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setDate(1, Date.valueOf(today));
                ps.setString(2, method.getDbValue());
                ps.setString(3, receiptNumber);
                ps.setInt(4, paymentId);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return null;
                }
            }

            String filePath = this.createInvoiceForPayment(conn, paymentId, details.academicYear,
                    today, details, countInvoicesSql, insertInvoiceSql);
            conn.commit();
            return filePath;
        } catch (SQLException | IOException e) {
            ErrorLogger.log(PaymentDao.class, "settlePendingPayment", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ErrorLogger.log(PaymentDao.class, "settlePendingPayment.rollback", ex);
                }
            }
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ErrorLogger.log(PaymentDao.class, "settlePendingPayment.finally", ex);
                }
            }
        }
    }

    private String createInvoiceForPayment(Connection conn, int paymentId, int academicYear, LocalDate issueDate,
                                           InvoiceDetails details, String countSql, String insertSql)
            throws SQLException, IOException {
        int nextSeq = 1;
        try (PreparedStatement ps = conn.prepareStatement(countSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                nextSeq = rs.getInt(1);
            }
        }

        String invoiceNumber = String.format("FE-%d-%06d", academicYear, nextSeq);
        String fileName = String.format("factura_%06d.txt", nextSeq);
        String filePath = "invoices/" + fileName;

        details.invoiceId = nextSeq;
        details.invoiceNumber = invoiceNumber;
        String dateStr = details.issueDate.toString();
        String cufe = TxtInvoiceGenerator.calculateCufe(invoiceNumber, dateStr, details.netTotal, details.guardianDocument);

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, invoiceNumber);
            ps.setInt(2, paymentId);
            ps.setDate(3, Date.valueOf(issueDate));
            ps.setString(4, cufe);
            ps.setString(5, fileName);
            ps.setString(6, filePath);
            ps.executeUpdate();
        }

        return TxtInvoiceGenerator.generateInvoiceFile(details);
    }

    // Generate next unique receipt number
    public String generateNextReceiptNumber(int academicYear) {
        String sql = "SELECT COUNT(*) + 1 FROM pagos WHERE año_academico = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return String.format("REC-%d-%04d", academicYear, rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "generateNextReceiptNumber", e);
        }
        return "REC-" + academicYear + "-" + (System.currentTimeMillis() % 10000);
    }

    // Find payment by ID
    public Payment findById(int paymentId) {
        String sql = "SELECT * FROM pagos WHERE id_pago = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payment p = new Payment();
                    p.setId(rs.getInt("id_pago"));
                    p.setStudentId(rs.getInt("id_estudiante"));
                    p.setGuardianId(rs.getInt("id_acudiente"));
                    p.setConceptId(rs.getInt("id_concepto"));
                    p.setAmountPaid(rs.getDouble("monto_pagado"));
                    p.setDiscountApplied(rs.getDouble("descuento_aplicado"));
                    p.setStatus(PaymentStatus.fromDbValue(rs.getString("estado_pago")));
                    Date d = rs.getDate("fecha_pago");
                    if (d != null) {
                        p.setPaymentDate(d.toLocalDate());
                    }
                    p.setPaymentMethod(PaymentMethod.fromDbValue(rs.getString("metodo_pago")));
                    p.setReceiptNumber(rs.getString("numero_recibo"));
                    p.setAcademicYear(rs.getInt("año_academico"));
                    p.setAcademicPeriod(rs.getString("periodo_academico"));
                    p.setNotes(rs.getString("notas"));
                    return p;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "findById", e);
        }
        return null;
    }

    // List payment details of a student
    public List<PaymentDetail> findPaymentDetailsByStudent(int studentId) {
        List<PaymentDetail> list = new ArrayList<>();
        String sql = "SELECT p.id_pago, p.numero_recibo, p.id_estudiante, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "p.id_acudiente, CONCAT_WS(' ', a.primer_nombre, a.primer_apellido) AS guardian_name, "
                + "p.id_concepto, cp.nombre_concepto, p.año_academico, p.periodo_academico, "
                + "p.monto_pagado, p.descuento_aplicado, p.metodo_pago, p.fecha_pago, p.estado_pago, p.notas "
                + "FROM pagos p "
                + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                + "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente "
                + "INNER JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto "
                + "WHERE p.id_estudiante = ? ORDER BY p.fecha_pago DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapDetail(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "findPaymentDetailsByStudent", e);
        }
        return list;
    }

    // Consolidated payment history for all siblings under a guardian
    public List<PaymentDetail> findPaymentDetailsByGuardian(int guardianId) {
        List<PaymentDetail> list = new ArrayList<>();
        String sql = "SELECT DISTINCT p.id_pago, p.numero_recibo, p.id_estudiante, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "p.id_acudiente, CONCAT_WS(' ', a.primer_nombre, a.primer_apellido) AS guardian_name, "
                + "p.id_concepto, cp.nombre_concepto, p.año_academico, p.periodo_academico, "
                + "p.monto_pagado, p.descuento_aplicado, p.metodo_pago, p.fecha_pago, p.estado_pago, p.notas "
                + "FROM pagos p "
                + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                + "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente "
                + "INNER JOIN estudiante_acudiente ea ON e.id_estudiante = ea.id_estudiante "
                + "INNER JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto "
                + "WHERE ea.id_acudiente = ? OR p.id_acudiente = ? ORDER BY p.fecha_pago DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guardianId);
            ps.setInt(2, guardianId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapDetail(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "findPaymentDetailsByGuardian", e);
        }
        return list;
    }

    // List all payments for streams and reports
    public List<PaymentDetail> findAllPaymentDetails() {
        List<PaymentDetail> list = new ArrayList<>();
        String sql = "SELECT p.id_pago, p.numero_recibo, p.id_estudiante, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "p.id_acudiente, CONCAT_WS(' ', a.primer_nombre, a.primer_apellido) AS guardian_name, "
                + "p.id_concepto, cp.nombre_concepto, p.año_academico, p.periodo_academico, "
                + "p.monto_pagado, p.descuento_aplicado, p.metodo_pago, p.fecha_pago, p.estado_pago, p.notas "
                + "FROM pagos p "
                + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                + "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente "
                + "INNER JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto "
                + "ORDER BY p.fecha_pago DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(this.mapDetail(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "findAllPaymentDetails", e);
        }
        return list;
    }

    private PaymentDetail mapDetail(ResultSet rs) throws SQLException {
        PaymentDetail d = new PaymentDetail();
        d.paymentId = rs.getInt("id_pago");
        d.receiptNumber = rs.getString("numero_recibo");
        d.studentId = rs.getInt("id_estudiante");
        d.studentName = rs.getString("student_name");
        d.guardianId = rs.getInt("id_acudiente");
        d.guardianName = rs.getString("guardian_name");
        d.conceptId = rs.getInt("id_concepto");
        d.conceptName = rs.getString("nombre_concepto");
        d.academicYear = rs.getInt("año_academico");
        d.academicPeriod = rs.getString("periodo_academico");
        d.amountPaid = rs.getDouble("monto_pagado");
        d.discountApplied = rs.getDouble("descuento_aplicado");
        d.method = PaymentMethod.fromDbValue(rs.getString("metodo_pago"));
        Date dt = rs.getDate("fecha_pago");
        if (dt != null) {
            d.paymentDate = dt.toLocalDate();
        }
        d.status = PaymentStatus.fromDbValue(rs.getString("estado_pago"));
        d.notes = rs.getString("notas");
        return d;
    }
}
