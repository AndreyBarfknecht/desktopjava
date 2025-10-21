package com.example.repository;

import com.example.model.Responsavel;
import com.example.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResponsavelDAO {

    public int saveAndReturnId(Responsavel responsavel) {
        String sql = "INSERT INTO responsaveis (nome_completo, cpf, telefone, email) VALUES (?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             // Precisamos de dizer ao PreparedStatement que queremos as chaves geradas
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, responsavel.getNomeCompleto());
            pstmt.setString(2, responsavel.getCpf());
            pstmt.setString(3, responsavel.getTelefone());
            pstmt.setString(4, responsavel.getEmail()); // Assumindo que o modelo Responsavel tem email

            pstmt.executeUpdate();

            // Depois de executar, obtemos as chaves geradas
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1); // Pega o ID da primeira coluna
            }

        } catch (SQLException e) {
            System.err.println("Erro ao salvar responsável: " + e.getMessage());
            e.printStackTrace();
        }
        return generatedId;
    }

    public void update(Responsavel responsavel) {
        String sql = "UPDATE responsaveis SET nome_completo = ?, cpf = ?, telefone = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, responsavel.getNomeCompleto());
            pstmt.setString(2, responsavel.getCpf());
            pstmt.setString(3, responsavel.getTelefone());
            pstmt.setString(4, responsavel.getEmail());
            pstmt.setInt(5, responsavel.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar responsável: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void delete(int id) {
    String sql = "DELETE FROM responsaveis WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
}