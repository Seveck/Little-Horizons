package org.example.model;

import java.time.LocalDate;

public class ClubMembership {

    private int id;
    private int studentId;
    private int clubId;
    private LocalDate joinDate;
    private MembershipStatus status;
    private LocalDate withdrawalDate;
    private String withdrawalReason;
    private String studentName;
    private String clubName;

    public ClubMembership() {
        this.joinDate = LocalDate.now();
        this.status = MembershipStatus.ACTIVE;
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

    public int getClubId() {
        return this.clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
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

    public LocalDate getWithdrawalDate() {
        return this.withdrawalDate;
    }

    public void setWithdrawalDate(LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public String getWithdrawalReason() {
        return this.withdrawalReason;
    }

    public void setWithdrawalReason(String withdrawalReason) {
        this.withdrawalReason = withdrawalReason;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClubName() {
        return this.clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    @Override
    public String toString() {
        String st = (this.status != null) ? this.status.getDbValue() : "N/A";
        return String.format("Membresia #%d | Est: %s | Club: %s | Ingreso: %s | Estado: %s",
                this.id,
                this.studentName != null ? this.studentName : String.valueOf(this.studentId),
                this.clubName != null ? this.clubName : String.valueOf(this.clubId),
                this.joinDate, st);
    }
}
