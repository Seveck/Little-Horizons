package org.example.dao;

import org.example.model.Enrollment;
import org.example.model.EnrollmentStatus;
import org.example.model.Grade;
import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDao {

    // Insert a new enrollment
    public int insert(Enrollment enrollment) {
        String sql = "INSERT INTO matriculas (id_estudiante, id_grado, año_academico, estado_matricula, "
                + "fecha_matricula, fecha_retiro, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollment.getStudentId());
            ps.setInt(2, enrollment.getGradeId());
            ps.setInt(3, enrollment.getAcademicYear());
            ps.setString(4, enrollment.getStatus().getDbValue());
            ps.setDate(5, Date.valueOf(enrollment.getEnrollmentDate()));
            if (enrollment.getWithdrawalDate() != null) {
                ps.setDate(6, Date.valueOf(enrollment.getWithdrawalDate()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setString(7, enrollment.getObservations());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    enrollment.setId(id);
                    return id;
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

    // List enrollments of a student
    public List<Enrollment> findByStudentId(int studentId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT m.*, CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "g.nombre_grado "
                + "FROM matriculas m "
                + "INNER JOIN estudiantes e ON m.id_estudiante = e.id_estudiante "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE m.id_estudiante = ? ORDER BY m.año_academico DESC";
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

    // List enrollments by grade and year
    public List<Enrollment> findByGradeAndYear(int gradeId, int academicYear) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT m.*, CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "g.nombre_grado "
                + "FROM matriculas m "
                + "INNER JOIN estudiantes e ON m.id_estudiante = e.id_estudiante "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE m.id_grado = ? AND m.año_academico = ? "
                + "ORDER BY e.primer_apellido, e.primer_nombre";
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

    // Formatted enrollment details by grade and year
    public List<String> findEnrollmentDetailsByGradeAndYear(int gradeId, int academicYear) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT m.id_matricula, e.numero_documento, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "g.nombre_grado, m.año_academico, m.estado_matricula, m.fecha_matricula "
                + "FROM matriculas m "
                + "INNER JOIN estudiantes e ON m.id_estudiante = e.id_estudiante "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE m.id_grado = ? AND m.año_academico = ? "
                + "ORDER BY e.primer_apellido, e.primer_nombre";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gradeId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String line = String.format("Matricula #%d | %s (%s) | Grado: %s | Ano: %d | Estado: %s | Fecha: %s",
                            rs.getInt("id_matricula"),
                            rs.getString("student_name"),
                            rs.getString("numero_documento"),
                            rs.getString("nombre_grado"),
                            rs.getInt("año_academico"),
                            rs.getString("estado_matricula"),
                            rs.getDate("fecha_matricula"));
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EnrollmentDao.class, "findEnrollmentDetailsByGradeAndYear", e);
        }
        return list;
    }

    // List all school grades ordered by numeric level
    public List<Grade> findAllGrades() {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT * FROM grados ORDER BY nivel_numerico";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Grade g = new Grade();
                g.setId(rs.getInt("id_grado"));
                g.setName(rs.getString("nombre_grado"));
                g.setNumericLevel(rs.getInt("nivel_numerico"));
                list.add(g);
            }
        } catch (SQLException e) {
            ErrorLogger.log(EnrollmentDao.class, "findAllGrades", e);
        }
        return list;
    }

    // Find grade by ID
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
                    g.setNumericLevel(rs.getInt("nivel_numerico"));
                    return g;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EnrollmentDao.class, "findGradeById", e);
        }
        return null;
    }

    private Enrollment mapRow(ResultSet rs) throws SQLException {
        Enrollment m = new Enrollment();
        m.setId(rs.getInt("id_matricula"));
        m.setStudentId(rs.getInt("id_estudiante"));
        m.setGradeId(rs.getInt("id_grado"));
        m.setAcademicYear(rs.getInt("año_academico"));
        m.setStatus(EnrollmentStatus.fromDbValue(rs.getString("estado_matricula")));
        Date fmat = rs.getDate("fecha_matricula");
        if (fmat != null) {
            m.setEnrollmentDate(fmat.toLocalDate());
        }
        Date fret = rs.getDate("fecha_retiro");
        if (fret != null) {
            m.setWithdrawalDate(fret.toLocalDate());
        }
        m.setObservations(rs.getString("observaciones"));
        m.setStudentName(rs.getString("student_name"));
        m.setGradeName(rs.getString("nombre_grado"));
        return m;
    }
}
