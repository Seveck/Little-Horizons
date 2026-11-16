package org.example.model;

public class Club {

    private int id;
    private String name;
    private String description;
    private int supervisorTeacherId;
    private int maxCapacity;
    private String meetingSchedule;
    private boolean active;
    private String supervisorName;

    public Club() {
        this.maxCapacity = 30;
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

    public int getSupervisorTeacherId() {
        return this.supervisorTeacherId;
    }

    public void setSupervisorTeacherId(int supervisorTeacherId) {
        this.supervisorTeacherId = supervisorTeacherId;
    }

    public int getMaxCapacity() {
        return this.maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getMeetingSchedule() {
        return this.meetingSchedule;
    }

    public void setMeetingSchedule(String meetingSchedule) {
        this.meetingSchedule = meetingSchedule;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSupervisorName() {
        return this.supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | %s | Cupo: %d | Horario: %s | Sup: %s | Activo: %s",
                this.id, this.name, this.maxCapacity,
                this.meetingSchedule != null ? this.meetingSchedule : "-",
                this.supervisorName != null ? this.supervisorName : String.valueOf(this.supervisorTeacherId),
                this.active ? "Si" : "No");
    }
}
