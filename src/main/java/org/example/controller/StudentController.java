package org.example.controller;

import org.example.dao.EnrollmentDao;
import org.example.dao.GuardianDao;
import org.example.dao.StudentDao;
import org.example.model.Enrollment;
import org.example.model.Grade;
import org.example.model.Guardian;
import org.example.model.Student;
import org.example.model.StudentStatus;

import java.util.List;
import java.util.stream.Collectors;

public class StudentController {

    private final StudentDao studentDao;
    private final GuardianDao guardianDao;
    private final EnrollmentDao enrollmentDao;

    // Constructor
    public StudentController() {
        this.studentDao = new StudentDao();
        this.guardianDao = new GuardianDao();
        this.enrollmentDao = new EnrollmentDao();
    }

    // Register student without guardian
    public int registerStudent(Student student) {
        return this.studentDao.insert(student);
    }

    // Register student and link guardian in one transaction
    public int registerStudentWithGuardian(Student student, int guardianId, boolean isPrimary, String relationship) {
        return this.studentDao.registerStudentWithGuardian(student, guardianId, isPrimary, relationship);
    }

    // List all students
    public List<Student> listAllStudents() {
        return this.studentDao.findAll();
    }

    // Search students by name using Java Stream API
    public List<Student> searchStudentsByName(String search) {
        String text = search.toLowerCase();
        return this.studentDao.findAll().stream()
                .filter(s -> s.getFullName().toLowerCase().contains(text))
                .collect(Collectors.toList());
    }

    // Find student by id
    public Student findStudentById(int id) {
        return this.studentDao.findById(id);
    }

    // Find student by document number
    public Student findStudentByDocument(String documentNumber) {
        return this.studentDao.findByDocument(documentNumber);
    }

    // Change student status
    public boolean updateStudentStatus(int studentId, StudentStatus status) {
        return this.studentDao.updateStatus(studentId, status);
    }

    // Register a new guardian
    public int registerGuardian(Guardian guardian) {
        return this.guardianDao.insert(guardian);
    }

    // List all guardians
    public List<Guardian> listAllGuardians() {
        return this.guardianDao.findAll();
    }

    // Find guardian by id
    public Guardian findGuardianById(int id) {
        return this.guardianDao.findById(id);
    }

    // Find guardian by document
    public Guardian findGuardianByDocument(String document) {
        return this.guardianDao.findByDocument(document);
    }

    // Link an existing guardian to a student (supports siblings)
    public boolean linkGuardianToStudent(int studentId, int guardianId, boolean isPrimary, String relationship) {
        return this.studentDao.linkGuardian(studentId, guardianId, isPrimary, relationship);
    }

    // Get guardians of a student
    public List<Guardian> getGuardiansByStudent(int studentId) {
        return this.guardianDao.findByStudentId(studentId);
    }

    // Get all students (siblings) under the same guardian
    public List<Student> getStudentsByGuardian(int guardianId) {
        return this.studentDao.findByGuardianId(guardianId);
    }

    // List all grades (grados)
    public List<Grade> listAllGrades() {
        return this.enrollmentDao.findAllGrades();
    }

    // Enroll student in a grade for an academic year
    public int enrollStudent(Enrollment enrollment) {
        return this.enrollmentDao.insert(enrollment);
    }

    // List enrollments by grade and year
    public List<String> listEnrollmentsByGradeAndYear(int gradeId, int academicYear) {
        return this.enrollmentDao.findEnrollmentDetailsByGradeAndYear(gradeId, academicYear);
    }
}
