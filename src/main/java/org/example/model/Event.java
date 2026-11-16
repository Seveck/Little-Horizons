package org.example.model;

import java.time.LocalDate;

public class Event {

    private int id;
    private String name;
    private String description;
    private EventType eventType;
    private LocalDate eventDate;
    private String location;
    private boolean hasCompetition;
    private int organizerTeacherId;
    private boolean active;
    private String organizerName;

    public Event() {
        this.active = true;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public LocalDate getEventDate() {
        return this.eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isHasCompetition() {
        return this.hasCompetition;
    }

    public void setHasCompetition(boolean hasCompetition) {
        this.hasCompetition = hasCompetition;
    }

    public int getOrganizerTeacherId() {
        return this.organizerTeacherId;
    }

    public void setOrganizerTeacherId(int organizerTeacherId) {
        this.organizerTeacherId = organizerTeacherId;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getOrganizerName() {
        return this.organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    @Override
    public String toString() {
        String type = (this.eventType != null) ? this.eventType.getDbValue() : "N/A";
        return String.format("ID: %d | %s | Tipo: %s | Fecha: %s | Lugar: %s | Competencia: %s",
                this.id, this.name, type, this.eventDate,
                this.location != null ? this.location : "-",
                this.hasCompetition ? "Si" : "No");
    }
}
