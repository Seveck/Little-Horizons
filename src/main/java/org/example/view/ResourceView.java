package org.example.view;

import org.example.controller.ResourceController;
import org.example.controller.StudentController;
import org.example.controller.TeacherController;
import org.example.dao.LoanDao.LoanDetail;
import org.example.model.LoanStatus;
import org.example.model.Resource;
import org.example.model.ResourceCategory;
import org.example.model.ResourceStatus;
import org.example.model.Student;
import org.example.model.StudentLoan;
import org.example.model.Teacher;
import org.example.model.TeacherLoan;
import org.example.util.InputValidator;

import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ResourceView {

    private final Scanner scanner;
    private final ResourceController controller;
    private final StudentController studentController;
    private final TeacherController teacherController;

    // Constructor
    public ResourceView(Scanner scanner) {
        this.scanner = scanner;
        this.controller = new ResourceController();
        this.studentController = new StudentController();
        this.teacherController = new TeacherController();
    }

    // Main menu loop for Library & Lab Resources
    public void showMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("     MODULO 4: GESTION DE RECURSOS Y PRESTAMOS");
            System.out.println("==================================================");
            System.out.println("1. Listar inventario de recursos (todos / disponibles)");
            System.out.println("2. Registrar nuevo recurso en inventario");
            System.out.println("3. Cambiar estado de recurso (Mantenimiento / Baja / Disponible)");
            System.out.println("4. Registrar prestamo a ESTUDIANTE");
            System.out.println("5. Registrar prestamo a PROFESOR");
            System.out.println("6. Registrar devolucion de prestamo");
            System.out.println("7. Consultar prestamos activos y por persona");
            System.out.println("8. Alertas de prestamos vencidos (retrasos)");
            System.out.println("9. Resumen del inventario por estado");
            System.out.println("0. Volver al menu principal");
            System.out.println("Digite su opcion:");

            try {
                int option = this.scanner.nextInt();
                this.scanner.nextLine();

                switch (option) {
                    case 1:
                        this.listResourcesMenu();
                        break;
                    case 2:
                        this.registerResourceMenu();
                        break;
                    case 3:
                        this.changeResourceStatusMenu();
                        break;
                    case 4:
                        this.loanToStudentMenu();
                        break;
                    case 5:
                        this.loanToTeacherMenu();
                        break;
                    case 6:
                        this.returnLoanMenu();
                        break;
                    case 7:
                        this.queryLoansMenu();
                        break;
                    case 8:
                        this.overdueAlertsMenu();
                        break;
                    case 9:
                        this.inventorySummaryMenu();
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

    // 1. List resources
    private void listResourcesMenu() {
        System.out.println("\n--- INVENTARIO DE RECURSOS ---");
        System.out.println("1. Ver todos los recursos");
        System.out.println("2. Ver solo recursos disponibles");
        System.out.println("3. Filtrar por categoria");
        int op = InputValidator.readInt(this.scanner, "Digite su opcion: ");

        List<Resource> list;
        if (op == 1) {
            list = this.controller.listAllResources();
        } else if (op == 2) {
            list = this.controller.listAvailableResources();
        } else {
            List<ResourceCategory> categories = this.controller.listAllCategories();
            for (ResourceCategory rc : categories) {
                System.out.println(rc.toString());
            }
            int catId = InputValidator.readInt(this.scanner, "ID de la categoria: ");
            list = this.controller.listResourcesByCategory(catId);
        }

        if (list.isEmpty()) {
            System.out.println("No hay recursos para mostrar.");
            return;
        }
        for (Resource r : list) {
            System.out.println(r.toString());
        }
        System.out.println("Total recursos mostrados: " + list.size());
    }

    // 2. Register new resource
    private void registerResourceMenu() {
        System.out.println("\n--- REGISTRAR NUEVO RECURSO ---");
        String code = InputValidator.readString(this.scanner, "Codigo unico (ej. LIB-099): ").toUpperCase();
        if (this.controller.findResourceByCode(code) != null) {
            System.out.println("Error, ya existe un recurso con ese codigo.");
            return;
        }

        String name = InputValidator.readString(this.scanner, "Nombre del recurso: ");
        List<ResourceCategory> categories = this.controller.listAllCategories();
        for (ResourceCategory rc : categories) {
            System.out.println(rc.toString());
        }
        int categoryId = InputValidator.readInt(this.scanner, "ID de la categoria: ");
        if (this.controller.findCategoryById(categoryId) == null) {
            System.out.println("Categoria no encontrada.");
            return;
        }

        String description = InputValidator.readOptionalString(this.scanner, "Descripcion (opcional): ");
        String location = InputValidator.readString(this.scanner, "Ubicacion fisica (ej. Biblioteca Estante B2): ");
        LocalDate acqDate = InputValidator.readDate(this.scanner, "Fecha de adquisicion (AAAA-MM-DD): ");

        Resource res = new Resource();
        res.setCode(code);
        res.setName(name);
        res.setCategoryId(categoryId);
        res.setDescription(description.isEmpty() ? null : description);
        res.setPhysicalLocation(location);
        res.setStatus(ResourceStatus.AVAILABLE);
        res.setAcquisitionDate(acqDate);
        int id = this.controller.registerResource(res);
        if (id > 0) {
            System.out.println("Recurso registrado exitosamente. ID: " + id);
        } else {
            System.out.println("Error al registrar el recurso.");
        }
    }

    // 3. Change resource status
    private void changeResourceStatusMenu() {
        System.out.println("\n--- CAMBIAR ESTADO DE RECURSO ---");
        int resId = InputValidator.readInt(this.scanner, "ID del recurso: ");
        Resource res = this.controller.findResourceById(resId);
        if (res == null) {
            System.out.println("Recurso no encontrado.");
            return;
        }
        System.out.println("Recurso actual: " + res.toString());
        if (res.getStatus() == ResourceStatus.LOANED) {
            System.out.println("Este recurso esta prestado actualmente. Registre primero su devolucion.");
            return;
        }

        System.out.println("Nuevo estado (1. Disponible | 2. En Mantenimiento | 3. Dado de Baja):");
        int op = InputValidator.readInt(this.scanner, "Digite su opcion: ");
        ResourceStatus newStatus = ResourceStatus.AVAILABLE;
        if (op == 2) newStatus = ResourceStatus.MAINTENANCE;
        if (op == 3) newStatus = ResourceStatus.RETIRED;

        if (this.controller.updateResourceStatus(resId, newStatus)) {
            System.out.println("Estado del recurso actualizado correctamente.");
        } else {
            System.out.println("No se pudo actualizar el estado.");
        }
    }

    // 4. Loan resource to a student
    private void loanToStudentMenu() {
        System.out.println("\n--- PRESTAMO DE RECURSO A ESTUDIANTE ---");
        int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        System.out.println("Estudiante: " + student.getFullName());

        int resId = InputValidator.readInt(this.scanner, "ID del recurso a prestar: ");
        LocalDate loanDate = LocalDate.now();
        LocalDate dueDate = InputValidator.readDate(this.scanner, "Fecha programada de devolucion (AAAA-MM-DD): ");
        String remarks = InputValidator.readOptionalString(this.scanner, "Observaciones del prestamo: ");

        StudentLoan loan = new StudentLoan(0, resId, studentId, loanDate, dueDate, null, LoanStatus.ACTIVE, remarks);
        int result = this.controller.loanToStudent(loan);
        this.printLoanCreationResult(result);
    }

    // 5. Loan resource to a teacher
    private void loanToTeacherMenu() {
        System.out.println("\n--- PRESTAMO DE RECURSO A PROFESOR ---");
        int teacherId = InputValidator.readInt(this.scanner, "ID del profesor: ");
        Teacher teacher = this.teacherController.findTeacherById(teacherId);
        if (teacher == null) {
            System.out.println("Profesor no encontrado.");
            return;
        }
        System.out.println("Profesor: " + teacher.getFullName());

        int resId = InputValidator.readInt(this.scanner, "ID del recurso a prestar: ");
        LocalDate loanDate = LocalDate.now();
        LocalDate dueDate = InputValidator.readDate(this.scanner, "Fecha programada de devolucion (AAAA-MM-DD): ");
        String remarks = InputValidator.readOptionalString(this.scanner, "Observaciones del prestamo: ");

        TeacherLoan loan = new TeacherLoan(0, resId, teacherId, loanDate, dueDate, null, LoanStatus.ACTIVE, remarks);
        int result = this.controller.loanToTeacher(loan);
        this.printLoanCreationResult(result);
    }

    private void printLoanCreationResult(int result) {
        if (result > 0) {
            System.out.println("Prestamo registrado con exito. Prestamo ID: " + result);
        } else if (result == -2) {
            System.out.println("Recurso no encontrado.");
        } else if (result == -3) {
            System.out.println("El recurso no esta disponible actualmente para prestamo.");
        } else {
            System.out.println("Error al registrar el prestamo.");
        }
    }

    // 6. Register return of a loan
    private void returnLoanMenu() {
        System.out.println("\n--- REGISTRAR DEVOLUCION DE RECURSO ---");
        System.out.println("Tipo de prestamo (1. Estudiante | 2. Profesor):");
        int type = InputValidator.readInt(this.scanner, "Digite su opcion: ");
        int loanId = InputValidator.readInt(this.scanner, "ID del prestamo: ");
        LocalDate returnDate = InputValidator.readDate(this.scanner, "Fecha real de devolucion (AAAA-MM-DD): ");

        boolean ok = (type == 1)
                ? this.controller.returnStudentLoan(loanId, returnDate)
                : this.controller.returnTeacherLoan(loanId, returnDate);

        if (ok) {
            System.out.println("Devolucion registrada. El recurso vuelve a estar Disponible.");
        } else {
            System.out.println("No se pudo registrar la devolucion (verifique el ID o si ya fue devuelto).");
        }
    }

    // 7. Query active loans or history by borrower
    private void queryLoansMenu() {
        System.out.println("\n--- CONSULTA DE PRESTAMOS ---");
        System.out.println("1. Ver todos los prestamos pendientes de devolucion");
        System.out.println("2. Consultar prestamos de un estudiante");
        System.out.println("3. Consultar prestamos de un profesor");
        int op = InputValidator.readInt(this.scanner, "Digite su opcion: ");

        List<LoanDetail> list;
        if (op == 1) {
            list = this.controller.listPendingReturnLoans();
        } else if (op == 2) {
            int studentId = InputValidator.readInt(this.scanner, "ID del estudiante: ");
            list = this.controller.getStudentLoanHistory(studentId);
        } else {
            int teacherId = InputValidator.readInt(this.scanner, "ID del profesor: ");
            list = this.controller.getTeacherLoanHistory(teacherId);
        }

        if (list.isEmpty()) {
            System.out.println("No se encontraron prestamos con ese criterio.");
            return;
        }
        for (LoanDetail ld : list) {
            System.out.println(ld.toString());
        }
    }

    // 8. Overdue alerts (Streams)
    private void overdueAlertsMenu() {
        System.out.println("\n--- ALERTAS DE PRESTAMOS VENCIDOS ---");
        List<LoanDetail> list = this.controller.getOverdueLoanAlerts();
        if (list.isEmpty()) {
            System.out.println("Excelente: no hay prestamos vencidos a la fecha.");
            return;
        }
        for (LoanDetail ld : list) {
            System.out.println("VENCIDO -> " + ld.toString());
        }
        System.out.println("Total prestamos vencidos: " + list.size());
    }

    // 9. Inventory status summary (Streams)
    private void inventorySummaryMenu() {
        System.out.println("\n--- RESUMEN DEL INVENTARIO POR ESTADO ---");
        Map<String, Long> summary = this.controller.countResourcesByStatus();
        summary.forEach((statusName, count) ->
                System.out.println("  - " + statusName + ": " + count + " recurso(s)"));
    }
}
