package com.example.repository;

import com.example.model.Curso;
import com.example.util.DatabaseConnection; //
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    
    // Podes adicionar métodos update() e delete() aqui mais tarde,
    // seguindo o exemplo do ProfessorDAO
}