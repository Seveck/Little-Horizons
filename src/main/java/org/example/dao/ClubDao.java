package org.example.dao;

import org.example.model.Club;
import org.example.model.ClubMembership;
import org.example.model.MembershipStatus;
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

public class ClubDao {

    // Insert a new extracurricular club
    public int insertClub(Club club) {
        String sql = "INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, "
                + "horario_reunion, esta_activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, club.getName());
            ps.setString(2, club.getDescription());
            if (club.getSupervisorTeacherId() > 0) {
                ps.setInt(3, club.getSupervisorTeacherId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, club.getMaxCapacity());
            ps.setString(5, club.getMeetingSchedule());
            ps.setBoolean(6, club.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    club.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "insertClub", e);
        }
        return -1;
    }

    // Find club by ID
    public Club findById(int clubId) {
        String sql = "SELECT c.*, CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS supervisor_name "
                + "FROM clubes c LEFT JOIN profesores p ON c.id_profesor_supervisor = p.id_profesor "
                + "WHERE c.id_club = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapClub(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "findById", e);
        }
        return null;
    }

    // List all clubs
    public List<Club> findAllClubs() {
        List<Club> list = new ArrayList<>();
        String sql = "SELECT c.*, CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS supervisor_name "
                + "FROM clubes c LEFT JOIN profesores p ON c.id_profesor_supervisor = p.id_profesor "
                + "ORDER BY c.nombre_club";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(this.mapClub(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "findAllClubs", e);
        }
        return list;
    }

    // Count active members in a club
    public int countActiveMembers(int clubId) {
        String sql = "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado_membresia = 'Activa'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "countActiveMembers", e);
        }
        return 0;
    }

    // Enroll student inside a transaction validating club status, duplicate active membership, and max capacity
    // Returns > 0 membershipId, -2 inactive/missing, -3 full capacity, -4 already enrolled, -1 SQL error
    public int enrollStudent(int clubId, int studentId, LocalDate joinDate) {
        String clubSql = "SELECT capacidad_maxima, esta_activo FROM clubes WHERE id_club = ?";
        String checkDuplicateSql = "SELECT id_membresia FROM membresias_club "
                + "WHERE id_club = ? AND id_estudiante = ? AND estado_membresia = 'Activa'";
        String countSql = "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado_membresia = 'Activa'";
        String insertSql = "INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia) "
                + "VALUES (?, ?, ?, 'Activa')";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            int maxCapacity = 0;
            boolean active = false;
            try (PreparedStatement ps = conn.prepareStatement(clubSql)) {
                ps.setInt(1, clubId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return -2;
                    }
                    maxCapacity = rs.getInt("capacidad_maxima");
                    active = rs.getBoolean("esta_activo");
                }
            }

            if (!active) {
                conn.rollback();
                return -2;
            }

            try (PreparedStatement ps = conn.prepareStatement(checkDuplicateSql)) {
                ps.setInt(1, clubId);
                ps.setInt(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        return -4;
                    }
                }
            }

            int currentActive = 0;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setInt(1, clubId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentActive = rs.getInt(1);
                    }
                }
            }

            if (currentActive >= maxCapacity) {
                conn.rollback();
                return -3;
            }

            int membershipId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, studentId);
                ps.setInt(2, clubId);
                ps.setDate(3, Date.valueOf(joinDate != null ? joinDate : LocalDate.now()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        membershipId = rs.getInt(1);
                    }
                }
            }

            conn.commit();
            return membershipId;
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "enrollStudent", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ErrorLogger.log(ClubDao.class, "enrollStudent.rollback", ex);
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ErrorLogger.log(ClubDao.class, "enrollStudent.finally", ex);
                }
            }
        }
    }

    // Withdraw a student from a club
    public boolean withdrawStudent(int clubId, int studentId) {
        return this.withdrawStudent(clubId, studentId, "Retiro registrado por consola");
    }

    // Withdraw a student from a club with reason
    public boolean withdrawStudent(int clubId, int studentId, String reason) {
        String sql = "UPDATE membresias_club SET estado_membresia = 'Retirada', fecha_retiro = ?, motivo_retiro = ? "
                + "WHERE id_club = ? AND id_estudiante = ? AND estado_membresia = 'Activa'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setString(2, reason);
            ps.setInt(3, clubId);
            ps.setInt(4, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "withdrawStudent", e);
            return false;
        }
    }

    // List club members with details
    public List<String> findClubMembersDetails(int clubId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT mc.id_membresia, e.numero_documento, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "mc.fecha_ingreso, mc.estado_membresia "
                + "FROM membresias_club mc "
                + "INNER JOIN estudiantes e ON mc.id_estudiante = e.id_estudiante "
                + "WHERE mc.id_club = ? ORDER BY mc.estado_membresia, e.primer_apellido";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String line = String.format("Membresia #%d | %s (%s) | Ingreso: %s | Estado: %s",
                            rs.getInt("id_membresia"),
                            rs.getString("student_name"),
                            rs.getString("numero_documento"),
                            rs.getDate("fecha_ingreso"),
                            rs.getString("estado_membresia"));
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "findClubMembersDetails", e);
        }
        return list;
    }

    // List clubs a student belongs to
    public List<String> findClubsByStudentDetails(int studentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT mc.id_membresia, c.nombre_club, c.horario_reunion, mc.fecha_ingreso, mc.estado_membresia "
                + "FROM membresias_club mc "
                + "INNER JOIN clubes c ON mc.id_club = c.id_club "
                + "WHERE mc.id_estudiante = ? ORDER BY mc.fecha_ingreso DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String line = String.format("Membresia #%d | %s | Horario: %s | Ingreso: %s | Estado: %s",
                            rs.getInt("id_membresia"),
                            rs.getString("nombre_club"),
                            rs.getString("horario_reunion") != null ? rs.getString("horario_reunion") : "-",
                            rs.getDate("fecha_ingreso"),
                            rs.getString("estado_membresia"));
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "findClubsByStudentDetails", e);
        }
        return list;
    }

    // Find latest membership between club and student
    public ClubMembership findMembership(int clubId, int studentId) {
        String sql = "SELECT * FROM membresias_club WHERE id_club = ? AND id_estudiante = ? ORDER BY id_membresia DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ClubMembership m = new ClubMembership();
                    m.setId(rs.getInt("id_membresia"));
                    m.setStudentId(rs.getInt("id_estudiante"));
                    m.setClubId(rs.getInt("id_club"));
                    Date joinDate = rs.getDate("fecha_ingreso");
                    if (joinDate != null) {
                        m.setJoinDate(joinDate.toLocalDate());
                    }
                    m.setStatus(MembershipStatus.fromDbValue(rs.getString("estado_membresia")));
                    Date retDate = rs.getDate("fecha_retiro");
                    if (retDate != null) {
                        m.setWithdrawalDate(retDate.toLocalDate());
                    }
                    m.setWithdrawalReason(rs.getString("motivo_retiro"));
                    return m;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "findMembership", e);
        }
        return null;
    }

    private Club mapClub(ResultSet rs) throws SQLException {
        Club c = new Club();
        c.setId(rs.getInt("id_club"));
        c.setName(rs.getString("nombre_club"));
        c.setDescription(rs.getString("descripcion"));
        c.setSupervisorTeacherId(rs.getInt("id_profesor_supervisor"));
        c.setMaxCapacity(rs.getInt("capacidad_maxima"));
        c.setMeetingSchedule(rs.getString("horario_reunion"));
        c.setActive(rs.getBoolean("esta_activo"));
        c.setSupervisorName(rs.getString("supervisor_name"));
        return c;
    }
}
