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
    public static String readString(Scanner consola, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = consola.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Error, este campo no puede quedar vacio.");
        }
    }

    // Read optional text (can be empty)
    public static String readOptionalString(Scanner consola, String mensaje) {
        System.out.print(mensaje);
        return consola.nextLine().trim();
    }

    // Read an integer number
    public static int readInt(Scanner consola, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = consola.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Error, digito invalido. Ingrese un numero entero.");
            }
        }
    }

    // Read an integer inside a range [min, max]
    public static int readIntRange(Scanner consola, String mensaje, int min, int max) {
        while (true) {
            int numero = readInt(consola, mensaje);
            if (numero >= min && numero <= max) {
                return numero;
            }
            System.out.println("Error, opcion invalida. Debe estar entre " + min + " y " + max + ".");
        }
    }

    // Read menu option
    public static int readMenuOption(Scanner consola, int min, int max) {
        return readIntRange(consola, "Digite su opcion [" + min + "-" + max + "]: ", min, max);
    }

    // Read decimal inside a range (for grades 0.0 to 5.0)
    public static double readDoubleRange(Scanner consola, String mensaje, double min, double max) {
        while (true) {
            System.out.print(mensaje);
            String texto = consola.nextLine().trim().replace(",", ".");
            try {
                double valor = Double.parseDouble(texto);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("Error, el valor debe estar entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Error, ingrese un numero decimal valido (ejemplo: 4.5).");
            }
        }
    }

    // Read positive decimal (for payments and prices)
    public static double readPositiveDouble(Scanner consola, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = consola.nextLine().trim().replace(",", ".");
            try {
                double valor = Double.parseDouble(texto);
                if (valor >= 0) {
                    return valor;
                }
                System.out.println("Error, el valor no puede ser negativo.");
            } catch (NumberFormatException e) {
                System.out.println("Error, ingrese un monto valido.");
            }
        }
    }

    // Read a date in yyyy-MM-dd format
    public static LocalDate readDate(Scanner consola, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = consola.nextLine().trim();
            try {
                return LocalDate.parse(texto, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Error, fecha invalida. Use el formato AAAA-MM-DD (ejemplo: 2026-03-15).");
            }
        }
    }

    // Read a time in HH:mm format
    public static LocalTime readTime(Scanner consola, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = consola.nextLine().trim();
            try {
                return LocalTime.parse(texto, TIME_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Error, hora invalida. Use formato 24h HH:MM (ejemplo: 07:00 o 14:30).");
            }
        }
    }

    // Read a valid email
    public static String readEmail(Scanner consola, String mensaje) {
        while (true) {
            String correo = readString(consola, mensaje).toLowerCase();
            if (correo.contains("@") && correo.contains(".") && !correo.contains(" ")) {
                return correo;
            }
            System.out.println("Error, correo invalido. Ejemplo: usuario@correo.com");
        }
    }

    // Read a valid document number
    public static String readDocument(Scanner consola, String mensaje) {
        while (true) {
            String doc = readString(consola, mensaje);
            if (doc.length() >= 5 && doc.length() <= 20 && !doc.contains(" ")) {
                return doc;
            }
            System.out.println("Error, el documento debe tener entre 5 y 20 caracteres sin espacios.");
        }
    }

    // Read Yes/No confirmation
    public static boolean readConfirmation(Scanner consola, String mensaje) {
        while (true) {
            System.out.print(mensaje + " (S/N): ");
            String respuesta = consola.nextLine().trim().toUpperCase();
            if (respuesta.equals("S") || respuesta.equals("SI")) {
                return true;
            }
            if (respuesta.equals("N") || respuesta.equals("NO")) {
                return false;
            }
            System.out.println("Error, escriba S para Si o N para No.");
        }
    }
}
