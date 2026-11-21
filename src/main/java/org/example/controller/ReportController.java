package org.example.controller;

import org.example.dao.LoanDao.LoanDetail;
import org.example.dao.PaymentDao.PaymentDetail;
import org.example.model.Club;
import org.example.model.StudentStatus;
import org.example.model.SubjectPerformance;

import java.util.List;
import java.util.Map;

public class ReportController {

    private final StudentController studentController;
    private final TeacherController teacherController;
    private final AcademicController academicController;
    private final ExtracurricularController extracurricularController;
    private final FinancialController financialController;
    private final ResourceController resourceController;

    // Constructor
    public ReportController() {
        this.studentController = new StudentController();
        this.teacherController = new TeacherController();
        this.academicController = new AcademicController();
        this.extracurricularController = new ExtracurricularController();
        this.financialController = new FinancialController();
        this.resourceController = new ResourceController();
    }

    // Count total students
    public int countTotalStudents() {
        return this.studentController.listAllStudents().size();
    }

    // Count active students using Java Stream API
    public long countActiveStudents() {
        return this.studentController.listAllStudents().stream()
                .filter(s -> s.getStatus() == StudentStatus.ACTIVE)
                .count();
    }

    // Count active teachers
    public int countActiveTeachers() {
        return this.teacherController.listActiveTeachers().size();
    }

    // Count registered subjects
    public int countTotalSubjects() {
        return this.teacherController.listAllSubjects().size();
    }

    // Academic averages across all subjects for a given year
    public Map<String, Double> getCourseAveragesByYear(int year) {
        return this.academicController.calculateAllCoursesAverages(year);
    }

    // Weekly absence alert lines (> minAbsences absences in one week)
    public List<String> getAbsenceAlerts(int minAbsences) {
        return this.academicController.getWeeklyAbsencePatterns(minAbsences);
    }

    // Total active clubs and club list
    public List<Club> getActiveClubs() {
        return this.extracurricularController.listActiveClubs();
    }

    // Count total active spots used across clubs
    public int getAvailableSpots(int clubId) {
        return this.extracurricularController.getAvailableSpots(clubId);
    }

    // Count active members in a club
    public int getClubMembersCount(int clubId) {
        return this.extracurricularController.getActiveMembersCount(clubId);
    }

    // Count events grouped by event type
    public Map<String, Long> getEventsSummaryByType() {
        return this.extracurricularController.countEventsByType();
    }

    // Total events registered
    public int countTotalEvents() {
        return this.extracurricularController.listAllEvents().size();
    }

    // Total revenue collected in a given year
    public double getTotalCollectedByYear(int year) {
        return this.financialController.calculateTotalCollectedByYear(year);
    }

    // Overdue payments (> 30 days pending)
    public List<PaymentDetail> getDelinquentPayments(int minDays) {
        return this.financialController.getDelinquentPayments(minDays);
    }

    // Inventory counts grouped by resource status
    public Map<String, Long> getInventorySummaryByStatus() {
        return this.resourceController.countResourcesByStatus();
    }

    // Overdue resource loans
    public List<LoanDetail> getOverdueLoans() {
        return this.resourceController.getOverdueLoanAlerts();
    }

    // Subject performance ranking across all academic records (Streams)
    public List<SubjectPerformance> getSubjectPerformanceRanking() {
        return this.academicController.getSubjectPerformanceRanking();
    }

    // Top performing subject(s) supporting ties (Streams)
    public List<SubjectPerformance> findTopPerformingSubjects(List<SubjectPerformance> ranking) {
        return this.academicController.findTopPerformingSubjects(ranking);
    }

    // Build subject performance report text
    public String buildSubjectPerformanceReport(List<SubjectPerformance> ranking, List<SubjectPerformance> topSubjects) {
        return this.academicController.buildSubjectPerformanceReport(ranking, topSubjects);
    }

    // Save subject performance report to .txt
    public String saveSubjectPerformanceReportTxt(String reportContent) {
        return this.academicController.saveSubjectPerformanceReportTxt(reportContent);
    }
}
