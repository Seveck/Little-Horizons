package org.example.view;

import org.example.controller.StudentController;
import org.example.controller.TeacherController;
import org.example.model.AcademicPeriod;
import org.example.model.DocumentType;
import org.example.model.Enrollment;
import org.example.model.EnrollmentStatus;
import org.example.model.Grade;
import org.example.model.Student;
import org.example.model.Subject;
import org.example.model.SubjectEnrollment;
import org.example.model.SubjectEnrollmentStatus;
import org.example.model.Teacher;
import org.example.model.TeacherAssignment;
import org.example.model.TeacherStatus;
import org.example.util.InputValidator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class TeacherView {

    private final Scanner scanner;
    private final TeacherController teacherController;
    private final StudentController studentController;

    // Constructor
    public TeacherView(Scanner scanner) {
        this.scanner = scanner;
        this.teacherController = new TeacherController();
        this.studentController = new StudentController();
    }

    // Menu loop for Teachers, Subjects & Assignments
    public void showMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("     MODULO 1B: PROFESORES, MATERIAS E INSCRIPCION");
            System.out.println("==================================================");
            System.out.println("1. Registrar nuevo profesor");
            System.out.println("2. Listar directorio de profesores");
            System.out.println("3. Registrar nueva materia por grado");
            System.out.println("4. Listar catalogo de materias");
            System.out.println("5. Asignar materia y horario a un profesor");
            System.out.println("6. Listar asignaciones de profesores por año");
            System.out.println("7. Inscribir estudiante en una materia");
            System.out.println("8. Consultar materias inscritas de un estudiante");
            System.out.println("0. Volver al menu principal");
            System.out.println("Digite su opcion:");

            try {
                int option = this.scanner.nextInt();
                this.scanner.nextLine();

                switch (option) {
                    case 1:
                        this.registerTeacherMenu();
                        break;
                    case 2:
                        this.listTeachersMenu();
                        break;
                    case 3:
                        this.registerSubjectMenu();
                        break;
                    case 4:
                        this.listSubjectsMenu();
                        break;
                    case 5:
                        this.assignTeacherMenu();
                        break;
                    case 6:
                        this.listAssignmentsMenu();
                        break;
                    case 7:
                        this.enrollStudentInSubjectMenu();
                        break;
                    case 8:
                        this.listStudentSubjectsMenu();
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

    // 1. Register new teacher
    private void registerTeacherMenu() {
        System.out.println("\n--- REGISTRAR NUEVO PROFESOR ---");
        System.out.println("Tipo de documento (1. CC | 2. CE | 3. Pasaporte):");
        int dt = InputValidator.readInt(this.scanner, "Digite su opcion: ");
        DocumentType docType = DocumentType.CC;
        if (dt == 2) docType = DocumentType.CE;
        if (dt == 3) docType = DocumentType.PASSPORT;

        String docNum = InputValidator.readDocument(this.scanner, "Numero de documento: ");
        String firstName = InputValidator.readString(this.scanner, "Primer nombre: ");
        String middleName = InputValidator.readOptionalString(this.scanner, "Segundo nombre (opcional): ");
        String lastName = InputValidator.readString(this.scanner, "Primer apellido: ");
        String secondLastName = InputValidator.readOptionalString(this.scanner, "Segundo apellido (opcional): ");
        String specialization = InputValidator.readString(this.scanner, "Especializacion o area: ");
        String email = InputValidator.readEmail(this.scanner, "Correo institucional: ");
        String phone = InputValidator.readOptionalString(this.scanner, "Telefono: ");
        LocalDate hireDate = InputValidator.readDate(this.scanner, "Fecha contratacion (AAAA-MM-DD): ");

        Teacher t = new Teacher();
        t.setDocumentType(docType);
        t.setDocumentNumber(docNum);
        t.setFirstName(firstName);
        t.setMiddleName(middleName.isEmpty() ? null : middleName);
        t.setLastName(lastName);
        t.setSecondLastName(secondLastName.isEmpty() ? null : secondLastName);
        t.setSpecialization(specialization);
        t.setEmail(email);
        t.setPhone(phone.isEmpty() ? null : phone);
        t.setHireDate(hireDate);
        t.setStatus(TeacherStatus.ACTIVE);

        int id = this.teacherController.registerTeacher(t);
        if (id > 0) {
            System.out.println("Profesor registrado exitosamente. ID: " + id);
        } else {
            System.out.println("Error al registrar profesor (verifique documento o correo duplicado).");
        }
    }

    // 2. List teachers
    private void listTeachersMenu() {
        System.out.println("\n--- DIRECTORIO DE PROFESORES ---");
        System.out.println("1. Ver todos los profesores");
        System.out.println("2. Ver solo profesores activos");
        int op = InputValidator.readInt(this.scanner, "Digite su opcion: ");

        List<Teacher> list;
        if (op == 1) {
            list = this.teacherController.listAllTeachers();
        } else {
            list = this.teacherController.listActiveTeachers();
        }

        if (list.isEmpty()) {
            System.out.println("No hay profesores para mostrar.");
            return;
        }
        for (Teacher t : list) {
            System.out.println(t.toString());
        }
        System.out.println("Total profesores: " + list.size());
    }

    // 3. Register subject linked to a grade
    private void registerSubjectMenu() {
        System.out.println("\n--- REGISTRAR NUEVA MATERIA ---");
        String code = InputValidator.readString(this.scanner, "Codigo materia (ej. MAT-6): ").toUpperCase();
        String name = InputValidator.readString(this.scanner, "Nombre de la materia: ");
        String desc = InputValidator.readOptionalString(this.scanner, "Descripcion breve: ");

        List<Grade> grades = this.studentController.listAllGrades();
        for (Grade g : grades) {
            System.out.println(g.toString());
        }
        int gradeId = InputValidator.readInt(this.scanner, "ID del grado al que pertenece la materia: ");
        int weeklyHours = InputValidator.readInt(this.scanner, "Horas semanales (ej. 4): ");

        Subject s = new Subject();
        s.setCode(code);
        s.setName(name);
        s.setDescription(desc.isEmpty() ? null : desc);
        s.setGradeId(gradeId);
        s.setWeeklyHours(weeklyHours);
        s.setActive(true);

        int id = this.teacherController.registerSubject(s);
        if (id > 0) {
            System.out.println("Materia registrada con exito. ID: " + id);
        } else {
            System.out.println("Error al registrar materia (verifique codigo duplicado o ID de grado).");
        }
    }

    // 4. List subjects
    private void listSubjectsMenu() {
        System.out.println("\n--- CATALOGO DE MATERIAS ---");
        List<Subject> list = this.teacherController.listAllSubjects();
        if (list.isEmpty()) {
            System.out.println("No hay materias registradas.");
            return;
        }
        for (Subject s : list) {
            System.out.println(s.toString());
        }
        System.out.println("Total materias: " + list.size());
    }

    // 5. Assign teacher to subject, section and schedule
    private void assignTeacherMenu() {
        System.out.println("\n--- ASIGNAR PROFESOR A MATERIA Y HORARIO ---");
        int teacherId = InputValidator.readInt(this.scanner, "ID del profesor: ");
        Teacher teacher = this.teacherController.findTeacherById(teacherId);
        if (teacher == null) {
            System.out.println("Profesor no encontrado.");
            return;
        }
        System.out.println("Profesor: " + teacher.getFullName());

        int subjectId = InputValidator.readInt(this.scanner, "ID de la materia: ");
        Subject subject = this.teacherController.findSubjectById(subjectId);
        if (subject == null) {
            System.out.println("Materia no encontrada.");
            return;
        }
        System.out.println("Materia: " + subject.getName() + " (" + subject.getGradeName() + ")");

        String section = InputValidator.readString(this.scanner, "Seccion (ej. A, B): ").toUpperCase();
        int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");
        String scheduleDays = InputValidator.readString(this.scanner, "Dias de clase (ej. Lunes, Miercoles): ");
        LocalTime startTime = InputValidator.readTime(this.scanner, "Hora de inicio (HH:MM, ej. 07:00): ");
        LocalTime endTime;
        while (true) {
            endTime = InputValidator.readTime(this.scanner, "Hora de fin (HH:MM, ej. 08:30): ");
            if (endTime.isAfter(startTime)) {
                break;
            }
            System.out.println("Error, la hora de fin debe ser posterior a la hora de inicio.");
        }

        TeacherAssignment ta = new TeacherAssignment();
        ta.setTeacherId(teacherId);
        ta.setSubjectId(subjectId);
        ta.setAcademicYear(year);
        ta.setSection(section);
        ta.setScheduleDays(scheduleDays);
        ta.setStartTime(startTime);
        ta.setEndTime(endTime);

        int id = this.teacherController.assignTeacherToSubject(ta);
        if (id > 0) {
            System.out.println("Asignacion creada con exito. ID Asignacion: " + id);
        } else if (id == -2) {
            System.out.println("Conflicto de horario: el profesor ya tiene clase asignada en esos dias y horas.");
        } else {
            System.out.println("No se pudo crear la asignacion (verifique datos o si ya existe).");
        }
    }

    // 6. List assignments by year
    private void listAssignmentsMenu() {
        System.out.println("\n--- ASIGNACIONES ACADEMICAS ---");
        int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");
        List<String> list = this.teacherController.listAssignmentsByYear(year);
        if (list.isEmpty()) {
            System.out.println("No hay asignaciones registradas para el año " + year);
            return;
        }
        for (String line : list) {
            System.out.println(line);
        }
        System.out.println("Total asignaciones: " + list.size());
    }

    // 7. Enroll student in subject matching their enrolled grade
    private void enrollStudentInSubjectMenu() {
        System.out.println("\n--- INSCRIBIR ESTUDIANTE EN MATERIA ---");
        int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        System.out.println("Estudiante: " + student.getFullName());

        int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");
        int enrolledGradeId = -1;
        List<Enrollment> enrollments = this.studentController.getStudentEnrollments(studentId);
        for (Enrollment e : enrollments) {
            if (e.getAcademicYear() == year && e.getStatus() == EnrollmentStatus.ACTIVE) {
                enrolledGradeId = e.getGradeId();
                System.out.println("Grado matriculado en " + year + ": " + e.getGradeName());
                break;
            }
        }

        List<Subject> subjects = this.teacherController.listAllSubjects();
        System.out.println("\nMaterias disponibles" + (enrolledGradeId > 0 ? " para su grado:" : ":"));
        for (Subject s : subjects) {
            if (enrolledGradeId <= 0 || s.getGradeId() == enrolledGradeId) {
                System.out.println(s.toString());
            }
        }

        int subjectId = InputValidator.readInt(this.scanner, "ID de la materia: ");
        Subject chosen = this.teacherController.findSubjectById(subjectId);
        if (chosen == null) {
            System.out.println("Materia no encontrada.");
            return;
        }
        if (enrolledGradeId > 0 && chosen.getGradeId() != enrolledGradeId) {
            System.out.println("Error, esta materia pertenece a " + chosen.getGradeName()
                    + " y no corresponde al grado matriculado del estudiante.");
            return;
        }

        System.out.println("Periodo academico (1. I | 2. II | 3. III | 4. IV):");
        int perOp = InputValidator.readInt(this.scanner, "Digite su opcion: ");
        AcademicPeriod period = AcademicPeriod.FIRST;
        if (perOp == 2) period = AcademicPeriod.SECOND;
        if (perOp == 3) period = AcademicPeriod.THIRD;
        if (perOp == 4) period = AcademicPeriod.FOURTH;

        String section = InputValidator.readString(this.scanner, "Seccion (ej. A, B): ").toUpperCase();

        SubjectEnrollment se = new SubjectEnrollment();
        se.setStudentId(studentId);
        se.setSubjectId(subjectId);
        se.setAcademicYear(year);
        se.setPeriod(period);
        se.setSection(section);
        se.setEnrollmentDate(LocalDate.now());
        se.setStatus(SubjectEnrollmentStatus.ACTIVE);

        int id = this.teacherController.enrollStudentInSubject(se);
        if (id > 0) {
            System.out.println("Estudiante inscrito en la materia. ID de inscripcion: " + id);
        } else {
            System.out.println("No se pudo inscribir (verifique si ya esta inscrito en esa materia y periodo).");
        }
    }

    // 8. List student's enrolled subjects
    private void listStudentSubjectsMenu() {
        System.out.println("\n--- MATERIAS INSCRITAS DE UN ESTUDIANTE ---");
        int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
        int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");

        List<String> list = this.teacherController.listStudentSubjects(studentId, year);
        if (list.isEmpty()) {
            System.out.println("El estudiante no tiene materias inscritas en " + year);
            return;
        }
        for (String line : list) {
            System.out.println(line);
        }
    }
}
