package org.example.dao;

import org.example.model.Attendance;
import org.example.model.AttendanceStatus;
import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDao {

    // Helper class with joined details for reports
    public static class AttendanceDetail {
        public int attendanceId;
        public int studentId;
        public String studentDocument;
        public String studentName;
        public int subjectId;
        public String subjectName;
        public LocalDate date;
        public AttendanceStatus status;
        public String observations;
        public String recordedByName;

        @Override
        public String toString() {
            String st = (this.status != null) ? this.status.getDbValue() : "";
            return String.format("Asist #%d | %s | Est: %s (%s) | Materia: %s | Estado: %s",
                    this.attendanceId, this.date, this.studentName, this.studentDocument,
                    this.subjectName, st);
        }
    }

    // Register daily student attendance
    public int insert(Attendance attendance) {
        String sql = "INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, estado_asistencia, "
                + "observaciones, registrado_por) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, attendance.getStudentId());
            ps.setInt(2, attendance.getSubjectId());
            ps.setDate(3, Date.valueOf(attendance.getAttendanceDate()));
            ps.setString(4, attendance.getStatus().getDbValue());
            ps.setString(5, attendance.getObservations());
            if (attendance.getRecordedBy() > 0) {
                ps.setInt(6, attendance.getRecordedBy());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    attendance.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(AttendanceDao.class, "insert", e);
        }
        return -1;
    }

    // Update attendance status
    public boolean updateStatus(int attendanceId, AttendanceStatus status, String observations) {
        String sql = "UPDATE asistencias SET estado_asistencia = ?, observaciones = ? WHERE id_asistencia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.getDbValue());
            ps.setString(2, observations);
            ps.setInt(3, attendanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(AttendanceDao.class, "updateStatus", e);
            return false;
        }
    }

    // List attendance history of a student
    public List<AttendanceDetail> findDetailsByStudent(int studentId) {
        List<AttendanceDetail> list = new ArrayList<>();
        String sql = "SELECT a.id_asistencia, a.id_estudiante, e.numero_documento, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "a.id_materia, m.nombre_materia, a.fecha_asistencia, a.estado_asistencia, a.observaciones, "
                + "CONCAT_WS(' ', p.primer_nombre, p.primer_apellido) AS recorded_by_name "
                + "FROM asistencias a "
                + "INNER JOIN estudiantes e ON a.id_estudiante = e.id_estudiante "
                + "INNER JOIN materias m ON a.id_materia = m.id_materia "
                + "LEFT JOIN profesores p ON a.registrado_por = p.id_profesor "
                + "WHERE a.id_estudiante = ? ORDER BY a.fecha_asistencia DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapDetail(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(AttendanceDao.class, "findDetailsByStudent", e);
        }
        return list;
    }

    // List all attendance records
    public List<AttendanceDetail> findAllDetails() {
        List<AttendanceDetail> list = new ArrayList<>();
        String sql = "SELECT a.id_asistencia, a.id_estudiante, e.numero_documento, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "a.id_materia, m.nombre_materia, a.fecha_asistencia, a.estado_asistencia, a.observaciones, "
                + "CONCAT_WS(' ', p.primer_nombre, p.primer_apellido) AS recorded_by_name "
                + "FROM asistencias a "
                + "INNER JOIN estudiantes e ON a.id_estudiante = e.id_estudiante "
                + "INNER JOIN materias m ON a.id_materia = m.id_materia "
                + "LEFT JOIN profesores p ON a.registrado_por = p.id_profesor "
                + "ORDER BY a.fecha_asistencia DESC, e.primer_apellido";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(this.mapDetail(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(AttendanceDao.class, "findAllDetails", e);
        }
        return list;
    }

    private AttendanceDetail mapDetail(ResultSet rs) throws SQLException {
        AttendanceDetail d = new AttendanceDetail();
        d.attendanceId = rs.getInt("id_asistencia");
        d.studentId = rs.getInt("id_estudiante");
        d.studentDocument = rs.getString("numero_documento");
        d.studentName = rs.getString("student_name");
        d.subjectId = rs.getInt("id_materia");
        d.subjectName = rs.getString("nombre_materia");
        Date dt = rs.getDate("fecha_asistencia");
        if (dt != null) {
            d.date = dt.toLocalDate();
        }
        d.status = AttendanceStatus.fromDbValue(rs.getString("estado_asistencia"));
        d.observations = rs.getString("observaciones");
        d.recordedByName = rs.getString("recorded_by_name");
        return d;
    }
}
