package org.example.model;

public class TeacherAssignment {

    private int id;
    private int teacherId;
    private int subjectId;
    private int gradeId;
    private String section = "A";
    private int academicYear;

    // Empty constructor
    public TeacherAssignment() {
    }

    // Constructor with fields
    public TeacherAssignment(int id, int teacherId, int subjectId, int gradeId, String section, int academicYear) {
        this.id = id;
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.gradeId = gradeId;
        this.section = section;
        this.academicYear = academicYear;
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

    @Override
    public String toString() {
        return "Asignacion ID: " + this.id
                + " | Profesor ID: " + this.teacherId
                + " | Materia ID: " + this.subjectId
                + " | Grado ID: " + this.gradeId
                + " | Seccion: " + this.section
                + " | Año: " + this.academicYear;
    }
}
