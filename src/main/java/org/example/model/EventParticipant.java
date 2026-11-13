package org.example.model;

public class EventParticipant {

    private int id;
    private int eventId;
    private int studentId;
    private EventRank rank = EventRank.PARTICIPANT;
    private String remarks;

    // Empty constructor
    public EventParticipant() {
    }

    // Constructor with fields
    public EventParticipant(int id, int eventId, int studentId, EventRank rank, String remarks) {
        this.id = id;
        this.eventId = eventId;
        this.studentId = studentId;
        this.rank = rank;
        this.remarks = remarks;
    }

    // Getters and Setters
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

    public EventRank getRank() {
        return this.rank;
    }

    public void setRank(EventRank rank) {
        this.rank = rank;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        String rk = (this.rank != null) ? this.rank.getDbValue() : "";
        return "Participacion ID: " + this.id
                + " | Evento ID: " + this.eventId
                + " | Estudiante ID: " + this.studentId
                + " | Puesto: " + rk
                + " | Obs: " + this.remarks;
    }
}
