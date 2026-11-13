package org.example.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    // Generate the .txt invoice file inside invoices/
    public static String generateInvoiceFile(InvoiceDetails data) throws IOException {
        File folder = new File(FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = String.format("factura_%06d.txt", data.invoiceId);
        String fullPath = FOLDER + "/" + fileName;
        File file = new File(folder, fileName);

        String dateStr = data.issueDate.format(DATE_FORMAT);
        String subtotalStr = formatMoney(data.baseAmount);
        String discountStr = "-" + formatMoney(data.discount);
        String ivaStr = formatMoney(0.0);
        String totalStr = formatMoney(data.netTotal);
        String cufe = calculateCufe(data.invoiceNumber, dateStr, data.netTotal, data.guardianDocument);

        StringBuilder sb = new StringBuilder();
        sb.append("┌────────────────────────────────────────────────────────────────────────────────┐\n");
        sb.append(centerLine("COLEGIO INTERNACIONAL LITTLE HORIZONS S.A.S."));
        sb.append(centerLine("NIT: 901.584.930-1"));
        sb.append(centerLine("Resolucion DIAN No. 18764000123456 de 2026-01-01"));
        sb.append(centerLine("Rango Habilitado: FE-2026-000001 al FE-2026-999999"));
        sb.append(centerLine("Direccion: Av. Libertadores # 10-05, Cucuta, Colombia"));
        sb.append(centerLine("Telefono: +57 (607) 589-1000 | facturacion@littlehorizons.edu.co"));
        sb.append("├────────────────────────────────────────────────────────────────────────────────┤\n");
        sb.append(boxLine("FACTURA ELECTRONICA DE VENTA: " + data.invoiceNumber));
        sb.append(boxLine("Fecha y Hora de Emision:      " + dateStr));
        sb.append(boxLine("Metodo de Pago:               " + data.paymentMethod));
        sb.append(boxLine("Año Academico:                " + data.academicYear));
        sb.append("├────────────────────────────────────────────────────────────────────────────────┤\n");
        sb.append(boxLine("DATOS DEL CLIENTE / ACUDIENTE:"));
        sb.append(boxLine("Acudiente:  " + data.guardianName));
        sb.append(boxLine("Documento:  " + data.guardianDocument));
        sb.append(boxLine("Telefono:   " + data.guardianPhone));
        sb.append(boxLine("Correo:     " + data.guardianEmail));
        sb.append(boxLine("Direccion:  " + data.guardianAddress));
        sb.append("├────────────────────────────────────────────────────────────────────────────────┤\n");
        sb.append(boxLine("ESTUDIANTE BENEFICIARIO:"));
        sb.append(boxLine("Nombre:     " + data.studentName));
        sb.append(boxLine("Documento:  " + data.studentDocument));
        sb.append("├────────────────────────────────────────────────────────────────────────────────┤\n");
        sb.append(boxLine("DETALLE DE CONCEPTOS:"));
        sb.append(boxLine(String.format("%-11s %-31s %4s %14s %14s", "Codigo", "Concepto", "Cant", "V. Unitario", "Total")));
        sb.append(boxLine("──────────────────────────────────────────────────────────────────────────────"));

        String code = data.conceptCode;
        if (code.length() > 11) {
            code = code.substring(0, 11);
        }
        String concept = data.conceptName;
        if (concept.length() > 31) {
            concept = concept.substring(0, 28) + "...";
        }
        sb.append(boxLine(String.format("%-11s %-31s %4d %14s %14s", code, concept, 1, subtotalStr, subtotalStr)));

        if (data.remarks != null && !data.remarks.trim().isEmpty()) {
            sb.append(boxLine("  Obs: " + data.remarks.trim()));
        }

        sb.append("├────────────────────────────────────────────────────────────────────────────────┤\n");
        sb.append(twoColLine("SUBTOTAL:", subtotalStr));
        sb.append(twoColLine("DESCUENTO:", discountStr));
        sb.append(twoColLine("IVA (0% - Servicio Educativo Exento):", ivaStr));
        sb.append(boxLine("──────────────────────────────────────────────────────────────────────────────"));
        sb.append(twoColLine("TOTAL PAGADO:", totalStr));
        sb.append("├────────────────────────────────────────────────────────────────────────────────┤\n");
        sb.append(boxLine("CUFE (Codigo Unico de Factura Electronica):"));
        sb.append(boxLine(cufe));
        sb.append("├────────────────────────────────────────────────────────────────────────────────┤\n");
        sb.append(boxLine("CODIGO QR DE VALIDACION DIAN (SIMULADO):"));
        sb.append(centerLine("██████████████  ██  ██████████████"));
        sb.append(centerLine("██          ██  ██  ██          ██"));
        sb.append(centerLine("██  ██████  ██      ██  ██████  ██"));
        sb.append(centerLine("██  ██████  ██  ██  ██  ██████  ██"));
        sb.append(centerLine("██          ██  ██  ██          ██"));
        sb.append(centerLine("██████████████  ██  ██████████████"));
        sb.append(centerLine("                ████              "));
        sb.append(centerLine("██████  ██████  ██  ██████  ██████"));
        sb.append(centerLine("██  ██  ██  ██  ██████  ██  ██  ██"));
        sb.append(centerLine("██████████████  ██  ██████████████"));
        sb.append(centerLine("Consulte este documento en: https://catalogo-vpfe.dian.gov.co"));
        sb.append("└────────────────────────────────────────────────────────────────────────────────┘\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            ErrorLogger.log(TxtInvoiceGenerator.class, "generateInvoiceFile", e);
            throw e;
        }

        return fullPath;
    }

    // Pad text to 78 characters inside box borders
    private static String boxLine(String text) {
        String safe = "";
        if (text != null) {
            safe = text;
        }
        if (safe.length() > 78) {
            safe = safe.substring(0, 75) + "...";
        }
        return String.format("│ %-78s │\n", safe);
    }

    // Center text inside 78-character box width
    private static String centerLine(String text) {
        String safe = "";
        if (text != null) {
            safe = text;
        }
        if (safe.length() >= 78) {
            return boxLine(safe);
        }
        int left = (78 - safe.length()) / 2;
        int right = 78 - safe.length() - left;
        return "│ " + " ".repeat(left) + safe + " ".repeat(right) + " │\n";
    }

    // Align left and right columns inside box
    private static String twoColLine(String left, String right) {
        int spaces = 78 - left.length() - right.length();
        if (spaces < 1) {
            return boxLine(left + " " + right);
        }
        return "│ " + left + " ".repeat(spaces) + right + " │\n";
    }
}
