package org.example.model;

public class Subject {

    private int id;
    private String code;
    private String name;
    private String description;
    private int weeklyHours;
    private String area;

    // Empty constructor
    public Subject() {
    }

    // Constructor with fields
    public Subject(int id, String code, String name, String description, int weeklyHours, String area) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.weeklyHours = weeklyHours;
        this.area = area;
    }

    // Getters and Setters
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

    public int getWeeklyHours() {
        return this.weeklyHours;
    }

    public void setWeeklyHours(int weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public String getArea() {
        return this.area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String toString() {
        return "ID: " + this.id
                + " | Codigo: " + this.code
                + " | Materia: " + this.name
                + " | Area: " + this.area
                + " | Horas/Sem: " + this.weeklyHours;
    }
}
