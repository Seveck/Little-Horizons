package org.example.dao;

import org.example.model.DocumentType;
import org.example.model.Teacher;
import org.example.model.TeacherAssignment;
import org.example.model.TeacherStatus;
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

public class TeacherDao {

    // Insert a new teacher and return generated id
    public int insert(Teacher teacher) {
        String sql = "INSERT INTO profesores (tipo_documento, numero_documento, nombres, apellidos, "
                + "especialidad, email, telefono, fecha_contratacion, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, teacher.getDocumentType().getDbValue());
            ps.setString(2, teacher.getDocumentNumber());
            ps.setString(3, teacher.getFirstName());
            ps.setString(4, teacher.getLastName());
            ps.setString(5, teacher.getSpecialty());
            ps.setString(6, teacher.getEmail());
            ps.setString(7, teacher.getPhone());
            ps.setDate(8, Date.valueOf(teacher.getHireDate()));
            ps.setString(9, teacher.getStatus().getDbValue());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        teacher.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "insert", e);
        }
        return -1;
    }

    // Update teacher info
    public boolean update(Teacher teacher) {
        String sql = "UPDATE profesores SET tipo_documento = ?, numero_documento = ?, nombres = ?, apellidos = ?, "
                + "especialidad = ?, email = ?, telefono = ?, fecha_contratacion = ?, estado = ? "
                + "WHERE id_profesor = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, teacher.getDocumentType().getDbValue());
            ps.setString(2, teacher.getDocumentNumber());
            ps.setString(3, teacher.getFirstName());
            ps.setString(4, teacher.getLastName());
            ps.setString(5, teacher.getSpecialty());
            ps.setString(6, teacher.getEmail());
            ps.setString(7, teacher.getPhone());
            ps.setDate(8, Date.valueOf(teacher.getHireDate()));
            ps.setString(9, teacher.getStatus().getDbValue());
            ps.setInt(10, teacher.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "update", e);
            return false;
        }
    }

    // Find teacher by id
    public Teacher findById(int id) {
        String sql = "SELECT * FROM profesores WHERE id_profesor = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "findById", e);
        }
        return null;
    }

    // Find teacher by document number
    public Teacher findByDocument(String documentNumber) {
        String sql = "SELECT * FROM profesores WHERE numero_documento = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "findByDocument", e);
        }
        return null;
    }

    // List all teachers
    public List<Teacher> findAll() {
        List<Teacher> list = new ArrayList<>();
        String sql = "SELECT * FROM profesores ORDER BY id_profesor";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(this.mapRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "findAll", e);
        }
        return list;
    }

    // Insert a teacher assignment to a subject, grade, section and academic year
    public int insertAssignment(TeacherAssignment assignment) {
        String sql = "INSERT INTO asignaciones_profesor (id_profesor, id_materia, id_grado, seccion, año_academico) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, assignment.getTeacherId());
            ps.setInt(2, assignment.getSubjectId());
            ps.setInt(3, assignment.getGradeId());
            ps.setString(4, assignment.getSection());
            ps.setInt(5, assignment.getAcademicYear());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        assignment.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "insertAssignment", e);
        }
        return -1;
    }

    // Find assignments by year with descriptive text
    public List<String> findAssignmentDetailsByYear(int academicYear) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT ap.id_asignacion, CONCAT(p.nombres, ' ', p.apellidos) AS profesor, "
                + "m.codigo_materia, m.nombre_materia, g.nombre_grado, ap.seccion, ap.año_academico "
                + "FROM asignaciones_profesor ap "
                + "INNER JOIN profesores p ON ap.id_profesor = p.id_profesor "
                + "INNER JOIN materias m ON ap.id_materia = m.id_materia "
                + "INNER JOIN grados g ON ap.id_grado = g.id_grado "
                + "WHERE ap.año_academico = ? "
                + "ORDER BY g.id_grado, m.nombre_materia";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String line = "Asignacion ID: " + rs.getInt("id_asignacion")
                            + " | Profesor: " + rs.getString("profesor")
                            + " | Materia: [" + rs.getString("codigo_materia") + "] " + rs.getString("nombre_materia")
                            + " | Grado: " + rs.getString("nombre_grado") + " " + rs.getString("seccion")
                            + " | Año: " + rs.getInt("año_academico");
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "findAssignmentDetailsByYear", e);
        }
        return list;
    }

    // Find assignment by id
    public TeacherAssignment findAssignmentById(int assignmentId) {
        String sql = "SELECT * FROM asignaciones_profesor WHERE id_asignacion = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TeacherAssignment ta = new TeacherAssignment();
                    ta.setId(rs.getInt("id_asignacion"));
                    ta.setTeacherId(rs.getInt("id_profesor"));
                    ta.setSubjectId(rs.getInt("id_materia"));
                    ta.setGradeId(rs.getInt("id_grado"));
                    ta.setSection(rs.getString("seccion"));
                    ta.setAcademicYear(rs.getInt("año_academico"));
                    return ta;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "findAssignmentById", e);
        }
        return null;
    }

    // Helper to convert ResultSet row to Teacher object
    private Teacher mapRow(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getInt("id_profesor"));
        t.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        t.setDocumentNumber(rs.getString("numero_documento"));
        t.setFirstName(rs.getString("nombres"));
        t.setLastName(rs.getString("apellidos"));
        t.setSpecialty(rs.getString("especialidad"));
        t.setEmail(rs.getString("email"));
        t.setPhone(rs.getString("telefono"));
        Date hire = rs.getDate("fecha_contratacion");
        if (hire != null) {
            t.setHireDate(hire.toLocalDate());
        }
        t.setStatus(TeacherStatus.fromDbValue(rs.getString("estado")));
        return t;
    }
}
