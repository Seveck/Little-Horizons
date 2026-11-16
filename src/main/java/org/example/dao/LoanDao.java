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

    // Helper class for combined student & teacher loan listings
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
            return String.format("%s #%d | [%s] %s | Solicitante: %s | Prestamo: %s | Esperada: %s | Dev: %s | %s",
                    this.borrowerType, this.loanId, this.resourceCode, this.resourceName,
                    this.borrowerName, this.loanDate, this.dueDate, ret, st);
        }
    }

    // Register a transactional loan to a student (>0 ID, -2 missing, -3 unavailable, -1 error)
    public int createStudentLoan(StudentLoan loan) {
        String insertSql = "INSERT INTO prestamos_estudiante (id_recurso, id_estudiante, fecha_prestamo, "
                + "fecha_devolucion_esperada, estado_prestamo, observaciones) VALUES (?, ?, ?, ?, 'Activo', ?)";
        int id = this.executeCreateLoan(insertSql, loan.getResourceId(), loan.getStudentId(),
                loan.getLoanDate(), loan.getDueDate(), loan.getRemarks());
        if (id > 0) {
            loan.setId(id);
        }
        return id;
    }

    // Register a transactional loan to a teacher (>0 ID, -2 missing, -3 unavailable, -1 error)
    public int createTeacherLoan(TeacherLoan loan) {
        String insertSql = "INSERT INTO prestamos_profesor (id_recurso, id_profesor, fecha_prestamo, "
                + "fecha_devolucion_esperada, estado_prestamo, observaciones) VALUES (?, ?, ?, ?, 'Activo', ?)";
        int id = this.executeCreateLoan(insertSql, loan.getResourceId(), loan.getTeacherId(),
                loan.getLoanDate(), loan.getDueDate(), loan.getRemarks());
        if (id > 0) {
            loan.setId(id);
        }
        return id;
    }

    private int executeCreateLoan(String insertSql, int resourceId, int borrowerId,
                                  LocalDate loanDate, LocalDate dueDate, String remarks) {
        String checkResourceSql = "SELECT estado_recurso FROM recursos WHERE id_recurso = ?";
        String updateResourceSql = "UPDATE recursos SET estado_recurso = 'Prestado' WHERE id_recurso = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String currentStatus;
            try (PreparedStatement ps = conn.prepareStatement(checkResourceSql)) {
                ps.setInt(1, resourceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return -2;
                    }
                    currentStatus = rs.getString("estado_recurso");
                }
            }

            if (!"Disponible".equalsIgnoreCase(currentStatus)) {
                conn.rollback();
                return -3;
            }

            int loanId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, resourceId);
                ps.setInt(2, borrowerId);
                ps.setDate(3, Date.valueOf(loanDate));
                ps.setDate(4, Date.valueOf(dueDate));
                ps.setString(5, remarks);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        loanId = rs.getInt(1);
                    }
                }
            }

            if (loanId <= 0) {
                conn.rollback();
                return -1;
            }

            try (PreparedStatement ps = conn.prepareStatement(updateResourceSql)) {
                ps.setInt(1, resourceId);
                ps.executeUpdate();
            }

            conn.commit();
            return loanId;
        } catch (SQLException e) {
            ErrorLogger.log(LoanDao.class, "executeCreateLoan", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ErrorLogger.log(LoanDao.class, "executeCreateLoan.rollback", ex);
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ErrorLogger.log(LoanDao.class, "executeCreateLoan.finally", ex);
                }
            }
        }
    }

    // Transactional return of a student loan
    public boolean returnStudentLoan(int loanId, LocalDate returnDate) {
        return this.executeReturnLoan("prestamos_estudiante", loanId, returnDate);
    }

    // Transactional return of a teacher loan
    public boolean returnTeacherLoan(int loanId, LocalDate returnDate) {
        return this.executeReturnLoan("prestamos_profesor", loanId, returnDate);
    }

    private boolean executeReturnLoan(String tableName, int loanId, LocalDate returnDate) {
        String findSql = "SELECT id_recurso, estado_prestamo FROM " + tableName + " WHERE id_prestamo = ?";
        String updateLoanSql = "UPDATE " + tableName + " SET estado_prestamo = 'Devuelto', fecha_devolucion_real = ? "
                + "WHERE id_prestamo = ?";
        String updateResourceSql = "UPDATE recursos SET estado_recurso = 'Disponible' WHERE id_recurso = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            int resourceId = -1;
            String currentStatus = null;
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setInt(1, loanId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    resourceId = rs.getInt("id_recurso");
                    currentStatus = rs.getString("estado_prestamo");
                }
            }

            if ("Devuelto".equalsIgnoreCase(currentStatus)) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(updateLoanSql)) {
                ps.setDate(1, Date.valueOf(returnDate != null ? returnDate : LocalDate.now()));
                ps.setInt(2, loanId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(updateResourceSql)) {
                ps.setInt(1, resourceId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            ErrorLogger.log(LoanDao.class, "executeReturnLoan", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ErrorLogger.log(LoanDao.class, "executeReturnLoan.rollback", ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ErrorLogger.log(LoanDao.class, "executeReturnLoan.finally", ex);
                }
            }
        }
    }

    // Mark active loans past due date as 'Vencido'
    public int refreshOverdueLoans(LocalDate referenceDate) {
        int updated = 0;
        String sqlStudent = "UPDATE prestamos_estudiante SET estado_prestamo = 'Vencido' "
                + "WHERE estado_prestamo = 'Activo' AND fecha_devolucion_esperada < ?";
        String sqlTeacher = "UPDATE prestamos_profesor SET estado_prestamo = 'Vencido' "
                + "WHERE estado_prestamo = 'Activo' AND fecha_devolucion_esperada < ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlStudent)) {
                ps.setDate(1, Date.valueOf(referenceDate));
                updated += ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlTeacher)) {
                ps.setDate(1, Date.valueOf(referenceDate));
                updated += ps.executeUpdate();
            }
        } catch (SQLException e) {
            ErrorLogger.log(LoanDao.class, "refreshOverdueLoans", e);
        }
        return updated;
    }

    // List all student loans with details
    public List<LoanDetail> findAllStudentLoanDetails() {
        List<LoanDetail> list = new ArrayList<>();
        String sql = "SELECT pe.id_prestamo, pe.id_estudiante AS borrower_id, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS borrower_name, "
                + "pe.id_recurso, r.codigo_recurso, r.nombre_recurso, pe.fecha_prestamo, "
                + "pe.fecha_devolucion_esperada, pe.fecha_devolucion_real, pe.estado_prestamo, pe.observaciones "
                + "FROM prestamos_estudiante pe "
                + "INNER JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante "
                + "INNER JOIN recursos r ON pe.id_recurso = r.id_recurso "
                + "ORDER BY pe.fecha_prestamo DESC, pe.id_prestamo DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(this.mapLoanDetail(rs, "Estudiante"));
            }
        } catch (SQLException e) {
            ErrorLogger.log(LoanDao.class, "findAllStudentLoanDetails", e);
        }
        return list;
    }

    // List all teacher loans with details
    public List<LoanDetail> findAllTeacherLoanDetails() {
        List<LoanDetail> list = new ArrayList<>();
        String sql = "SELECT pp.id_prestamo, pp.id_profesor AS borrower_id, "
                + "CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS borrower_name, "
                + "pp.id_recurso, r.codigo_recurso, r.nombre_recurso, pp.fecha_prestamo, "
                + "pp.fecha_devolucion_esperada, pp.fecha_devolucion_real, pp.estado_prestamo, pp.observaciones "
                + "FROM prestamos_profesor pp "
                + "INNER JOIN profesores p ON pp.id_profesor = p.id_profesor "
                + "INNER JOIN recursos r ON pp.id_recurso = r.id_recurso "
                + "ORDER BY pp.fecha_prestamo DESC, pp.id_prestamo DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(this.mapLoanDetail(rs, "Profesor"));
            }
        } catch (SQLException e) {
            ErrorLogger.log(LoanDao.class, "findAllTeacherLoanDetails", e);
        }
        return list;
    }

    // Combine student and teacher loans into a single list
    public List<LoanDetail> findAllCombinedLoanDetails() {
        List<LoanDetail> combined = new ArrayList<>();
        combined.addAll(this.findAllStudentLoanDetails());
        combined.addAll(this.findAllTeacherLoanDetails());
        return combined;
    }

    private LoanDetail mapLoanDetail(ResultSet rs, String borrowerType) throws SQLException {
        LoanDetail d = new LoanDetail();
        d.loanId = rs.getInt("id_prestamo");
        d.borrowerType = borrowerType;
        d.borrowerId = rs.getInt("borrower_id");
        d.borrowerName = rs.getString("borrower_name");
        d.resourceId = rs.getInt("id_recurso");
        d.resourceCode = rs.getString("codigo_recurso");
        d.resourceName = rs.getString("nombre_recurso");
        Date fpres = rs.getDate("fecha_prestamo");
        if (fpres != null) {
            d.loanDate = fpres.toLocalDate();
        }
        Date fesp = rs.getDate("fecha_devolucion_esperada");
        if (fesp != null) {
            d.dueDate = fesp.toLocalDate();
        }
        Date freal = rs.getDate("fecha_devolucion_real");
        if (freal != null) {
            d.actualReturnDate = freal.toLocalDate();
        }
        d.status = LoanStatus.fromDbValue(rs.getString("estado_prestamo"));
        d.remarks = rs.getString("observaciones");
        return d;
    }
}
