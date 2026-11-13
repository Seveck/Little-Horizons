package org.example.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorLogger {

    private static final String LOG_FILE = "error_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Log an exception using Class reference
    public static synchronized void log(Class<?> sourceClass, String methodName, Throwable error) {
        String className = "UnknownClass";
        if (sourceClass != null) {
            className = sourceClass.getSimpleName();
        }
        log(className, methodName, error);
    }

    // Log an exception with class name and method name
    public static synchronized void log(String className, String methodName, Throwable error) {
        String date = LocalDateTime.now().format(FORMATTER);
        String errorType = "UnknownError";
        String message = "No message";
        String trace = "";

        if (error != null) {
            errorType = error.getClass().getName();
            if (error.getMessage() != null) {
                message = error.getMessage();
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            error.printStackTrace(pw);
            trace = sw.toString();
        }

        String text = "========================================================================\n"
                + "[" + date + "] ERROR en " + className + "." + methodName + "()\n"
                + "Tipo: " + errorType + "\n"
                + "Mensaje: " + message + "\n"
                + "Detalle:\n" + trace
                + "========================================================================\n\n";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, StandardCharsets.UTF_8, true))) {
            writer.write(text);
        } catch (IOException e) {
            System.out.println("Error al guardar en error_log.txt: " + e.getMessage());
        }
    }
}
