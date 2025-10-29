package com.example.repository;

import com.example.model.Disciplina;
import com.example.util.DatabaseConnection; //
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    /**
     * Salva uma nova disciplina na base de dados.
     */
    public void save(Disciplina disciplina) throws SQLException {
        String sql = "INSERT INTO disciplinas (nome_disciplina, carga_horaria) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, disciplina.getNomeDisciplina());
            pstmt.setInt(2, disciplina.getCargaHoraria());
            pstmt.executeUpdate();
        }
    }

    /**
     * Busca todas as disciplinas registadas.
     */
    public List<Disciplina> getAll() {
        String sql = "SELECT * FROM disciplinas";
        List<Disciplina> disciplinas = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Disciplina disciplina = new Disciplina(
                    rs.getString("nome_disciplina"),
                    rs.getInt("carga_horaria")
                );
                disciplina.setId(rs.getInt("id"));
                disciplinas.add(disciplina);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar disciplinas: " + e.getMessage());
            e.printStackTrace();
        }
        return disciplinas;
    }
}