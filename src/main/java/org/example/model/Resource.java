package org.example.model;

import java.time.LocalDate;

public class Resource {

    private int id;
    private String uniqueCode;
    private String name;
    private int categoryId;
    private String physicalLocation;
    private ResourceStatus status = ResourceStatus.AVAILABLE;
    private LocalDate acquisitionDate;

    // Empty constructor
    public Resource() {
    }

    // Constructor with fields
    public Resource(int id, String uniqueCode, String name, int categoryId,
                    String physicalLocation, ResourceStatus status, LocalDate acquisitionDate) {
        this.id = id;
        this.uniqueCode = uniqueCode;
        this.name = name;
        this.categoryId = categoryId;
        this.physicalLocation = physicalLocation;
        this.status = status;
        this.acquisitionDate = acquisitionDate;
    }

    // Getters and Setters
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUniqueCode() {
        return this.uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getPhysicalLocation() {
        return this.physicalLocation;
    }

    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    public ResourceStatus getStatus() {
        return this.status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public LocalDate getAcquisitionDate() {
        return this.acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "";
        return "ID: " + this.id
                + " | Codigo: " + this.uniqueCode
                + " | Recurso: " + this.name
                + " | Ubicacion: " + this.physicalLocation
                + " | Estado: " + st;
    }
}
