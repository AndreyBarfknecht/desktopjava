package com.example.repository;

import com.example.model.Curso;
import com.example.model.Disciplina;
import com.example.util.DatabaseConnection; //
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    /**
     * Salva uma nova disciplina na base de dados.
     */
    public void save(Disciplina disciplina) throws SQLException {
        String sql = "INSERT INTO disciplinas (nome_disciplina, carga_horaria) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, disciplina.getNomeDisciplina());
            pstmt.setInt(2, disciplina.getCargaHoraria());
            pstmt.executeUpdate();
        }
    }

    /**
     * Busca todas as disciplinas registadas.
     */
    public List<Disciplina> getAll() {
        String sql = "SELECT * FROM disciplinas";
        List<Disciplina> disciplinas = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Disciplina disciplina = new Disciplina(
                    rs.getString("nome_disciplina"),
                    rs.getInt("carga_horaria")
                );
                disciplina.setId(rs.getInt("id"));
                disciplinas.add(disciplina);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar disciplinas: " + e.getMessage());
            e.printStackTrace();
        }
        return disciplinas;
    }

    public void update(Disciplina disciplina) throws SQLException {
        String sql = "UPDATE disciplinas SET nome_disciplina = ?, carga_horaria = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, disciplina.getNomeDisciplina());
            pstmt.setInt(2, disciplina.getCargaHoraria());
            pstmt.setInt(3, disciplina.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        // Antes de excluir, verificar se o curso está sendo usado em 'turmas' ou 'grade_curricular'
        // Esta é uma simplificação. O ideal seria tratar a exceção de chave estrangeira.
        // falta fazer <<<
        String sql = "DELETE FROM disciplinas WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir disciplina: " + e.getMessage());
            throw e; // Lança para o controller tratar (ex: mostrar alerta)
        }
    }

    public List<Disciplina> searchByName(String nome) {
        String sql = "SELECT * FROM disciplinas WHERE nome_disciplina LIKE ? LIMIT 10";
        List<Disciplina> disciplinas = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + nome + "%"); // Procura por qualquer disciplina que contenha o nome
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Disciplina disciplina = new Disciplina(
                        rs.getString("nome_disciplina"),
                        rs.getInt("carga_horaria")
                    );
                    disciplina.setId(rs.getInt("id"));
                    disciplinas.add(disciplina);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar disciplinas por nome: " + e.getMessage());
            e.printStackTrace();
        }
        return disciplinas;
    }

    // --- NOVO MÉTODO ADICIONADO ---
    /**
     * Busca disciplinas pelo nome, mas filtradas por um curso específico.
     * Utiliza a tabela grade_curricular para encontrar as associações.
     *
     * @param name    O termo de busca para o nome da disciplina.
     * @param cursoId O ID do curso ao qual as disciplinas devem pertencer.
     * @return Uma lista de Disciplinas.
     */
    public List<Disciplina> searchByNameAndCursoId(String name, int cursoId) {
        List<Disciplina> disciplinas = new ArrayList<>();
        
        // Este SQL junta as disciplinas com a grade curricular
        // para filtrar apenas as do curso desejado.
        String sql = "SELECT d.id, d.nome_disciplina, d.carga_horaria " +
                     "FROM disciplinas d " +
                     "JOIN grade_curricular gc ON d.id = gc.id_disciplina " +
                     "WHERE gc.id_curso = ? " +
                     "AND d.nome_disciplina LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cursoId);           // Filtro do ID do Curso
            pstmt.setString(2, "%" + name + "%"); // Filtro do Nome
            
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Disciplina disciplina = new Disciplina(
                    rs.getString("nome_disciplina"),
                    rs.getInt("carga_horaria")
                );
                disciplina.setId(rs.getInt("id"));
                disciplinas.add(disciplina);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar disciplinas por curso: " + e.getMessage());
            e.printStackTrace();
        }
        return disciplinas;
    }

    public int countDisciplinasFiltradas(String termoBusca) {
        String sqlBase = "SELECT COUNT(*) FROM disciplinas ";
        String termoLike = "%" + termoBusca + "%";
        // Busca por nome ou carga horária (convertendo para texto)
        String sqlWhere = "WHERE nome_disciplina LIKE ? OR CAST(carga_horaria AS CHAR) LIKE ?";
        
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

    public List<Disciplina> getDisciplinasPaginadoEFiltrado(String termoBusca, int pagina, int limitePorPagina) {
        List<Disciplina> disciplinas = new ArrayList<>();
        
        String sqlBase = "SELECT * FROM disciplinas ";
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE nome_disciplina LIKE ? OR CAST(carga_horaria AS CHAR) LIKE ? ";
        
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
            }
            pstmt.setInt(paramIndex++, limitePorPagina);
            pstmt.setInt(paramIndex++, offset);

            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                 Disciplina disciplina = new Disciplina(
                     rs.getString("nome_disciplina"),
                     rs.getInt("carga_horaria")
                 );
                 disciplina.setId(rs.getInt("id"));
                 disciplinas.add(disciplina);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return disciplinas;
    }
}