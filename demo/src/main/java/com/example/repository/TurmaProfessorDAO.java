package com.example.repository;

import com.example.util.DatabaseConnection; // Importa a tua classe de ligação!
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO para gerir a tabela de ligação 'turma_professor'.
 */
public class TurmaProfessorDAO {

    /**
     * Adiciona uma associação entre um professor e uma turma na tabela 
     * 'turma_professor'.
     *
     * @param idTurma O ID da turma (da tabela 'turmas').
     * @param idProfessor O ID do professor (da tabela 'professores').
     * @throws SQLException Lança uma exceção em caso de erro de SQL.
     */
    public void adicionarProfessorNaTurma(int idTurma, int idProfessor) throws SQLException {
        
        // 1. Define o comando SQL
        String sql = "INSERT INTO turma_professor (id_turma, id_professor) VALUES (?, ?)";

        // 2. Usa o try-with-resources para garantir que tudo é fechado
        //    E usa a TUA classe de ligação: DatabaseConnection.getConnection()
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 3. Define os valores para os placeholders ('?')
            pstmt.setInt(1, idTurma);
            pstmt.setInt(2, idProfessor);

            // 4. Executa o comando
            pstmt.executeUpdate();

        } catch (SQLException e) {
            // 5. Trata possíveis erros.
            // O erro 1062 é para "Entrada Duplicada". No teu pom.xml vi que usas
            // o driver MariaDB, que partilha este código com o MySQL.
            if (e.getErrorCode() == 1062) {
                System.err.println("Aviso: Este professor (ID=" + idProfessor + ") já está associado a esta turma (ID=" + idTurma + ").");
                // Não lançamos a exceção porque não é um erro fatal,
                // apenas uma tentativa de duplicar.
            } else {
                // Para outros erros (ex: ID da turma não existe),
                // lança a exceção para que o Controller a possa tratar.
                System.err.println("Erro de SQL ao tentar associar professor à turma: " + e.getMessage());
                throw e; // Lança a exceção para o método que chamou
            }
        }
    }

    // (Opcional: Podes adicionar métodos de 'delete' aqui mais tarde, se precisares)
    // public void removerProfessorDaTurma(int idTurma, int idProfessor) { ... }
}