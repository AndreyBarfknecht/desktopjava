package com.example.repository;

import com.example.util.DatabaseConnection; // Importa a nossa classe de ligação
import com.example.model.Professor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfessorDAO {

    public void save(Professor professor) {
        String sql = "INSERT INTO professores (nome_completo, cpf, data_nascimento, email, telefone, disciplina_principal) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, professor.getNomeCompleto());
            pstmt.setString(2, professor.getCpf());
            // O campo data_nascimento não está no modelo Professor, vamos adicionar depois. Por agora, fica nulo.
            if (professor.getDataNascimento() != null) {
                 pstmt.setDate(3, Date.valueOf(professor.getDataNascimento()));
            } else {
                 pstmt.setNull(3, java.sql.Types.DATE);
            }
            pstmt.setString(4, professor.getEmail());
            pstmt.setString(5, professor.getTelefone());
            pstmt.setString(6, professor.getDisciplinaPrincipal());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao salvar professor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}