package org.example.model;

import java.time.LocalDate;

public class Attendance {

    private int id;
    private int studentId;
    private int assignmentId;
    private LocalDate date;
    private AttendanceStatus status = AttendanceStatus.PRESENT;
    private String remarks;

    // Empty constructor
    public Attendance() {
    }

    // Constructor with fields
    public Attendance(int id, int studentId, int assignmentId, LocalDate date,
                      AttendanceStatus status, String remarks) {
        this.id = id;
        this.studentId = studentId;
        this.assignmentId = assignmentId;
        this.date = date;
        this.status = status;
        this.remarks = remarks;
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

    public int getAssignmentId() {
        return this.assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AttendanceStatus getStatus() {
        return this.status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "";
        return "Asistencia ID: " + this.id
                + " | Estudiante ID: " + this.studentId
                + " | Fecha: " + this.date
                + " | Estado: " + st
                + " | Obs: " + this.remarks;
    }
}
