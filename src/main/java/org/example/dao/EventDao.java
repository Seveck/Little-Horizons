package org.example.dao;

import org.example.model.Event;
import org.example.model.EventParticipant;
import org.example.model.EventRank;
import org.example.model.EventType;
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

public class EventDao {

    // Insert a new institutional event
    public int insertEvent(Event event) {
        String sql = "INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, "
                + "tiene_competencia, id_organizador, esta_activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getName());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getEventType().getDbValue());
            ps.setDate(4, Date.valueOf(event.getEventDate()));
            ps.setString(5, event.getLocation());
            ps.setBoolean(6, event.isHasCompetition());
            if (event.getOrganizerTeacherId() > 0) {
                ps.setInt(7, event.getOrganizerTeacherId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setBoolean(8, event.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    event.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "insertEvent", e);
        }
        return -1;
    }

    // Find event by ID
    public Event findById(int eventId) {
        String sql = "SELECT ev.*, CONCAT_WS(' ', p.primer_nombre, p.primer_apellido) AS organizer_name "
                + "FROM eventos ev LEFT JOIN profesores p ON ev.id_organizador = p.id_profesor "
                + "WHERE ev.id_evento = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapEvent(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "findById", e);
        }
        return null;
    }

    // List all events ordered by date
    public List<Event> findAllEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT ev.*, CONCAT_WS(' ', p.primer_nombre, p.primer_apellido) AS organizer_name "
                + "FROM eventos ev LEFT JOIN profesores p ON ev.id_organizador = p.id_profesor "
                + "ORDER BY ev.fecha_evento DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(this.mapEvent(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "findAllEvents", e);
        }
        return list;
    }

    // Register a student participant in an event
    public int registerParticipant(EventParticipant participant) {
        String sql = "INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, "
                + "posicion_obtenida, observaciones) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, participant.getEventId());
            ps.setInt(2, participant.getStudentId());
            ps.setString(3, participant.getRole().getDbValue());
            ps.setString(4, participant.getRank());
            ps.setString(5, participant.getObservations());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    participant.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "registerParticipant", e);
        }
        return -1;
    }

    // Update competition rank of a participant
    public boolean updateParticipantRank(int eventId, int studentId, EventRank rank, String observations) {
        String sql = "UPDATE participantes_evento SET posicion_obtenida = ?, observaciones = ? "
                + "WHERE id_evento = ? AND id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rank.getDbValue());
            ps.setString(2, observations);
            ps.setInt(3, eventId);
            ps.setInt(4, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "updateParticipantRank", e);
            return false;
        }
    }

    // List participants of a specific event
    public List<String> findEventParticipantsDetails(int eventId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT pe.id_participacion, e.numero_documento, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS student_name, "
                + "pe.rol_participacion, pe.posicion_obtenida, pe.observaciones "
                + "FROM participantes_evento pe "
                + "INNER JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante "
                + "WHERE pe.id_evento = ? ORDER BY e.primer_apellido";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String obs = rs.getString("observaciones");
                    String pos = rs.getString("posicion_obtenida");
                    String line = String.format("Part #%d | %s (%s) | Rol: %s | Puesto: %s%s",
                            rs.getInt("id_participacion"),
                            rs.getString("student_name"),
                            rs.getString("numero_documento"),
                            rs.getString("rol_participacion"),
                            pos != null ? pos : "Participante",
                            (obs != null && !obs.isEmpty()) ? " | Obs: " + obs : "");
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "findEventParticipantsDetails", e);
        }
        return list;
    }

    // List events in which a student participated
    public List<String> findEventsByStudentDetails(int studentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT ev.nombre_evento, ev.tipo_evento, ev.fecha_evento, pe.rol_participacion, pe.posicion_obtenida "
                + "FROM participantes_evento pe "
                + "INNER JOIN eventos ev ON pe.id_evento = ev.id_evento "
                + "WHERE pe.id_estudiante = ? ORDER BY ev.fecha_evento DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String pos = rs.getString("posicion_obtenida");
                    String line = String.format("%s (%s) | Fecha: %s | Rol: %s | Puesto: %s",
                            rs.getString("nombre_evento"),
                            rs.getString("tipo_evento"),
                            rs.getDate("fecha_evento"),
                            rs.getString("rol_participacion"),
                            pos != null ? pos : "Participante");
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "findEventsByStudentDetails", e);
        }
        return list;
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        Event ev = new Event();
        ev.setId(rs.getInt("id_evento"));
        ev.setName(rs.getString("nombre_evento"));
        ev.setDescription(rs.getString("descripcion"));
        ev.setEventType(EventType.fromDbValue(rs.getString("tipo_evento")));
        Date d = rs.getDate("fecha_evento");
        if (d != null) {
            ev.setEventDate(d.toLocalDate());
        }
        ev.setLocation(rs.getString("lugar"));
        ev.setHasCompetition(rs.getBoolean("tiene_competencia"));
        ev.setOrganizerTeacherId(rs.getInt("id_organizador"));
        ev.setActive(rs.getBoolean("esta_activo"));
        ev.setOrganizerName(rs.getString("organizer_name"));
        return ev;
    }
}
