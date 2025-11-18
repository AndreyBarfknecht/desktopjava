package com.example.repository;

import com.example.model.Aluno;
import com.example.model.Curso;
import com.example.model.Matricula;
import com.example.model.PeriodoLetivo;
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

            // Correção para definir valores padrão se forem nulos
            if (matricula.getDataMatricula() != null) {
                pstmt.setDate(3, Date.valueOf(matricula.getDataMatricula()));
            } else {
                pstmt.setDate(3, Date.valueOf(LocalDate.now())); // Define a data de hoje
            }
            
            if (matricula.getStatus() != null && !matricula.getStatus().isEmpty()) {
                 pstmt.setString(4, matricula.getStatus());
            } else {
                 pstmt.setString(4, "Ativo"); // Define "Ativo" como padrão
            }

            pstmt.executeUpdate();
        }
    }

    public List<Matricula> getAll() {
        String sql = "SELECT m.id_matricula, m.data_matricula, m.status, " +
                     "a.id as aluno_id, a.nome_completo as aluno_nome, a.cpf as aluno_cpf, " +
                     "t.id as turma_id, t.nome_turma as turma_nome " +
                     "FROM matriculas m " +
                     "JOIN estudantes a ON m.id_aluno = a.id " +
                     "JOIN turmas t ON m.id_turma = t.id";
        List<Matricula> matriculas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Aluno aluno = new Aluno(rs.getString("aluno_nome"), rs.getString("aluno_cpf"), null, null, null, null);
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
            System.err.println("Erro ao buscar matrículas: " + e.getMessage());
            e.printStackTrace();
        }
        return matriculas;
    }

    public List<Matricula> getMatriculasPorAluno(int alunoId) {
    // SQL ATUALIZADA: Agora faz JOIN em Cursos e Periodos Letivos
    String sql = "SELECT m.id_matricula, m.data_matricula, m.status, " +
                 "a.id as aluno_id, a.nome_completo as aluno_nome, a.cpf as aluno_cpf, " +
                 "t.id as turma_id, t.nome_turma as turma_nome, t.turno, t.salaAula, " +
                 "c.id as curso_id, c.nome_curso, c.nivel, c.duracao_semestres, " +
                 "p.id as periodo_id, p.nome as periodo_nome, p.data_inicio, p.data_fim, p.status as periodo_status " +
                 "FROM matriculas m " +
                 "JOIN estudantes a ON m.id_aluno = a.id " +
                 "JOIN turmas t ON m.id_turma = t.id " +
                 "JOIN cursos c ON t.id_curso = c.id " + // <-- JOIN ADICIONADO
                 "JOIN periodos_letivos p ON t.id_periodo_letivo = p.id " + // <-- JOIN ADICIONADO
                 "WHERE m.id_aluno = ?";

    List<Matricula> matriculas = new ArrayList<>();

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, alunoId);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                // 1. Monta o Aluno (simplificado, já que temos o ID)
                Aluno aluno = new Aluno(rs.getString("aluno_nome"), rs.getString("aluno_cpf"), null, null, null, null);
                aluno.setId(rs.getInt("aluno_id"));

                // 2. Monta o Curso
                Curso curso = new Curso(
                    rs.getString("nome_curso"),
                    rs.getString("nivel"),
                    rs.getInt("duracao_semestres")
                );
                curso.setId(rs.getInt("curso_id"));

                // 3. Monta o PeriodoLetivo
                PeriodoLetivo periodo = new PeriodoLetivo(
                    rs.getInt("periodo_id"),
                    rs.getString("periodo_nome"),
                    rs.getDate("data_inicio").toLocalDate(),
                    rs.getDate("data_fim").toLocalDate(),
                    rs.getString("periodo_status")
                );

                // 4. Monta a Turma (agora com Curso e Periodo)
                Turma turma = new Turma(
                    rs.getString("turma_nome"),
                    curso, // <-- AGORA TEMOS O CURSO
                    periodo,
                    rs.getString("turno"),
                    rs.getString("salaAula")
                );
                turma.setId(rs.getInt("turma_id"));

                // 5. Monta a Matrícula
                LocalDate dataMatricula = rs.getDate("data_matricula").toLocalDate();
                String status = rs.getString("status");
                Matricula matricula = new Matricula(aluno, turma, dataMatricula, status);
                matricula.setId(rs.getInt("id_matricula"));

                matriculas.add(matricula);
            }
        }
    } catch (SQLException e) {
        System.err.println("Erro ao buscar matrículas do aluno: " + e.getMessage());
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
                return rs.getInt("id_matricula");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar ID da matrícula: " + e.getMessage());
            e.printStackTrace();
        }
        return null; 
    }

    public void updateStatus(int matriculaId, String novoStatus) {
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
        
        // --- ALTERAÇÃO AQUI: Adicionado "a.cpf LIKE ?" ---
        String sqlWhere = "WHERE a.nome_completo LIKE ? OR a.cpf LIKE ? OR t.nome_turma LIKE ? OR m.status LIKE ?";
        
        String sqlFinal = termoBusca.isEmpty() ? sqlBase : sqlBase + sqlWhere;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlFinal)) {

            if (!termoBusca.isEmpty()) {
                pstmt.setString(1, termoLike); // nome
                pstmt.setString(2, termoLike); // cpf
                pstmt.setString(3, termoLike); // turma
                pstmt.setString(4, termoLike); // status
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
        
        String sqlBase = "SELECT m.id_matricula, m.data_matricula, m.status, " +
                         "a.id as aluno_id, a.nome_completo as aluno_nome, a.cpf as aluno_cpf, " +
                         "t.id as turma_id, t.nome_turma as turma_nome " +
                         "FROM matriculas m " +
                         "JOIN estudantes a ON m.id_aluno = a.id " +
                         "JOIN turmas t ON m.id_turma = t.id ";
        
        String termoLike = "%" + termoBusca + "%";

        // --- ALTERAÇÃO AQUI: Adicionado "a.cpf LIKE ?" ---
        String sqlWhere = "WHERE a.nome_completo LIKE ? OR a.cpf LIKE ? OR t.nome_turma LIKE ? OR m.status LIKE ? ";
        
        int offset = (pagina - 1) * limitePorPagina;
        
        String sqlFinal = termoBusca.isEmpty() ? 
                          sqlBase + "LIMIT ? OFFSET ?" : 
                          sqlBase + sqlWhere + "LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlFinal)) {

            int paramIndex = 1;
            if (!termoBusca.isEmpty()) {
                pstmt.setString(paramIndex++, termoLike); // nome
                pstmt.setString(paramIndex++, termoLike); // cpf
                pstmt.setString(paramIndex++, termoLike); // turma
                pstmt.setString(paramIndex++, termoLike); // status
            }
            pstmt.setInt(paramIndex++, limitePorPagina);
            pstmt.setInt(paramIndex++, offset);

            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Aluno aluno = new Aluno(rs.getString("aluno_nome"), rs.getString("aluno_cpf"), null, null, null, null);
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