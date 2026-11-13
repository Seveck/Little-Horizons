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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDao {

    // Helper class with student and subject info for attendance reports
    public static class AttendanceDetail {
        public int attendanceId;
        public int studentId;
        public String studentDocument;
        public String studentName;
        public int assignmentId;
        public String subjectName;
        public LocalDate date;
        public AttendanceStatus status;
        public String remarks;

        @Override
        public String toString() {
            String st = (this.status != null) ? this.status.getDbValue() : "";
            return "Fecha: " + this.date
                    + " | Estudiante: " + this.studentName
                    + " | Materia: " + this.subjectName
                    + " | Estado: " + st
                    + " | Obs: " + this.remarks;
        }
    }

    // Insert a daily attendance record
    public int insert(Attendance attendance) {
        String sql = "INSERT INTO asistencias (id_estudiante, id_asignacion, fecha, estado_asistencia, observaciones) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, attendance.getStudentId());
            ps.setInt(2, attendance.getAssignmentId());
            ps.setDate(3, Date.valueOf(attendance.getDate()));
            ps.setString(4, attendance.getStatus().getDbValue());
            ps.setString(5, attendance.getRemarks());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        attendance.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(AttendanceDao.class, "insert", e);
        }
        return -1;
    }

    // Update attendance status
    public boolean updateStatus(int attendanceId, AttendanceStatus status, String remarks) {
        String sql = "UPDATE asistencias SET estado_asistencia = ?, observaciones = ? WHERE id_asistencia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.getDbValue());
            ps.setString(2, remarks);
            ps.setInt(3, attendanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(AttendanceDao.class, "updateStatus", e);
            return false;
        }
    }

    // Find attendance details for a specific student
    public List<AttendanceDetail> findDetailsByStudent(int studentId) {
        List<AttendanceDetail> list = new ArrayList<>();
        String sql = "SELECT a.id_asistencia, e.id_estudiante, e.numero_documento, "
                + "CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, a.id_asignacion, "
                + "m.nombre_materia, a.fecha, a.estado_asistencia, a.observaciones "
                + "FROM asistencias a "
                + "INNER JOIN estudiantes e ON a.id_estudiante = e.id_estudiante "
                + "INNER JOIN asignaciones_profesor ap ON a.id_asignacion = ap.id_asignacion "
                + "INNER JOIN materias m ON ap.id_materia = m.id_materia "
                + "WHERE a.id_estudiante = ? "
                + "ORDER BY a.fecha DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapDetailRow(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(AttendanceDao.class, "findDetailsByStudent", e);
        }
        return list;
    }

    // Fetch all attendance details (used by controller with Streams to detect weekly absences)
    public List<AttendanceDetail> findAllDetails() {
        List<AttendanceDetail> list = new ArrayList<>();
        String sql = "SELECT a.id_asistencia, e.id_estudiante, e.numero_documento, "
                + "CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, a.id_asignacion, "
                + "m.nombre_materia, a.fecha, a.estado_asistencia, a.observaciones "
                + "FROM asistencias a "
                + "INNER JOIN estudiantes e ON a.id_estudiante = e.id_estudiante "
                + "INNER JOIN asignaciones_profesor ap ON a.id_asignacion = ap.id_asignacion "
                + "INNER JOIN materias m ON ap.id_materia = m.id_materia "
                + "ORDER BY a.fecha DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(this.mapDetailRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(AttendanceDao.class, "findAllDetails", e);
        }
        return list;
    }

    // Helper to map joined AttendanceDetail row
    private AttendanceDetail mapDetailRow(ResultSet rs) throws SQLException {
        AttendanceDetail detail = new AttendanceDetail();
        detail.attendanceId = rs.getInt("id_asistencia");
        detail.studentId = rs.getInt("id_estudiante");
        detail.studentDocument = rs.getString("numero_documento");
        detail.studentName = rs.getString("estudiante");
        detail.assignmentId = rs.getInt("id_asignacion");
        detail.subjectName = rs.getString("nombre_materia");
        Date d = rs.getDate("fecha");
        if (d != null) {
            detail.date = d.toLocalDate();
        }
        detail.status = AttendanceStatus.fromDbValue(rs.getString("estado_asistencia"));
        detail.remarks = rs.getString("observaciones");
        return detail;
    }
}
