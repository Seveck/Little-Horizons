package org.example.dao;

import org.example.model.AcademicPeriod;
import org.example.model.Subject;
import org.example.model.SubjectEnrollment;
import org.example.model.SubjectEnrollmentStatus;
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

public class SubjectDao {

    // Insert a new subject linked to a grade
    public int insert(Subject subject) {
        String sql = "INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, subject.getCode());
            ps.setString(2, subject.getName());
            ps.setString(3, subject.getDescription());
            ps.setInt(4, subject.getGradeId());
            ps.setInt(5, subject.getWeeklyHours());
            ps.setBoolean(6, subject.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    subject.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "insert", e);
        }
        return -1;
    }

    // Update subject info
    public boolean update(Subject subject) {
        String sql = "UPDATE materias SET nombre_materia = ?, descripcion = ?, id_grado = ?, horas_semanales = ?, esta_activa = ? "
                + "WHERE id_materia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject.getName());
            ps.setString(2, subject.getDescription());
            ps.setInt(3, subject.getGradeId());
            ps.setInt(4, subject.getWeeklyHours());
            ps.setBoolean(5, subject.isActive());
            ps.setInt(6, subject.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "update", e);
            return false;
        }
    }

    // Find subject by ID
    public Subject findById(int id) {
        String sql = "SELECT m.*, g.nombre_grado FROM materias m "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE m.id_materia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "findById", e);
        }
        return null;
    }

    // Find subject by code
    public Subject findByCode(String code) {
        String sql = "SELECT m.*, g.nombre_grado FROM materias m "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE m.codigo_materia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "findByCode", e);
        }
        return null;
    }

    // List all subjects
    public List<Subject> findAll() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT m.*, g.nombre_grado FROM materias m "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "ORDER BY g.nivel_numerico, m.nombre_materia";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(this.mapRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "findAll", e);
        }
        return list;
    }

    // Enroll a student in a subject for a year and academic period
    public int enrollStudentInSubject(SubjectEnrollment enrollment) {
        String sql = "INSERT INTO inscripciones_materia (id_estudiante, id_materia, año_academico, periodo_academico, "
                + "seccion, fecha_inscripcion, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollment.getStudentId());
            ps.setInt(2, enrollment.getSubjectId());
            ps.setInt(3, enrollment.getAcademicYear());
            ps.setString(4, enrollment.getPeriod().getDbValue());
            ps.setString(5, enrollment.getSection());
            ps.setDate(6, Date.valueOf(enrollment.getEnrollmentDate()));
            ps.setString(7, enrollment.getStatus().getDbValue());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    enrollment.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "enrollStudentInSubject", e);
        }
        return -1;
    }

    // Find subject enrollment by ID
    public SubjectEnrollment findEnrollmentById(int enrollmentId) {
        String sql = "SELECT im.*, CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "m.nombre_materia "
                + "FROM inscripciones_materia im "
                + "INNER JOIN estudiantes e ON im.id_estudiante = e.id_estudiante "
                + "INNER JOIN materias m ON im.id_materia = m.id_materia "
                + "WHERE im.id_inscripcion = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SubjectEnrollment se = new SubjectEnrollment();
                    se.setId(rs.getInt("id_inscripcion"));
                    se.setStudentId(rs.getInt("id_estudiante"));
                    se.setSubjectId(rs.getInt("id_materia"));
                    se.setAcademicYear(rs.getInt("año_academico"));
                    se.setPeriod(AcademicPeriod.fromDbValue(rs.getString("periodo_academico")));
                    se.setSection(rs.getString("seccion"));
                    Date finsc = rs.getDate("fecha_inscripcion");
                    if (finsc != null) {
                        se.setEnrollmentDate(finsc.toLocalDate());
                    }
                    se.setStatus(SubjectEnrollmentStatus.fromDbValue(rs.getString("estado")));
                    se.setStudentName(rs.getString("student_name"));
                    se.setSubjectName(rs.getString("nombre_materia"));
                    return se;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "findEnrollmentById", e);
        }
        return null;
    }

    // Formatted list of subjects a student is enrolled in for a given year
    public List<String> findStudentEnrollmentDetails(int studentId, int academicYear) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT im.id_inscripcion, m.codigo_materia, m.nombre_materia, g.nombre_grado, "
                + "im.periodo_academico, im.seccion, im.estado "
                + "FROM inscripciones_materia im "
                + "INNER JOIN materias m ON im.id_materia = m.id_materia "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE im.id_estudiante = ? AND im.año_academico = ? "
                + "ORDER BY im.periodo_academico, m.nombre_materia";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String line = String.format("Insc #%d | [%s] %s | Grado: %s | Per: %s | Sec: %s | Estado: %s",
                            rs.getInt("id_inscripcion"),
                            rs.getString("codigo_materia"),
                            rs.getString("nombre_materia"),
                            rs.getString("nombre_grado"),
                            rs.getString("periodo_academico"),
                            rs.getString("seccion") != null ? rs.getString("seccion") : "-",
                            rs.getString("estado"));
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "findStudentEnrollmentDetails", e);
        }
        return list;
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject sub = new Subject();
        sub.setId(rs.getInt("id_materia"));
        sub.setCode(rs.getString("codigo_materia"));
        sub.setName(rs.getString("nombre_materia"));
        sub.setDescription(rs.getString("descripcion"));
        sub.setGradeId(rs.getInt("id_grado"));
        sub.setWeeklyHours(rs.getInt("horas_semanales"));
        sub.setActive(rs.getBoolean("esta_activa"));
        sub.setGradeName(rs.getString("nombre_grado"));
        return sub;
    }
}
