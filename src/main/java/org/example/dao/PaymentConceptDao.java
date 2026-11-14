package org.example.dao;

import org.example.model.PaymentConcept;
import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PaymentConceptDao {

    // Insert a new payment concept
    public int insert(PaymentConcept concept) {
        String sql = "INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, descripcion, monto_base, es_periodico) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, concept.getCode());
            ps.setString(2, concept.getName());
            ps.setString(3, concept.getDescription());
            ps.setDouble(4, concept.getBaseAmount());
            ps.setBoolean(5, concept.isPeriodic());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        concept.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentConceptDao.class, "insert", e);
        }
        return -1;
    }

    // Update base amount of an existing concept
    public boolean updateBaseAmount(int conceptId, double newAmount) {
        String sql = "UPDATE conceptos_pago SET monto_base = ? WHERE id_concepto = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newAmount);
            ps.setInt(2, conceptId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(PaymentConceptDao.class, "updateBaseAmount", e);
            return false;
        }
    }

    // Find concept by id
    public PaymentConcept findById(int id) {
        String sql = "SELECT * FROM conceptos_pago WHERE id_concepto = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentConceptDao.class, "findById", e);
        }
        return null;
    }

    // List all payment concepts
    public List<PaymentConcept> findAll() {
        List<PaymentConcept> list = new ArrayList<>();
        String sql = "SELECT * FROM conceptos_pago ORDER BY id_concepto";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(this.mapRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentConceptDao.class, "findAll", e);
        }
        return list;
    }

    // Helper to convert ResultSet row to PaymentConcept
    private PaymentConcept mapRow(ResultSet rs) throws SQLException {
        PaymentConcept pc = new PaymentConcept();
        pc.setId(rs.getInt("id_concepto"));
        pc.setCode(rs.getString("codigo_concepto"));
        pc.setName(rs.getString("nombre_concepto"));
        pc.setDescription(rs.getString("descripcion"));
        pc.setBaseAmount(rs.getDouble("monto_base"));
        pc.setPeriodic(rs.getBoolean("es_periodico"));
        return pc;
    }
}
