package org.example.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "tu_usuario";
    private static final String DEFAULT_PASSWORD = "";

    private String url;
    private String user;
    private String password;

    // Private constructor for Singleton
    private DatabaseConnection() {
        this.url = DEFAULT_URL;
        this.user = DEFAULT_USER;
        this.password = DEFAULT_PASSWORD;
        this.loadConfig();
    }

    // Load credentials from config.txt
    private void loadConfig() {
        File file = new File("config.txt");
        if (!file.exists()) {
            file = new File("IdeaProjects/Little-Horizons/config.txt");
        }

        if (!file.exists()) {
            System.out.println("Aviso: No se encontro el archivo config.txt.");
            System.out.println("Copie config.example.txt como config.txt y configure sus credenciales de MySQL.");
            return;
        }

        Properties props = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            props.load(reader);
            String fileUrl = props.getProperty("db.url");
            if (fileUrl != null && fileUrl.contains("erp_little_horizons")) {
                this.url = fileUrl;
                this.user = props.getProperty("db.user", DEFAULT_USER);
                this.password = props.getProperty("db.password", DEFAULT_PASSWORD);
            }
        } catch (IOException e) {
            ErrorLogger.log(DatabaseConnection.class, "loadConfig", e);
        }
    }

    // Get Singleton instance
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // Get active JDBC connection
    public synchronized Connection getConnection() throws SQLException {
        try {
            if (this.connection == null || this.connection.isClosed() || !this.connection.isValid(2)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                this.connection = DriverManager.getConnection(this.url, this.user, this.password);
            }
        } catch (ClassNotFoundException e) {
            ErrorLogger.log(DatabaseConnection.class, "getConnection", e);
            throw new SQLException("No se encontro el driver de MySQL", e);
        } catch (SQLException e) {
            ErrorLogger.log(DatabaseConnection.class, "getConnection", e);
            throw e;
        }
        return this.connection;
    }

    // Test if database connection works
    public boolean testConnection() {
        try (Connection conn = this.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            ErrorLogger.log(DatabaseConnection.class, "testConnection", e);
            return false;
        }
    }

    // Close open connection
    public synchronized void closeConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException e) {
            ErrorLogger.log(DatabaseConnection.class, "closeConnection", e);
        } finally {
            this.connection = null;
        }
    }
}
