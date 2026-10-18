package com.example.productoscrud.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Inicializador automático de la base de datos MySQL.
 * Se ejecuta al arrancar el proyecto y crea la base de datos y la tabla
 * si no existen, para que no sea necesario ejecutar el script SQL manualmente.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Value("${DB_USER:root}")
    private String dbUser;

    // @Value("${DB_PASSWORD:123}")
    // private String dbPassword;

    @Value("${DB_PASSWORD:}")
    private String dbPassword;

    @Value("${DB_HOST:localhost}")
    private String dbHost;

    @Value("${DB_PORT:3306}")
    private String dbPort;

    @Value("${DB_NAME:productoscruddb}")
    private String dbName;

    private String urlSinDb() {
        return "jdbc:mysql://" + dbHost + ":" + dbPort + "/";
    }

    private String urlConDb() {
        return "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Crear la base de datos si no existe
        try (Connection conn = DriverManager.getConnection(urlSinDb(), dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + dbName
                    + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        // 2. Crear la tabla y datos de ejemplo si no existen
        try (Connection conn = DriverManager.getConnection(urlConDb(), dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INT NOT NULL AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    price DECIMAL(12,2) NOT NULL,
                    description VARCHAR(255) NOT NULL DEFAULT '',
                    PRIMARY KEY (id),
                    CONSTRAINT precio_no_negativo CHECK (price >= 0)
                ) ENGINE=InnoDB
                """);

            // Insertar ejemplos solo si la tabla está vacía
            stmt.execute("""
                INSERT INTO products (name, price, description)
                SELECT 'Portátil', 2500000.00, 'Equipo de ejemplo para programación'
                WHERE (SELECT COUNT(*) FROM products) = 0
                """);
            stmt.execute("""
                INSERT INTO products (name, price, description)
                SELECT 'Mouse', 45000.00, 'Mouse USB'
                WHERE (SELECT COUNT(*) FROM products) = 0
                """);
            stmt.execute("""
                INSERT INTO products (name, price, description)
                SELECT 'Teclado', 85000.00, 'Teclado para el laboratorio'
                WHERE (SELECT COUNT(*) FROM products) = 0
                """);
        }
    }
}
