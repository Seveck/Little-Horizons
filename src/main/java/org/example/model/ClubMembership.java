package org.example.model;

import java.time.LocalDate;

public class ClubMembership {

    private int id;
    private int clubId;
    private int studentId;
    private LocalDate joinDate;
    private MembershipStatus status = MembershipStatus.ACTIVE;

    // Empty constructor
    public ClubMembership() {
    }

    // Constructor with fields
    public ClubMembership(int id, int clubId, int studentId, LocalDate joinDate, MembershipStatus status) {
        this.id = id;
        this.clubId = clubId;
        this.studentId = studentId;
        this.joinDate = joinDate;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClubId() {
        return this.clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    public int getStudentId() {
        return this.studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public LocalDate getJoinDate() {
        return this.joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public MembershipStatus getStatus() {
        return this.status;
    }

    public void setStatus(MembershipStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "";
        return "Membresia ID: " + this.id
                + " | Club ID: " + this.clubId
                + " | Estudiante ID: " + this.studentId
                + " | Fecha Ingreso: " + this.joinDate
                + " | Estado: " + st;
    }
}
