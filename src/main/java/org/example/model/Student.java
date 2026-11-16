package org.example.model;

import java.time.LocalDate;

public class Student {

    private int id;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String secondLastName;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private String phone;
    private String email;
    private String bloodType;
    private String allergies;
    private String medicalConditions;
    private LocalDate enrollmentDate;
    private StudentStatus status;

    public Student() {
        this.documentType = DocumentType.TI;
        this.status = StudentStatus.ACTIVE;
        this.enrollmentDate = LocalDate.now();
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

    public String getBloodType() {
        return this.bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getAllergies() {
        return this.allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getMedicalConditions() {
        return this.medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public LocalDate getEnrollmentDate() {
        return this.enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public StudentStatus getStatus() {
        return this.status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "N/A";
        String dt = (this.documentType != null) ? this.documentType.getDbValue() : "";
        return String.format("ID: %d | %s %s | %s | Nac: %s | Tel: %s | Sangre: %s | Estado: %s",
                this.id, dt, this.documentNumber, this.getFullName(), this.birthDate,
                this.phone != null ? this.phone : "-",
                this.bloodType != null ? this.bloodType : "-", st);
    }
}
