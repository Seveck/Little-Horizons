package org.example.view;

import org.example.controller.StudentController;
import org.example.model.DocumentType;
import org.example.model.Enrollment;
import org.example.model.EnrollmentStatus;
import org.example.model.Gender;
import org.example.model.Grade;
import org.example.model.Guardian;
import org.example.model.Student;
import org.example.model.StudentStatus;
import org.example.util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class StudentView {

    private final Scanner consola;
    private final StudentController controller;

    // Constructor
    public StudentView(Scanner consola) {
        this.consola = consola;
        this.controller = new StudentController();
    }

    // Main menu loop for Student & Guardian management
    public void showMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("     MODULO 1A: GESTION DE ESTUDIANTES Y MATRICULA");
            System.out.println("==================================================");
            System.out.println("1. Registrar nuevo estudiante (con acudiente)");
            System.out.println("2. Listar todos los estudiantes");
            System.out.println("3. Buscar estudiante por nombre o documento");
            System.out.println("4. Cambiar estado de un estudiante");
            System.out.println("5. Registrar nuevo acudiente");
            System.out.println("6. Asociar acudiente existente a estudiante (hermanos)");
            System.out.println("7. Consultar acudientes de estudiante / hermanos por acudiente");
            System.out.println("8. Matricular estudiante en grado y año academico");
            System.out.println("9. Consultar matriculas por grado y año");
            System.out.println("0. Volver al menu principal");

            int opcion = InputValidator.readMenuOption(this.consola, 0, 9);

            switch (opcion) {
                case 1:
                    this.registerStudentMenu();
                    break;
                case 2:
                    this.listAllStudents();
                    break;
                case 3:
                    this.searchStudent();
                    break;
                case 4:
                    this.changeStudentStatus();
                    break;
                case 5:
                    this.registerGuardianMenu();
                    break;
                case 6:
                    this.linkGuardianMenu();
                    break;
                case 7:
                    this.queryFamilyRelations();
                    break;
                case 8:
                    this.enrollStudentMenu();
                    break;
                case 9:
                    this.listEnrollmentsMenu();
                    break;
                case 0:
                    return;
            }
        }
    }

    // 1. Register student and link with guardian (new or existing)
    private void registerStudentMenu() {
        System.out.println("\n--- REGISTRO DE NUEVO ESTUDIANTE ---");
        DocumentType docType = this.askStudentDocType();
        String docNum = InputValidator.readDocument(this.consola, "Numero de documento: ");

        if (this.controller.findStudentByDocument(docNum) != null) {
            System.out.println("Error, ya existe un estudiante con ese documento.");
            return;
        }

        String firstName = InputValidator.readString(this.consola, "Nombres: ");
        String lastName = InputValidator.readString(this.consola, "Apellidos: ");
        LocalDate birthDate = InputValidator.readDate(this.consola, "Fecha de nacimiento (AAAA-MM-DD): ");
        Gender gender = this.askGender();
        String address = InputValidator.readString(this.consola, "Direccion: ");
        String phone = InputValidator.readOptionalString(this.consola, "Telefono (opcional): ");
        String email = InputValidator.readEmail(this.consola, "Correo institucional o personal: ");

        Student student = new Student(0, docType, docNum, firstName, lastName,
                birthDate, gender, address, phone, email, StudentStatus.ACTIVE);

        System.out.println("\n¿El acudiente ya esta registrado en el sistema?");
        System.out.println("1. Si, buscar acudiente existente (soporta hermanos)");
        System.out.println("2. No, registrar un nuevo acudiente ahora");
        int choice = InputValidator.readMenuOption(this.consola, 1, 2);

        int guardianId = -1;
        String relationship = "Padre/Madre";

        if (choice == 1) {
            String gDoc = InputValidator.readDocument(this.consola, "Documento del acudiente existente: ");
            Guardian g = this.controller.findGuardianByDocument(gDoc);
            if (g == null) {
                System.out.println("No se encontro acudiente con documento " + gDoc);
                return;
            }
            System.out.println("Acudiente encontrado: " + g.getFullName());
            guardianId = g.getId();
            relationship = InputValidator.readString(this.consola, "Relacion con el estudiante (ej. Padre, Madre, Tutor): ");
        } else {
            Guardian newGuardian = this.promptGuardianData();
            guardianId = this.controller.registerGuardian(newGuardian);
            if (guardianId <= 0) {
                System.out.println("Error al guardar el acudiente.");
                return;
            }
            relationship = newGuardian.getRelationship();
        }

        int newId = this.controller.registerStudentWithGuardian(student, guardianId, true, relationship);
        if (newId > 0) {
            System.out.println("Estudiante registrado y asociado con su acudiente. ID asignado: " + newId);
        } else {
            System.out.println("Error al registrar el estudiante. Revise error_log.txt si persiste.");
        }
    }

    // 2. List all students
    private void listAllStudents() {
        System.out.println("\n--- LISTA DE ESTUDIANTES ---");
        List<Student> list = this.controller.listAllStudents();
        if (list.isEmpty()) {
            System.out.println("No hay estudiantes registrados.");
            return;
        }
        for (Student s : list) {
            System.out.println(s.toString());
        }
        System.out.println("Total estudiantes: " + list.size());
    }

    // 3. Search student by name or document
    private void searchStudent() {
        System.out.println("\n--- BUSCAR ESTUDIANTE ---");
        System.out.println("1. Buscar por documento");
        System.out.println("2. Buscar por nombre o apellido");
        int op = InputValidator.readMenuOption(this.consola, 1, 2);

        if (op == 1) {
            String doc = InputValidator.readDocument(this.consola, "Ingrese numero de documento: ");
            Student s = this.controller.findStudentByDocument(doc);
            if (s != null) {
                System.out.println(s.toString());
            } else {
                System.out.println("No se encontro ningun estudiante con documento " + doc);
            }
        } else {
            String text = InputValidator.readString(this.consola, "Ingrese nombre o apellido a buscar: ");
            List<Student> results = this.controller.searchStudentsByName(text);
            if (results.isEmpty()) {
                System.out.println("No se encontraron coincidencias.");
            } else {
                for (Student s : results) {
                    System.out.println(s.toString());
                }
            }
        }
    }

    // 4. Update student status
    private void changeStudentStatus() {
        System.out.println("\n--- CAMBIAR ESTADO DE ESTUDIANTE ---");
        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        Student s = this.controller.findStudentById(studentId);
        if (s == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        System.out.println("Estudiante: " + s.getFullName() + " | Estado actual: " + s.getStatus().getDbValue());
        System.out.println("Seleccione nuevo estado:");
        System.out.println("1. Activo");
        System.out.println("2. Graduado");
        System.out.println("3. Retirado");
        System.out.println("4. Suspendido");
        int op = InputValidator.readMenuOption(this.consola, 1, 4);

        StudentStatus status = StudentStatus.ACTIVE;
        if (op == 2) status = StudentStatus.GRADUATED;
        if (op == 3) status = StudentStatus.WITHDRAWN;
        if (op == 4) status = StudentStatus.SUSPENDED;

        if (this.controller.updateStudentStatus(studentId, status)) {
            System.out.println("Estado actualizado correctamente a: " + status.getDbValue());
        } else {
            System.out.println("No se pudo actualizar el estado.");
        }
    }

    // 5. Register a new guardian
    private void registerGuardianMenu() {
        System.out.println("\n--- REGISTRO DE NUEVO ACUDIENTE ---");
        Guardian g = this.promptGuardianData();
        int id = this.controller.registerGuardian(g);
        if (id > 0) {
            System.out.println("Acudiente registrado exitosamente. ID asignado: " + id);
        } else {
            System.out.println("Error al registrar el acudiente (verifique que el documento no este repetido).");
        }
    }

    // 6. Link existing guardian to existing student
    private void linkGuardianMenu() {
        System.out.println("\n--- ASOCIAR ACUDIENTE A ESTUDIANTE ---");
        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        Student s = this.controller.findStudentById(studentId);
        if (s == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        int guardianId = InputValidator.readInt(this.consola, "ID del acudiente: ");
        Guardian g = this.controller.findGuardianById(guardianId);
        if (g == null) {
            System.out.println("Acudiente no encontrado.");
            return;
        }
        boolean primary = InputValidator.readConfirmation(this.consola, "¿Es el acudiente principal?");
        String rel = InputValidator.readString(this.consola, "Parentesco / Relacion (ej. Madre, Padre, Tio): ");

        if (this.controller.linkGuardianToStudent(studentId, guardianId, primary, rel)) {
            System.out.println("Asociacion guardada entre " + s.getFullName() + " y " + g.getFullName());
        } else {
            System.out.println("No se pudo asociar (es posible que ya exista esa relacion).");
        }
    }

    // 7. Query guardians of a student or siblings under one guardian
    private void queryFamilyRelations() {
        System.out.println("\n--- CONSULTA DE ACUDIENTES Y HERMANOS ---");
        System.out.println("1. Ver acudientes de un estudiante");
        System.out.println("2. Ver estudiantes (hermanos) a cargo de un acudiente");
        int op = InputValidator.readMenuOption(this.consola, 1, 2);

        if (op == 1) {
            int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
            List<Guardian> list = this.controller.getGuardiansByStudent(studentId);
            if (list.isEmpty()) {
                System.out.println("No hay acudientes asociados a ese estudiante.");
            } else {
                for (Guardian g : list) {
                    System.out.println(g.toString());
                }
            }
        } else {
            int guardianId = InputValidator.readInt(this.consola, "ID del acudiente: ");
            List<Student> list = this.controller.getStudentsByGuardian(guardianId);
            if (list.isEmpty()) {
                System.out.println("No hay estudiantes asociados a ese acudiente.");
            } else {
                System.out.println("Estudiantes a cargo:");
                for (Student s : list) {
                    System.out.println(s.toString());
                }
            }
        }
    }

    // 8. Enroll student in grade & academic year
    private void enrollStudentMenu() {
        System.out.println("\n--- MATRICULAR ESTUDIANTE ---");
        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        Student s = this.controller.findStudentById(studentId);
        if (s == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        System.out.println("Estudiante seleccionado: " + s.getFullName());

        System.out.println("\nGrados disponibles:");
        List<Grade> grades = this.controller.listAllGrades();
        for (Grade g : grades) {
            System.out.println(g.toString());
        }
        int gradeId = InputValidator.readInt(this.consola, "Seleccione ID del grado: ");
        int year = InputValidator.readIntRange(this.consola, "Año academico (ej. 2026): ", 2020, 2035);
        double cost = InputValidator.readPositiveDouble(this.consola, "Costo de matricula ($): ");

        Enrollment enrollment = new Enrollment(0, studentId, gradeId, year,
                LocalDate.now(), EnrollmentStatus.ENROLLED, cost);
        int id = this.controller.enrollStudent(enrollment);
        if (id > 0) {
            System.out.println("Matricula registrada con exito. ID: " + id);
        } else {
            System.out.println("No se pudo registrar la matricula (verifique que no este matriculado ya en ese año).");
        }
    }

    // 9. List enrollments by grade and year
    private void listEnrollmentsMenu() {
        System.out.println("\n--- CONSULTAR MATRICULAS POR GRADO Y AÑO ---");
        List<Grade> grades = this.controller.listAllGrades();
        for (Grade g : grades) {
            System.out.println(g.toString());
        }
        int gradeId = InputValidator.readInt(this.consola, "ID del grado: ");
        int year = InputValidator.readIntRange(this.consola, "Año academico: ", 2020, 2035);

        List<String> list = this.controller.listEnrollmentsByGradeAndYear(gradeId, year);
        if (list.isEmpty()) {
            System.out.println("No hay estudiantes matriculados en ese grado para el año " + year);
        } else {
            for (String line : list) {
                System.out.println(line);
            }
            System.out.println("Total matriculados: " + list.size());
        }
    }

    // Helper: ask guardian details
    private Guardian promptGuardianData() {
        System.out.println("Tipo de documento del acudiente (1. CC | 2. CE | 3. Pasaporte):");
        int dt = InputValidator.readMenuOption(this.consola, 1, 3);
        DocumentType docType = DocumentType.CC;
        if (dt == 2) docType = DocumentType.CE;
        if (dt == 3) docType = DocumentType.PASSPORT;

        String docNum = InputValidator.readDocument(this.consola, "Documento del acudiente: ");
        String firstName = InputValidator.readString(this.consola, "Nombres acudiente: ");
        String lastName = InputValidator.readString(this.consola, "Apellidos acudiente: ");
        String relationship = InputValidator.readString(this.consola, "Parentesco (Padre, Madre, Tutor): ");
        String phone = InputValidator.readString(this.consola, "Telefono: ");
        String email = InputValidator.readEmail(this.consola, "Correo electronico: ");
        String address = InputValidator.readString(this.consola, "Direccion: ");
        String occupation = InputValidator.readOptionalString(this.consola, "Profesion u ocupacion: ");

        return new Guardian(0, docType, docNum, firstName, lastName,
                relationship, phone, email, address, occupation);
    }

    private DocumentType askStudentDocType() {
        System.out.println("Tipo de documento (1. TI | 2. CC | 3. CE | 4. Pasaporte):");
        int op = InputValidator.readMenuOption(this.consola, 1, 4);
        if (op == 1) return DocumentType.TI;
        if (op == 2) return DocumentType.CC;
        if (op == 3) return DocumentType.CE;
        return DocumentType.PASSPORT;
    }

    private Gender askGender() {
        System.out.println("Genero (1. Masculino [M] | 2. Femenino [F] | 3. Otro):");
        int op = InputValidator.readMenuOption(this.consola, 1, 3);
        if (op == 1) return Gender.MALE;
        if (op == 2) return Gender.FEMALE;
        return Gender.OTHER;
    }
}
