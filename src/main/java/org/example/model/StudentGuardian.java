package org.example.model;

public class StudentGuardian {

    private int studentId;
    private int guardianId;
    private boolean primary;
    private boolean emergencyContact;

    public StudentGuardian() {
        this.emergencyContact = true;
    }

    public StudentGuardian(int studentId, int guardianId, boolean primary, boolean emergencyContact) {
        this.studentId = studentId;
        this.guardianId = guardianId;
        this.primary = primary;
        this.emergencyContact = emergencyContact;
    }

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

    public boolean isEmergencyContact() {
        return this.emergencyContact;
    }

    public void setEmergencyContact(boolean emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}
