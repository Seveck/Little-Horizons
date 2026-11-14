package org.example.view;

import org.example.controller.StudentController;
import org.example.controller.TeacherController;
import org.example.model.DocumentType;
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
import java.util.List;
import java.util.Scanner;

public class TeacherView {

    private final Scanner consola;
    private final TeacherController teacherController;
    private final StudentController studentController;

    // Constructor
    public TeacherView(Scanner consola) {
        this.consola = consola;
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
            System.out.println("3. Registrar nueva materia");
            System.out.println("4. Listar catalogo de materias");
            System.out.println("5. Asignar materia y grado a un profesor");
            System.out.println("6. Listar asignaciones de profesores por año");
            System.out.println("7. Inscribir estudiante en una materia");
            System.out.println("8. Consultar materias inscritas de un estudiante");
            System.out.println("0. Volver al menu principal");

            int opcion = InputValidator.readMenuOption(this.consola, 0, 8);

            switch (opcion) {
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
            }
        }
    }

    // 1. Register new teacher
    private void registerTeacherMenu() {
        System.out.println("\n--- REGISTRAR NUEVO PROFESOR ---");
        System.out.println("Tipo de documento (1. CC | 2. CE | 3. Pasaporte):");
        int dt = InputValidator.readMenuOption(this.consola, 1, 3);
        DocumentType docType = DocumentType.CC;
        if (dt == 2) docType = DocumentType.CE;
        if (dt == 3) docType = DocumentType.PASSPORT;

        String docNum = InputValidator.readDocument(this.consola, "Numero de documento: ");
        String firstName = InputValidator.readString(this.consola, "Nombres: ");
        String lastName = InputValidator.readString(this.consola, "Apellidos: ");
        String specialty = InputValidator.readString(this.consola, "Especialidad o titulo: ");
        String email = InputValidator.readEmail(this.consola, "Correo institucional: ");
        String phone = InputValidator.readString(this.consola, "Telefono: ");
        LocalDate hireDate = InputValidator.readDate(this.consola, "Fecha contratacion (AAAA-MM-DD): ");

        Teacher t = new Teacher(0, docType, docNum, firstName, lastName,
                specialty, email, phone, hireDate, TeacherStatus.ACTIVE);
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
        int op = InputValidator.readMenuOption(this.consola, 1, 2);

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

    // 3. Register subject
    private void registerSubjectMenu() {
        System.out.println("\n--- REGISTRAR NUEVA MATERIA ---");
        String code = InputValidator.readString(this.consola, "Codigo materia (ej. MAT-601): ").toUpperCase();
        String name = InputValidator.readString(this.consola, "Nombre de la materia: ");
        String desc = InputValidator.readOptionalString(this.consola, "Descripcion breve: ");
        int hours = InputValidator.readIntRange(this.consola, "Intensidad horaria semanal (1-10): ", 1, 10);
        String area = InputValidator.readString(this.consola, "Area academica (ej. Matematicas, Ciencias Naturales): ");

        Subject s = new Subject(0, code, name, desc, hours, area);
        int id = this.teacherController.registerSubject(s);
        if (id > 0) {
            System.out.println("Materia registrada con exito. ID: " + id);
        } else {
            System.out.println("Error al registrar materia (verifique codigo duplicado).");
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

    // 5. Assign teacher to subject, grade, section
    private void assignTeacherMenu() {
        System.out.println("\n--- ASIGNAR PROFESOR A MATERIA Y GRADO ---");
        int teacherId = InputValidator.readInt(this.consola, "ID del profesor: ");
        Teacher teacher = this.teacherController.findTeacherById(teacherId);
        if (teacher == null) {
            System.out.println("Profesor no encontrado.");
            return;
        }
        System.out.println("Profesor: " + teacher.getFullName());

        int subjectId = InputValidator.readInt(this.consola, "ID de la materia: ");
        Subject subject = this.teacherController.findSubjectById(subjectId);
        if (subject == null) {
            System.out.println("Materia no encontrada.");
            return;
        }
        System.out.println("Materia: " + subject.getName());

        List<Grade> grades = this.studentController.listAllGrades();
        for (Grade g : grades) {
            System.out.println(g.toString());
        }
        int gradeId = InputValidator.readInt(this.consola, "ID del grado: ");
        String section = InputValidator.readString(this.consola, "Seccion (ej. A, B): ").toUpperCase();
        int year = InputValidator.readIntRange(this.consola, "Año academico (ej. 2026): ", 2020, 2035);
        String scheduleDays = InputValidator.readString(this.consola, "Dias de clase (ej. Lunes, Miercoles): ");
        LocalTime startTime = InputValidator.readTime(this.consola, "Hora de inicio (HH:MM, ej. 07:00): ");
        LocalTime endTime;
        while (true) {
            endTime = InputValidator.readTime(this.consola, "Hora de fin (HH:MM, ej. 08:30): ");
            if (endTime.isAfter(startTime)) {
                break;
            }
            System.out.println("Error, la hora de fin debe ser posterior a la hora de inicio.");
        }

        TeacherAssignment ta = new TeacherAssignment(
                0, teacherId, subjectId, gradeId, section, year, scheduleDays, startTime, endTime);
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
        int year = InputValidator.readIntRange(this.consola, "Año academico (ej. 2026): ", 2020, 2035);
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

    // 7. Enroll student in subject (assignment)
    private void enrollStudentInSubjectMenu() {
        System.out.println("\n--- INSCRIBIR ESTUDIANTE EN MATERIA ---");
        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        System.out.println("Estudiante: " + student.getFullName());

        int year = InputValidator.readIntRange(this.consola, "Año academico (ej. 2026): ", 2020, 2035);
        List<String> assignments = this.teacherController.listAssignmentsByYear(year);
        if (assignments.isEmpty()) {
            System.out.println("No hay asignaciones disponibles en ese año.");
            return;
        }
        System.out.println("\nAsignaciones disponibles:");
        for (String a : assignments) {
            System.out.println(a);
        }

        int assignmentId = InputValidator.readInt(this.consola, "Digite el ID de la asignacion: ");
        if (this.teacherController.findAssignmentById(assignmentId) == null) {
            System.out.println("Asignacion no encontrada.");
            return;
        }

        SubjectEnrollment se = new SubjectEnrollment(0, studentId, assignmentId, year, SubjectEnrollmentStatus.IN_PROGRESS);
        int id = this.teacherController.enrollStudentInSubject(se);
        if (id > 0) {
            System.out.println("Estudiante inscrito en la materia. ID de inscripcion: " + id);
        } else {
            System.out.println("No se pudo inscribir (verifique si ya esta inscrito en esa asignacion).");
        }
    }

    // 8. List student's enrolled subjects
    private void listStudentSubjectsMenu() {
        System.out.println("\n--- MATERIAS INSCRITAS DE UN ESTUDIANTE ---");
        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        int year = InputValidator.readIntRange(this.consola, "Año academico (ej. 2026): ", 2020, 2035);

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
