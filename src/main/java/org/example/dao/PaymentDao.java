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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PaymentDao {

    // Simple helper class to show joined payment info in history and delinquent reports
    public static class PaymentDetail {
        public int paymentId;
        public String receiptNumber;
        public int studentId;
        public String studentName;
        public int conceptId;
        public String conceptName;
        public int academicYear;
        public double amountPaid;
        public double discountApplied;
        public PaymentMethod method;
        public LocalDateTime paymentDate;
        public PaymentStatus status;
        public String remarks;

        @Override
        public String toString() {
            String rcp = (this.receiptNumber != null) ? this.receiptNumber : "SIN RECIBO";
            String met = (this.method != null) ? this.method.getDbValue() : "";
            String st = (this.status != null) ? this.status.getDbValue() : "";
            return "Pago #" + this.paymentId
                    + " | Recibo: " + rcp
                    + " | Estudiante: " + this.studentName
                    + " | Concepto: " + this.conceptName
                    + " | Monto: $" + this.amountPaid
                    + " | Desc: $" + this.discountApplied
                    + " | Metodo: " + met
                    + " | Estado: " + st
                    + " | Fecha: " + this.paymentDate;
        }
    }

    // Insert a pending charge without generating an invoice
    public int insertPendingPayment(Payment payment) {
        String sql = "INSERT INTO pagos (numero_recibo, id_estudiante, id_concepto, año_academico, "
                + "monto_pagado, metodo_pago, fecha_pago, estado_pago, descuento_aplicado, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, payment.getReceiptNumber());
            ps.setInt(2, payment.getStudentId());
            ps.setInt(3, payment.getConceptId());
            ps.setInt(4, payment.getAcademicYear());
            ps.setDouble(5, payment.getAmountPaid());
            ps.setString(6, payment.getPaymentMethod().getDbValue());
            LocalDateTime dt = (payment.getPaymentDate() != null) ? payment.getPaymentDate() : LocalDateTime.now();
            ps.setTimestamp(7, Timestamp.valueOf(dt));
            ps.setString(8, PaymentStatus.PENDING.getDbValue());
            ps.setDouble(9, payment.getDiscountApplied());
            ps.setString(10, payment.getRemarks());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        payment.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "insertPendingPayment", e);
        }
        return -1;
    }

    // Register a confirmed payment AND generate its DIAN invoice (.txt + database row) inside one transaction
    // Returns generated invoice file path, or null if it fails
    public String registerPaymentWithInvoice(Payment payment, InvoiceDetails details) {
        String insertPaySql = "INSERT INTO pagos (numero_recibo, id_estudiante, id_concepto, año_academico, "
                + "monto_pagado, metodo_pago, fecha_pago, estado_pago, descuento_aplicado, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            LocalDateTime now = (payment.getPaymentDate() != null) ? payment.getPaymentDate() : LocalDateTime.now();
            payment.setPaymentDate(now);
            payment.setStatus(PaymentStatus.PAID);

            int paymentId = -1;
            try (PreparedStatement psPay = conn.prepareStatement(insertPaySql, Statement.RETURN_GENERATED_KEYS)) {
                psPay.setString(1, payment.getReceiptNumber());
                psPay.setInt(2, payment.getStudentId());
                psPay.setInt(3, payment.getConceptId());
                psPay.setInt(4, payment.getAcademicYear());
                psPay.setDouble(5, payment.getAmountPaid());
                psPay.setString(6, payment.getPaymentMethod().getDbValue());
                psPay.setTimestamp(7, Timestamp.valueOf(now));
                psPay.setString(8, PaymentStatus.PAID.getDbValue());
                psPay.setDouble(9, payment.getDiscountApplied());
                psPay.setString(10, payment.getRemarks());

                if (psPay.executeUpdate() > 0) {
                    try (ResultSet rs = psPay.getGeneratedKeys()) {
                        if (rs.next()) {
                            paymentId = rs.getInt(1);
                            payment.setId(paymentId);
                        }
                    }
                }
            }

            if (paymentId <= 0) {
                conn.rollback();
                return null;
            }

            String filePath = this.createInvoiceRecordAndFile(conn, payment, details);
            if (filePath == null) {
                conn.rollback();
                return null;
            }

            conn.commit();
            return filePath;
        } catch (SQLException | IOException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ErrorLogger.log(PaymentDao.class, "registerPaymentWithInvoice-rollback", ex);
                }
            }
            ErrorLogger.log(PaymentDao.class, "registerPaymentWithInvoice", e);
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ErrorLogger.log(PaymentDao.class, "registerPaymentWithInvoice-close", ex);
                }
            }
        }
    }

    // Mark an existing pending payment as paid and emit its invoice in one transaction
    public String settlePendingPayment(int paymentId, String receiptNumber, PaymentMethod method, InvoiceDetails details) {
        String updatePaySql = "UPDATE pagos SET numero_recibo = ?, metodo_pago = ?, fecha_pago = ?, estado_pago = 'Pagado' "
                + "WHERE id_pago = ? AND estado_pago = 'Pendiente'";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            Payment existing = this.findByIdWithConnection(conn, paymentId);
            if (existing == null || existing.getStatus() != PaymentStatus.PENDING) {
                conn.rollback();
                return null;
            }

            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement psUpd = conn.prepareStatement(updatePaySql)) {
                psUpd.setString(1, receiptNumber);
                psUpd.setString(2, method.getDbValue());
                psUpd.setTimestamp(3, Timestamp.valueOf(now));
                psUpd.setInt(4, paymentId);
                if (psUpd.executeUpdate() <= 0) {
                    conn.rollback();
                    return null;
                }
            }

            existing.setReceiptNumber(receiptNumber);
            existing.setPaymentMethod(method);
            existing.setPaymentDate(now);
            existing.setStatus(PaymentStatus.PAID);

            String filePath = this.createInvoiceRecordAndFile(conn, existing, details);
            if (filePath == null) {
                conn.rollback();
                return null;
            }

            conn.commit();
            return filePath;
        } catch (SQLException | IOException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ErrorLogger.log(PaymentDao.class, "settlePendingPayment-rollback", ex);
                }
            }
            ErrorLogger.log(PaymentDao.class, "settlePendingPayment", e);
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ErrorLogger.log(PaymentDao.class, "settlePendingPayment-close", ex);
                }
            }
        }
    }

    // Shared helper to insert facturas row, write .txt DIAN invoice, and update final invoice metadata
    private String createInvoiceRecordAndFile(Connection conn, Payment payment, InvoiceDetails details)
            throws SQLException, IOException {
        String insertInvSql = "INSERT INTO facturas (id_pago, numero_factura, cufe, nombre_archivo_pdf, "
                + "ruta_archivo, fecha_emision, subtotal, iva, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateInvSql = "UPDATE facturas SET numero_factura = ?, cufe = ?, nombre_archivo_pdf = ?, ruta_archivo = ? "
                + "WHERE id_factura = ?";

        LocalDateTime issueDate = (payment.getPaymentDate() != null) ? payment.getPaymentDate() : LocalDateTime.now();
        details.issueDate = issueDate;

        int invoiceId = -1;
        String tempCode = "TEMP-" + payment.getId();
        try (PreparedStatement psInv = conn.prepareStatement(insertInvSql, Statement.RETURN_GENERATED_KEYS)) {
            psInv.setInt(1, payment.getId());
            psInv.setString(2, tempCode);
            psInv.setString(3, tempCode);
            psInv.setString(4, tempCode);
            psInv.setString(5, tempCode);
            psInv.setTimestamp(6, Timestamp.valueOf(issueDate));
            psInv.setDouble(7, details.baseAmount);
            psInv.setDouble(8, 0.0);
            psInv.setDouble(9, details.netTotal);

            if (psInv.executeUpdate() > 0) {
                try (ResultSet rs = psInv.getGeneratedKeys()) {
                    if (rs.next()) {
                        invoiceId = rs.getInt(1);
                    }
                }
            }
        }

        if (invoiceId <= 0) {
            return null;
        }

        String invoiceNumber = String.format("FE-%d-%06d", payment.getAcademicYear(), invoiceId);
        String dateStr = issueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String cufe = TxtInvoiceGenerator.calculateCufe(invoiceNumber, dateStr, details.netTotal, details.guardianDocument);
        String fileName = String.format("factura_%06d.txt", invoiceId);

        details.invoiceId = invoiceId;
        details.invoiceNumber = invoiceNumber;
        String fullPath = TxtInvoiceGenerator.generateInvoiceFile(details);

        try (PreparedStatement psUpd = conn.prepareStatement(updateInvSql)) {
            psUpd.setString(1, invoiceNumber);
            psUpd.setString(2, cufe);
            psUpd.setString(3, fileName);
            psUpd.setString(4, fullPath);
            psUpd.setInt(5, invoiceId);
            psUpd.executeUpdate();
        }

        return fullPath;
    }

    // Generate next suggested receipt number for a given year
    public String generateNextReceiptNumber(int academicYear) {
        String sql = "SELECT COALESCE(MAX(id_pago), 0) + 1 FROM pagos";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int nextId = rs.getInt(1);
                return String.format("REC-%d-%04d", academicYear, nextId);
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "generateNextReceiptNumber", e);
        }
        return "REC-" + academicYear + "-" + System.currentTimeMillis();
    }

    // Find payment by id
    public Payment findById(int paymentId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return this.findByIdWithConnection(conn, paymentId);
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "findById", e);
            return null;
        }
    }

    private Payment findByIdWithConnection(Connection conn, int paymentId) throws SQLException {
        String sql = "SELECT * FROM pagos WHERE id_pago = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapPaymentRow(rs);
                }
            }
        }
        return null;
    }

    // List all payments of a specific student
    public List<PaymentDetail> findPaymentDetailsByStudent(int studentId) {
        List<PaymentDetail> list = new ArrayList<>();
        String sql = "SELECT pg.*, CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, cp.nombre_concepto "
                + "FROM pagos pg "
                + "INNER JOIN estudiantes e ON pg.id_estudiante = e.id_estudiante "
                + "INNER JOIN conceptos_pago cp ON pg.id_concepto = cp.id_concepto "
                + "WHERE pg.id_estudiante = ? "
                + "ORDER BY pg.fecha_pago DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapDetailRow(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "findPaymentDetailsByStudent", e);
        }
        return list;
    }

    // List all payments across all children belonging to a guardian
    public List<PaymentDetail> findPaymentDetailsByGuardian(int guardianId) {
        List<PaymentDetail> list = new ArrayList<>();
        String sql = "SELECT pg.*, CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, cp.nombre_concepto "
                + "FROM pagos pg "
                + "INNER JOIN estudiantes e ON pg.id_estudiante = e.id_estudiante "
                + "INNER JOIN estudiante_acudiente ea ON e.id_estudiante = ea.id_estudiante "
                + "INNER JOIN conceptos_pago cp ON pg.id_concepto = cp.id_concepto "
                + "WHERE ea.id_acudiente = ? "
                + "ORDER BY pg.fecha_pago DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, guardianId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapDetailRow(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "findPaymentDetailsByGuardian", e);
        }
        return list;
    }

    // List all payments in the database (for Stream filtering of overdue accounts and total revenue)
    public List<PaymentDetail> findAllPaymentDetails() {
        List<PaymentDetail> list = new ArrayList<>();
        String sql = "SELECT pg.*, CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, cp.nombre_concepto "
                + "FROM pagos pg "
                + "INNER JOIN estudiantes e ON pg.id_estudiante = e.id_estudiante "
                + "INNER JOIN conceptos_pago cp ON pg.id_concepto = cp.id_concepto "
                + "ORDER BY pg.fecha_pago DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(this.mapDetailRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentDao.class, "findAllPaymentDetails", e);
        }
        return list;
    }

    // Helper to map Payment model row
    private Payment mapPaymentRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id_pago"));
        p.setReceiptNumber(rs.getString("numero_recibo"));
        p.setStudentId(rs.getInt("id_estudiante"));
        p.setConceptId(rs.getInt("id_concepto"));
        p.setAcademicYear(rs.getInt("año_academico"));
        p.setAmountPaid(rs.getDouble("monto_pagado"));
        p.setPaymentMethod(PaymentMethod.fromDbValue(rs.getString("metodo_pago")));
        Timestamp ts = rs.getTimestamp("fecha_pago");
        if (ts != null) {
            p.setPaymentDate(ts.toLocalDateTime());
        }
        p.setStatus(PaymentStatus.fromDbValue(rs.getString("estado_pago")));
        p.setDiscountApplied(rs.getDouble("descuento_aplicado"));
        p.setRemarks(rs.getString("observaciones"));
        return p;
    }

    // Helper to map joined PaymentDetail row
    private PaymentDetail mapDetailRow(ResultSet rs) throws SQLException {
        PaymentDetail pd = new PaymentDetail();
        pd.paymentId = rs.getInt("id_pago");
        pd.receiptNumber = rs.getString("numero_recibo");
        pd.studentId = rs.getInt("id_estudiante");
        pd.studentName = rs.getString("estudiante");
        pd.conceptId = rs.getInt("id_concepto");
        pd.conceptName = rs.getString("nombre_concepto");
        pd.academicYear = rs.getInt("año_academico");
        pd.amountPaid = rs.getDouble("monto_pagado");
        pd.discountApplied = rs.getDouble("descuento_aplicado");
        pd.method = PaymentMethod.fromDbValue(rs.getString("metodo_pago"));
        Timestamp ts = rs.getTimestamp("fecha_pago");
        if (ts != null) {
            pd.paymentDate = ts.toLocalDateTime();
        }
        pd.status = PaymentStatus.fromDbValue(rs.getString("estado_pago"));
        pd.remarks = rs.getString("observaciones");
        return pd;
    }
}
