package org.example.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Student {

    private int id;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private String phone;
    private String email;
    private StudentStatus status = StudentStatus.ACTIVE;
    private LocalDateTime createdAt;

    // Empty constructor
    public Student() {
    }

    // Constructor with main fields
    public Student(int id, DocumentType documentType, String documentNumber, String firstName,
                   String lastName, LocalDate birthDate, Gender gender, String address,
                   String phone, String email, StudentStatus status) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.status = status;
    }

    // Helper method for full name
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

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Gender getGender() {
        return this.gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public StudentStatus getStatus() {
        return this.status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        String doc = (this.documentType != null) ? this.documentType.getDbValue() : "";
        String st = (this.status != null) ? this.status.getDbValue() : "";
        return "ID: " + this.id
                + " | Doc: " + doc + " " + this.documentNumber
                + " | Nombre: " + this.firstName + " " + this.lastName
                + " | Estado: " + st
                + " | Tel: " + this.phone;
    }
}
