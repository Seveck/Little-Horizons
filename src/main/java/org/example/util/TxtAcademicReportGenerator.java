package org.example.util;

import org.example.model.SubjectPerformance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TxtAcademicReportGenerator {

    private static final String FOLDER = "reports";
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LINE = "================================================================";

    // Build the formatted academic performance report for console and .txt file
    public static String buildReportContent(List<SubjectPerformance> ranking, List<SubjectPerformance> topSubjects) {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        String title = "REPORTE DE RENDIMIENTO ACADEMICO POR MATERIA - " + currentYear;

        StringBuilder sb = new StringBuilder();
        sb.append("+--------------------------------------------------------------+\n");
        sb.append(centerTitle(title));
        sb.append("+--------------------------------------------------------------+\n");
        sb.append("Fecha de generacion: ").append(now.format(DISPLAY_DATE)).append("\n\n");

        sb.append(LINE).append("\n");
        sb.append("PROMEDIO GENERAL POR MATERIA (Ordenado de Mayor a Menor)\n");
        sb.append(LINE).append("\n\n");

        for (SubjectPerformance item : ranking) {
            sb.append(String.format(Locale.US, "Codigo: %-10s | Materia: %-32s | Promedio: %.1f%n",
                    safe(item.getSubjectCode()),
                    safe(item.getSubjectName()),
                    item.getAverageGrade()));
        }

        sb.append("\n").append(LINE).append("\n");
        sb.append("  MATERIA(S) CON MEJOR RENDIMIENTO\n");
        sb.append(LINE).append("\n");

        if (topSubjects.size() == 1) {
            SubjectPerformance best = topSubjects.get(0);
            sb.append("MATERIA DESTACADA: ").append(safe(best.getSubjectName())).append("\n");
            sb.append("  Codigo: ").append(safe(best.getSubjectCode())).append("\n");
            sb.append(String.format(Locale.US, "  Promedio General: %.1f%n", best.getAverageGrade()));
            sb.append("  Esta materia presenta el mejor rendimiento academico del colegio.\n");
        } else if (topSubjects.size() > 1) {
            sb.append("\nMATERIAS DESTACADAS (Empate):\n\n");
            for (SubjectPerformance tied : topSubjects) {
                sb.append(String.format(Locale.US, "  - %-32s | Promedio: %.1f%n",
                        safe(tied.getSubjectName()), tied.getAverageGrade()));
            }
            sb.append("\n  Estas materias comparten el mejor rendimiento academico del colegio.\n");
        }

        sb.append("\n").append(LINE).append("\n");
        return sb.toString();
    }

    // Save the formatted report content into reports/reporte_rendimiento_materias_YYYYMMDD_HHMMSS.txt
    public static String saveReportFile(String reportContent) throws IOException {
        File folder = new File(FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP);
        String fileName = "reporte_rendimiento_materias_" + timestamp + ".txt";
        String fullPath = FOLDER + "/" + fileName;
        Path outPath = Path.of(FOLDER, fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8)) {
            writer.write(reportContent);
        }

        return fullPath;
    }

    private static String safe(String text) {
        return (text != null) ? text : "";
    }

    private static String centerTitle(String text) {
        int width = 62;
        if (text.length() >= width) {
            return "|" + text.substring(0, width) + "|\n";
        }
        int leftPad = (width - text.length()) / 2;
        int rightPad = width - text.length() - leftPad;
        return "|" + " ".repeat(leftPad) + text + " ".repeat(rightPad) + "|\n";
    }
}
