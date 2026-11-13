package org.example.dao;

import org.example.model.Enrollment;
import org.example.model.EnrollmentStatus;
import org.example.model.Grade;
import org.example.model.GradeLevel;
import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDao {

    // Insert a new enrollment in matriculas
    public int insert(Enrollment enrollment) {
        String sql = "INSERT INTO matriculas (id_estudiante, id_grado, año_academico, fecha_matricula, "
                + "estado_matricula, costo_matricula) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, enrollment.getStudentId());
            ps.setInt(2, enrollment.getGradeId());
            ps.setInt(3, enrollment.getAcademicYear());
            ps.setDate(4, Date.valueOf(enrollment.getEnrollmentDate()));
            ps.setString(5, enrollment.getStatus().getDbValue());
            ps.setDouble(6, enrollment.getEnrollmentCost());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        enrollment.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EnrollmentDao.class, "insert", e);
        }
        return -1;
    }

    // Update enrollment status
    public boolean updateStatus(int enrollmentId, EnrollmentStatus status) {
        String sql = "UPDATE matriculas SET estado_matricula = ? WHERE id_matricula = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.getDbValue());
            ps.setInt(2, enrollmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(EnrollmentDao.class, "updateStatus", e);
            return false;
        }
    }

    // Find enrollments by student
    public List<Enrollment> findByStudentId(int studentId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT * FROM matriculas WHERE id_estudiante = ? ORDER BY año_academico DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EnrollmentDao.class, "findByStudentId", e);
        }
        return list;
    }

    // Find enrollments by grade and academic year
    public List<Enrollment> findByGradeAndYear(int gradeId, int academicYear) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT * FROM matriculas WHERE id_grado = ? AND año_academico = ? ORDER BY id_matricula";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, gradeId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EnrollmentDao.class, "findByGradeAndYear", e);
        }
        return list;
    }

    // Find enrollment details formatted with student and grade names
    public List<String> findEnrollmentDetailsByGradeAndYear(int gradeId, int academicYear) {
        List<String> details = new ArrayList<>();
        String sql = "SELECT m.id_matricula, e.numero_documento, CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, "
                + "g.nombre_grado, m.año_academico, m.estado_matricula, m.costo_matricula "
                + "FROM matriculas m "
                + "INNER JOIN estudiantes e ON m.id_estudiante = e.id_estudiante "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE m.id_grado = ? AND m.año_academico = ? "
                + "ORDER BY e.apellidos, e.nombres";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, gradeId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String line = "Matricula ID: " + rs.getInt("id_matricula")
                            + " | Doc: " + rs.getString("numero_documento")
                            + " | Estudiante: " + rs.getString("estudiante")
                            + " | Grado: " + rs.getString("nombre_grado")
                            + " | Año: " + rs.getInt("año_academico")
                            + " | Estado: " + rs.getString("estado_matricula")
                            + " | Costo: $" + rs.getDouble("costo_matricula");
                    details.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EnrollmentDao.class, "findEnrollmentDetailsByGradeAndYear", e);
        }
        return details;
    }

    // Find all grades from catalog table grados
    public List<Grade> findAllGrades() {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM grados ORDER BY id_grado";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Grade g = new Grade();
                g.setId(rs.getInt("id_grado"));
                g.setName(rs.getString("nombre_grado"));
                g.setLevel(GradeLevel.fromDbValue(rs.getString("nivel")));
                grades.add(g);
            }
        } catch (SQLException e) {
            ErrorLogger.log(EnrollmentDao.class, "findAllGrades", e);
        }
        return grades;
    }

    // Find single grade by id
    public Grade findGradeById(int gradeId) {
        String sql = "SELECT * FROM grados WHERE id_grado = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, gradeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Grade g = new Grade();
                    g.setId(rs.getInt("id_grado"));
                    g.setName(rs.getString("nombre_grado"));
                    g.setLevel(GradeLevel.fromDbValue(rs.getString("nivel")));
                    return g;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EnrollmentDao.class, "findGradeById", e);
        }
        return null;
    }

    // Helper to convert ResultSet row to Enrollment object
    private Enrollment mapRow(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setId(rs.getInt("id_matricula"));
        e.setStudentId(rs.getInt("id_estudiante"));
        e.setGradeId(rs.getInt("id_grado"));
        e.setAcademicYear(rs.getInt("año_academico"));
        Date date = rs.getDate("fecha_matricula");
        if (date != null) {
            e.setEnrollmentDate(date.toLocalDate());
        }
        e.setStatus(EnrollmentStatus.fromDbValue(rs.getString("estado_matricula")));
        e.setEnrollmentCost(rs.getDouble("costo_matricula"));
        return e;
    }
}
