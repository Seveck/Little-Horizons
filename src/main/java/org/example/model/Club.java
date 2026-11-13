package org.example.model;

public class Club {

    private int id;
    private String name;
    private String description;
    private int supervisorTeacherId;
    private int maxCapacity = 30;
    private String schedule;
    private String location;
    private ClubStatus status = ClubStatus.ACTIVE;

    // Empty constructor
    public Club() {
    }

    // Constructor with fields
    public Club(int id, String name, String description, int supervisorTeacherId,
                int maxCapacity, String schedule, String location, ClubStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.supervisorTeacherId = supervisorTeacherId;
        this.maxCapacity = maxCapacity;
        this.schedule = schedule;
        this.location = location;
        this.status = status;
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

    public String getSchedule() {
        return this.schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ClubStatus getStatus() {
        return this.status;
    }

    public void setStatus(ClubStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "";
        return "ID: " + this.id
                + " | Club: " + this.name
                + " | Capacidad: " + this.maxCapacity
                + " | Horario: " + this.schedule
                + " | Estado: " + st;
    }
}
