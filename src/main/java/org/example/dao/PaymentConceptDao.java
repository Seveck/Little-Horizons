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
        String sql = "INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, descripcion, monto_base, "
                + "es_recurrente, esta_activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, concept.getCode());
            ps.setString(2, concept.getName());
            ps.setString(3, concept.getDescription());
            ps.setDouble(4, concept.getBaseAmount());
            ps.setBoolean(5, concept.isRecurring());
            ps.setBoolean(6, concept.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    concept.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(PaymentConceptDao.class, "insert", e);
        }
        return -1;
    }

    // Update base amount of a concept
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

    // Find concept by ID
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

    // List all concepts
    public List<PaymentConcept> findAll() {
        List<PaymentConcept> list = new ArrayList<>();
        String sql = "SELECT * FROM conceptos_pago ORDER BY nombre_concepto";
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

    private PaymentConcept mapRow(ResultSet rs) throws SQLException {
        PaymentConcept c = new PaymentConcept();
        c.setId(rs.getInt("id_concepto"));
        c.setCode(rs.getString("codigo_concepto"));
        c.setName(rs.getString("nombre_concepto"));
        c.setDescription(rs.getString("descripcion"));
        c.setBaseAmount(rs.getDouble("monto_base"));
        c.setRecurring(rs.getBoolean("es_recurrente"));
        c.setActive(rs.getBoolean("esta_activo"));
        return c;
    }
}
