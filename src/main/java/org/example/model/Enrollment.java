package org.example.model;

import java.time.LocalDate;

public class Enrollment {

    private int id;
    private int studentId;
    private int gradeId;
    private int academicYear;
    private EnrollmentStatus status;
    private LocalDate enrollmentDate;
    private LocalDate withdrawalDate;
    private String observations;
    private String studentName;
    private String gradeName;

    public Enrollment() {
        this.status = EnrollmentStatus.ACTIVE;
        this.enrollmentDate = LocalDate.now();
    }

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

    public EnrollmentStatus getStatus() {
        return this.status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDate getEnrollmentDate() {
        return this.enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public LocalDate getWithdrawalDate() {
        return this.withdrawalDate;
    }

    public void setWithdrawalDate(LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public String getObservations() {
        return this.observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getGradeName() {
        return this.gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "N/A";
        return String.format("Matricula #%d | Est: %s | Grado: %s | Ano: %d | Estado: %s | Fecha: %s",
                this.id,
                this.studentName != null ? this.studentName : String.valueOf(this.studentId),
                this.gradeName != null ? this.gradeName : String.valueOf(this.gradeId),
                this.academicYear, st, this.enrollmentDate);
    }
}
