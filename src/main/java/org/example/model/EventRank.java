package org.example.model;

public enum EventRank {
    FIRST_PLACE("1er Lugar"),
    SECOND_PLACE("2do Lugar"),
    THIRD_PLACE("3er Lugar"),
    PARTICIPANT("Participante");

    private final String dbValue;

    EventRank(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public static EventRank fromDbValue(String value) {
        for (EventRank rank : values()) {
            if (rank.dbValue.equalsIgnoreCase(value)) {
                return rank;
            }
        }
        return PARTICIPANT;
    }
}
