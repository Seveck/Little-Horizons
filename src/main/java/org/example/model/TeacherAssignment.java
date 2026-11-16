package org.example.model;

import java.time.LocalTime;

public class TeacherAssignment {

    private int id;
    private int teacherId;
    private int subjectId;
    private int academicYear;
    private String section;
    private String scheduleDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private String teacherName;
    private String subjectName;
    private String gradeName;

    public TeacherAssignment() {
        this.section = "A";
        this.scheduleDays = "Lunes";
        this.startTime = LocalTime.of(7, 0);
        this.endTime = LocalTime.of(8, 30);
    }

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

    public int getAcademicYear() {
        return this.academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public String getSection() {
        return this.section;
    }

    public void setSection(String section) {
        this.section = section;
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

    public String getTeacherName() {
        return this.teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getSubjectName() {
        return this.subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getGradeName() {
        return this.gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    @Override
    public String toString() {
        String st = (this.startTime != null) ? this.startTime.toString() : "N/A";
        String et = (this.endTime != null) ? this.endTime.toString() : "N/A";
        return String.format("Asig #%d | Prof: %s | Materia: %s (%s) | Sec: %s | Ano: %d | Horario: %s %s-%s",
                this.id,
                this.teacherName != null ? this.teacherName : String.valueOf(this.teacherId),
                this.subjectName != null ? this.subjectName : String.valueOf(this.subjectId),
                this.gradeName != null ? this.gradeName : "-",
                this.section != null ? this.section : "-",
                this.academicYear,
                this.scheduleDays != null ? this.scheduleDays : "-", st, et);
    }
}
