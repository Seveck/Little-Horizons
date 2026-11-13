package org.example.model;

import java.time.LocalDate;

public class Enrollment {

    private int id;
    private int studentId;
    private int gradeId;
    private int academicYear;
    private LocalDate enrollmentDate;
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;
    private double enrollmentCost;

    // Empty constructor
    public Enrollment() {
    }

    // Constructor with fields
    public Enrollment(int id, int studentId, int gradeId, int academicYear,
                      LocalDate enrollmentDate, EnrollmentStatus status, double enrollmentCost) {
        this.id = id;
        this.studentId = studentId;
        this.gradeId = gradeId;
        this.academicYear = academicYear;
        this.enrollmentDate = enrollmentDate;
        this.status = status;
        this.enrollmentCost = enrollmentCost;
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

    public int getGradeId() {
        return this.gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getAcademicYear() {
        return this.academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public LocalDate getEnrollmentDate() {
        return this.enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public EnrollmentStatus getStatus() {
        return this.status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public double getEnrollmentCost() {
        return this.enrollmentCost;
    }

    public void setEnrollmentCost(double enrollmentCost) {
        this.enrollmentCost = enrollmentCost;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "";
        return "Matricula ID: " + this.id
                + " | Estudiante ID: " + this.studentId
                + " | Grado ID: " + this.gradeId
                + " | Año: " + this.academicYear
                + " | Estado: " + st
                + " | Costo: $" + this.enrollmentCost;
    }
}
