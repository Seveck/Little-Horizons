package org.example.model;

import java.time.LocalTime;

public class TeacherAssignment {

    private int id;
    private int teacherId;
    private int subjectId;
    private int gradeId;
    private String section = "A";
    private int academicYear;
    private String scheduleDays = "Lunes";
    private LocalTime startTime = LocalTime.of(7, 0);
    private LocalTime endTime = LocalTime.of(8, 30);

    // Empty constructor
    public TeacherAssignment() {
    }

    // Basic constructor without schedule
    public TeacherAssignment(int id, int teacherId, int subjectId, int gradeId, String section, int academicYear) {
        this.id = id;
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.gradeId = gradeId;
        this.section = section;
        this.academicYear = academicYear;
    }

    // Full constructor with schedule fields
    public TeacherAssignment(int id, int teacherId, int subjectId, int gradeId, String section, int academicYear,
                             String scheduleDays, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.gradeId = gradeId;
        this.section = section;
        this.academicYear = academicYear;
        this.scheduleDays = scheduleDays;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeacherId() {
        return this.teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public int getSubjectId() {
        return this.subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getGradeId() {
        return this.gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public String getSection() {
        return this.section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public int getAcademicYear() {
        return this.academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public String getScheduleDays() {
        return this.scheduleDays;
    }

    public void setScheduleDays(String scheduleDays) {
        this.scheduleDays = scheduleDays;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Asignacion ID: " + this.id
                + " | Profesor ID: " + this.teacherId
                + " | Materia ID: " + this.subjectId
                + " | Grado ID: " + this.gradeId
                + " | Seccion: " + this.section
                + " | Año: " + this.academicYear
                + " | Horario: " + this.scheduleDays + " (" + this.startTime + " - " + this.endTime + ")";
    }
}
