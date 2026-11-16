package org.example;

import org.example.util.DatabaseConnection;
import org.example.util.ErrorLogger;
import org.example.view.AcademicView;
import org.example.view.ExtracurricularView;
import org.example.view.FinancialView;
import org.example.view.ReportView;
import org.example.view.ResourceView;
import org.example.view.StudentView;
import org.example.view.TeacherView;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=========================================================");
        System.out.println("   SISTEMA ERP - COLEGIO INTERNACIONAL LITTLE HORIZONS   ");
        System.out.println("=========================================================");

        if (!DatabaseConnection.getInstance().testConnection()) {
            System.out.println("No se pudo conectar a MySQL.");
            System.out.println("Verifique el archivo config.txt y que el servidor este encendido.");
            System.out.println("Revise error_log.txt para mas detalles.");
            scanner.close();
            return;
        }

        System.out.println("Conexion exitosa con la base de datos MySQL.\n");

        StudentView studentView = new StudentView(scanner);
        AcademicView academicView = new AcademicView(scanner);
        TeacherView teacherView = new TeacherView(scanner);
        ExtracurricularView extracurricularView = new ExtracurricularView(scanner);
        FinancialView financialView = new FinancialView(scanner);
        ResourceView resourceView = new ResourceView(scanner);
        ReportView reportView = new ReportView(scanner);

        try {
            while (true) {
                System.out.println("\n==================================================");
                System.out.println("                  MENU PRINCIPAL                  ");
                System.out.println("==================================================");
                System.out.println("1. Gestion de Estudiantes");
                System.out.println("2. Gestion Academica");
                System.out.println("3. Gestion de Profesores");
                System.out.println("4. Actividades Extracurriculares");
                System.out.println("5. Gestion Financiera");
                System.out.println("6. Gestion de Recursos");
                System.out.println("7. Reportes y Estadisticas");
                System.out.println("8. Salir");
                System.out.println("Digite su opcion:");

                try {
                    int option = scanner.nextInt();
                    scanner.nextLine();

                    switch (option) {
                        case 1:
                            studentView.showMenu();
                            break;
                        case 2:
                            academicView.showMenu();
                            break;
                        case 3:
                            teacherView.showMenu();
                            break;
                        case 4:
                            extracurricularView.showMenu();
                            break;
                        case 5:
                            financialView.showMenu();
                            break;
                        case 6:
                            resourceView.showMenu();
                            break;
                        case 7:
                            reportView.showMenu();
                            break;
                        case 8:
                            System.out.println("Saliendo del sistema Little Horizons ERP. Hasta luego!");
                            return;
                        default:
                            System.out.println("Error, opcion invalida");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Error, digito invalido");
                    scanner.nextLine();
                }
            }
        } catch (Exception e) {
            ErrorLogger.log(Main.class, "main", e);
            System.out.println("Ocurrio un error inesperado. Se registro el detalle en error_log.txt.");
        } finally {
            DatabaseConnection.getInstance().closeConnection();
            scanner.close();
        }
    }
}
