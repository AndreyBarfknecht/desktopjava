package com.example.repository;

import com.example.model.PeriodoLetivo;
import com.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PeriodoLetivoDAO {

    public void save(PeriodoLetivo periodo) throws SQLException {
        String sql = "INSERT INTO periodos_letivos (nome, data_inicio, data_fim, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, periodo.getNome());
            pstmt.setDate(2, Date.valueOf(periodo.getDataInicio()));
            pstmt.setDate(3, Date.valueOf(periodo.getDataFim()));
            pstmt.setString(4, periodo.getStatus());
            pstmt.executeUpdate();
        }
    }

    public List<PeriodoLetivo> getAll() {
        String sql = "SELECT * FROM periodos_letivos";
        List<PeriodoLetivo> periodos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                PeriodoLetivo periodo = new PeriodoLetivo(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getDate("data_inicio").toLocalDate(),
                    rs.getDate("data_fim").toLocalDate(),
                    rs.getString("status")
                );
                periodos.add(periodo);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar períodos letivos: " + e.getMessage());
            e.printStackTrace();
        }
        return periodos;
    }

    public List<PeriodoLetivo> searchByName(String nome) {
        String sql = "SELECT * FROM periodos_letivos WHERE nome LIKE ? LIMIT 10";
        List<PeriodoLetivo> periodos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {
            
            pstmt.setString(1, "%" + nome + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                PeriodoLetivo periodo = new PeriodoLetivo(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getDate("data_inicio").toLocalDate(),
                    rs.getDate("data_fim").toLocalDate(),
                    rs.getString("status")
                );
                periodos.add(periodo);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar períodos letivos por nome: " + e.getMessage());
            e.printStackTrace();
        }
        return periodos;
    }

    public void update(PeriodoLetivo periodo) throws SQLException {
        String sql = "UPDATE periodos_letivos SET nome = ?, data_inicio = ?, data_fim = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, periodo.getNome());
            pstmt.setDate(2, Date.valueOf(periodo.getDataInicio()));
            pstmt.setDate(3, Date.valueOf(periodo.getDataFim()));
            pstmt.setString(4, periodo.getStatus());
            pstmt.setInt(5, periodo.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM periodos_letivos WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao excluir período letivo: " + e.getMessage());
            throw e;
        }
    }

    // ... (depois do método delete()) ...

    /**
     * NOVO: Conta o número total de períodos que correspondem a um termo de busca.
     * A busca é feita no nome ou status.
     *
     * @param termoBusca O texto para filtrar. Se for vazio, conta todos os períodos.
     * @return O número total de períodos encontrados.
     */
    public int countPeriodosLetivosFiltrados(String termoBusca) {
        String sqlBase = "SELECT COUNT(*) FROM periodos_letivos ";
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE nome LIKE ? OR status LIKE ?";
        
        String sqlFinal = termoBusca.isEmpty() ? sqlBase : sqlBase + sqlWhere;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlFinal)) {

            if (!termoBusca.isEmpty()) {
                pstmt.setString(1, termoLike);
                pstmt.setString(2, termoLike);
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

    /**
     * NOVO: Busca uma "página" de períodos, opcionalmente filtrada.
     *
     * @param termoBusca O texto para filtrar (nome, status).
     * @param pagina O número da página (começando em 1).
     * @param limitePorPagina O número de registos por página.
     * @return Uma lista de PeriodoLetivo.
     */
    public List<PeriodoLetivo> getPeriodosLetivosPaginadoEFiltrado(String termoBusca, int pagina, int limitePorPagina) {
        List<PeriodoLetivo> periodos = new ArrayList<>();
        
        String sqlBase = "SELECT * FROM periodos_letivos ";
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE nome LIKE ? OR status LIKE ? ";
        
        int offset = (pagina - 1) * limitePorPagina;
        
        String sqlFinal = termoBusca.isEmpty() ? 
                          sqlBase + "ORDER BY data_inicio DESC LIMIT ? OFFSET ?" : 
                          sqlBase + sqlWhere + "ORDER BY data_inicio DESC LIMIT ? OFFSET ?";
        // Adicionei "ORDER BY data_inicio DESC" para mostrar os mais novos primeiro

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlFinal)) {

            int paramIndex = 1;
            if (!termoBusca.isEmpty()) {
                pstmt.setString(paramIndex++, termoLike);
                pstmt.setString(paramIndex++, termoLike);
            }
            pstmt.setInt(paramIndex++, limitePorPagina);
            pstmt.setInt(paramIndex++, offset);

            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                 PeriodoLetivo periodo = new PeriodoLetivo(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getDate("data_inicio").toLocalDate(),
                    rs.getDate("data_fim").toLocalDate(),
                    rs.getString("status")
                );
                periodos.add(periodo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return periodos;
    }
}