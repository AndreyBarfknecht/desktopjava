package com.example.repository;

import com.example.model.Aluno;
import com.example.model.Matricula;
import com.example.model.Turma;
import com.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MatriculaDAO {

    public void save(Matricula matricula) throws SQLException {
        String sql = "INSERT INTO matriculas (id_aluno, id_turma, data_matricula, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, matricula.getAluno().getId());
            pstmt.setInt(2, matricula.getTurma().getId());
            pstmt.setDate(3, Date.valueOf(matricula.getDataMatricula()));
            pstmt.setString(4, matricula.getStatus());

            pstmt.executeUpdate();
        }
    }

    public List<Matricula> getAll() {
        String sql = "SELECT m.id, m.data_matricula, m.status, " +
                     "a.id as aluno_id, a.nome_completo as aluno_nome, " +
                     "t.id as turma_id, t.nome_turma as turma_nome " +
                     "FROM matriculas m " +
                     "JOIN estudantes a ON m.id_aluno = a.id " +
                     "JOIN turmas t ON m.id_turma = t.id";
        List<Matricula> matriculas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Cria objeto Aluno simplificado apenas com os dados necessários para a matrícula
                // Usamos o construtor existente, mas com valores nulos/vazios para campos não buscados.
                Aluno aluno = new Aluno(rs.getString("aluno_nome"), null, null, null, null);
                aluno.setId(rs.getInt("aluno_id"));

                // Cria objeto Turma simplificado apenas com os dados necessários
                // Usamos o construtor existente, mas com valores nulos/vazios para campos não buscados.
                Turma turma = new Turma(rs.getString("turma_nome"), null, null, null, sql);
                turma.setId(rs.getInt("turma_id"));

                // Cria objeto Matricula
                LocalDate dataMatricula = rs.getDate("data_matricula").toLocalDate();
                String status = rs.getString("status");
                Matricula matricula = new Matricula(aluno, turma, dataMatricula, status);
                matricula.setId(rs.getInt("id"));

                matriculas.add(matricula);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar matrículas: " + e.getMessage());
            e.printStackTrace();
        }
        return matriculas;
    }

    public Integer getMatriculaId(int alunoId, int turmaId) {
        String sql = "SELECT id_matricula FROM matriculas WHERE id_aluno = ? AND id_turma = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, alunoId);
            pstmt.setInt(2, turmaId);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id_matricula"); // Encontrou e retorna o ID
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar ID da matrícula: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Não encontrou
    }

    public void updateStatus(int matriculaId, String novoStatus) {
        String sql = "UPDATE matriculas SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, novoStatus);
            pstmt.setInt(2, matriculaId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status da matrícula: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(int matriculaId) {
        String sql = "DELETE FROM matriculas WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, matriculaId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao excluir matrícula: " + e.getMessage());
            e.printStackTrace();
        }
    }
}