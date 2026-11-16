package org.example.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TxtInvoiceGenerator {

    private static final String FOLDER = "invoices";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Simple DTO class to pass invoice information
    public static class InvoiceDetails {
        public int invoiceId;
        public String invoiceNumber;
        public LocalDateTime issueDate;
        public String studentName;
        public String studentDocument;
        public String guardianName;
        public String guardianDocument;
        public String guardianPhone;
        public String guardianEmail;
        public String guardianAddress;
        public String conceptCode;
        public String conceptName;
        public double baseAmount;
        public double discount;
        public double netTotal;
        public String paymentMethod;
        public int academicYear;
        public String remarks;

        public InvoiceDetails() {
            this.issueDate = LocalDateTime.now();
        }
    }

    // Format Colombian pesos
    private static String formatMoney(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("es", "CO"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("$#,##0.00", symbols);
        return df.format(amount);
    }

    // Calculate simulated CUFE hash using SHA-256
    public static String calculateCufe(String invoiceNumber, String dateStr, double total, String guardianDoc) {
        String raw = invoiceNumber + "|" + dateStr + "|" + total + "|901584930-1|" + guardianDoc + "|LITTLE_HORIZONS";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) {
                    hex.append("0");
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            ErrorLogger.log(TxtInvoiceGenerator.class, "calculateCufe", e);
            return "CUFE-" + System.currentTimeMillis();
        }
    }

    // Generate the .txt invoice file inside invoices/ using UTF-8 BufferedWriter
    public static String generateInvoiceFile(InvoiceDetails data) throws IOException {
        File folder = new File(FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = String.format("factura_%06d.txt", data.invoiceId);
        String fullPath = FOLDER + "/" + fileName;
        Path outPath = Path.of(FOLDER, fileName);

        String dateStr = data.issueDate.format(DATE_FORMAT);
        String subtotalStr = formatMoney(data.baseAmount);
        String discountStr = "-" + formatMoney(data.discount);
        String ivaStr = formatMoney(0.0);
        String totalStr = formatMoney(data.netTotal);
        String cufe = calculateCufe(data.invoiceNumber, dateStr, data.netTotal, data.guardianDocument);

        String itemDesc = "[" + safe(data.conceptCode) + "] " + safe(data.conceptName);
        if (itemDesc.length() > 34) {
            itemDesc = itemDesc.substring(0, 31) + "...";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("+----------------------------------------------------------------------------------+\n");
        sb.append(centerLine("COLEGIO INTERNACIONAL LITTLE HORIZONS S.A.S."));
        sb.append(centerLine("NIT: 901.584.930-1 | Regimen Comun"));
        sb.append(centerLine("Calle 127 No. 15-40, Bogota D.C. | Tel: (601) 555-0192"));
        sb.append(centerLine("Correo: facturacion@littlehorizons.edu.co"));
        sb.append(centerLine("Resolucion DIAN No. 18764000123456 de 2026-01-01"));
        sb.append(centerLine("Rango Habilitado: FE-2026-000001 al FE-2026-999999"));
        sb.append("+----------------------------------------------------------------------------------+\n");
        sb.append(rowLine("FACTURA ELECTRONICA DE VENTA No: " + safe(data.invoiceNumber)));
        sb.append(rowLine("Fecha de Emision: " + dateStr + "    |    Año Academico: " + data.academicYear));
        sb.append(rowLine("Metodo de Pago:   " + safe(data.paymentMethod)));
        sb.append("+----------------------------------------------------------------------------------+\n");
        sb.append(rowLine("DATOS DEL ADQUIRIENTE (ACUDIENTE RESPONSABLE)"));
        sb.append(rowLine("Nombre:     " + safe(data.guardianName)));
        sb.append(rowLine("Documento:  " + safe(data.guardianDocument) + "    Tel: " + safe(data.guardianPhone)));
        sb.append(rowLine("Correo:     " + safe(data.guardianEmail)));
        sb.append(rowLine("Direccion:  " + safe(data.guardianAddress)));
        sb.append(rowLine("Estudiante: " + safe(data.studentName) + " (" + safe(data.studentDocument) + ")"));
        sb.append("+------------------------------------+------+------------------+-------------------+\n");
        sb.append(String.format("| %-34s | %-4s | %-16s | %-17s |%n",
                "DESCRIPCION", "CANT", "VALOR UNIT.", "SUBTOTAL"));
        sb.append("+------------------------------------+------+------------------+-------------------+\n");
        sb.append(String.format("| %-34s | %-4s | %16s | %17s |%n",
                itemDesc, "1", subtotalStr, subtotalStr));
        sb.append("+------------------------------------+------+------------------+-------------------+\n");
        if (data.remarks != null && !data.remarks.trim().isEmpty()) {
            sb.append(rowLine("Notas: " + data.remarks));
            sb.append("+----------------------------------------------------------------------------------+\n");
        }
        sb.append(rowLine(String.format("%-48s %32s", "Subtotal Base:", subtotalStr)));
        sb.append(rowLine(String.format("%-48s %32s", "Descuento / Beca:", discountStr)));
        sb.append(rowLine(String.format("%-48s %32s", "IVA (Exento 0%):", ivaStr)));
        sb.append(rowLine(String.format("%-48s %32s", "TOTAL PAGADO:", totalStr)));
        sb.append("+----------------------------------------------------------------------------------+\n");
        sb.append(rowLine("VALIDACION DIAN - CUFE (SHA-256)"));
        sb.append(rowLine(cufe));
        sb.append(rowLine("CODIGO QR SIMULADO:"));
        sb.append(rowLine("  [####  ##  ####]   FE=" + safe(data.invoiceNumber) + " | NIT=901584930-1"));
        sb.append(rowLine("  [#  # #### #  #]   Doc=" + safe(data.guardianDocument) + " | Total=" + totalStr));
        sb.append(rowLine("  [####  ##  ####]   Fecha=" + dateStr));
        sb.append("+----------------------------------------------------------------------------------+\n");
        sb.append(centerLine("Factura generada automaticamente por Little Horizons ERP"));
        sb.append("+----------------------------------------------------------------------------------+\n");

        try (BufferedWriter writer = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8)) {
            writer.write(sb.toString());
        }

        return fullPath;
    }

    private static String safe(String text) {
        return (text != null) ? text : "";
    }

    private static String centerLine(String text) {
        int width = 80;
        if (text.length() >= width) {
            return "| " + text.substring(0, width) + " |\n";
        }
        int leftPad = (width - text.length()) / 2;
        int rightPad = width - text.length() - leftPad;
        return "| " + " ".repeat(leftPad) + text + " ".repeat(rightPad) + " |\n";
    }

    private static String rowLine(String text) {
        int width = 80;
        if (text.length() > width) {
            text = text.substring(0, width - 3) + "...";
        }
        return String.format("| %-80s |%n", text);
    }
}
