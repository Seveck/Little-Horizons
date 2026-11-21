package org.example.dao;

import org.example.model.AcademicPeriod;
import org.example.model.AcademicRecord;
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

public class AcademicRecordDao {

    // Helper class with joined details for averages and reports
    public static class GradeDetail {
        public int recordId;
        public int studentId;
        public String studentName;
        public int subjectId;
        public String subjectCode;
        public String subjectName;
        public int teacherId;
        public String teacherName;
        public String gradeName;
        public int academicYear;
        public AcademicPeriod period;
        public double gradeValue;
        public String observations;

        @Override
        public String toString() {
            String per = (this.period != null) ? this.period.getDbValue() : "";
            return String.format("Reg #%d | %d Per %s | Est: %s | [%s] %s (%s) | Nota: %.1f",
                    this.recordId, this.academicYear, per, this.studentName,
                    this.subjectCode, this.subjectName, this.gradeName, this.gradeValue);
        }
    }

    // Insert a grade record (0.0 to 5.0)
    public int insert(AcademicRecord record) {
        String sql = "INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, "
                + "periodo_academico, valor_nota, observaciones, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getStudentId());
            ps.setInt(2, record.getSubjectId());
            ps.setInt(3, record.getTeacherId());
            ps.setInt(4, record.getAcademicYear());
            ps.setString(5, record.getPeriod().getDbValue());
            ps.setDouble(6, record.getGradeValue());
            ps.setString(7, record.getObservations());
            ps.setDate(8, Date.valueOf(record.getRecordDate()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    record.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(AcademicRecordDao.class, "insert", e);
        }
        return -1;
    }

    // Update existing grade record
    public boolean updateGrade(int recordId, double newGrade, String observations) {
        String sql = "UPDATE registros_academicos SET valor_nota = ?, observaciones = ? WHERE id_registro = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newGrade);
            ps.setString(2, observations);
            ps.setInt(3, recordId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(AcademicRecordDao.class, "updateGrade", e);
            return false;
        }
    }

    // Find all academic records joined with subject info for performance analysis
    public List<AcademicRecord> findAllWithSubject() {
        List<AcademicRecord> list = new ArrayList<>();
        String sql = "SELECT ra.id_registro, ra.valor_nota, ra.periodo_academico, ra.año_academico, "
                + "m.id_materia, m.codigo_materia, m.nombre_materia "
                + "FROM registros_academicos ra "
                + "INNER JOIN materias m ON ra.id_materia = m.id_materia "
                + "ORDER BY m.nombre_materia, ra.id_registro";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AcademicRecord record = new AcademicRecord();
                record.setId(rs.getInt("id_registro"));
                record.setGradeValue(rs.getDouble("valor_nota"));
                record.setPeriod(AcademicPeriod.fromDbValue(rs.getString("periodo_academico")));
                record.setAcademicYear(rs.getInt("año_academico"));
                record.setSubjectId(rs.getInt("id_materia"));
                record.setSubjectCode(rs.getString("codigo_materia"));
                record.setSubjectName(rs.getString("nombre_materia"));
                list.add(record);
            }
        } catch (SQLException e) {
            ErrorLogger.log(AcademicRecordDao.class, "findAllWithSubject", e);
        }
        return list;
    }

    // Find all grade records for an academic year
    public List<GradeDetail> findGradeDetailsByYear(int academicYear) {
        List<GradeDetail> list = new ArrayList<>();
        String sql = "SELECT ra.id_registro, ra.id_estudiante, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "ra.id_materia, m.codigo_materia, m.nombre_materia, g.nombre_grado, "
                + "ra.id_profesor, CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS teacher_name, "
                + "ra.año_academico, ra.periodo_academico, ra.valor_nota, ra.observaciones "
                + "FROM registros_academicos ra "
                + "INNER JOIN estudiantes e ON ra.id_estudiante = e.id_estudiante "
                + "INNER JOIN materias m ON ra.id_materia = m.id_materia "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "INNER JOIN profesores p ON ra.id_profesor = p.id_profesor "
                + "WHERE ra.año_academico = ? "
                + "ORDER BY e.primer_apellido, m.nombre_materia, ra.periodo_academico";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapGradeDetail(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(AcademicRecordDao.class, "findGradeDetailsByYear", e);
        }
        return list;
    }

    // Find complete multi-year academic history for a student
    public List<GradeDetail> findAllGradeDetailsByStudent(int studentId) {
        List<GradeDetail> list = new ArrayList<>();
        String sql = "SELECT ra.id_registro, ra.id_estudiante, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "ra.id_materia, m.codigo_materia, m.nombre_materia, g.nombre_grado, "
                + "ra.id_profesor, CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS teacher_name, "
                + "ra.año_academico, ra.periodo_academico, ra.valor_nota, ra.observaciones "
                + "FROM registros_academicos ra "
                + "INNER JOIN estudiantes e ON ra.id_estudiante = e.id_estudiante "
                + "INNER JOIN materias m ON ra.id_materia = m.id_materia "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "INNER JOIN profesores p ON ra.id_profesor = p.id_profesor "
                + "WHERE ra.id_estudiante = ? "
                + "ORDER BY ra.año_academico DESC, m.nombre_materia, ra.periodo_academico";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapGradeDetail(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(AcademicRecordDao.class, "findAllGradeDetailsByStudent", e);
        }
        return list;
    }

    private GradeDetail mapGradeDetail(ResultSet rs) throws SQLException {
        GradeDetail d = new GradeDetail();
        d.recordId = rs.getInt("id_registro");
        d.studentId = rs.getInt("id_estudiante");
        d.studentName = rs.getString("student_name");
        d.subjectId = rs.getInt("id_materia");
        d.subjectCode = rs.getString("codigo_materia");
        d.subjectName = rs.getString("nombre_materia");
        d.teacherId = rs.getInt("id_profesor");
        d.teacherName = rs.getString("teacher_name");
        d.gradeName = rs.getString("nombre_grado");
        d.academicYear = rs.getInt("año_academico");
        d.period = AcademicPeriod.fromDbValue(rs.getString("periodo_academico"));
        d.gradeValue = rs.getDouble("valor_nota");
        d.observations = rs.getString("observaciones");
        return d;
    }
}
