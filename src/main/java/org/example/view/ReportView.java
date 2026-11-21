package org.example.view;

import org.example.controller.ReportController;
import org.example.dao.LoanDao.LoanDetail;
import org.example.dao.PaymentDao.PaymentDetail;
import org.example.model.Club;
import org.example.model.SubjectPerformance;
import org.example.util.InputValidator;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ReportView {

    private final Scanner scanner;
    private final ReportController controller;

    // Constructor
    public ReportView(Scanner scanner) {
        this.scanner = scanner;
        this.controller = new ReportController();
    }

    // Main menu loop for institutional reports
    public void showMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("     MODULO 7: REPORTES Y ESTADISTICAS GLOBALES");
            System.out.println("==================================================");
            System.out.println("1. Resumen ejecutivo general de la institucion");
            System.out.println("2. Reporte academico (promedios y ausencias)");
            System.out.println("3. Reporte financiero (recaudo anual y morosos)");
            System.out.println("4. Reporte de recursos e inventario vencido");
            System.out.println("5. Reporte extracurricular (clubes y eventos)");
            System.out.println("6. Generar Reporte de Rendimiento Academico por Materia (.txt)");
            System.out.println("0. Volver al menu principal");
            System.out.println("Digite su opcion:");

            try {
                int option = this.scanner.nextInt();
                this.scanner.nextLine();

                switch (option) {
                    case 1:
                        this.showExecutiveSummaryMenu();
                        break;
                    case 2:
                        this.showAcademicReportMenu();
                        break;
                    case 3:
                        this.showFinancialReportMenu();
                        break;
                    case 4:
                        this.showResourceReportMenu();
                        break;
                    case 5:
                        this.showExtracurricularReportMenu();
                        break;
                    case 6:
                        this.showSubjectPerformanceReportMenu();
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

    // 1. General institutional summary
    private void showExecutiveSummaryMenu() {
        int year = InputValidator.readInt(this.scanner, "Año academico para resumen (ej. 2026): ");

        int totalStudents = this.controller.countTotalStudents();
        long activeStudents = this.controller.countActiveStudents();
        int activeTeachers = this.controller.countActiveTeachers();
        int totalSubjects = this.controller.countTotalSubjects();
        int activeClubs = this.controller.getActiveClubs().size();
        int totalEvents = this.controller.countTotalEvents();
        double collected = this.controller.getTotalCollectedByYear(year);
        int delinquentCount = this.controller.getDelinquentPayments(30).size();
        int overdueLoansCount = this.controller.getOverdueLoans().size();

        System.out.println("\n==================================================");
        System.out.println("   RESUMEN EJECUTIVO - LITTLE HORIZONS (" + year + ")");
        System.out.println("==================================================");
        System.out.println("Estudiantes registrados:    " + totalStudents + " (Activos: " + activeStudents + ")");
        System.out.println("Profesores activos:         " + activeTeachers);
        System.out.println("Materias registradas:       " + totalSubjects);
        System.out.println("Clubes activos:             " + activeClubs);
        System.out.println("Eventos registrados:        " + totalEvents);
        System.out.println("Recaudo anual (" + year + "):       $" + String.format("%,.2f", collected));
        System.out.println("Pagos en mora (>30 dias):   " + delinquentCount);
        System.out.println("Prestamos vencidos:         " + overdueLoansCount);
        System.out.println("==================================================");
    }

    // 2. Academic averages & absence alerts
    private void showAcademicReportMenu() {
        int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");
        System.out.println("\n--- PROMEDIOS POR MATERIA (" + year + ") ---");
        Map<String, Double> averages = this.controller.getCourseAveragesByYear(year);
        if (averages.isEmpty()) {
            System.out.println("No hay calificaciones registradas en " + year);
        } else {
            averages.forEach((subject, avg) ->
                    System.out.printf("  - %-40s : %.2f%n", subject, avg));
        }

        System.out.println("\n--- ALERTAS DE INASISTENCIA (>3 AUSENCIAS EN UNA SEMANA) ---");
        List<String> alerts = this.controller.getAbsenceAlerts(3);
        if (alerts.isEmpty()) {
            System.out.println("Sin alertas de inasistencia semanal.");
        } else {
            for (String alert : alerts) {
                System.out.println(alert);
            }
        }
    }

    // 3. Financial revenue & delinquent report
    private void showFinancialReportMenu() {
        int year = InputValidator.readInt(this.scanner, "Año academico (ej. 2026): ");
        double collected = this.controller.getTotalCollectedByYear(year);
        System.out.println("\n--- REPORTE FINANCIERO (" + year + ") ---");
        System.out.println("Total recaudado: $" + String.format("%,.2f", collected));

        List<PaymentDetail> delinquentList = this.controller.getDelinquentPayments(30);
        System.out.println("Pagos pendientes con mas de 30 dias (" + delinquentList.size() + "):");
        if (delinquentList.isEmpty()) {
            System.out.println("No hay morosos registrados.");
        } else {
            for (PaymentDetail pd : delinquentList) {
                System.out.println("  - " + pd.toString());
            }
        }
    }

    // 4. Resources status summary & overdue loans
    private void showResourceReportMenu() {
        System.out.println("\n--- INVENTARIO DE RECURSOS POR ESTADO ---");
        Map<String, Long> inventory = this.controller.getInventorySummaryByStatus();
        inventory.forEach((status, count) ->
                System.out.println("  - " + status + ": " + count + " recurso(s)"));

        System.out.println("\n--- PRESTAMOS VENCIDOS ---");
        List<LoanDetail> overdue = this.controller.getOverdueLoans();
        if (overdue.isEmpty()) {
            System.out.println("No hay prestamos vencidos actualmente.");
        } else {
            for (LoanDetail ld : overdue) {
                System.out.println("  - " + ld.toString());
            }
        }
    }

    // 5. Extracurricular clubs & events report
    private void showExtracurricularReportMenu() {
        System.out.println("\n--- OCUPACION DE CLUBES ACTIVOS ---");
        List<Club> clubs = this.controller.getActiveClubs();
        for (Club c : clubs) {
            int used = this.controller.getClubMembersCount(c.getId());
            int free = this.controller.getAvailableSpots(c.getId());
            System.out.println("  - " + c.getName() + " | Inscritos: " + used + " | Cupos libres: " + free);
        }

        System.out.println("\n--- EVENTOS POR TIPO ---");
        Map<String, Long> byType = this.controller.getEventsSummaryByType();
        byType.forEach((type, count) ->
                System.out.println("  - " + type + ": " + count + " evento(s)"));
    }

    // 6. Academic performance report by subject (console + .txt)
    private void showSubjectPerformanceReportMenu() {
        List<SubjectPerformance> ranking = this.controller.getSubjectPerformanceRanking();

        if (ranking.isEmpty()) {
            System.out.println("\n================================================================");
            System.out.println("No hay registros academicos suficientes para generar estadisticas.");
            System.out.println("Por favor, asegurese de que existan notas registradas en el sistema.");
            System.out.println("================================================================");
            System.out.println("Presione Enter para volver al menu...");
            this.scanner.nextLine();
            return;
        }

        List<SubjectPerformance> topSubjects = this.controller.findTopPerformingSubjects(ranking);
        String reportContent = this.controller.buildSubjectPerformanceReport(ranking, topSubjects);
        String savedFilePath = this.controller.saveSubjectPerformanceReportTxt(reportContent);

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
}
