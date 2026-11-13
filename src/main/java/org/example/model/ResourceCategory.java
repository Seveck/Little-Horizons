package org.example.model;

public class ResourceCategory {

    private int id;
    private String name;
    private String description;

    // Empty constructor
    public ResourceCategory() {
    }

    // Constructor with fields
    public ResourceCategory(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    @Override
    public String toString() {
        return "ID: " + this.id + " | Categoria: " + this.name + " | Desc: " + this.description;
    }
}
