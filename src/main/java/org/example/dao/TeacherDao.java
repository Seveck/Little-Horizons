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
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeacherDao {

    // Insert a new teacher
    public int insert(Teacher teacher) {
        String sql = "INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, teacher.getDocumentType().getDbValue());
            ps.setString(2, teacher.getDocumentNumber());
            ps.setString(3, teacher.getFirstName());
            ps.setString(4, teacher.getMiddleName());
            ps.setString(5, teacher.getLastName());
            ps.setString(6, teacher.getSecondLastName());
            ps.setString(7, teacher.getEmail());
            ps.setString(8, teacher.getPhone());
            ps.setString(9, teacher.getSpecialization());
            ps.setDate(10, Date.valueOf(teacher.getHireDate()));
            ps.setString(11, teacher.getStatus().getDbValue());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    teacher.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "insert", e);
        }
        return -1;
    }

    // Update teacher info
    public boolean update(Teacher teacher) {
        String sql = "UPDATE profesores SET primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, "
                + "segundo_apellido = ?, correo_electronico = ?, telefono = ?, especializacion = ?, estado = ? "
                + "WHERE id_profesor = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, teacher.getFirstName());
            ps.setString(2, teacher.getMiddleName());
            ps.setString(3, teacher.getLastName());
            ps.setString(4, teacher.getSecondLastName());
            ps.setString(5, teacher.getEmail());
            ps.setString(6, teacher.getPhone());
            ps.setString(7, teacher.getSpecialization());
            ps.setString(8, teacher.getStatus().getDbValue());
            ps.setInt(9, teacher.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "update", e);
            return false;
        }
    }

    // Find teacher by ID
    public Teacher findById(int id) {
        String sql = "SELECT * FROM profesores WHERE id_profesor = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapTeacher(rs);
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
                    return this.mapTeacher(rs);
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
        String sql = "SELECT * FROM profesores ORDER BY primer_apellido, primer_nombre";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(this.mapTeacher(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "findAll", e);
        }
        return list;
    }

    // Assign teacher to a subject with schedule conflict check (-2 if overlap)
    public int insertAssignment(TeacherAssignment assignment) {
        if (this.hasScheduleConflict(assignment)) {
            return -2;
        }

        String sql = "INSERT INTO asignaciones_profesor (id_profesor, id_materia, año_academico, seccion, "
                + "dias_horario, hora_inicio, hora_fin) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, assignment.getTeacherId());
            ps.setInt(2, assignment.getSubjectId());
            ps.setInt(3, assignment.getAcademicYear());
            ps.setString(4, assignment.getSection());
            ps.setString(5, assignment.getScheduleDays());
            ps.setTime(6, Time.valueOf(assignment.getStartTime()));
            ps.setTime(7, Time.valueOf(assignment.getEndTime()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    assignment.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "insertAssignment", e);
        }
        return -1;
    }

    // List teacher assignments by academic year
    public List<String> findAssignmentDetailsByYear(int academicYear) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT ap.id_asignacion, "
                + "CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS teacher_name, "
                + "m.codigo_materia, m.nombre_materia, g.nombre_grado, ap.seccion, ap.año_academico, "
                + "ap.dias_horario, ap.hora_inicio, ap.hora_fin "
                + "FROM asignaciones_profesor ap "
                + "INNER JOIN profesores p ON ap.id_profesor = p.id_profesor "
                + "INNER JOIN materias m ON ap.id_materia = m.id_materia "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE ap.año_academico = ? "
                + "ORDER BY p.primer_apellido, g.nivel_numerico, ap.seccion";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time start = rs.getTime("hora_inicio");
                    Time end = rs.getTime("hora_fin");
                    String startStr = (start != null) ? start.toLocalTime().toString() : "N/A";
                    String endStr = (end != null) ? end.toLocalTime().toString() : "N/A";
                    String line = String.format("Asig #%d | Prof: %s | [%s] %s | Grado: %s (%s) | Horario: %s %s-%s",
                            rs.getInt("id_asignacion"),
                            rs.getString("teacher_name"),
                            rs.getString("codigo_materia"),
                            rs.getString("nombre_materia"),
                            rs.getString("nombre_grado"),
                            rs.getString("seccion"),
                            rs.getString("dias_horario"),
                            startStr, endStr);
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "findAssignmentDetailsByYear", e);
        }
        return list;
    }

    // Find assignment by ID
    public TeacherAssignment findAssignmentById(int assignmentId) {
        String sql = "SELECT ap.*, "
                + "CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS teacher_name, "
                + "m.nombre_materia, g.nombre_grado "
                + "FROM asignaciones_profesor ap "
                + "INNER JOIN profesores p ON ap.id_profesor = p.id_profesor "
                + "INNER JOIN materias m ON ap.id_materia = m.id_materia "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE ap.id_asignacion = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapAssignment(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "findAssignmentById", e);
        }
        return null;
    }

    // List assignments for a specific teacher and year
    public List<TeacherAssignment> findAssignmentsByTeacherAndYear(int teacherId, int academicYear) {
        List<TeacherAssignment> list = new ArrayList<>();
        String sql = "SELECT ap.*, "
                + "CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS teacher_name, "
                + "m.nombre_materia, g.nombre_grado "
                + "FROM asignaciones_profesor ap "
                + "INNER JOIN profesores p ON ap.id_profesor = p.id_profesor "
                + "INNER JOIN materias m ON ap.id_materia = m.id_materia "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE ap.id_profesor = ? AND ap.año_academico = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapAssignment(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(TeacherDao.class, "findAssignmentsByTeacherAndYear", e);
        }
        return list;
    }

    // Check if candidate assignment overlaps in days and time with existing teacher assignments
    public boolean hasScheduleConflict(TeacherAssignment candidate) {
        List<TeacherAssignment> existing = this.findAssignmentsByTeacherAndYear(
                candidate.getTeacherId(), candidate.getAcademicYear());
        List<String> newDays = this.parseDays(candidate.getScheduleDays());

        for (TeacherAssignment current : existing) {
            List<String> existingDays = this.parseDays(current.getScheduleDays());
            boolean sharedDay = false;
            for (String day : newDays) {
                if (existingDays.contains(day)) {
                    sharedDay = true;
                    break;
                }
            }
            if (sharedDay && current.getStartTime() != null && current.getEndTime() != null) {
                boolean timeOverlap = candidate.getStartTime().isBefore(current.getEndTime())
                        && candidate.getEndTime().isAfter(current.getStartTime());
                if (timeOverlap) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> parseDays(String daysText) {
        List<String> result = new ArrayList<>();
        if (daysText == null) {
            return result;
        }
        for (String part : Arrays.asList(daysText.split("[,/;\\s]+"))) {
            String trimmed = part.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private Teacher mapTeacher(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getInt("id_profesor"));
        t.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        t.setDocumentNumber(rs.getString("numero_documento"));
        t.setFirstName(rs.getString("primer_nombre"));
        t.setMiddleName(rs.getString("segundo_nombre"));
        t.setLastName(rs.getString("primer_apellido"));
        t.setSecondLastName(rs.getString("segundo_apellido"));
        t.setEmail(rs.getString("correo_electronico"));
        t.setPhone(rs.getString("telefono"));
        t.setSpecialization(rs.getString("especializacion"));
        Date hire = rs.getDate("fecha_contratacion");
        if (hire != null) {
            t.setHireDate(hire.toLocalDate());
        }
        t.setStatus(TeacherStatus.fromDbValue(rs.getString("estado")));
        return t;
    }

    private TeacherAssignment mapAssignment(ResultSet rs) throws SQLException {
        TeacherAssignment a = new TeacherAssignment();
        a.setId(rs.getInt("id_asignacion"));
        a.setTeacherId(rs.getInt("id_profesor"));
        a.setSubjectId(rs.getInt("id_materia"));
        a.setAcademicYear(rs.getInt("año_academico"));
        a.setSection(rs.getString("seccion"));
        a.setScheduleDays(rs.getString("dias_horario"));
        Time start = rs.getTime("hora_inicio");
        if (start != null) {
            a.setStartTime(start.toLocalTime());
        }
        Time end = rs.getTime("hora_fin");
        if (end != null) {
            a.setEndTime(end.toLocalTime());
        }
        a.setTeacherName(rs.getString("teacher_name"));
        a.setSubjectName(rs.getString("nombre_materia"));
        a.setGradeName(rs.getString("nombre_grado"));
        return a;
    }
}
