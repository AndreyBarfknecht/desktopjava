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
     * (ATUALIZADO PARA O NOVO ESQUEMA DA BD)
     */
    public void save(Nota nota) throws SQLException {
        // SQL ATUALIZADO (usando id_matricula e id_disciplina)
        String sql = "INSERT INTO notas (id_matricula, id_disciplina, avaliacao, valor) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Mapeamento ATUALIZADO para os novos campos do objeto Nota
            pstmt.setInt(1, nota.getIdMatricula()); // <-- NOVO
            pstmt.setInt(2, nota.getIdDisciplina()); // <-- NOVO
            pstmt.setString(3, nota.getAvaliacao());
            pstmt.setDouble(4, nota.getValor());
            pstmt.executeUpdate();
        }
    }

    /**
     * Busca todas as notas de um aluno específico (usando JOIN).
     * (Este método também foi atualizado para o novo esquema)
     */
    public List<Nota> getNotasByAlunoId(int alunoId) {
        // Este método agora precisa de um JOIN na tabela matriculas
        String sql = "SELECT n.* FROM notas n " +
                     "JOIN matriculas m ON n.id_matricula = m.id_matricula " +
                     "WHERE m.id_aluno = ?";
        
        List<Nota> notas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, alunoId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Criamos a nota usando o novo construtor
                Nota nota = new Nota(
                    rs.getInt("id_matricula"),
                    rs.getInt("id_disciplina"),
                    rs.getDouble("valor"),
                    rs.getString("avaliacao")
                );
                nota.setId(rs.getInt("id"));
                notas.add(nota);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notas;
    }
}