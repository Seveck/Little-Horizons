package org.example.model;

import java.time.LocalDate;

public class SubjectEnrollment {

    private int id;
    private int studentId;
    private int subjectId;
    private int academicYear;
    private AcademicPeriod period;
    private String section;
    private LocalDate enrollmentDate;
    private SubjectEnrollmentStatus status;
    private String studentName;
    private String subjectName;

    public SubjectEnrollment() {
        this.period = AcademicPeriod.FIRST;
        this.section = "A";
        this.enrollmentDate = LocalDate.now();
        this.status = SubjectEnrollmentStatus.ACTIVE;
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

    public String getSection() {
        return this.section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public LocalDate getEnrollmentDate() {
        return this.enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public SubjectEnrollmentStatus getStatus() {
        return this.status;
    }

    public void setStatus(SubjectEnrollmentStatus status) {
        this.status = status;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubjectName() {
        return this.subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "N/A";
        String per = (this.period != null) ? this.period.getDbValue() : "I";
        return String.format("Insc #%d | Est: %s | Materia: %s | Ano: %d Per %s | Sec: %s | Estado: %s",
                this.id,
                this.studentName != null ? this.studentName : String.valueOf(this.studentId),
                this.subjectName != null ? this.subjectName : String.valueOf(this.subjectId),
                this.academicYear, per, this.section != null ? this.section : "-", st);
    }
}
