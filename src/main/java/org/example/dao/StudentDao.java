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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {

    // Insert a single student and return generated id
    public int insert(Student student) {
        String sql = "INSERT INTO estudiantes (tipo_documento, numero_documento, nombres, apellidos, "
                + "fecha_nacimiento, genero, direccion, telefono, email, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getDocumentType().getDbValue());
            ps.setString(2, student.getDocumentNumber());
            ps.setString(3, student.getFirstName());
            ps.setString(4, student.getLastName());
            ps.setDate(5, Date.valueOf(student.getBirthDate()));
            ps.setString(6, student.getGender().getDbValue());
            ps.setString(7, student.getAddress());
            ps.setString(8, student.getPhone());
            ps.setString(9, student.getEmail());
            ps.setString(10, student.getStatus().getDbValue());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        student.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "insert", e);
        }
        return -1;
    }

    // Transaction: insert student and link guardian in one atomic operation
    public int registerStudentWithGuardian(Student student, int guardianId, boolean isPrimary, String relationship) {
        String sqlStudent = "INSERT INTO estudiantes (tipo_documento, numero_documento, nombres, apellidos, "
                + "fecha_nacimiento, genero, direccion, telefono, email, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlLink = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, relacion) "
                + "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            int studentId = -1;
            try (PreparedStatement psStudent = conn.prepareStatement(sqlStudent, Statement.RETURN_GENERATED_KEYS)) {
                psStudent.setString(1, student.getDocumentType().getDbValue());
                psStudent.setString(2, student.getDocumentNumber());
                psStudent.setString(3, student.getFirstName());
                psStudent.setString(4, student.getLastName());
                psStudent.setDate(5, Date.valueOf(student.getBirthDate()));
                psStudent.setString(6, student.getGender().getDbValue());
                psStudent.setString(7, student.getAddress());
                psStudent.setString(8, student.getPhone());
                psStudent.setString(9, student.getEmail());
                psStudent.setString(10, student.getStatus().getDbValue());

                psStudent.executeUpdate();
                try (ResultSet rs = psStudent.getGeneratedKeys()) {
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

            try (PreparedStatement psLink = conn.prepareStatement(sqlLink)) {
                psLink.setInt(1, studentId);
                psLink.setInt(2, guardianId);
                psLink.setBoolean(3, isPrimary);
                psLink.setString(4, relationship);
                psLink.executeUpdate();
            }

            conn.commit();
            return studentId;
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "registerStudentWithGuardian", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ErrorLogger.log(StudentDao.class, "registerStudentWithGuardian.rollback", ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    ErrorLogger.log(StudentDao.class, "registerStudentWithGuardian.close", e);
                }
            }
        }
        return -1;
    }

    // Link an existing guardian to an existing student
    public boolean linkGuardian(int studentId, int guardianId, boolean isPrimary, String relationship) {
        String sql = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, relacion) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, guardianId);
            ps.setBoolean(3, isPrimary);
            ps.setString(4, relationship);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "linkGuardian", e);
            return false;
        }
    }

    // Update student data
    public boolean update(Student student) {
        String sql = "UPDATE estudiantes SET tipo_documento = ?, numero_documento = ?, nombres = ?, apellidos = ?, "
                + "fecha_nacimiento = ?, genero = ?, direccion = ?, telefono = ?, email = ?, estado = ? "
                + "WHERE id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getDocumentType().getDbValue());
            ps.setString(2, student.getDocumentNumber());
            ps.setString(3, student.getFirstName());
            ps.setString(4, student.getLastName());
            ps.setDate(5, Date.valueOf(student.getBirthDate()));
            ps.setString(6, student.getGender().getDbValue());
            ps.setString(7, student.getAddress());
            ps.setString(8, student.getPhone());
            ps.setString(9, student.getEmail());
            ps.setString(10, student.getStatus().getDbValue());
            ps.setInt(11, student.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(StudentDao.class, "update", e);
            return false;
        }
    }

    // Update only student status
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

    // Find student by id
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
        String sql = "SELECT * FROM estudiantes ORDER BY id_estudiante";
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

    // Find students associated with a specific guardian (siblings support)
    public List<Student> findByGuardianId(int guardianId) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT e.* FROM estudiantes e "
                + "INNER JOIN estudiante_acudiente ea ON e.id_estudiante = ea.id_estudiante "
                + "WHERE ea.id_acudiente = ? ORDER BY e.nombres, e.apellidos";
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

    // Helper to convert ResultSet row to Student object
    private Student mapRow(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id_estudiante"));
        student.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        student.setDocumentNumber(rs.getString("numero_documento"));
        student.setFirstName(rs.getString("nombres"));
        student.setLastName(rs.getString("apellidos"));
        Date birth = rs.getDate("fecha_nacimiento");
        if (birth != null) {
            student.setBirthDate(birth.toLocalDate());
        }
        student.setGender(Gender.fromDbValue(rs.getString("genero")));
        student.setAddress(rs.getString("direccion"));
        student.setPhone(rs.getString("telefono"));
        student.setEmail(rs.getString("email"));
        student.setStatus(StudentStatus.fromDbValue(rs.getString("estado")));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            student.setCreatedAt(ts.toLocalDateTime());
        }
        return student;
    }
}
