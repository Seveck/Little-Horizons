package org.example.dao;

import org.example.model.Subject;
import org.example.model.SubjectEnrollment;
import org.example.model.SubjectEnrollmentStatus;
import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SubjectDao {

    // Insert a new subject
    public int insert(Subject subject) {
        String sql = "INSERT INTO materias (codigo_materia, nombre_materia, descripcion, intensidad_horaria, area) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, subject.getCode());
            ps.setString(2, subject.getName());
            ps.setString(3, subject.getDescription());
            ps.setInt(4, subject.getWeeklyHours());
            ps.setString(5, subject.getArea());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        subject.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "insert", e);
        }
        return -1;
    }

    // Update subject
    public boolean update(Subject subject) {
        String sql = "UPDATE materias SET codigo_materia = ?, nombre_materia = ?, descripcion = ?, "
                + "intensidad_horaria = ?, area = ? WHERE id_materia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, subject.getCode());
            ps.setString(2, subject.getName());
            ps.setString(3, subject.getDescription());
            ps.setInt(4, subject.getWeeklyHours());
            ps.setString(5, subject.getArea());
            ps.setInt(6, subject.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "update", e);
            return false;
        }
    }

    // Find subject by id
    public Subject findById(int id) {
        String sql = "SELECT * FROM materias WHERE id_materia = ?";
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
        String sql = "SELECT * FROM materias WHERE codigo_materia = ?";
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
        String sql = "SELECT * FROM materias ORDER BY id_materia";
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

    // Enroll a student into a teacher assignment (course)
    public int enrollStudentInSubject(SubjectEnrollment enrollment) {
        String sql = "INSERT INTO inscripciones_materia (id_estudiante, id_asignacion, año_academico, estado_inscripcion) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, enrollment.getStudentId());
            ps.setInt(2, enrollment.getAssignmentId());
            ps.setInt(3, enrollment.getAcademicYear());
            ps.setString(4, enrollment.getStatus().getDbValue());

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
            ErrorLogger.log(SubjectDao.class, "enrollStudentInSubject", e);
        }
        return -1;
    }

    // Find subject enrollment by id
    public SubjectEnrollment findEnrollmentById(int enrollmentId) {
        String sql = "SELECT * FROM inscripciones_materia WHERE id_inscripcion = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SubjectEnrollment se = new SubjectEnrollment();
                    se.setId(rs.getInt("id_inscripcion"));
                    se.setStudentId(rs.getInt("id_estudiante"));
                    se.setAssignmentId(rs.getInt("id_asignacion"));
                    se.setAcademicYear(rs.getInt("año_academico"));
                    se.setStatus(SubjectEnrollmentStatus.fromDbValue(rs.getString("estado_inscripcion")));
                    return se;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "findEnrollmentById", e);
        }
        return null;
    }

    // Find student enrolled subjects with descriptive names
    public List<String> findStudentEnrollmentDetails(int studentId, int academicYear) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT im.id_inscripcion, m.codigo_materia, m.nombre_materia, "
                + "CONCAT(p.nombres, ' ', p.apellidos) AS profesor, g.nombre_grado, ap.seccion, im.estado_inscripcion "
                + "FROM inscripciones_materia im "
                + "INNER JOIN asignaciones_profesor ap ON im.id_asignacion = ap.id_asignacion "
                + "INNER JOIN materias m ON ap.id_materia = m.id_materia "
                + "INNER JOIN profesores p ON ap.id_profesor = p.id_profesor "
                + "INNER JOIN grados g ON ap.id_grado = g.id_grado "
                + "WHERE im.id_estudiante = ? AND im.año_academico = ? "
                + "ORDER BY m.nombre_materia";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String line = "Inscripcion ID: " + rs.getInt("id_inscripcion")
                            + " | Materia: [" + rs.getString("codigo_materia") + "] " + rs.getString("nombre_materia")
                            + " | Profesor: " + rs.getString("profesor")
                            + " | Curso: " + rs.getString("nombre_grado") + " " + rs.getString("seccion")
                            + " | Estado: " + rs.getString("estado_inscripcion");
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(SubjectDao.class, "findStudentEnrollmentDetails", e);
        }
        return list;
    }

    // Helper to map Subject row
    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getInt("id_materia"));
        s.setCode(rs.getString("codigo_materia"));
        s.setName(rs.getString("nombre_materia"));
        s.setDescription(rs.getString("descripcion"));
        s.setWeeklyHours(rs.getInt("intensidad_horaria"));
        s.setArea(rs.getString("area"));
        return s;
    }
}
