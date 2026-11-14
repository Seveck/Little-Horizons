package org.example.controller;

import org.example.dao.LoanDao;
import org.example.dao.LoanDao.LoanDetail;
import org.example.dao.ResourceDao;
import org.example.model.LoanStatus;
import org.example.model.Resource;
import org.example.model.ResourceCategory;
import org.example.model.ResourceStatus;
import org.example.model.StudentLoan;
import org.example.model.TeacherLoan;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceController {

    private final ResourceDao resourceDao;
    private final LoanDao loanDao;

    // Constructor
    public ResourceController() {
        this.resourceDao = new ResourceDao();
        this.loanDao = new LoanDao();
    }

    // Register new resource category
    public int registerCategory(ResourceCategory category) {
        return this.resourceDao.insertCategory(category);
    }

    // List all resource categories
    public List<ResourceCategory> listAllCategories() {
        return this.resourceDao.findAllCategories();
    }

    // Find category by id
    public ResourceCategory findCategoryById(int categoryId) {
        return this.resourceDao.findCategoryById(categoryId);
    }

    // Register a new resource
    public int registerResource(Resource resource) {
        return this.resourceDao.insertResource(resource);
    }

    // Update resource status
    public boolean updateResourceStatus(int resourceId, ResourceStatus status) {
        return this.resourceDao.updateStatus(resourceId, status);
    }

    // Find resource by id
    public Resource findResourceById(int resourceId) {
        return this.resourceDao.findById(resourceId);
    }

    // Find resource by code
    public Resource findResourceByCode(String code) {
        return this.resourceDao.findByCode(code);
    }

    // List all resources
    public List<Resource> listAllResources() {
        return this.resourceDao.findAllResources();
    }

    // Filter only available resources using Java Stream API
    public List<Resource> listAvailableResources() {
        return this.resourceDao.findAllResources().stream()
                .filter(r -> r.getStatus() == ResourceStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    // Filter resources by category using Java Stream API
    public List<Resource> listResourcesByCategory(int categoryId) {
        return this.resourceDao.findAllResources().stream()
                .filter(r -> r.getCategoryId() == categoryId)
                .collect(Collectors.toList());
    }

    // Summary count of resources grouped by status using Java Stream API
    public Map<String, Long> countResourcesByStatus() {
        return this.resourceDao.findAllResources().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus().getDbValue(),
                        Collectors.counting()
                ));
    }

    // Loan a resource to a student
    public int loanToStudent(StudentLoan loan) {
        return this.loanDao.createStudentLoan(loan);
    }

    // Loan a resource to a teacher
    public int loanToTeacher(TeacherLoan loan) {
        return this.loanDao.createTeacherLoan(loan);
    }

    // Return a student loan
    public boolean returnStudentLoan(int loanId, LocalDate returnDate) {
        return this.loanDao.returnStudentLoan(loanId, returnDate);
    }

    // Return a teacher loan
    public boolean returnTeacherLoan(int loanId, LocalDate returnDate) {
        return this.loanDao.returnTeacherLoan(loanId, returnDate);
    }

    // Update active loans past due date to 'Vencido'
    public int refreshOverdueLoans() {
        return this.loanDao.refreshOverdueLoans(LocalDate.now());
    }

    // List active or overdue loans using Java Stream API
    public List<LoanDetail> listPendingReturnLoans() {
        return this.loanDao.findAllCombinedLoanDetails().stream()
                .filter(l -> l.status == LoanStatus.ACTIVE || l.status == LoanStatus.OVERDUE)
                .collect(Collectors.toList());
    }

    // Filter overdue loans using Java Stream API
    public List<LoanDetail> getOverdueLoanAlerts() {
        this.loanDao.refreshOverdueLoans(LocalDate.now());
        LocalDate today = LocalDate.now();
        return this.loanDao.findAllCombinedLoanDetails().stream()
                .filter(l -> l.status == LoanStatus.OVERDUE
                        || (l.status == LoanStatus.ACTIVE && l.dueDate != null && l.dueDate.isBefore(today)))
                .collect(Collectors.toList());
    }

    // List loan history for a specific student using Streams
    public List<LoanDetail> getStudentLoanHistory(int studentId) {
        return this.loanDao.findAllStudentLoanDetails().stream()
                .filter(l -> l.borrowerId == studentId)
                .collect(Collectors.toList());
    }

    // List loan history for a specific teacher using Streams
    public List<LoanDetail> getTeacherLoanHistory(int teacherId) {
        return this.loanDao.findAllTeacherLoanDetails().stream()
                .filter(l -> l.borrowerId == teacherId)
                .collect(Collectors.toList());
    }
}
