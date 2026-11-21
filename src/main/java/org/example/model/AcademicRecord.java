package org.example.model;

import java.time.LocalDate;

public class AcademicRecord {

    private int id;
    private int studentId;
    private int subjectId;
    private int teacherId;
    private int academicYear;
    private AcademicPeriod period;
    private double gradeValue;
    private String observations;
    private LocalDate recordDate;
    private String studentName;
    private String subjectCode;
    private String subjectName;
    private String teacherName;

    public AcademicRecord() {
        this.recordDate = LocalDate.now();
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

    public int getSubjectId() {
        return this.subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getTeacherId() {
        return this.teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
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

    public String getObservations() {
        return this.observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDate getRecordDate() {
        return this.recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubjectCode() {
        return this.subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return this.subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getTeacherName() {
        return this.teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    @Override
    public String toString() {
        String per = (this.period != null) ? this.period.getDbValue() : "N/A";
        return String.format("Reg #%d | Est: %s | Materia: %s | Ano: %d Per: %s | Nota: %.1f | Fecha: %s",
                this.id,
                this.studentName != null ? this.studentName : String.valueOf(this.studentId),
                this.subjectName != null ? this.subjectName : String.valueOf(this.subjectId),
                this.academicYear, per, this.gradeValue, this.recordDate);
    }
}
