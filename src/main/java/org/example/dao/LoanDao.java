package org.example.dao;

import org.example.model.LoanStatus;
import org.example.model.StudentLoan;
import org.example.model.TeacherLoan;
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

public class LoanDao {

    // Helper DTO for unified display and Stream filtering across student and teacher loans
    public static class LoanDetail {
        public int loanId;
        public String borrowerType; // "Estudiante" or "Profesor"
        public int borrowerId;
        public String borrowerName;
        public int resourceId;
        public String resourceCode;
        public String resourceName;
        public LocalDate loanDate;
        public LocalDate dueDate;
        public LocalDate actualReturnDate;
        public LoanStatus status;
        public String remarks;

        @Override
        public String toString() {
            String st = (this.status != null) ? this.status.getDbValue() : "";
            String ret = (this.actualReturnDate != null) ? this.actualReturnDate.toString() : "Pendiente";
            return "[" + this.borrowerType + "] Prestamo #" + this.loanId
                    + " | Solicitante: " + this.borrowerName
                    + " | Recurso: [" + this.resourceCode + "] " + this.resourceName
                    + " | Salida: " + this.loanDate
                    + " | Limite: " + this.dueDate
                    + " | Devuelto: " + ret
                    + " | Estado: " + st;
        }
    }

    // Create a student loan and update resource status to 'Prestado' in one transaction
    // Returns > 0 loan id, -2 if resource is not available, -3 if resource not found, -1 on error
    public int createStudentLoan(StudentLoan loan) {
        int id = this.createLoan(true, loan.getResourceId(), loan.getStudentId(),
                loan.getLoanDate(), loan.getDueDate(), loan.getRemarks());
        if (id > 0) {
            loan.setId(id);
        }
        return id;
    }

    // Create a teacher loan and update resource status to 'Prestado' in one transaction
    public int createTeacherLoan(TeacherLoan loan) {
        int id = this.createLoan(false, loan.getResourceId(), loan.getTeacherId(),
                loan.getLoanDate(), loan.getDueDate(), loan.getRemarks());
        if (id > 0) {
            loan.setId(id);
        }
        return id;
    }

    // Shared transactional loan creation for both student and teacher tables
    private int createLoan(boolean isStudent, int resourceId, int borrowerId,
                           LocalDate loanDate, LocalDate dueDate, String remarks) {
        String checkResourceSql = "SELECT estado_recurso FROM recursos WHERE id_recurso = ?";
        String insertLoanSql = isStudent
                ? "INSERT INTO prestamos_estudiante (id_recurso, id_estudiante, fecha_prestamo, fecha_devolucion_programada, estado_prestamo, observaciones) VALUES (?, ?, ?, ?, 'Activo', ?)"
                : "INSERT INTO prestamos_profesor (id_recurso, id_profesor, fecha_prestamo, fecha_devolucion_programada, estado_prestamo, observaciones) VALUES (?, ?, ?, ?, 'Activo', ?)";
        String updateResourceSql = "UPDATE recursos SET estado_recurso = 'Prestado' WHERE id_recurso = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String currentStatus = null;
            try (PreparedStatement psCheck = conn.prepareStatement(checkResourceSql)) {
                psCheck.setInt(1, resourceId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = rs.getString("estado_recurso");
                    } else {
                        conn.rollback();
                        return -3;
                    }
                }
            }

            if (!"Disponible".equalsIgnoreCase(currentStatus)) {
                conn.rollback();
                return -2;
            }

            int generatedId = -1;
            try (PreparedStatement psInsert = conn.prepareStatement(insertLoanSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, resourceId);
                psInsert.setInt(2, borrowerId);
                psInsert.setDate(3, Date.valueOf(loanDate));
                psInsert.setDate(4, Date.valueOf(dueDate));
                psInsert.setString(5, remarks);
                if (psInsert.executeUpdate() > 0) {
                    try (ResultSet rsKeys = psInsert.getGeneratedKeys()) {
                        if (rsKeys.next()) {
                            generatedId = rsKeys.getInt(1);
                        }
                    }
                }
            }

