package org.example.dao;

import org.example.model.DocumentType;
import org.example.model.Guardian;
import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GuardianDao {

    // Insert a new guardian and return generated id
    public int insert(Guardian guardian) {
        String sql = "INSERT INTO acudientes (tipo_documento, numero_documento, nombres, apellidos, "
                + "parentesco, telefono, email, direccion, profesion_ocupacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, guardian.getDocumentType().getDbValue());
            ps.setString(2, guardian.getDocumentNumber());
            ps.setString(3, guardian.getFirstName());
            ps.setString(4, guardian.getLastName());
            ps.setString(5, guardian.getRelationship());
            ps.setString(6, guardian.getPhone());
            ps.setString(7, guardian.getEmail());
            ps.setString(8, guardian.getAddress());
            ps.setString(9, guardian.getOccupation());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        guardian.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(GuardianDao.class, "insert", e);
        }
        return -1;
    }

    // Update guardian details
    public boolean update(Guardian guardian) {
        String sql = "UPDATE acudientes SET tipo_documento = ?, numero_documento = ?, nombres = ?, apellidos = ?, "
                + "parentesco = ?, telefono = ?, email = ?, direccion = ?, profesion_ocupacion = ? "
                + "WHERE id_acudiente = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, guardian.getDocumentType().getDbValue());
            ps.setString(2, guardian.getDocumentNumber());
            ps.setString(3, guardian.getFirstName());
            ps.setString(4, guardian.getLastName());
            ps.setString(5, guardian.getRelationship());
            ps.setString(6, guardian.getPhone());
            ps.setString(7, guardian.getEmail());
            ps.setString(8, guardian.getAddress());
            ps.setString(9, guardian.getOccupation());
            ps.setInt(10, guardian.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(GuardianDao.class, "update", e);
            return false;
        }
    }

    // Find guardian by id
    public Guardian findById(int id) {
        String sql = "SELECT * FROM acudientes WHERE id_acudiente = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(GuardianDao.class, "findById", e);
        }
        return null;
    }

    // Find guardian by document number
    public Guardian findByDocument(String documentNumber) {
        String sql = "SELECT * FROM acudientes WHERE numero_documento = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(GuardianDao.class, "findByDocument", e);
        }
        return null;
    }

    // List all guardians
    public List<Guardian> findAll() {
        List<Guardian> list = new ArrayList<>();
        String sql = "SELECT * FROM acudientes ORDER BY id_acudiente";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(this.mapRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(GuardianDao.class, "findAll", e);
        }
        return list;
    }

    // List guardians associated with a student
    public List<Guardian> findByStudentId(int studentId) {
        List<Guardian> list = new ArrayList<>();
        String sql = "SELECT a.* FROM acudientes a "
                + "INNER JOIN estudiante_acudiente ea ON a.id_acudiente = ea.id_acudiente "
                + "WHERE ea.id_estudiante = ? ORDER BY ea.es_principal DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(GuardianDao.class, "findByStudentId", e);
        }
        return list;
    }

    // Helper to convert ResultSet row to Guardian object
    private Guardian mapRow(ResultSet rs) throws SQLException {
        Guardian g = new Guardian();
        g.setId(rs.getInt("id_acudiente"));
        g.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        g.setDocumentNumber(rs.getString("numero_documento"));
        g.setFirstName(rs.getString("nombres"));
        g.setLastName(rs.getString("apellidos"));
        g.setRelationship(rs.getString("parentesco"));
        g.setPhone(rs.getString("telefono"));
        g.setEmail(rs.getString("email"));
        g.setAddress(rs.getString("direccion"));
        g.setOccupation(rs.getString("profesion_ocupacion"));
        return g;
    }
}
