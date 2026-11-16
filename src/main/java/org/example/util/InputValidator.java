package org.example.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputValidator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    // Read a text that cannot be empty
    public static String readString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Error, este campo no puede quedar vacio.");
        }
    }

    // Read optional text (can be empty)
    public static String readOptionalString(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // Read an integer inside a simple loop
    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Error, digito invalido. Ingrese un numero entero.");
            }
        }
    }

    // Read positive decimal (for payments and grades)
    public static double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replace(",", ".");
            try {
                double value = Double.parseDouble(input);
                if (value >= 0) {
                    return value;
                }
                System.out.println("Error, el valor no puede ser negativo.");
            } catch (NumberFormatException e) {
                System.out.println("Error, ingrese un monto valido.");
            }
        }
    }

    // Read a date in yyyy-MM-dd format
    public static LocalDate readDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Error, fecha invalida. Use el formato AAAA-MM-DD (ejemplo: 2026-03-15).");
            }
        }
    }

    // Read a time in HH:mm format
    public static LocalTime readTime(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalTime.parse(input, TIME_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Error, hora invalida. Use formato 24h HH:MM (ejemplo: 07:00 o 14:30).");
            }
        }
    }

    // Read a valid email
    public static String readEmail(Scanner scanner, String prompt) {
        while (true) {
            String email = readString(scanner, prompt).toLowerCase();
            if (email.contains("@") && email.contains(".") && !email.contains(" ")) {
                return email;
            }
            System.out.println("Error, correo invalido. Ejemplo: usuario@correo.com");
        }
    }

    // Read a valid document number
    public static String readDocument(Scanner scanner, String prompt) {
        while (true) {
            String doc = readString(scanner, prompt);
            if (doc.length() >= 5 && doc.length() <= 20 && !doc.contains(" ")) {
                return doc;
            }
            System.out.println("Error, el documento debe tener entre 5 y 20 caracteres sin espacios.");
        }
    }

    // Read Yes/No confirmation
    public static boolean readConfirmation(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (S/N): ");
            String answer = scanner.nextLine().trim().toUpperCase();
            if (answer.equals("S") || answer.equals("SI")) {
                return true;
            }
            if (answer.equals("N") || answer.equals("NO")) {
                return false;
            }
            System.out.println("Error, escriba S para Si o N para No.");
        }
    }
}
