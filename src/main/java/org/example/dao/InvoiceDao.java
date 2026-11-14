package org.example.dao;

import org.example.model.Invoice;
import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDao {

    // Find invoice by payment id
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

    // Find invoice by invoice number
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

    // Helper to convert ResultSet row to Invoice
    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id_factura"));
        inv.setPaymentId(rs.getInt("id_pago"));
        inv.setInvoiceNumber(rs.getString("numero_factura"));
        inv.setCufe(rs.getString("cufe"));
        inv.setFileName(rs.getString("nombre_archivo_pdf"));
        inv.setFilePath(rs.getString("ruta_archivo"));
        Timestamp ts = rs.getTimestamp("fecha_emision");
        if (ts != null) {
            inv.setIssueDate(ts.toLocalDateTime());
        }
        inv.setSubtotal(rs.getDouble("subtotal"));
        inv.setIva(rs.getDouble("iva"));
        inv.setTotal(rs.getDouble("total"));
        return inv;
    }
}
