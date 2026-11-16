package org.example.model;

public enum EventRole {
    PARTICIPANT("Participante"),
    COMPETITOR("Competidor"),
    ORGANIZER("Organizador"),
    ASSISTANT("Asistente");

    private final String dbValue;

    EventRole(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public static EventRole fromDbValue(String value) {
        for (EventRole role : values()) {
            if (role.dbValue.equalsIgnoreCase(value)) {
                return role;
            }
        }
        return PARTICIPANT;
    }
}
