package org.example.model;

public class Grade {

    private int id;
    private String name;
    private GradeLevel level;

    // Empty constructor
    public Grade() {
    }

    // Constructor with fields
    public Grade(int id, String name, GradeLevel level) {
        this.id = id;
        this.name = name;
        this.level = level;
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

    public GradeLevel getLevel() {
        return this.level;
    }

    public void setLevel(GradeLevel level) {
        this.level = level;
    }

    @Override
    public String toString() {
        String levelText = (this.level != null) ? this.level.getDbValue() : "";
        return "ID: " + this.id + " | Grado: " + this.name + " | Nivel: " + levelText;
    }
}
