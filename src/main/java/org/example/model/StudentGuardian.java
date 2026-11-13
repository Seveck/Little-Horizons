package org.example.model;

public class StudentGuardian {

    private int studentId;
    private int guardianId;
    private boolean primary = true;
    private String relationship;

    // Empty constructor
    public StudentGuardian() {
    }

    // Constructor with fields
    public StudentGuardian(int studentId, int guardianId, boolean primary, String relationship) {
        this.studentId = studentId;
        this.guardianId = guardianId;
        this.primary = primary;
        this.relationship = relationship;
    }

    // Getters and Setters
    public int getStudentId() {
        return this.studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getGuardianId() {
        return this.guardianId;
    }

    public void setGuardianId(int guardianId) {
        this.guardianId = guardianId;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getRelationship() {
        return this.relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    @Override
    public String toString() {
        String tipo = this.primary ? "Principal" : "Secundario";
        return "Estudiante ID: " + this.studentId
                + " | Acudiente ID: " + this.guardianId
                + " | Relacion: " + this.relationship
                + " (" + tipo + ")";
    }
}
