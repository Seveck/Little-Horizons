package org.example.model;

public class Guardian {

    private int id;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String relationship;
    private String phone;
    private String email;
    private String address;
    private String occupation;

    // Empty constructor
    public Guardian() {
    }

    // Constructor with fields
    public Guardian(int id, DocumentType documentType, String documentNumber, String firstName,
                    String lastName, String relationship, String phone, String email,
                    String address, String occupation) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.relationship = relationship;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.occupation = occupation;
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

    public String getRelationship() {
        return this.relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
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

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOccupation() {
        return this.occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    @Override
    public String toString() {
        String doc = (this.documentType != null) ? this.documentType.getDbValue() : "";
        return "ID: " + this.id
                + " | Doc: " + doc + " " + this.documentNumber
                + " | Acudiente: " + this.firstName + " " + this.lastName
                + " | Parentesco: " + this.relationship
                + " | Tel: " + this.phone;
    }
}
