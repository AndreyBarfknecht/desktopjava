package com.example.repository;

import com.example.model.Curso;
import com.example.model.Turma;
import com.example.model.PeriodoLetivo;
import com.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {

    public void save(Turma turma) throws SQLException {
        String sql = "INSERT INTO turmas (nome_turma, id_curso, id_periodo_letivo, turno, salaAula) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, turma.getNome());
            pstmt.setInt(2, turma.getCurso().getId());
            pstmt.setInt(3, turma.getPeriodoLetivo().getId());
            pstmt.setString(4, turma.getTurno());
            pstmt.setString(5, turma.getSala());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao salvar turma: " + e.getMessage());
            e.printStackTrace();
            throw e; // Lança a exceção para o controller tratar
        }
    }

    public List<Turma> getAll() {
        // Query com JOINs para buscar os dados completos da turma, curso e período letivo
        String sql = "SELECT " +
                     "t.id as turma_id, t.nome_turma, t.turno, t.salaAula, " +
                     "c.id as curso_id, c.nome_curso, c.nivel, c.duracao_semestres, " +
                     "p.id as periodo_id, p.nome as periodo_nome, p.data_inicio, p.data_fim, p.status " +
                     "FROM turmas t " +
                     "JOIN cursos c ON t.id_curso = c.id " +
                     "JOIN periodos_letivos p ON t.id_periodo_letivo = p.id";

        List<Turma> turmas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                // 1. Monta o objeto Curso
                Curso curso = new Curso(
                    rs.getString("nome_curso"),
                    rs.getString("nivel"),
                    rs.getInt("duracao_semestres")
                );
                curso.setId(rs.getInt("curso_id"));
                
                // 2. Monta o objeto PeriodoLetivo
                PeriodoLetivo periodo = new PeriodoLetivo(
                    rs.getInt("periodo_id"),
                    rs.getString("periodo_nome"),
                    rs.getDate("data_inicio").toLocalDate(),
                    rs.getDate("data_fim").toLocalDate(),
                    rs.getString("status")
                );
                
                // 3. Monta o objeto Turma com os objetos Curso e PeriodoLetivo
                Turma turma = new Turma(
                    rs.getString("nome_turma"),
                    curso,
                    periodo,
                    rs.getString("turno"),
                    rs.getString("salaAula")
                );
                turma.setId(rs.getInt("turma_id"));

                turmas.add(turma);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar turmas: " + e.getMessage());
            e.printStackTrace();
        }
        return turmas;
    }

    // --- NOVO MÉTODO UPDATE ---
    public void update(Turma turma) throws SQLException {
        String sql = "UPDATE turmas SET nome_turma = ?, id_curso = ?, id_periodo_letivo = ?, turno = ?, salaAula = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, turma.getNome());
            pstmt.setInt(2, turma.getCurso().getId());
            pstmt.setInt(3, turma.getPeriodoLetivo().getId());
            pstmt.setString(4, turma.getTurno());
            pstmt.setString(5, turma.getSala());
            pstmt.setInt(6, turma.getId()); // ID para a cláusula WHERE
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar turma: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // --- NOVO MÉTODO DELETE ---
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM turmas WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erro ao excluir turma: " + e.getMessage());
            e.printStackTrace();
            throw e; // Lança para o controller tratar (ex: turma com alunos)
        }
    }
}