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
import org.example.model.SubjectPerformance;
import org.example.model.Teacher;
import org.example.util.InputValidator;

import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AcademicView {

    private final Scanner scanner;
    private final AcademicController academicController;
    private final StudentController studentController;
    private final TeacherController teacherController;

    // Constructor
    public AcademicView(Scanner scanner) {
        this.scanner = scanner;
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
            System.out.println("8. Corregir calificacion registrada");
            System.out.println("9. Actualizar estado de asistencia");
            System.out.println("10. Generar Reporte de Rendimiento Academico por Materia (.txt)");
            System.out.println("0. Volver al menu principal");
            System.out.println("Digite su opcion:");

            try {
                int option = this.scanner.nextInt();
                this.scanner.nextLine();

                switch (option) {
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
                    case 8:
                        this.updateGradeMenu();
                        break;
                    case 9:
                        this.updateAttendanceMenu();
                        break;
                    case 10:
                        this.subjectPerformanceReportMenu();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Error, opcion invalida");
                }
            } catch (InputMismatchException e) {
                System.out.println("Error, digito invalido");
                this.scanner.nextLine();
            }
        }
    }

    // 1. Record a grade (0.0 to 5.0)
    private void recordGradeMenu() {
        System.out.println("\n--- REGISTRAR CALIFICACION ---");
        int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        System.out.println("Estudiante: " + student.getFullName());

        List<Subject> allSubjects = this.teacherController.listAllSubjects();
        for (Subject s : allSubjects) {
            System.out.println(s.toString());
        }
        int subjectId = InputValidator.readInt(this.scanner, "ID de la materia: ");
        Subject subject = this.teacherController.findSubjectById(subjectId);
        if (subject == null) {
            System.out.println("Materia no encontrada.");
            return;
        }

        int teacherId = InputValidator.readInt(this.scanner, "ID del profesor que califica: ");
        Teacher teacher = this.teacherController.findTeacherById(teacherId);
        if (teacher == null) {
            System.out.println("Profesor no encontrado.");
            return;
        }

        int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");
        AcademicPeriod period = this.askPeriod();
        double note = InputValidator.readPositiveDouble(this.scanner, "Valor de la nota (0.0 a 5.0): ");
        while (note > 5.0) {
            System.out.println("Error, la nota debe estar entre 0.0 y 5.0.");
            note = InputValidator.readPositiveDouble(this.scanner, "Valor de la nota (0.0 a 5.0): ");
        }
        String observations = InputValidator.readOptionalString(this.scanner, "Observaciones: ");

        AcademicRecord record = new AcademicRecord();
        record.setStudentId(studentId);
        record.setSubjectId(subjectId);
        record.setTeacherId(teacherId);
        record.setAcademicYear(year);
        record.setPeriod(period);
        record.setGradeValue(note);
        record.setObservations(observations.isEmpty() ? null : observations);
        record.setRecordDate(LocalDate.now());

        int id = this.academicController.recordGrade(record);
        if (id > 0) {
            System.out.println("Calificacion guardada correctamente. Registro ID: " + id);
        } else {
            System.out.println("Error al guardar la calificacion (verifique si ya existe nota en ese periodo).");
        }
    }

    // 2. Academic history and average per subject (Streams)
    private void studentAcademicHistoryMenu() {
        System.out.println("\n--- HISTORIAL ACADEMICO Y PROMEDIO POR MATERIA ---");
        int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }

        System.out.println("1. Consultar todo el historial desde su ingreso");
        System.out.println("2. Filtrar por un año academico especifico");
        int op = InputValidator.readInt(this.scanner, "Digite su opcion: ");

        List<GradeDetail> history;
        Map<String, Double> avgBySubject;
        if (op == 1) {
            history = this.academicController.getFullStudentAcademicHistory(studentId);
            avgBySubject = this.academicController.calculateFullStudentAverageBySubject(studentId);
        } else {
            int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");
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
        int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");
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
        int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");
        System.out.println("1. Consultar una materia especifica");
        System.out.println("2. Ver resumen de todas las materias con notas");
        int op = InputValidator.readInt(this.scanner, "Digite su opcion: ");

        if (op == 1) {
            int subjectId = InputValidator.readInt(this.scanner, "ID de la materia: ");
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
        int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        System.out.println("Estudiante: " + student.getFullName());

        List<Subject> subjects = this.teacherController.listAllSubjects();
        for (Subject s : subjects) {
            System.out.println(s.toString());
        }
        int subjectId = InputValidator.readInt(this.scanner, "ID de la materia: ");
        if (this.teacherController.findSubjectById(subjectId) == null) {
            System.out.println("Materia no encontrada.");
            return;
        }

        int teacherId = InputValidator.readInt(this.scanner, "ID del profesor que registra: ");
        if (this.teacherController.findTeacherById(teacherId) == null) {
            System.out.println("Profesor no encontrado.");
            return;
        }

        LocalDate date = InputValidator.readDate(this.scanner, "Fecha (AAAA-MM-DD): ");
        AttendanceStatus status = this.askAttendanceStatus();
        String observations = InputValidator.readOptionalString(this.scanner, "Observaciones: ");

        Attendance att = new Attendance();
        att.setStudentId(studentId);
        att.setSubjectId(subjectId);
        att.setAttendanceDate(date);
        att.setStatus(status);
        att.setObservations(observations.isEmpty() ? null : observations);
        att.setRecordedBy(teacherId);

        int id = this.academicController.recordAttendance(att);
        if (id > 0) {
            System.out.println("Asistencia registrada con exito. ID: " + id);
        } else {
            System.out.println("Error al registrar asistencia (verifique si ya existe asistencia ese dia en esa materia).");
        }
    }

    // 6. Student attendance history
    private void studentAttendanceHistoryMenu() {
        System.out.println("\n--- HISTORIAL DE ASISTENCIA DE ESTUDIANTE ---");
        int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
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

    // 8. Update existing grade record
    private void updateGradeMenu() {
        System.out.println("\n--- CORREGIR CALIFICACION REGISTRADA ---");
        int recordId = InputValidator.readInt(this.scanner, "ID del registro academico: ");
        double newGrade = InputValidator.readPositiveDouble(this.scanner, "Nueva nota (0.0 a 5.0): ");
        while (newGrade > 5.0) {
            System.out.println("Error, la nota debe estar entre 0.0 y 5.0.");
            newGrade = InputValidator.readPositiveDouble(this.scanner, "Nueva nota (0.0 a 5.0): ");
        }
        String observations = InputValidator.readOptionalString(this.scanner, "Observaciones actualizadas: ");

        if (this.academicController.updateGrade(recordId, newGrade, observations.isEmpty() ? null : observations)) {
            System.out.println("Calificacion actualizada correctamente.");
        } else {
            System.out.println("No se encontro un registro con ese ID.");
        }
    }

    // 9. Update existing attendance record
    private void updateAttendanceMenu() {
        System.out.println("\n--- ACTUALIZAR ESTADO DE ASISTENCIA ---");
        int attendanceId = InputValidator.readInt(this.scanner, "ID de la asistencia: ");
        AttendanceStatus status = this.askAttendanceStatus();
        String observations = InputValidator.readOptionalString(this.scanner, "Observaciones actualizadas: ");

        if (this.academicController.updateAttendanceStatus(attendanceId, status, observations.isEmpty() ? null : observations)) {
            System.out.println("Asistencia actualizada correctamente.");
        } else {
            System.out.println("No se encontro una asistencia con ese ID.");
        }
    }

    // 10. Generate Academic Performance Report by Subject (console + .txt file)
    public void subjectPerformanceReportMenu() {
        List<SubjectPerformance> ranking = this.academicController.getSubjectPerformanceRanking();

        if (ranking.isEmpty()) {
            System.out.println("\n================================================================");
            System.out.println("No hay registros academicos suficientes para generar estadisticas.");
            System.out.println("Por favor, asegurese de que existan notas registradas en el sistema.");
            System.out.println("================================================================");
            System.out.println("Presione Enter para volver al menu...");
            this.scanner.nextLine();
            return;
        }

        List<SubjectPerformance> topSubjects = this.academicController.findTopPerformingSubjects(ranking);
        String reportContent = this.academicController.buildSubjectPerformanceReport(ranking, topSubjects);
        String savedFilePath = this.academicController.saveSubjectPerformanceReportTxt(reportContent);

        System.out.println();
        System.out.print(reportContent);
        if (savedFilePath != null) {
            System.out.println("Reporte generado exitosamente.");
            System.out.println("Archivo .txt guardado en: " + savedFilePath);
        } else {
            System.out.println("Reporte mostrado en consola, pero ocurrio un error al guardar el archivo .txt (revise error_log.txt).");
        }
        System.out.println("Presione Enter para volver al menu...");
        System.out.println("================================================================");
        this.scanner.nextLine();
    }

    private AcademicPeriod askPeriod() {
        System.out.println("Seleccione periodo (1. I | 2. II | 3. III | 4. IV | 5. Final):");
        int op = InputValidator.readInt(this.scanner, "Digite su opcion: ");
        if (op == 1) return AcademicPeriod.FIRST;
        if (op == 2) return AcademicPeriod.SECOND;
        if (op == 3) return AcademicPeriod.THIRD;
        if (op == 4) return AcademicPeriod.FOURTH;
        return AcademicPeriod.FINAL;
    }

    private AttendanceStatus askAttendanceStatus() {
        System.out.println("Estado (1. Presente | 2. Ausente | 3. Tardanza | 4. Excusa Medica):");
        int op = InputValidator.readInt(this.scanner, "Digite su opcion: ");
        if (op == 1) return AttendanceStatus.PRESENT;
        if (op == 2) return AttendanceStatus.ABSENT;
        if (op == 3) return AttendanceStatus.LATE;
        return AttendanceStatus.MEDICAL_EXCUSE;
    }
}
