package com.example.repository;

import com.example.model.Aluno;
import com.example.model.Matricula;
import com.example.model.Turma;
import com.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MatriculaDAO {

    public void save(Matricula matricula) throws SQLException {
        String sql = "INSERT INTO matriculas (id_aluno, id_turma, data_matricula, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, matricula.getAluno().getId());
            pstmt.setInt(2, matricula.getTurma().getId());
            pstmt.setDate(3, Date.valueOf(matricula.getDataMatricula()));
            pstmt.setString(4, matricula.getStatus());

            pstmt.executeUpdate();
        }
    }

    public List<Matricula> getAll() {
        // --- CORREÇÃO 1: Alterado de 'm.id' para 'm.id_matricula' ---
        String sql = "SELECT m.id_matricula, m.data_matricula, m.status, " +
                     "a.id as aluno_id, a.nome_completo as aluno_nome, " +
                     "t.id as turma_id, t.nome_turma as turma_nome " +
                     "FROM matriculas m " +
                     "JOIN estudantes a ON m.id_aluno = a.id " +
                     "JOIN turmas t ON m.id_turma = t.id";
        List<Matricula> matriculas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Cria objeto Aluno simplificado
                Aluno aluno = new Aluno(rs.getString("aluno_nome"), null, null, null, null, null);
                aluno.setId(rs.getInt("aluno_id"));

                // --- CORREÇÃO 2 (Menor): Corrigido construtor da Turma ---
                // O último parâmetro é 'sala', estava a passar a string SQL inteira por engano.
                // Passamos null pois não buscamos a sala nesta query.
                Turma turma = new Turma(rs.getString("turma_nome"), null, null, null, null);
                turma.setId(rs.getInt("turma_id"));

                // Cria objeto Matricula
                LocalDate dataMatricula = rs.getDate("data_matricula").toLocalDate();
                String status = rs.getString("status");
                Matricula matricula = new Matricula(aluno, turma, dataMatricula, status);
                
                // --- CORREÇÃO 3: Alterado de 'id' para 'id_matricula' ---
                matricula.setId(rs.getInt("id_matricula"));

                matriculas.add(matricula);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar matrículas: " + e.getMessage());
            e.printStackTrace();
        }
        return matriculas;
    }

    public Integer getMatriculaId(int alunoId, int turmaId) {
        String sql = "SELECT id_matricula FROM matriculas WHERE id_aluno = ? AND id_turma = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, alunoId);
            pstmt.setInt(2, turmaId);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id_matricula"); // (Este já estava correto)
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar ID da matrícula: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Não encontrou
    }

    public void updateStatus(int matriculaId, String novoStatus) {
        // --- CORREÇÃO 4: Alterado de 'id = ?' para 'id_matricula = ?' ---
        String sql = "UPDATE matriculas SET status = ? WHERE id_matricula = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, novoStatus);
            pstmt.setInt(2, matriculaId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status da matrícula: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(int matriculaId) {
        // --- CORREÇÃO 5: Alterado de 'id = ?' para 'id_matricula = ?' ---
        String sql = "DELETE FROM matriculas WHERE id_matricula = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, matriculaId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao excluir matrícula: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int countMatriculasFiltradas(String termoBusca) {
        String sqlBase = "SELECT COUNT(m.id_matricula) " +
                         "FROM matriculas m " +
                         "JOIN estudantes a ON m.id_aluno = a.id " +
                         "JOIN turmas t ON m.id_turma = t.id ";
        
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE a.nome_completo LIKE ? OR t.nome_turma LIKE ? OR m.status LIKE ?";
        
        String sqlFinal = termoBusca.isEmpty() ? sqlBase : sqlBase + sqlWhere;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlFinal)) {

            if (!termoBusca.isEmpty()) {
                pstmt.setString(1, termoLike);
                pstmt.setString(2, termoLike);
                pstmt.setString(3, termoLike);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Matricula> getMatriculasPaginadoEFiltrado(String termoBusca, int pagina, int limitePorPagina) {
        List<Matricula> matriculas = new ArrayList<>();
        
        // SQL copiado do seu método getAll(), com adição de WHERE e LIMIT/OFFSET
        String sqlBase = "SELECT m.id_matricula, m.data_matricula, m.status, " +
                         "a.id as aluno_id, a.nome_completo as aluno_nome, " +
                         "t.id as turma_id, t.nome_turma as turma_nome " +
                         "FROM matriculas m " +
                         "JOIN estudantes a ON m.id_aluno = a.id " +
                         "JOIN turmas t ON m.id_turma = t.id ";
        
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE a.nome_completo LIKE ? OR t.nome_turma LIKE ? OR m.status LIKE ? ";
        
        int offset = (pagina - 1) * limitePorPagina;
        
        String sqlFinal = termoBusca.isEmpty() ? 
                          sqlBase + "LIMIT ? OFFSET ?" : 
                          sqlBase + sqlWhere + "LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlFinal)) {

            int paramIndex = 1;
            if (!termoBusca.isEmpty()) {
                pstmt.setString(paramIndex++, termoLike);
                pstmt.setString(paramIndex++, termoLike);
                pstmt.setString(paramIndex++, termoLike);
            }
            pstmt.setInt(paramIndex++, limitePorPagina);
            pstmt.setInt(paramIndex++, offset);

            ResultSet rs = pstmt.executeQuery();
            
            // Loop de construção de objeto (idêntico ao seu getAll())
            while (rs.next()) {
                Aluno aluno = new Aluno(rs.getString("aluno_nome"), null, null, null, null, null);
                aluno.setId(rs.getInt("aluno_id"));

                Turma turma = new Turma(rs.getString("turma_nome"), null, null, null, null);
                turma.setId(rs.getInt("turma_id"));

                LocalDate dataMatricula = rs.getDate("data_matricula").toLocalDate();
                String status = rs.getString("status");
                Matricula matricula = new Matricula(aluno, turma, dataMatricula, status);
                matricula.setId(rs.getInt("id_matricula"));

                matriculas.add(matricula);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar matrículas paginadas: " + e.getMessage());
            e.printStackTrace();
        }
        return matriculas;
    }
}