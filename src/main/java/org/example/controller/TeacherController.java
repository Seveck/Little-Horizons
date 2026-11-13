package org.example.controller;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.model.Subject;
import org.example.model.SubjectEnrollment;
import org.example.model.Teacher;
import org.example.model.TeacherAssignment;
import org.example.model.TeacherStatus;

import java.util.List;
import java.util.stream.Collectors;

public class TeacherController {

    private final TeacherDao teacherDao;
    private final SubjectDao subjectDao;

    // Constructor
    public TeacherController() {
        this.teacherDao = new TeacherDao();
        this.subjectDao = new SubjectDao();
    }

    // Register a new teacher
    public int registerTeacher(Teacher teacher) {
        return this.teacherDao.insert(teacher);
    }

    // List all teachers
    public List<Teacher> listAllTeachers() {
        return this.teacherDao.findAll();
    }

    // Filter only active teachers using Java Stream API
    public List<Teacher> listActiveTeachers() {
        return this.teacherDao.findAll().stream()
                .filter(t -> t.getStatus() == TeacherStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    // Find teacher by id
    public Teacher findTeacherById(int id) {
        return this.teacherDao.findById(id);
    }

    // Register a new subject
    public int registerSubject(Subject subject) {
        return this.subjectDao.insert(subject);
    }

    // List all subjects
    public List<Subject> listAllSubjects() {
        return this.subjectDao.findAll();
    }

    // Find subject by id
    public Subject findSubjectById(int id) {
        return this.subjectDao.findById(id);
    }

    // Assign a teacher to a subject, grade, section and academic year
    public int assignTeacherToSubject(TeacherAssignment assignment) {
        return this.teacherDao.insertAssignment(assignment);
    }

    // List assignments for a specific academic year
    public List<String> listAssignmentsByYear(int academicYear) {
        return this.teacherDao.findAssignmentDetailsByYear(academicYear);
    }

    // Find assignment by id
    public TeacherAssignment findAssignmentById(int assignmentId) {
        return this.teacherDao.findAssignmentById(assignmentId);
    }

    // Enroll a student into a course assignment
    public int enrollStudentInSubject(SubjectEnrollment enrollment) {
        return this.subjectDao.enrollStudentInSubject(enrollment);
    }

    // List subjects enrolled by a student in a year
    public List<String> listStudentSubjects(int studentId, int academicYear) {
        return this.subjectDao.findStudentEnrollmentDetails(studentId, academicYear);
    }

    // Check if subject enrollment exists
    public SubjectEnrollment findSubjectEnrollmentById(int enrollmentId) {
        return this.subjectDao.findEnrollmentById(enrollmentId);
    }
}
