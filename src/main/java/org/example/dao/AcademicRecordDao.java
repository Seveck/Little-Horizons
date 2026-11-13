package org.example.dao;

import org.example.model.AcademicPeriod;
import org.example.model.AcademicRecord;
import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AcademicRecordDao {

    // Simple helper class to hold joined grade data for Stream calculations and console display
    public static class GradeDetail {
        public int recordId;
        public int enrollmentSubjectId;
        public int studentId;
        public String studentName;
        public int subjectId;
        public String subjectCode;
        public String subjectName;
        public String gradeName;
        public int academicYear;
        public AcademicPeriod period;
        public double gradeValue;
        public double percentage;
        public String remarks;

        @Override
        public String toString() {
            String p = (this.period != null) ? this.period.getDbValue() : "";
            return "Registro #" + this.recordId
                    + " | Estudiante: " + this.studentName
                    + " | Materia: [" + this.subjectCode + "] " + this.subjectName
                    + " | Periodo: " + p
                    + " | Nota: " + this.gradeValue
                    + " | Obs: " + this.remarks;
        }
    }

    // Insert a grade record (valor_nota between 0.0 and 5.0)
    public int insert(AcademicRecord record) {
        String sql = "INSERT INTO registros_academicos (id_inscripcion, año_academico, periodo, "
                + "valor_nota, porcentaje, observaciones) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, record.getEnrollmentSubjectId());
            ps.setInt(2, record.getAcademicYear());
            ps.setString(3, record.getPeriod().getDbValue());
            ps.setDouble(4, record.getGradeValue());
            ps.setDouble(5, record.getPercentage());
            ps.setString(6, record.getRemarks());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        record.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(AcademicRecordDao.class, "insert", e);
        }
        return -1;
    }

    // Update a grade record
    public boolean updateGrade(int recordId, double newGrade, String remarks) {
        String sql = "UPDATE registros_academicos SET valor_nota = ?, observaciones = ? WHERE id_registro = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newGrade);
            ps.setString(2, remarks);
            ps.setInt(3, recordId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(AcademicRecordDao.class, "updateGrade", e);
            return false;
        }
    }

    // Find all records for a given subject enrollment
    public List<AcademicRecord> findBySubjectEnrollmentId(int enrollmentSubjectId) {
        List<AcademicRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM registros_academicos WHERE id_inscripcion = ? ORDER BY periodo";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, enrollmentSubjectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(AcademicRecordDao.class, "findBySubjectEnrollmentId", e);
        }
        return list;
    }

    // Fetch complete grade details for an academic year (used for history and Stream averages)
    public List<GradeDetail> findGradeDetailsByYear(int academicYear) {
        List<GradeDetail> list = new ArrayList<>();
        String sql = "SELECT ra.id_registro, ra.id_inscripcion, e.id_estudiante, "
                + "CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, "
                + "m.id_materia, m.codigo_materia, m.nombre_materia, g.nombre_grado, "
                + "ra.año_academico, ra.periodo, ra.valor_nota, ra.porcentaje, ra.observaciones "
                + "FROM registros_academicos ra "
                + "INNER JOIN inscripciones_materia im ON ra.id_inscripcion = im.id_inscripcion "
                + "INNER JOIN estudiantes e ON im.id_estudiante = e.id_estudiante "
                + "INNER JOIN asignaciones_profesor ap ON im.id_asignacion = ap.id_asignacion "
                + "INNER JOIN materias m ON ap.id_materia = m.id_materia "
                + "INNER JOIN grados g ON ap.id_grado = g.id_grado "
                + "WHERE ra.año_academico = ? "
                + "ORDER BY e.apellidos, m.nombre_materia, ra.periodo";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GradeDetail detail = new GradeDetail();
                    detail.recordId = rs.getInt("id_registro");
                    detail.enrollmentSubjectId = rs.getInt("id_inscripcion");
                    detail.studentId = rs.getInt("id_estudiante");
                    detail.studentName = rs.getString("estudiante");
                    detail.subjectId = rs.getInt("id_materia");
                    detail.subjectCode = rs.getString("codigo_materia");
                    detail.subjectName = rs.getString("nombre_materia");
                    detail.gradeName = rs.getString("nombre_grado");
                    detail.academicYear = rs.getInt("año_academico");
                    detail.period = AcademicPeriod.fromDbValue(rs.getString("periodo"));
                    detail.gradeValue = rs.getDouble("valor_nota");
                    detail.percentage = rs.getDouble("porcentaje");
                    detail.remarks = rs.getString("observaciones");
                    list.add(detail);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(AcademicRecordDao.class, "findGradeDetailsByYear", e);
        }
        return list;
    }

    // Helper to convert ResultSet row to AcademicRecord
    private AcademicRecord mapRow(ResultSet rs) throws SQLException {
        AcademicRecord ar = new AcademicRecord();
        ar.setId(rs.getInt("id_registro"));
        ar.setEnrollmentSubjectId(rs.getInt("id_inscripcion"));
        ar.setAcademicYear(rs.getInt("año_academico"));
        ar.setPeriod(AcademicPeriod.fromDbValue(rs.getString("periodo")));
        ar.setGradeValue(rs.getDouble("valor_nota"));
        ar.setPercentage(rs.getDouble("porcentaje"));
        ar.setRemarks(rs.getString("observaciones"));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            ar.setRecordDate(ts.toLocalDateTime());
        }
        return ar;
    }
}
