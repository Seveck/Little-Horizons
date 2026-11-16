package org.example.model;

public class EventParticipant {

    private int id;
    private int eventId;
    private int studentId;
    private EventRole role;
    private String rank;
    private String observations;
    private String eventName;
    private String studentName;

    public EventParticipant() {
        this.role = EventRole.PARTICIPANT;
        this.rank = "Participante";
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return this.eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getStudentId() {
        return this.studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public EventRole getRole() {
        return this.role;
    }

    public void setRole(EventRole role) {
        this.role = role;
    }

    public String getRank() {
        return this.rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getObservations() {
        return this.observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getEventName() {
        return this.eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public String toString() {
        String r = (this.role != null) ? this.role.getDbValue() : "Participante";
        return String.format("Part #%d | Evento: %s | Est: %s | Rol: %s | Posicion: %s",
                this.id,
                this.eventName != null ? this.eventName : String.valueOf(this.eventId),
                this.studentName != null ? this.studentName : String.valueOf(this.studentId),
                r, this.rank != null ? this.rank : "Participante");
    }
}
