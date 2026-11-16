package org.example.dao;

import org.example.model.Invoice;
import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDao {

    // Find invoice by payment ID
    public Invoice findByPaymentId(int paymentId) {
        String sql = "SELECT * FROM facturas WHERE id_pago = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(InvoiceDao.class, "findByPaymentId", e);
        }
        return null;
    }

    // Find invoice by consecutive number
    public Invoice findByInvoiceNumber(String invoiceNumber) {
        String sql = "SELECT * FROM facturas WHERE numero_factura = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(InvoiceDao.class, "findByInvoiceNumber", e);
        }
        return null;
    }

    // List all generated invoices
    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM facturas ORDER BY fecha_emision DESC, id_factura DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(this.mapRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(InvoiceDao.class, "findAll", e);
        }
        return list;
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id_factura"));
        inv.setInvoiceNumber(rs.getString("numero_factura"));
        inv.setPaymentId(rs.getInt("id_pago"));
        Date d = rs.getDate("fecha_emision");
        if (d != null) {
            inv.setIssueDate(d.toLocalDate());
        }
        inv.setCufe(rs.getString("cufe"));
        inv.setPdfFileName(rs.getString("nombre_archivo_pdf"));
        inv.setFilePath(rs.getString("ruta_archivo"));
        return inv;
    }
}
