package org.example.dao;

import org.example.model.Club;
import org.example.model.ClubMembership;
import org.example.model.ClubStatus;
import org.example.model.MembershipStatus;
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

public class ClubDao {

    // Insert a new extracurricular club and return generated id
    public int insertClub(Club club) {
        String sql = "INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, "
                + "capacidad_maxima, horario_reunion, lugar_reunion, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, club.getName());
            ps.setString(2, club.getDescription());
            ps.setInt(3, club.getSupervisorTeacherId());
            ps.setInt(4, club.getMaxCapacity());
            ps.setString(5, club.getSchedule());
            ps.setString(6, club.getLocation());
            ps.setString(7, club.getStatus().getDbValue());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        club.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "insertClub", e);
        }
        return -1;
    }

    // Find club by id
    public Club findById(int clubId) {
        String sql = "SELECT * FROM clubes WHERE id_club = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapClubRow(rs);
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
        String sql = "SELECT * FROM clubes ORDER BY id_club";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(this.mapClubRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "findAllClubs", e);
        }
        return list;
    }

    // Count active members currently enrolled in a club
    public int countActiveMembers(int clubId) {
        String sql = "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado = 'Activa'";
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

    // Enroll student in a club inside a transaction validating max capacity
    // Returns > 0 membership id, -2 if club is full, -3 if already active, -4 if club inactive/not found, -1 on error
    public int enrollStudent(int clubId, int studentId, LocalDate joinDate) {
        String lockClubSql = "SELECT capacidad_maxima, estado FROM clubes WHERE id_club = ? FOR UPDATE";
        String countSql = "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado = 'Activa'";
        String checkExistingSql = "SELECT id_membresia, estado FROM membresias_club WHERE id_club = ? AND id_estudiante = ?";
        String insertSql = "INSERT INTO membresias_club (id_club, id_estudiante, fecha_ingreso, estado) VALUES (?, ?, ?, 'Activa')";
        String reactivateSql = "UPDATE membresias_club SET fecha_ingreso = ?, estado = 'Activa' WHERE id_membresia = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            int maxCapacity;
            String clubStatus;
            try (PreparedStatement psClub = conn.prepareStatement(lockClubSql)) {
                psClub.setInt(1, clubId);
                try (ResultSet rsClub = psClub.executeQuery()) {
                    if (!rsClub.next()) {
                        conn.rollback();
                        return -4;
                    }
                    maxCapacity = rsClub.getInt("capacidad_maxima");
                    clubStatus = rsClub.getString("estado");
                }
            }

            if (!"Activo".equalsIgnoreCase(clubStatus)) {
                conn.rollback();
                return -4;
            }

            int existingMembershipId = -1;
            String existingStatus = null;
            try (PreparedStatement psCheck = conn.prepareStatement(checkExistingSql)) {
                psCheck.setInt(1, clubId);
                psCheck.setInt(2, studentId);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        existingMembershipId = rsCheck.getInt("id_membresia");
                        existingStatus = rsCheck.getString("estado");
                    }
                }
            }

            if ("Activa".equalsIgnoreCase(existingStatus)) {
                conn.rollback();
                return -3;
            }

            int activeCount = 0;
            try (PreparedStatement psCount = conn.prepareStatement(countSql)) {
                psCount.setInt(1, clubId);
                try (ResultSet rsCount = psCount.executeQuery()) {
                    if (rsCount.next()) {
                        activeCount = rsCount.getInt(1);
                    }
                }
            }

            if (activeCount >= maxCapacity) {
                conn.rollback();
                return -2;
            }

            int resultId = -1;
            if (existingMembershipId > 0) {
                try (PreparedStatement psUpdate = conn.prepareStatement(reactivateSql)) {
                    psUpdate.setDate(1, Date.valueOf(joinDate));
                    psUpdate.setInt(2, existingMembershipId);
                    if (psUpdate.executeUpdate() > 0) {
                        resultId = existingMembershipId;
                    }
                }
            } else {
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    psInsert.setInt(1, clubId);
                    psInsert.setInt(2, studentId);
                    psInsert.setDate(3, Date.valueOf(joinDate));
                    if (psInsert.executeUpdate() > 0) {
                        try (ResultSet rsKeys = psInsert.getGeneratedKeys()) {
                            if (rsKeys.next()) {
                                resultId = rsKeys.getInt(1);
                            }
                        }
                    }
                }
            }

            conn.commit();
            return resultId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ErrorLogger.log(ClubDao.class, "enrollStudent-rollback", ex);
                }
            }
            ErrorLogger.log(ClubDao.class, "enrollStudent", e);
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ErrorLogger.log(ClubDao.class, "enrollStudent-close", ex);
                }
            }
        }
    }

    // Mark a student's club membership as withdrawn ('Retirada')
    public boolean withdrawStudent(int clubId, int studentId) {
        String sql = "UPDATE membresias_club SET estado = 'Retirada' "
                + "WHERE id_club = ? AND id_estudiante = ? AND estado = 'Activa'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clubId);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "withdrawStudent", e);
            return false;
        }
    }

    // List all members of a specific club with student details
    public List<String> findClubMembersDetails(int clubId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT mc.id_membresia, e.id_estudiante, CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, "
                + "mc.fecha_ingreso, mc.estado "
                + "FROM membresias_club mc "
                + "INNER JOIN estudiantes e ON mc.id_estudiante = e.id_estudiante "
                + "WHERE mc.id_club = ? "
                + "ORDER BY mc.estado, e.apellidos";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String line = "Membresia #" + rs.getInt("id_membresia")
                            + " | Estudiante ID: " + rs.getInt("id_estudiante")
                            + " - " + rs.getString("estudiante")
                            + " | Ingreso: " + rs.getDate("fecha_ingreso")
                            + " | Estado: " + rs.getString("estado");
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "findClubMembersDetails", e);
        }
        return list;
    }

    // List all clubs a student belongs to
    public List<String> findClubsByStudentDetails(int studentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT c.id_club, c.nombre_club, c.horario_reunion, "
                + "CONCAT(p.nombres, ' ', p.apellidos) AS supervisor, mc.fecha_ingreso, mc.estado "
                + "FROM membresias_club mc "
                + "INNER JOIN clubes c ON mc.id_club = c.id_club "
                + "INNER JOIN profesores p ON c.id_profesor_supervisor = p.id_profesor "
                + "WHERE mc.id_estudiante = ? "
                + "ORDER BY mc.estado, c.nombre_club";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String line = "Club ID: " + rs.getInt("id_club")
                            + " | " + rs.getString("nombre_club")
                            + " | Supervisor: " + rs.getString("supervisor")
                            + " | Horario: " + rs.getString("horario_reunion")
                            + " | Ingreso: " + rs.getDate("fecha_ingreso")
                            + " | Estado: " + rs.getString("estado");
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "findClubsByStudentDetails", e);
        }
        return list;
    }

    // Find membership object by club and student
    public ClubMembership findMembership(int clubId, int studentId) {
        String sql = "SELECT * FROM membresias_club WHERE id_club = ? AND id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clubId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ClubMembership cm = new ClubMembership();
                    cm.setId(rs.getInt("id_membresia"));
                    cm.setClubId(rs.getInt("id_club"));
                    cm.setStudentId(rs.getInt("id_estudiante"));
                    Date joinDate = rs.getDate("fecha_ingreso");
                    if (joinDate != null) {
                        cm.setJoinDate(joinDate.toLocalDate());
                    }
                    cm.setStatus(MembershipStatus.fromDbValue(rs.getString("estado")));
                    return cm;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(ClubDao.class, "findMembership", e);
        }
        return null;
    }

    // Helper to convert ResultSet row to Club object
    private Club mapClubRow(ResultSet rs) throws SQLException {
        Club c = new Club();
        c.setId(rs.getInt("id_club"));
        c.setName(rs.getString("nombre_club"));
        c.setDescription(rs.getString("descripcion"));
        c.setSupervisorTeacherId(rs.getInt("id_profesor_supervisor"));
        c.setMaxCapacity(rs.getInt("capacidad_maxima"));
        c.setSchedule(rs.getString("horario_reunion"));
        c.setLocation(rs.getString("lugar_reunion"));
        c.setStatus(ClubStatus.fromDbValue(rs.getString("estado")));
        return c;
    }
}
