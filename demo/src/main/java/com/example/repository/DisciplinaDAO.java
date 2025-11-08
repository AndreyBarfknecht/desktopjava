package com.example.repository;

import com.example.model.Curso;
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

    public void update(Disciplina disciplina) throws SQLException {
        String sql = "UPDATE disciplinas SET nome_disciplina = ?, carga_horaria = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, disciplina.getNomeDisciplina());
            pstmt.setInt(2, disciplina.getCargaHoraria());
            pstmt.setInt(3, disciplina.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        // Antes de excluir, verificar se o curso está sendo usado em 'turmas' ou 'grade_curricular'
        // Esta é uma simplificação. O ideal seria tratar a exceção de chave estrangeira.
        // falta fazer <<<
        String sql = "DELETE FROM disciplinas WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir disciplina: " + e.getMessage());
            throw e; // Lança para o controller tratar (ex: mostrar alerta)
        }
    }
}