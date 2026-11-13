package org.example.model;

import java.time.LocalDate;

public class Event {

    private int id;
    private String name;
    private EventType type;
    private String description;
    private LocalDate eventDate;
    private String location;
    private boolean hasCompetition = false;

    // Empty constructor
    public Event() {
    }

    // Constructor with fields
    public Event(int id, String name, EventType type, String description,
                 LocalDate eventDate, String location, boolean hasCompetition) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.hasCompetition = hasCompetition;
    }

    // Getters and Setters
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

    public EventType getType() {
        return this.type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public String toString() {
        String tp = (this.type != null) ? this.type.getDbValue() : "";
        String comp = this.hasCompetition ? "Si" : "No";
        return "ID: " + this.id
                + " | Evento: " + this.name
                + " | Tipo: " + tp
                + " | Fecha: " + this.eventDate
                + " | Lugar: " + this.location
                + " | Competencia: " + comp;
    }
}
