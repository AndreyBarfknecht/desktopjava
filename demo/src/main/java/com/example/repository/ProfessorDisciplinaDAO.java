package com.example.repository;

import com.example.model.Disciplina;
import com.example.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gerir a tabela de ligação 'professor_disciplina'.
 * Esta tabela define quais disciplinas um professor está apto a lecionar.
 */
public class ProfessorDisciplinaDAO {

    /**
     * Associa uma disciplina a um professor.
     */
    public void adicionarAssociacao(int idProfessor, int idDisciplina) throws SQLException {
        String sql = "INSERT INTO professor_disciplina (id_professor, id_disciplina) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProfessor);
            pstmt.setInt(2, idDisciplina);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // 1062 = "Entrada Duplicada"
                System.err.println("Aviso: Esta associação já existe.");
            } else {
                System.err.println("Erro de SQL ao tentar associar professor-disciplina: " + e.getMessage());
                throw e; 
            }
        }
    }

    /**
     * Remove uma associação entre um professor e uma disciplina.
     */
    public void removerAssociacao(int idProfessor, int idDisciplina) throws SQLException {
        String sql = "DELETE FROM professor_disciplina WHERE id_professor = ? AND id_disciplina = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idProfessor);
            pstmt.setInt(2, idDisciplina);
            pstmt.executeUpdate();
        }
    }

    /**
     * Busca todas as disciplinas que um professor específico pode lecionar.
     */
    public List<Disciplina> getDisciplinasDoProfessor(int idProfessor) {
        String sql = "SELECT d.id, d.nome_disciplina, d.carga_horaria " +
                     "FROM disciplinas d " +
                     "JOIN professor_disciplina pd ON d.id = pd.id_disciplina " +
                     "WHERE pd.id_professor = ?";
        
        List<Disciplina> disciplinas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idProfessor);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Disciplina disciplina = new Disciplina(
                        rs.getString("nome_disciplina"),
                        rs.getInt("carga_horaria")
                    );
                    disciplina.setId(rs.getInt("id"));
                    disciplinas.add(disciplina);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar disciplinas do professor: " + e.getMessage());
            e.printStackTrace();
        }
        return disciplinas;
    }
}