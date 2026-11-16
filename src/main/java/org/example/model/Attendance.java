package org.example.model;

import java.time.LocalDate;

public class Attendance {

    private int id;
    private int studentId;
    private int subjectId;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private String observations;
    private int recordedBy;
    private String studentName;
    private String subjectName;
    private String recordedByName;

    public Attendance() {
        this.attendanceDate = LocalDate.now();
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

    public LocalDate getAttendanceDate() {
        return this.attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public AttendanceStatus getStatus() {
        return this.status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getObservations() {
        return this.observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public int getRecordedBy() {
        return this.recordedBy;
    }

    public void setRecordedBy(int recordedBy) {
        this.recordedBy = recordedBy;
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

    public String getRecordedByName() {
        return this.recordedByName;
    }

    public void setRecordedByName(String recordedByName) {
        this.recordedByName = recordedByName;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "N/A";
        return String.format("Asist #%d | Fecha: %s | Est: %s | Materia: %s | Estado: %s",
                this.id, this.attendanceDate,
                this.studentName != null ? this.studentName : String.valueOf(this.studentId),
                this.subjectName != null ? this.subjectName : String.valueOf(this.subjectId),
                st);
    }
}
