package org.example.controller;

import org.example.dao.AcademicRecordDao;
import org.example.dao.AcademicRecordDao.GradeDetail;
import org.example.dao.AttendanceDao;
import org.example.dao.AttendanceDao.AttendanceDetail;
import org.example.model.AcademicPeriod;
import org.example.model.AcademicRecord;
import org.example.model.Attendance;
import org.example.model.AttendanceStatus;

import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AcademicController {

    private final AcademicRecordDao academicRecordDao;
    private final AttendanceDao attendanceDao;

    // Constructor
    public AcademicController() {
        this.academicRecordDao = new AcademicRecordDao();
        this.attendanceDao = new AttendanceDao();
    }

    // Record a new grade (0.0 to 5.0)
    public int recordGrade(AcademicRecord record) {
        return this.academicRecordDao.insert(record);
    }

    // Get full academic history of a student since enrollment
    public List<GradeDetail> getFullStudentAcademicHistory(int studentId) {
        return this.academicRecordDao.findAllGradeDetailsByStudent(studentId);
    }

    // Calculate student's average grade per subject across all history using Streams
    public Map<String, Double> calculateFullStudentAverageBySubject(int studentId) {
        List<GradeDetail> studentGrades = this.getFullStudentAcademicHistory(studentId);
        return studentGrades.stream()
                .collect(Collectors.groupingBy(
                        g -> "[" + g.subjectCode + "] " + g.subjectName,
                        Collectors.averagingDouble(g -> g.gradeValue)
                ));
    }

    // Get academic history of a student for a given year using Streams
    public List<GradeDetail> getStudentAcademicHistory(int studentId, int academicYear) {
        List<GradeDetail> allGrades = this.academicRecordDao.findGradeDetailsByYear(academicYear);
        return allGrades.stream()
                .filter(g -> g.studentId == studentId)
                .collect(Collectors.toList());
    }

    // Calculate student's average grade per subject using Streams
    public Map<String, Double> calculateStudentAverageBySubject(int studentId, int academicYear) {
        List<GradeDetail> studentGrades = this.getStudentAcademicHistory(studentId, academicYear);
        return studentGrades.stream()
                .collect(Collectors.groupingBy(
                        g -> "[" + g.subjectCode + "] " + g.subjectName,
                        Collectors.averagingDouble(g -> g.gradeValue)
                ));
    }

    // Calculate student's overall average in a specific period using Streams
    public double calculateStudentPeriodAverage(int studentId, int academicYear, AcademicPeriod period) {
        List<GradeDetail> studentGrades = this.getStudentAcademicHistory(studentId, academicYear);
        return studentGrades.stream()
                .filter(g -> g.period == period)
                .mapToDouble(g -> g.gradeValue)
                .average()
                .orElse(0.0);
    }

    // Calculate course average by subject for a given year using Streams
    public double calculateCourseAverageBySubject(int subjectId, int academicYear) {
        List<GradeDetail> allGrades = this.academicRecordDao.findGradeDetailsByYear(academicYear);
        return allGrades.stream()
                .filter(g -> g.subjectId == subjectId)
                .mapToDouble(g -> g.gradeValue)
                .average()
                .orElse(0.0);
    }

    // Calculate averages for all courses by subject using Streams
    public Map<String, Double> calculateAllCoursesAverages(int academicYear) {
        List<GradeDetail> allGrades = this.academicRecordDao.findGradeDetailsByYear(academicYear);
        return allGrades.stream()
                .collect(Collectors.groupingBy(
                        g -> "[" + g.subjectCode + "] " + g.subjectName,
                        Collectors.averagingDouble(g -> g.gradeValue)
                ));
    }

    // Record daily attendance
    public int recordAttendance(Attendance attendance) {
        return this.attendanceDao.insert(attendance);
    }

    // Get attendance history for a student
    public List<AttendanceDetail> getStudentAttendance(int studentId) {
        return this.attendanceDao.findDetailsByStudent(studentId);
    }

    // Report students with more than 'minAbsences' absences in a single week using Streams
    public List<String> getWeeklyAbsencePatterns(int minAbsences) {
        List<AttendanceDetail> allRecords = this.attendanceDao.findAllDetails();

        // Filter only ABSENT records
        List<AttendanceDetail> absents = allRecords.stream()
                .filter(a -> a.status == AttendanceStatus.ABSENT && a.date != null)
                .collect(Collectors.toList());

        // Group by student name + year-week key
        WeekFields weekFields = WeekFields.ISO;
        Map<String, Long> grouped = absents.stream()
                .collect(Collectors.groupingBy(
                        a -> a.studentId + " - " + a.studentName
                                + " (Semana " + a.date.get(weekFields.weekOfWeekBasedYear())
                                + " del año " + a.date.getYear() + ")",
                        Collectors.counting()
                ));

        // Filter entries exceeding the threshold (e.g. > 3 absences)
        List<String> alerts = new ArrayList<>();
        grouped.entrySet().stream()
                .filter(entry -> entry.getValue() > minAbsences)
                .forEach(entry -> alerts.add("ALERTA: Estudiante ID " + entry.getKey()
                        + " -> " + entry.getValue() + " ausencias registradas en la semana."));

        return alerts;
    }
}
