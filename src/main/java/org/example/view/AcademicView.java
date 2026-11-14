package org.example.view;

import org.example.controller.AcademicController;
import org.example.controller.StudentController;
import org.example.controller.TeacherController;
import org.example.dao.AcademicRecordDao.GradeDetail;
import org.example.dao.AttendanceDao.AttendanceDetail;
import org.example.model.AcademicPeriod;
import org.example.model.AcademicRecord;
import org.example.model.Attendance;
import org.example.model.AttendanceStatus;
import org.example.model.Student;
import org.example.model.Subject;
import org.example.util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AcademicView {

    private final Scanner consola;
    private final AcademicController academicController;
    private final StudentController studentController;
    private final TeacherController teacherController;

    // Constructor
    public AcademicView(Scanner consola) {
        this.consola = consola;
        this.academicController = new AcademicController();
        this.studentController = new StudentController();
        this.teacherController = new TeacherController();
    }

    // Menu loop for Grades and Attendance
    public void showMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("     MODULO 1C: GESTION ACADEMICA Y ASISTENCIAS");
            System.out.println("==================================================");
            System.out.println("1. Registrar calificacion (0.0 a 5.0) por periodo");
            System.out.println("2. Consultar historial academico y promedio por materia");
            System.out.println("3. Calcular promedio general de un estudiante en un periodo");
            System.out.println("4. Consultar promedio general del curso por materia");
            System.out.println("5. Registrar asistencia diaria");
            System.out.println("6. Consultar historial de asistencia de un estudiante");
            System.out.println("7. Reporte de patrones de inasistencia (>3 ausencias en 1 semana)");
            System.out.println("0. Volver al menu principal");

            int opcion = InputValidator.readMenuOption(this.consola, 0, 7);

            switch (opcion) {
                case 1:
                    this.recordGradeMenu();
                    break;
                case 2:
                    this.studentAcademicHistoryMenu();
                    break;
                case 3:
                    this.studentPeriodAverageMenu();
                    break;
                case 4:
                    this.courseAverageMenu();
                    break;
                case 5:
                    this.recordAttendanceMenu();
                    break;
                case 6:
                    this.studentAttendanceHistoryMenu();
                    break;
                case 7:
                    this.weeklyAbsenceReportMenu();
                    break;
                case 0:
                    return;
            }
        }
    }

    // 1. Record a grade (0.0 to 5.0)
    private void recordGradeMenu() {
        System.out.println("\n--- REGISTRAR CALIFICACION ---");
        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        int year = InputValidator.readIntRange(this.consola, "Año academico (ej. 2026): ", 2020, 2035);

        List<String> subjects = this.teacherController.listStudentSubjects(studentId, year);
        if (subjects.isEmpty()) {
            System.out.println("El estudiante no tiene materias inscritas en " + year);
            return;
        }
        System.out.println("Materias inscritas de " + student.getFullName() + ":");
        for (String line : subjects) {
            System.out.println(line);
        }

        int enrollmentId = InputValidator.readInt(this.consola, "Ingrese el ID de la inscripcion (Inscripcion ID): ");
        if (this.teacherController.findSubjectEnrollmentById(enrollmentId) == null) {
            System.out.println("Inscripcion no encontrada.");
            return;
        }

        AcademicPeriod period = this.askPeriod();
        double note = InputValidator.readDoubleRange(this.consola, "Valor de la nota (0.0 a 5.0): ", 0.0, 5.0);
        double percentage = InputValidator.readDoubleRange(this.consola, "Porcentaje del periodo (ej. 25): ", 1.0, 100.0);
        String remarks = InputValidator.readOptionalString(this.consola, "Observaciones: ");

        AcademicRecord record = new AcademicRecord(0, enrollmentId, year, period, note, percentage, remarks);
        int id = this.academicController.recordGrade(record);
        if (id > 0) {
            System.out.println("Calificacion guardada correctamente. Registro ID: " + id);
        } else {
            System.out.println("Error al guardar la calificacion.");
        }
    }

    // 2. Academic history and average per subject (Streams)
    private void studentAcademicHistoryMenu() {
        System.out.println("\n--- HISTORIAL ACADEMICO Y PROMEDIO POR MATERIA ---");
        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }

        System.out.println("1. Consultar todo el historial desde su ingreso");
        System.out.println("2. Filtrar por un año academico especifico");
        int op = InputValidator.readMenuOption(this.consola, 1, 2);

        List<GradeDetail> history;
        Map<String, Double> avgBySubject;
        if (op == 1) {
            history = this.academicController.getFullStudentAcademicHistory(studentId);
            avgBySubject = this.academicController.calculateFullStudentAverageBySubject(studentId);
        } else {
            int year = InputValidator.readIntRange(this.consola, "Año academico (ej. 2026): ", 2020, 2035);
            history = this.academicController.getStudentAcademicHistory(studentId, year);
            avgBySubject = this.academicController.calculateStudentAverageBySubject(studentId, year);
        }

        if (history.isEmpty()) {
            System.out.println("No hay calificaciones registradas para " + student.getFullName() + ".");
            return;
        }

        System.out.println("\nNotas registradas de " + student.getFullName() + ":");
        for (GradeDetail gd : history) {
            System.out.println(gd.toString());
        }

        System.out.println("\nPromedio por materia (calculado con Streams):");
        avgBySubject.forEach((subject, avg) ->
                System.out.printf("  - %-40s : %.2f%n", subject, avg));
    }

    // 3. Overall student average in a specific period (Streams)
    private void studentPeriodAverageMenu() {
        System.out.println("\n--- PROMEDIO GENERAL DEL ESTUDIANTE EN UN PERIODO ---");
        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        int year = InputValidator.readIntRange(this.consola, "Año academico (ej. 2026): ", 2020, 2035);
        AcademicPeriod period = this.askPeriod();

        double avg = this.academicController.calculateStudentPeriodAverage(studentId, year, period);
        if (avg == 0.0) {
            System.out.println("No se encontraron notas registradas para ese periodo.");
        } else {
            System.out.printf("Estudiante: %s | Periodo: %s (%d) | Promedio General: %.2f%n",
                    student.getFullName(), period.getDbValue(), year, avg);
        }
    }

    // 4. Course average by subject (Streams)
    private void courseAverageMenu() {
        System.out.println("\n--- PROMEDIO DEL CURSO POR MATERIA ---");
        int year = InputValidator.readIntRange(this.consola, "Año academico (ej. 2026): ", 2020, 2035);
        System.out.println("1. Consultar una materia especifica");
        System.out.println("2. Ver resumen de todas las materias con notas");
        int op = InputValidator.readMenuOption(this.consola, 1, 2);

        if (op == 1) {
            int subjectId = InputValidator.readInt(this.consola, "ID de la materia: ");
            Subject s = this.teacherController.findSubjectById(subjectId);
            if (s == null) {
                System.out.println("Materia no encontrada.");
                return;
            }
            double avg = this.academicController.calculateCourseAverageBySubject(subjectId, year);
            System.out.printf("Materia: [%s] %s | Promedio general del curso (%d): %.2f%n",
                    s.getCode(), s.getName(), year, avg);
        } else {
            Map<String, Double> averages = this.academicController.calculateAllCoursesAverages(year);
            if (averages.isEmpty()) {
                System.out.println("No hay calificaciones registradas en el año " + year);
            } else {
                System.out.println("\nPromedios por materia (" + year + "):");
                averages.forEach((subject, avg) ->
                        System.out.printf("  - %-45s : %.2f%n", subject, avg));
            }
        }
    }

    // 5. Record daily attendance
    private void recordAttendanceMenu() {
        System.out.println("\n--- REGISTRAR ASISTENCIA DIARIA ---");
        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        int year = InputValidator.readIntRange(this.consola, "Año academico (ej. 2026): ", 2020, 2035);
        List<String> assignments = this.teacherController.listAssignmentsByYear(year);
        if (assignments.isEmpty()) {
            System.out.println("No hay asignaciones disponibles.");
            return;
        }
        for (String line : assignments) {
            System.out.println(line);
        }

        int assignmentId = InputValidator.readInt(this.consola, "ID de la asignacion (clase): ");
        if (this.teacherController.findAssignmentById(assignmentId) == null) {
            System.out.println("Asignacion no encontrada.");
            return;
        }

        LocalDate date = InputValidator.readDate(this.consola, "Fecha (AAAA-MM-DD): ");
        AttendanceStatus status = this.askAttendanceStatus();
        String remarks = InputValidator.readOptionalString(this.consola, "Observaciones: ");

        Attendance att = new Attendance(0, studentId, assignmentId, date, status, remarks);
        int id = this.academicController.recordAttendance(att);
        if (id > 0) {
            System.out.println("Asistencia registrada con exito. ID: " + id);
        } else {
            System.out.println("Error al registrar asistencia (verifique si ya existe asistencia ese dia en esa clase).");
        }
    }

    // 6. Student attendance history
    private void studentAttendanceHistoryMenu() {
        System.out.println("\n--- HISTORIAL DE ASISTENCIA DE ESTUDIANTE ---");
        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        List<AttendanceDetail> list = this.academicController.getStudentAttendance(studentId);
        if (list.isEmpty()) {
            System.out.println("No hay registros de asistencia para ese estudiante.");
            return;
        }
        for (AttendanceDetail detail : list) {
            System.out.println(detail.toString());
        }
    }

    // 7. Weekly absence patterns (>3 absences in a week)
    private void weeklyAbsenceReportMenu() {
        System.out.println("\n--- REPORTE DE INASISTENCIA (>3 AUSENCIAS EN UNA SEMANA) ---");
        List<String> alerts = this.academicController.getWeeklyAbsencePatterns(3);
        if (alerts.isEmpty()) {
            System.out.println("No se detectaron estudiantes con mas de 3 ausencias en una misma semana.");
        } else {
            for (String alert : alerts) {
                System.out.println(alert);
            }
        }
    }

    private AcademicPeriod askPeriod() {
        System.out.println("Seleccione periodo (1. I | 2. II | 3. III | 4. IV | 5. Final):");
        int op = InputValidator.readMenuOption(this.consola, 1, 5);
        if (op == 1) return AcademicPeriod.FIRST;
        if (op == 2) return AcademicPeriod.SECOND;
        if (op == 3) return AcademicPeriod.THIRD;
        if (op == 4) return AcademicPeriod.FOURTH;
        return AcademicPeriod.FINAL;
    }

    private AttendanceStatus askAttendanceStatus() {
        System.out.println("Estado (1. Presente | 2. Ausente | 3. Tardanza | 4. Excusa Médica):");
        int op = InputValidator.readMenuOption(this.consola, 1, 4);
        if (op == 1) return AttendanceStatus.PRESENT;
        if (op == 2) return AttendanceStatus.ABSENT;
        if (op == 3) return AttendanceStatus.LATE;
        return AttendanceStatus.MEDICAL_EXCUSE;
    }
}
