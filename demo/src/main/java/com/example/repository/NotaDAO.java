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
     * Salva uma nova nota (corrigido para usar id_matricula)
     */
    public void save(Nota nota) throws SQLException {
        String sql = "INSERT INTO notas (id_matricula, id_disciplina, avaliacao, valor) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, nota.getIdMatricula()); 
            pstmt.setInt(2, nota.getIdDisciplina()); 
            pstmt.setString(3, nota.getAvaliacao());
            pstmt.setDouble(4, nota.getValor());
            pstmt.executeUpdate();
        }
    }

    // --- MÉTODO PRINCIPAL COM A TUA CONSULTA CORRIGIDA ---
    public List<Nota> getNotasByTurmaId(int turmaId) {
        List<Nota> notas = new ArrayList<>();
        
        // Esta é a tua consulta, com a correção no JOIN (n.id_matricula = m.id)
        String sql = "SELECT n.id_nota, n.id_matricula, n.id_disciplina, n.avaliacao, n.valor, e.nome_completo, d.nome_disciplina " +
                     "FROM notas n " +
                     "JOIN matriculas m ON n.id_matricula = m.id_matricula " + // <-- CORREÇÃO (deve ser m.id)
                     "JOIN estudantes e ON m.id_aluno = e.id " + // <-- CORRETO (estudantes)
                     "JOIN disciplinas d ON n.id_disciplina = d.id " +
                     "WHERE m.id_turma = ? " + 
                     "ORDER BY e.nome_completo, d.nome_disciplina";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, turmaId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Nota nota = new Nota(
                    rs.getInt("id_matricula"),
                    rs.getInt("id_disciplina"),
                    rs.getDouble("valor"),
                    rs.getString("avaliacao")
                );
                
                // --- CORRIGIDO para usar id_nota ---
                nota.setIdNota(rs.getInt("id_nota")); 
                
                nota.setNomeDisciplina(rs.getString("nome_disciplina")); 
                nota.setNomeAluno(rs.getString("nome_completo")); 
                
                notas.add(nota);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar notas da turma: " + e.getMessage());
            e.printStackTrace(); 
        }
        return notas;
    }


    // --- MÉTODOS DE ATUALIZAÇÃO (CORRIGIDOS para id_nota) ---

    public void update(Nota nota) throws SQLException {
        String sql = "UPDATE notas SET avaliacao = ?, valor = ? WHERE id_nota = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nota.getAvaliacao());
            pstmt.setDouble(2, nota.getValor());
            pstmt.setInt(3, nota.getIdNota()); // <-- CORRIGIDO
            pstmt.executeUpdate();
        }
    }

    public void delete(int idNota) throws SQLException {
        String sql = "DELETE FROM notas WHERE id_nota = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idNota); // <-- CORRIGIDO
            pstmt.executeUpdate();
        }
    }
}