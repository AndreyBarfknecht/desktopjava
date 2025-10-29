package com.example.repository;

import com.example.model.Turma;
import com.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {

    public void save(Turma turma) {
        // Esta consulta INSERT estava correta, usando 'salaAula'
        String sql = "INSERT INTO turmas (nome_turma, ano_letivo, turno, salaAula) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, turma.getNome());
            pstmt.setString(2, turma.getAnoLetivo());
            pstmt.setString(3, turma.getTurno());
            pstmt.setString(4, turma.getSala());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao salvar turma: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Turma> getAll() {
        
        // CORREÇÃO: Simplificamos a query para buscar apenas os dados da turma,
        // removendo o JOIN com professores que não era necessário aqui.
        String sql = "SELECT id, nome_turma, ano_letivo, turno, salaAula FROM turmas";

        List<Turma> turmas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                
                // CORREÇÃO: Usar o construtor e garantir que o ano letivo seja uma String.
                Turma turma = new Turma(
                    rs.getString("nome_turma"),
                    rs.getString("ano_letivo"), // Lendo como String
                    rs.getString("turno"),
                    rs.getString("salaAula") // <-- Corrigido aqui também
                );
                turma.setId(id);

                turmas.add(turma);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar turmas: " + e.getMessage());
            e.printStackTrace();
        }

        return turmas;
    }
}