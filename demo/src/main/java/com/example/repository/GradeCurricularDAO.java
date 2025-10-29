package com.example.repository;

import com.example.model.Disciplina;
import com.example.util.DatabaseConnection; //
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GradeCurricularDAO {

    /**
     * Adiciona uma disciplina à grade de um curso.
     */
    public void adicionarDisciplinaNaGrade(int idCurso, int idDisciplina) throws SQLException {
        String sql = "INSERT INTO grade_curricular (id_curso, id_disciplina) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCurso);
            pstmt.setInt(2, idDisciplina);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Código de erro para chave duplicada
                System.err.println("Aviso: Essa disciplina já está na grade desse curso.");
                // Não lança o erro, apenas avisa
            } else {
                System.err.println("Erro ao adicionar disciplina à grade: " + e.getMessage());
                throw e; // Lança outros erros
            }
        }
    }

    /**
     * Remove uma disciplina da grade de um curso.
     */
    public void removerDisciplinaDaGrade(int idCurso, int idDisciplina) {
        String sql = "DELETE FROM grade_curricular WHERE id_curso = ? AND id_disciplina = ?";
        try (Connection conn = DatabaseConnection.getConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCurso);
            pstmt.setInt(2, idDisciplina);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao remover disciplina da grade: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lista todas as disciplinas de um curso específico.
     */
    public List<Disciplina> getDisciplinasByCurso(int idCurso) {
        String sql = "SELECT d.id, d.nome_disciplina, d.carga_horaria " +
                     "FROM disciplinas d " +
                     "JOIN grade_curricular gc ON d.id = gc.id_disciplina " +
                     "WHERE gc.id_curso = ?";
        List<Disciplina> disciplinas = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idCurso);
            
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
            System.err.println("Erro ao buscar disciplinas do curso: " + e.getMessage());
            e.printStackTrace();
        }
        return disciplinas;
    }
}