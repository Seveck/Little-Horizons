package org.example.model;

import java.time.LocalDate;

public class Teacher {

    private int id;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String secondLastName;
    private String email;
    private String phone;
    private String specialization;
    private LocalDate hireDate;
    private TeacherStatus status;

    public Teacher() {
        this.documentType = DocumentType.CC;
        this.status = TeacherStatus.ACTIVE;
        this.hireDate = LocalDate.now();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DocumentType getDocumentType() {
        return this.documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return this.documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return this.middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSecondLastName() {
        return this.secondLastName;
    }

    public void setSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (this.firstName != null && !this.firstName.isEmpty()) {
            sb.append(this.firstName);
        }
        if (this.middleName != null && !this.middleName.isEmpty()) {
            sb.append(" ").append(this.middleName);
        }
        if (this.lastName != null && !this.lastName.isEmpty()) {
            sb.append(" ").append(this.lastName);
        }
        if (this.secondLastName != null && !this.secondLastName.isEmpty()) {
            sb.append(" ").append(this.secondLastName);
        }
        return sb.toString().trim();
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return this.specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDate getHireDate() {
        return this.hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public TeacherStatus getStatus() {
        return this.status;
    }

    public void setStatus(TeacherStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "N/A";
        String dt = (this.documentType != null) ? this.documentType.getDbValue() : "";
        return String.format("ID: %d | %s %s | %s | Esp: %s | Email: %s | Estado: %s",
                this.id, dt, this.documentNumber, this.getFullName(),
                this.specialization != null ? this.specialization : "-", this.email, st);
    }
}
