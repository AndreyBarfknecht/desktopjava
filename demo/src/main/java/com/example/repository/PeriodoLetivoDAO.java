package com.example.repository;

import com.example.model.PeriodoLetivo;
import com.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PeriodoLetivoDAO {

    public void save(PeriodoLetivo periodo) throws SQLException {
        String sql = "INSERT INTO periodos_letivos (nome, data_inicio, data_fim, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, periodo.getNome());
            pstmt.setDate(2, Date.valueOf(periodo.getDataInicio()));
            pstmt.setDate(3, Date.valueOf(periodo.getDataFim()));
            pstmt.setString(4, periodo.getStatus());
            pstmt.executeUpdate();
        }
    }

    public List<PeriodoLetivo> getAll() {
        String sql = "SELECT * FROM periodos_letivos";
        List<PeriodoLetivo> periodos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                PeriodoLetivo periodo = new PeriodoLetivo(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getDate("data_inicio").toLocalDate(),
                    rs.getDate("data_fim").toLocalDate(),
                    rs.getString("status")
                );
                periodos.add(periodo);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar períodos letivos: " + e.getMessage());
            e.printStackTrace();
        }
        return periodos;
    }
}