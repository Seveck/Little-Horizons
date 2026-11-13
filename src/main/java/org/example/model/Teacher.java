package org.example.model;

import java.time.LocalDate;

public class Teacher {

    private int id;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String specialty;
    private String email;
    private String phone;
    private LocalDate hireDate;
    private TeacherStatus status = TeacherStatus.ACTIVE;

    // Empty constructor
    public Teacher() {
    }

    // Constructor with fields
    public Teacher(int id, DocumentType documentType, String documentNumber, String firstName,
                   String lastName, String specialty, String email, String phone,
                   LocalDate hireDate, TeacherStatus status) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialty = specialty;
        this.email = email;
        this.phone = phone;
        this.hireDate = hireDate;
        this.status = status;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    // Getters and Setters
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

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSpecialty() {
        return this.specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
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
        String st = (this.status != null) ? this.status.getDbValue() : "";
        return "ID: " + this.id
                + " | Profesor: " + this.firstName + " " + this.lastName
                + " | Especialidad: " + this.specialty
                + " | Estado: " + st
                + " | Tel: " + this.phone;
    }
}
