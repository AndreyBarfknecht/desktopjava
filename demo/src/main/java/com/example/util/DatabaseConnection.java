package com.example.util; // <-- ALTERADO para corresponder à nova localização

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // ... o resto do código permanece igual
    private static final String URL = "jdbc:mariadb://localhost:3306/SistemaAcademico";
    private static final String USER = "root";
    private static final String PASSWORD = "Andrey11!";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("### ERRO DE CONEXÃO COM A BASE DE DADOS ###");
            e.printStackTrace();
            throw new RuntimeException("Erro ao conectar-se à base de dados", e);
        }
    }
}