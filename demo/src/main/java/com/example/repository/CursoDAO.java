package com.example.repository;

import com.example.model.Curso;
import com.example.util.DatabaseConnection; //
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap; // <-- NOVA IMPORTAÇÃO
import java.util.List;
import java.util.Map; // <-- NOVA IMPORTAÇÃO

public class CursoDAO {

    /**
     * Salva um novo curso na base de dados.
     */
    public void save(Curso curso) throws SQLException {
        String sql = "INSERT INTO cursos (nome_curso, nivel, duracao_semestres) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, curso.getNomeCurso());
            pstmt.setString(2, curso.getNivel());
            pstmt.setInt(3, curso.getDuracaoSemestres());
            pstmt.executeUpdate();
        }
    }

    /**
     * Busca todos os cursos registados.
     */
    public List<Curso> getAll() {
        String sql = "SELECT * FROM cursos";
        List<Curso> cursos = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Curso curso = new Curso(
                    rs.getString("nome_curso"),
                    rs.getString("nivel"),
                    rs.getInt("duracao_semestres")
                );
                curso.setId(rs.getInt("id"));
                cursos.add(curso);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar cursos: " + e.getMessage());
            e.printStackTrace();
        }
        return cursos;
    }
    
    public void update(Curso curso) throws SQLException {
        String sql = "UPDATE cursos SET nome_curso = ?, nivel = ?, duracao_semestres = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, curso.getNomeCurso());
            pstmt.setString(2, curso.getNivel());
            pstmt.setInt(3, curso.getDuracaoSemestres());
            pstmt.setInt(4, curso.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        // Antes de excluir, verificar se o curso está sendo usado em 'turmas' ou 'grade_curricular'
        // Esta é uma simplificação. O ideal seria tratar a exceção de chave estrangeira.
        String sql = "DELETE FROM cursos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir curso: " + e.getMessage());
            throw e; // Lança para o controller tratar (ex: mostrar alerta)
        }
    }

    // --- NOVO MÉTODO PARA O DASHBOARD ---
    /**
     * Busca a contagem de alunos matriculados por curso.
     * Usa LEFT JOINs para incluir cursos mesmo que tenham 0 alunos.
     * @return Um Map<String, Integer> onde a Chave é o nome do curso e o Valor é a contagem de alunos.
     */
    public Map<String, Integer> getContagemAlunosPorCurso() {
        String sql = "SELECT " +
                     "    c.nome_curso, " +
                     "    COUNT(DISTINCT m.id_aluno) as total_alunos " +
                     "FROM cursos c " +
                     "LEFT JOIN turmas t ON c.id = t.id_curso " +
                     "LEFT JOIN matriculas m ON t.id = m.id_turma " +
                     "GROUP BY c.nome_curso " +
                     "ORDER BY total_alunos DESC";
        
        Map<String, Integer> contagem = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                contagem.put(rs.getString("nome_curso"), rs.getInt("total_alunos"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar contagem de alunos por curso: " + e.getMessage());
            e.printStackTrace();
        }
        return contagem;
    }
}