            if (generatedId <= 0) {
                conn.rollback();
                return -1;
            }

            try (PreparedStatement psUpd = conn.prepareStatement(updateResourceSql)) {
                psUpd.setInt(1, resourceId);
                psUpd.executeUpdate();
            }

            conn.commit();
            return generatedId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ErrorLogger.log(LoanDao.class, "createLoan-rollback", ex);
                }
            }
            ErrorLogger.log(LoanDao.class, "createLoan", e);
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ErrorLogger.log(LoanDao.class, "createLoan-close", ex);
                }
            }
        }
    }

    // Register return of a student loan
    public boolean returnStudentLoan(int loanId, LocalDate returnDate) {
        return this.returnLoan(true, loanId, returnDate);
    }

    // Register return of a teacher loan
    public boolean returnTeacherLoan(int loanId, LocalDate returnDate) {
        return this.returnLoan(false, loanId, returnDate);
    }

    // Shared transactional loan return for student and teacher loans
    private boolean returnLoan(boolean isStudent, int loanId, LocalDate returnDate) {
        String selectSql = isStudent
                ? "SELECT id_recurso, estado_prestamo FROM prestamos_estudiante WHERE id_prestamo_estudiante = ?"
                : "SELECT id_recurso, estado_prestamo FROM prestamos_profesor WHERE id_prestamo_profesor = ?";
        String updateLoanSql = isStudent
                ? "UPDATE prestamos_estudiante SET fecha_devolucion_real = ?, estado_prestamo = 'Devuelto' WHERE id_prestamo_estudiante = ?"
                : "UPDATE prestamos_profesor SET fecha_devolucion_real = ?, estado_prestamo = 'Devuelto' WHERE id_prestamo_profesor = ?";
        String updateResourceSql = "UPDATE recursos SET estado_recurso = 'Disponible' WHERE id_recurso = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            int resourceId = -1;
            String loanStatus = null;
            try (PreparedStatement psSel = conn.prepareStatement(selectSql)) {
                psSel.setInt(1, loanId);
                try (ResultSet rs = psSel.executeQuery()) {
                    if (rs.next()) {
                        resourceId = rs.getInt("id_recurso");
                        loanStatus = rs.getString("estado_prestamo");
                    }
                }
            }

            if (resourceId <= 0 || "Devuelto".equalsIgnoreCase(loanStatus)) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement psUpdLoan = conn.prepareStatement(updateLoanSql)) {
                psUpdLoan.setDate(1, Date.valueOf(returnDate));
                psUpdLoan.setInt(2, loanId);
                if (psUpdLoan.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement psUpdRes = conn.prepareStatement(updateResourceSql)) {
                psUpdRes.setInt(1, resourceId);
                psUpdRes.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ErrorLogger.log(LoanDao.class, "returnLoan-rollback", ex);
                }
            }
            ErrorLogger.log(LoanDao.class, "returnLoan", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ErrorLogger.log(LoanDao.class, "returnLoan-close", ex);
                }
            }
        }
    }

    // Mark active loans whose scheduled return date has passed as 'Vencido'
    public int refreshOverdueLoans(LocalDate referenceDate) {
        int updated = 0;
        String sqlEst = "UPDATE prestamos_estudiante SET estado_prestamo = 'Vencido' "
                + "WHERE estado_prestamo = 'Activo' AND fecha_devolucion_programada < ?";
        String sqlProf = "UPDATE prestamos_profesor SET estado_prestamo = 'Vencido' "
                + "WHERE estado_prestamo = 'Activo' AND fecha_devolucion_programada < ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(sqlEst)) {
                ps1.setDate(1, Date.valueOf(referenceDate));
                updated += ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(sqlProf)) {
                ps2.setDate(1, Date.valueOf(referenceDate));
                updated += ps2.executeUpdate();
            }
        } catch (SQLException e) {
            ErrorLogger.log(LoanDao.class, "refreshOverdueLoans", e);
        }
        return updated;
    }

    // List all student loans with details
    public List<LoanDetail> findAllStudentLoanDetails() {
        List<LoanDetail> list = new ArrayList<>();
        String sql = "SELECT pe.id_prestamo_estudiante AS id_prestamo, 'Estudiante' AS tipo, "
                + "e.id_estudiante AS id_solicitante, CONCAT(e.nombres, ' ', e.apellidos) AS solicitante, "
                + "r.id_recurso, r.codigo_unico, r.nombre_recurso, "
                + "pe.fecha_prestamo, pe.fecha_devolucion_programada, pe.fecha_devolucion_real, "
                + "pe.estado_prestamo, pe.observaciones "
                + "FROM prestamos_estudiante pe "
                + "INNER JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante "
                + "INNER JOIN recursos r ON pe.id_recurso = r.id_recurso "
                + "ORDER BY pe.fecha_prestamo DESC, pe.id_prestamo_estudiante DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(this.mapLoanDetailRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(LoanDao.class, "findAllStudentLoanDetails", e);
        }
        return list;
    }

    // List all teacher loans with details
    public List<LoanDetail> findAllTeacherLoanDetails() {
        List<LoanDetail> list = new ArrayList<>();
        String sql = "SELECT pp.id_prestamo_profesor AS id_prestamo, 'Profesor' AS tipo, "
                + "p.id_profesor AS id_solicitante, CONCAT(p.nombres, ' ', p.apellidos) AS solicitante, "
                + "r.id_recurso, r.codigo_unico, r.nombre_recurso, "
                + "pp.fecha_prestamo, pp.fecha_devolucion_programada, pp.fecha_devolucion_real, "
                + "pp.estado_prestamo, pp.observaciones "
                + "FROM prestamos_profesor pp "
                + "INNER JOIN profesores p ON pp.id_profesor = p.id_profesor "
                + "INNER JOIN recursos r ON pp.id_recurso = r.id_recurso "
                + "ORDER BY pp.fecha_prestamo DESC, pp.id_prestamo_profesor DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(this.mapLoanDetailRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(LoanDao.class, "findAllTeacherLoanDetails", e);
        }
        return list;
    }

    // Combine student and teacher loan history
    public List<LoanDetail> findAllCombinedLoanDetails() {
        List<LoanDetail> combined = new ArrayList<>();
        combined.addAll(this.findAllStudentLoanDetails());
        combined.addAll(this.findAllTeacherLoanDetails());
        return combined;
    }

    // Helper to convert joined ResultSet row to LoanDetail
    private LoanDetail mapLoanDetailRow(ResultSet rs) throws SQLException {
        LoanDetail ld = new LoanDetail();
        ld.loanId = rs.getInt("id_prestamo");
        ld.borrowerType = rs.getString("tipo");
        ld.borrowerId = rs.getInt("id_solicitante");
        ld.borrowerName = rs.getString("solicitante");
        ld.resourceId = rs.getInt("id_recurso");
        ld.resourceCode = rs.getString("codigo_unico");
        ld.resourceName = rs.getString("nombre_recurso");
        Date loanDt = rs.getDate("fecha_prestamo");
        if (loanDt != null) {
            ld.loanDate = loanDt.toLocalDate();
        }
        Date dueDt = rs.getDate("fecha_devolucion_programada");
        if (dueDt != null) {
            ld.dueDate = dueDt.toLocalDate();
        }
        Date retDt = rs.getDate("fecha_devolucion_real");
        if (retDt != null) {
            ld.actualReturnDate = retDt.toLocalDate();
        }
        ld.status = LoanStatus.fromDbValue(rs.getString("estado_prestamo"));
        ld.remarks = rs.getString("observaciones");
        return ld;
    }
}
