package org.example.model;

public class SubjectEnrollment {

    private int id;
    private int studentId;
    private int assignmentId;
    private int academicYear;
    private SubjectEnrollmentStatus status = SubjectEnrollmentStatus.IN_PROGRESS;

    // Empty constructor
    public SubjectEnrollment() {
    }

    // Constructor with fields
    public SubjectEnrollment(int id, int studentId, int assignmentId, int academicYear,
                             SubjectEnrollmentStatus status) {
        this.id = id;
        this.studentId = studentId;
        this.assignmentId = assignmentId;
        this.academicYear = academicYear;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return this.studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getAssignmentId() {
        return this.assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getAcademicYear() {
        return this.academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public SubjectEnrollmentStatus getStatus() {
        return this.status;
    }

    public void setStatus(SubjectEnrollmentStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "";
        return "Inscripcion ID: " + this.id
                + " | Estudiante ID: " + this.studentId
                + " | Asignacion ID: " + this.assignmentId
                + " | Año: " + this.academicYear
                + " | Estado: " + st;
    }
}
