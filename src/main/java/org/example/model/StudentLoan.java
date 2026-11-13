package org.example.model;

import java.time.LocalDate;

public class StudentLoan {

    private int id;
    private int resourceId;
    private int studentId;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate actualReturnDate;
    private LoanStatus status = LoanStatus.ACTIVE;
    private String remarks;

    // Empty constructor
    public StudentLoan() {
    }

    // Constructor with fields
    public StudentLoan(int id, int resourceId, int studentId, LocalDate loanDate, LocalDate dueDate,
                       LocalDate actualReturnDate, LoanStatus status, String remarks) {
        this.id = id;
        this.resourceId = resourceId;
        this.studentId = studentId;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.actualReturnDate = actualReturnDate;
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

    public int getResourceId() {
        return this.resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getStudentId() {
        return this.studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public LocalDate getLoanDate() {
        return this.loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getDueDate() {
        return this.dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getActualReturnDate() {
        return this.actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public LoanStatus getStatus() {
        return this.status;
    }

    public void setStatus(LoanStatus status) {
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
        return "Prestamo Est. ID: " + this.id
                + " | Recurso ID: " + this.resourceId
                + " | Estudiante ID: " + this.studentId
                + " | Prestamo: " + this.loanDate
                + " | Dev. Prog: " + this.dueDate
                + " | Estado: " + st;
    }
}
