package org.example.model;

public class Subject {

    private int id;
    private String code;
    private String name;
    private String description;
    private int gradeId;
    private int weeklyHours;
    private boolean active;
    private String gradeName;

    public Subject() {
        this.weeklyHours = 2;
        this.active = true;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public int getGradeId() {
        return this.gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getWeeklyHours() {
        return this.weeklyHours;
    }

    public void setWeeklyHours(int weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getGradeName() {
        return this.gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | [%s] %s | Grado: %s | Horas/sem: %d | Activa: %s",
                this.id, this.code, this.name,
                this.gradeName != null ? this.gradeName : String.valueOf(this.gradeId),
                this.weeklyHours, this.active ? "Si" : "No");
    }
}
