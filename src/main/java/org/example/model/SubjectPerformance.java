package org.example.model;

import java.util.Locale;

public class SubjectPerformance {

    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private double averageGrade;
    private int totalGrades;

    public SubjectPerformance() {
    }

    public SubjectPerformance(int subjectId, String subjectCode, String subjectName, double averageGrade, int totalGrades) {
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.averageGrade = averageGrade;
        this.totalGrades = totalGrades;
    }

    public int getSubjectId() {
        return this.subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
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

    public double getAverageGrade() {
        return this.averageGrade;
    }

    public void setAverageGrade(double averageGrade) {
        this.averageGrade = averageGrade;
    }

    public int getTotalGrades() {
        return this.totalGrades;
    }

    public void setTotalGrades(int totalGrades) {
        this.totalGrades = totalGrades;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Codigo: %-10s | Materia: %-32s | Promedio: %.1f",
                this.subjectCode, this.subjectName, this.averageGrade);
    }
}
