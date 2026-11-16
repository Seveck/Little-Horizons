package org.example.dao;

import org.example.model.DocumentType;
import org.example.model.Gender;
import org.example.model.Student;
import org.example.model.StudentStatus;
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

public class StudentDao {

    // Insert a new student
    public int insert(Student student) {
        String sql = "INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, "
                + "tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            this.fillInsertParams(ps, student);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    student.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "insert", e);
        }
        return -1;
    }

    // Register student and link with guardian inside a transaction
    public int registerStudentWithGuardian(Student student, int guardianId, boolean isPrimary, boolean isEmergencyContact) {
        String insertStudentSql = "INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, "
                + "tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String linkSql = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) "
                + "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            int studentId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertStudentSql, Statement.RETURN_GENERATED_KEYS)) {
                this.fillInsertParams(ps, student);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        studentId = rs.getInt(1);
                        student.setId(studentId);
                    }
                }
            }

            if (studentId <= 0) {
                conn.rollback();
                return -1;
            }

            try (PreparedStatement linkPs = conn.prepareStatement(linkSql)) {
                linkPs.setInt(1, studentId);
                linkPs.setInt(2, guardianId);
                linkPs.setBoolean(3, isPrimary);
                linkPs.setBoolean(4, isEmergencyContact);
                linkPs.executeUpdate();
            }

            conn.commit();
            return studentId;
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "registerStudentWithGuardian", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    ErrorLogger.log(StudentDao.class, "registerStudentWithGuardian.rollback", rollbackEx);
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ErrorLogger.log(StudentDao.class, "registerStudentWithGuardian.finally", ex);
                }
            }
        }
    }

    private void fillInsertParams(PreparedStatement ps, Student student) throws SQLException {
        ps.setString(1, student.getDocumentType().getDbValue());
        ps.setString(2, student.getDocumentNumber());
        ps.setString(3, student.getFirstName());
        ps.setString(4, student.getMiddleName());
        ps.setString(5, student.getLastName());
        ps.setString(6, student.getSecondLastName());
        ps.setDate(7, Date.valueOf(student.getBirthDate()));
        ps.setString(8, student.getGender().getDbValue());
        ps.setString(9, student.getAddress());
        ps.setString(10, student.getPhone());
        ps.setString(11, student.getEmail());
        ps.setString(12, student.getBloodType());
        ps.setString(13, student.getAllergies());
        ps.setString(14, student.getMedicalConditions());
        if (student.getEnrollmentDate() != null) {
            ps.setDate(15, Date.valueOf(student.getEnrollmentDate()));
        } else {
            ps.setDate(15, Date.valueOf(java.time.LocalDate.now()));
        }
        ps.setString(16, student.getStatus().getDbValue());
    }

    // Link existing student with existing guardian
    public boolean linkGuardian(int studentId, int guardianId, boolean isPrimary, boolean isEmergencyContact) {
        String sql = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, guardianId);
            ps.setBoolean(3, isPrimary);
            ps.setBoolean(4, isEmergencyContact);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "linkGuardian", e);
            return false;
        }
    }

    // Update student contact info
    public boolean update(Student student) {
        String sql = "UPDATE estudiantes SET primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, "
                + "segundo_apellido = ?, direccion = ?, telefono = ?, correo_electronico = ?, tipo_sangre = ?, "
                + "alergias = ?, condiciones_medicas = ?, estado = ? WHERE id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getFirstName());
            ps.setString(2, student.getMiddleName());
            ps.setString(3, student.getLastName());
            ps.setString(4, student.getSecondLastName());
            ps.setString(5, student.getAddress());
            ps.setString(6, student.getPhone());
            if (student.getEmail() != null && !student.getEmail().isEmpty()) {
                ps.setString(7, student.getEmail());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            ps.setString(8, student.getBloodType());
            ps.setString(9, student.getAllergies());
            ps.setString(10, student.getMedicalConditions());
            ps.setString(11, student.getStatus().getDbValue());
            ps.setInt(12, student.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "update", e);
            return false;
        }
    }

    // Update student status only
    public boolean updateStatus(int studentId, StudentStatus status) {
        String sql = "UPDATE estudiantes SET estado = ? WHERE id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.getDbValue());
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "updateStatus", e);
            return false;
        }
    }

    // Find student by ID
    public Student findById(int id) {
        String sql = "SELECT * FROM estudiantes WHERE id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "findById", e);
        }
        return null;
    }

    // Find student by document number
    public Student findByDocument(String documentNumber) {
        String sql = "SELECT * FROM estudiantes WHERE numero_documento = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "findByDocument", e);
        }
        return null;
    }

    // List all students
    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes ORDER BY primer_apellido, primer_nombre";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(this.mapRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "findAll", e);
        }
        return list;
    }

    // Find students linked to a specific guardian (siblings support)
    public List<Student> findByGuardianId(int guardianId) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT e.* FROM estudiantes e "
                + "INNER JOIN estudiante_acudiente ea ON e.id_estudiante = ea.id_estudiante "
                + "WHERE ea.id_acudiente = ? ORDER BY e.primer_apellido, e.primer_nombre";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guardianId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(this.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "findByGuardianId", e);
        }
        return list;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id_estudiante"));
        s.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        s.setDocumentNumber(rs.getString("numero_documento"));
        s.setFirstName(rs.getString("primer_nombre"));
        s.setMiddleName(rs.getString("segundo_nombre"));
        s.setLastName(rs.getString("primer_apellido"));
        s.setSecondLastName(rs.getString("segundo_apellido"));
        Date dob = rs.getDate("fecha_nacimiento");
        if (dob != null) {
            s.setBirthDate(dob.toLocalDate());
        }
        s.setGender(Gender.fromDbValue(rs.getString("genero")));
        s.setAddress(rs.getString("direccion"));
        s.setPhone(rs.getString("telefono"));
        s.setEmail(rs.getString("correo_electronico"));
        s.setBloodType(rs.getString("tipo_sangre"));
        s.setAllergies(rs.getString("alergias"));
        s.setMedicalConditions(rs.getString("condiciones_medicas"));
        Date ing = rs.getDate("fecha_ingreso");
        if (ing != null) {
            s.setEnrollmentDate(ing.toLocalDate());
        }
        s.setStatus(StudentStatus.fromDbValue(rs.getString("estado")));
        return s;
    }
}
