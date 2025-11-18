package com.example.repository;

import com.example.model.Nota;
import com.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotaDAO {

    /**
     * Salva uma nova nota na base de dados.
     * (CORRIGIDO para corresponder ao Modelo e Diagrama)
     */
    public void save(Nota nota) throws SQLException {
        String sql = "INSERT INTO notas (id_matricula, id_disciplina, avaliacao, valor) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, nota.getIdMatricula()); // <-- CORRIGIDO
            pstmt.setInt(2, nota.getIdDisciplina()); 
            pstmt.setString(3, nota.getAvaliacao());
            pstmt.setDouble(4, nota.getValor());
            pstmt.executeUpdate();
        }
    }

    // --- MÉTODO PRINCIPAL CORRIGIDO ---
    /**
     * Busca TODAS as notas de uma turma específica.
     * Traz o nome do aluno e o nome da disciplina associados.
     */
    public List<Nota> getNotasByTurmaId(int turmaId) {
        List<Nota> notas = new ArrayList<>();
        
        // SQL Corrigida para usar 'estudantes' e as ligações corretas do diagrama
        String sql = "SELECT n.id, n.id_matricula, n.id_disciplina, n.avaliacao, n.valor, " +
                     "e.nome_completo, d.nome_disciplina " +
                     "FROM notas n " +
                     "JOIN matriculas m ON n.id_matricula = m.id " +
                     "JOIN estudantes e ON m.id_aluno = e.id " + // <-- CORRIGIDO (era 'alunos')
                     "JOIN disciplinas d ON n.id_disciplina = d.id " +
                     "WHERE m.id_turma = ? " + // Filtra pela turma
                     "ORDER BY e.nome_completo, d.nome_disciplina";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, turmaId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Usa o construtor corrigido
                Nota nota = new Nota(
                    rs.getInt("id_matricula"), // <-- CORRIGIDO
                    rs.getInt("id_disciplina"),
                    rs.getDouble("valor"),
                    rs.getString("avaliacao")
                );
                nota.setId(rs.getInt("id"));
                
                // Preenche os campos extra para a UI
                nota.setNomeDisciplina(rs.getString("nome_disciplina")); 
                nota.setNomeAluno(rs.getString("nome_completo")); // Vem de 'estudantes'
                
                notas.add(nota);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar notas da turma: " + e.getMessage());
            e.printStackTrace(); // Mantém isto para vermos o log se falhar
        }
        return notas;
    }


    // --- MÉTODOS DE ATUALIZAÇÃO E EXCLUSÃO (Estes não mudam) ---

    public void update(Nota nota) throws SQLException {
        String sql = "UPDATE notas SET avaliacao = ?, valor = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nota.getAvaliacao());
            pstmt.setDouble(2, nota.getValor());
            pstmt.setInt(3, nota.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int notaId) throws SQLException {
        String sql = "DELETE FROM notas WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notaId);
            pstmt.executeUpdate();
        }
    }
}