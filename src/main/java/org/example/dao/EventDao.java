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
import java.util.ArrayList;
import java.util.List;

public class EventDao {

    // Insert a new institutional event and return generated id
    public int insertEvent(Event event) {
        String sql = "INSERT INTO eventos (nombre_evento, tipo_evento, descripcion, fecha_evento, lugar, tiene_competencia) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, event.getName());
            ps.setString(2, event.getType().getDbValue());
            ps.setString(3, event.getDescription());
            ps.setDate(4, Date.valueOf(event.getEventDate()));
            ps.setString(5, event.getLocation());
            ps.setBoolean(6, event.isHasCompetition());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        event.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "insertEvent", e);
        }
        return -1;
    }

    // Find event by id
    public Event findById(int eventId) {
        String sql = "SELECT * FROM eventos WHERE id_evento = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.mapEventRow(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "findById", e);
        }
        return null;
    }

    // List all events ordered by date descending
    public List<Event> findAllEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM eventos ORDER BY fecha_evento DESC, id_evento";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(this.mapEventRow(rs));
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "findAllEvents", e);
        }
        return list;
    }

    // Register a student as participant in an event
    public int registerParticipant(EventParticipant participant) {
        String sql = "INSERT INTO participantes_evento (id_evento, id_estudiante, puesto, observaciones) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, participant.getEventId());
            ps.setInt(2, participant.getStudentId());
            ps.setString(3, participant.getRank().getDbValue());
            ps.setString(4, participant.getRemarks());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        participant.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "registerParticipant", e);
        }
        return -1;
    }

    // Update a participant's competition position and remarks
    public boolean updateParticipantRank(int eventId, int studentId, EventRank rank, String remarks) {
        String sql = "UPDATE participantes_evento SET puesto = ?, observaciones = ? "
                + "WHERE id_evento = ? AND id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rank.getDbValue());
            ps.setString(2, remarks);
            ps.setInt(3, eventId);
            ps.setInt(4, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "updateParticipantRank", e);
            return false;
        }
    }

    // List all participants of an event with descriptive info
    public List<String> findEventParticipantsDetails(int eventId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT pe.id_participacion, e.id_estudiante, CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, "
                + "pe.puesto, pe.observaciones "
                + "FROM participantes_evento pe "
                + "INNER JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante "
                + "WHERE pe.id_evento = ? "
                + "ORDER BY pe.puesto, e.apellidos";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String obs = rs.getString("observaciones");
                    String obsText = (obs != null && !obs.isEmpty()) ? obs : "Sin observaciones";
                    String line = "Participacion #" + rs.getInt("id_participacion")
                            + " | Estudiante ID: " + rs.getInt("id_estudiante")
                            + " - " + rs.getString("estudiante")
                            + " | Puesto: " + rs.getString("puesto")
                            + " | Obs: " + obsText;
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "findEventParticipantsDetails", e);
        }
        return list;
    }

    // List all events in which a student participated
    public List<String> findEventsByStudentDetails(int studentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT ev.id_evento, ev.nombre_evento, ev.tipo_evento, ev.fecha_evento, "
                + "pe.puesto, pe.observaciones "
                + "FROM participantes_evento pe "
                + "INNER JOIN eventos ev ON pe.id_evento = ev.id_evento "
                + "WHERE pe.id_estudiante = ? "
                + "ORDER BY ev.fecha_evento DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String line = "Evento ID: " + rs.getInt("id_evento")
                            + " | " + rs.getString("nombre_evento")
                            + " (" + rs.getString("tipo_evento") + ")"
                            + " | Fecha: " + rs.getDate("fecha_evento")
                            + " | Puesto: " + rs.getString("puesto");
                    list.add(line);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(EventDao.class, "findEventsByStudentDetails", e);
        }
        return list;
    }

    // Helper to convert ResultSet row to Event object
    private Event mapEventRow(ResultSet rs) throws SQLException {
        Event ev = new Event();
        ev.setId(rs.getInt("id_evento"));
        ev.setName(rs.getString("nombre_evento"));
        ev.setType(EventType.fromDbValue(rs.getString("tipo_evento")));
        ev.setDescription(rs.getString("descripcion"));
        Date date = rs.getDate("fecha_evento");
        if (date != null) {
            ev.setEventDate(date.toLocalDate());
        }
        ev.setLocation(rs.getString("lugar"));
        ev.setHasCompetition(rs.getBoolean("tiene_competencia"));
        return ev;
    }
}
