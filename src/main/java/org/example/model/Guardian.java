package org.example.model;

public class Guardian {

    private int id;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String secondLastName;
    private String relationship;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private String address;
    private String occupation;
    private boolean financialResponsible;

    public Guardian() {
        this.documentType = DocumentType.CC;
        this.financialResponsible = true;
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

    public String getRelationship() {
        return this.relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getPrimaryPhone() {
        return this.primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getSecondaryPhone() {
        return this.secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
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

    public boolean isFinancialResponsible() {
        return this.financialResponsible;
    }

    public void setFinancialResponsible(boolean financialResponsible) {
        this.financialResponsible = financialResponsible;
    }

    @Override
    public String toString() {
        String dt = (this.documentType != null) ? this.documentType.getDbValue() : "";
        return String.format("ID: %d | %s %s | %s | Parentesco: %s | Tel: %s | Email: %s | Resp. Financiero: %s",
                this.id, dt, this.documentNumber, this.getFullName(), this.relationship,
                this.primaryPhone, this.email != null ? this.email : "-",
                this.financialResponsible ? "Si" : "No");
    }
}
