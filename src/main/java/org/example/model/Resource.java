package org.example.model;

import java.time.LocalDate;

public class Resource {

    private int id;
    private String code;
    private String name;
    private int categoryId;
    private String description;
    private LocalDate acquisitionDate;
    private String physicalLocation;
    private ResourceStatus status;
    private String categoryName;

    public Resource() {
        this.status = ResourceStatus.AVAILABLE;
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

    public int getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getAcquisitionDate() {
        return this.acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
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

    public String getCategoryName() {
        return this.categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "N/A";
        return String.format("ID: %d | [%s] %s | Cat: %s | Ubicacion: %s | Estado: %s",
                this.id, this.code, this.name,
                this.categoryName != null ? this.categoryName : String.valueOf(this.categoryId),
                this.physicalLocation != null ? this.physicalLocation : "-", st);
    }
}
