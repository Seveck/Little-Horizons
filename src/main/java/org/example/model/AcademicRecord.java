package org.example.model;

import java.time.LocalDateTime;

public class AcademicRecord {

    private int id;
    private int enrollmentSubjectId;
    private int academicYear;
    private AcademicPeriod period;
    private double gradeValue;
    private double percentage = 25.0;
    private String remarks;
    private LocalDateTime recordDate;

    // Empty constructor
    public AcademicRecord() {
    }

    // Constructor with fields
    public AcademicRecord(int id, int enrollmentSubjectId, int academicYear, AcademicPeriod period,
                          double gradeValue, double percentage, String remarks) {
        this.id = id;
        this.enrollmentSubjectId = enrollmentSubjectId;
        this.academicYear = academicYear;
        this.period = period;
        this.gradeValue = gradeValue;
        this.percentage = percentage;
        this.remarks = remarks;
    }

    // Getters and Setters
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEnrollmentSubjectId() {
        return this.enrollmentSubjectId;
    }

    public void setEnrollmentSubjectId(int enrollmentSubjectId) {
        this.enrollmentSubjectId = enrollmentSubjectId;
    }

    public int getAcademicYear() {
        return this.academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public AcademicPeriod getPeriod() {
        return this.period;
    }

    public void setPeriod(AcademicPeriod period) {
        this.period = period;
    }

    public double getGradeValue() {
        return this.gradeValue;
    }

    public void setGradeValue(double gradeValue) {
        this.gradeValue = gradeValue;
    }

    public double getPercentage() {
        return this.percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getRecordDate() {
        return this.recordDate;
    }

    public void setRecordDate(LocalDateTime recordDate) {
        this.recordDate = recordDate;
    }

    @Override
    public String toString() {
        String p = (this.period != null) ? this.period.getDbValue() : "";
        return "Registro ID: " + this.id
                + " | Inscripcion ID: " + this.enrollmentSubjectId
                + " | Año: " + this.academicYear
                + " | Periodo: " + p
                + " | Nota: " + this.gradeValue
                + " | Obs: " + this.remarks;
    }
}
