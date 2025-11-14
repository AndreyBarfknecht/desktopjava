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

    public List<Turma> searchByName(String nome) {
        List<Turma> turmas = new ArrayList<>();
        
        // SQL com JOINs para construir os objetos Curso e PeriodoLetivo
        String sqlCompleto = "SELECT t.id as turma_id, t.nome_turma, t.turno, t.salaAula, " +
                   "c.id as curso_id, c.nome_curso, c.nivel, c.duracao_semestres, " +
                   "p.id as pl_id, p.nome as pl_nome, p.data_inicio, p.data_fim, p.status " +
                   "FROM turmas t " +
                   "JOIN cursos c ON t.id_curso = c.id " +
                   "JOIN periodos_letivos p ON t.id_periodo_letivo = p.id " +
                   "WHERE t.nome_turma LIKE ? LIMIT 10";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlCompleto)) {
            
            pstmt.setString(1, "%" + nome + "%"); 
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // (Lógica copiada do seu método 'getTurmaById')
                    Curso curso = new Curso(
                        rs.getString("nome_curso"),
                        rs.getString("nivel"),
                        rs.getInt("duracao_semestres")
                    );
                    curso.setId(rs.getInt("curso_id"));
                    
                    PeriodoLetivo periodo = new PeriodoLetivo(
                        rs.getInt("pl_id"),
                        rs.getString("pl_nome"),
                        rs.getDate("data_inicio").toLocalDate(),
                        rs.getDate("data_fim").toLocalDate(),
                        rs.getString("status")
                    );
                    
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
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar turmas por nome: " + e.getMessage());
            e.printStackTrace();
        }
        return turmas;
    }

    public int countTurmasFiltradas(String termoBusca) {
        String sqlBase = "SELECT COUNT(t.id) " +
                         "FROM turmas t " +
                         "JOIN cursos c ON t.id_curso = c.id " +
                         "JOIN periodos_letivos p ON t.id_periodo_letivo = p.id ";
        
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE t.nome_turma LIKE ? OR c.nome_curso LIKE ? OR t.turno LIKE ? OR p.nome LIKE ?";
        
        String sqlFinal = termoBusca.isEmpty() ? sqlBase : sqlBase + sqlWhere;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlFinal)) {

            if (!termoBusca.isEmpty()) {
                pstmt.setString(1, termoLike);
                pstmt.setString(2, termoLike);
                pstmt.setString(3, termoLike);
                pstmt.setString(4, termoLike);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); // Retorna a contagem
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Turma> getTurmasPaginadoEFiltrado(String termoBusca, int pagina, int limitePorPagina) {
        List<Turma> turmas = new ArrayList<>();
        
        // SQL base (o mesmo do seu getAll())
        String sqlBase = "SELECT t.id as turma_id, t.nome_turma, t.turno, t.salaAula, " +
                   "c.id as curso_id, c.nome_curso, c.nivel, c.duracao_semestres, " +
                   "p.id as pl_id, p.nome as pl_nome, p.data_inicio, p.data_fim, p.status " +
                   "FROM turmas t " +
                   "JOIN cursos c ON t.id_curso = c.id " +
                   "JOIN periodos_letivos p ON t.id_periodo_letivo = p.id ";
        
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE t.nome_turma LIKE ? OR c.nome_curso LIKE ? OR t.turno LIKE ? OR p.nome LIKE ? ";
        
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
                pstmt.setString(paramIndex++, termoLike);
            }
            pstmt.setInt(paramIndex++, limitePorPagina);
            pstmt.setInt(paramIndex++, offset);

            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                // (Lógica de construção de objeto copiada do seu getAll())
                Curso curso = new Curso(rs.getString("nome_curso"), rs.getString("nivel"), rs.getInt("duracao_semestres"));
                curso.setId(rs.getInt("curso_id"));

                PeriodoLetivo periodoLetivo = new PeriodoLetivo(
                        rs.getInt("pl_id"),
                        rs.getString("pl_nome"),
                        rs.getDate("data_inicio").toLocalDate(),
                        rs.getDate("data_fim").toLocalDate(),
                        rs.getString("status")
                );

                Turma turma = new Turma(
                        rs.getString("nome_turma"),
                        curso,
                        periodoLetivo,
                        rs.getString("turno"),
                        rs.getString("salaAula")
                );
                turma.setId(rs.getInt("turma_id"));
                turmas.add(turma);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return turmas;
    }
}