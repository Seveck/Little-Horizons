package org.example.model;

public class Grade {

    private int id;
    private String name;
    private int numericLevel;

    public Grade() {
    }

    public Grade(int id, String name, int numericLevel) {
        this.id = id;
        this.name = name;
        this.numericLevel = numericLevel;
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

    public int getNumericLevel() {
        return this.numericLevel;
    }

    public void setNumericLevel(int numericLevel) {
        this.numericLevel = numericLevel;
    }

    @Override
    public String toString() {
        return "Grado [" + this.id + "] " + this.name + " (Nivel " + this.numericLevel + ")";
    }
}
