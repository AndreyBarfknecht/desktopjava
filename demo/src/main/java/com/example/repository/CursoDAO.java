package com.example.repository;

import com.example.model.Curso;
import com.example.util.DatabaseConnection; //
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap; // <-- NOVA IMPORTAÇÃO
import java.util.List;
import java.util.Map; // <-- NOVA IMPORTAÇÃO

public class CursoDAO {

    /**
     * Salva um novo curso na base de dados.
     */
    public void save(Curso curso) throws SQLException {
        String sql = "INSERT INTO cursos (nome_curso, nivel, duracao_semestres) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, curso.getNomeCurso());
            pstmt.setString(2, curso.getNivel());
            pstmt.setInt(3, curso.getDuracaoSemestres());
            pstmt.executeUpdate();
        }
    }

    /**
     * Busca todos os cursos registados.
     */
    public List<Curso> getAll() {
        String sql = "SELECT * FROM cursos";
        List<Curso> cursos = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Curso curso = new Curso(
                    rs.getString("nome_curso"),
                    rs.getString("nivel"),
                    rs.getInt("duracao_semestres")
                );
                curso.setId(rs.getInt("id"));
                cursos.add(curso);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar cursos: " + e.getMessage());
            e.printStackTrace();
        }
        return cursos;
    }
    
    public void update(Curso curso) throws SQLException {
        String sql = "UPDATE cursos SET nome_curso = ?, nivel = ?, duracao_semestres = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, curso.getNomeCurso());
            pstmt.setString(2, curso.getNivel());
            pstmt.setInt(3, curso.getDuracaoSemestres());
            pstmt.setInt(4, curso.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        // Antes de excluir, verificar se o curso está sendo usado em 'turmas' ou 'grade_curricular'
        // Esta é uma simplificação. O ideal seria tratar a exceção de chave estrangeira.
        String sql = "DELETE FROM cursos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir curso: " + e.getMessage());
            throw e; // Lança para o controller tratar (ex: mostrar alerta)
        }
    }

    // --- NOVO MÉTODO PARA O DASHBOARD ---
    /**
     * Busca a contagem de alunos matriculados por curso.
     * Usa LEFT JOINs para incluir cursos mesmo que tenham 0 alunos.
     * @return Um Map<String, Integer> onde a Chave é o nome do curso e o Valor é a contagem de alunos.
     */
    public Map<String, Integer> getContagemAlunosPorCurso() {
        String sql = "SELECT " +
                     "    c.nome_curso, " +
                     "    COUNT(DISTINCT m.id_aluno) as total_alunos " +
                     "FROM cursos c " +
                     "LEFT JOIN turmas t ON c.id = t.id_curso " +
                     "LEFT JOIN matriculas m ON t.id = m.id_turma " +
                     "GROUP BY c.nome_curso " +
                     "ORDER BY total_alunos DESC";
        
        Map<String, Integer> contagem = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                contagem.put(rs.getString("nome_curso"), rs.getInt("total_alunos"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar contagem de alunos por curso: " + e.getMessage());
            e.printStackTrace();
        }
        return contagem;
    }

    public List<Curso> searchByName(String nome) {
        String sql = "SELECT * FROM cursos WHERE nome_curso LIKE ? LIMIT 10";
        List<Curso> cursos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + nome + "%"); // Procura por qualquer curso que contenha o nome
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Curso curso = new Curso(
                        rs.getString("nome_curso"),
                        rs.getString("nivel"),
                        rs.getInt("duracao_semestres")
                    );
                    curso.setId(rs.getInt("id"));
                    cursos.add(curso);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar cursos por nome: " + e.getMessage());
            e.printStackTrace();
        }
        return cursos;
    }

    public int countCursosFiltrados(String termoBusca) {
        String sqlBase = "SELECT COUNT(*) FROM cursos ";
        String termoLike = "%" + termoBusca + "%";
        // Busca por nome, nível ou duração (convertendo duração para texto)
        String sqlWhere = "WHERE nome_curso LIKE ? OR nivel LIKE ? OR CAST(duracao_semestres AS CHAR) LIKE ?";
        
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
                return rs.getInt(1); // Retorna a contagem
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Curso> getCursosPaginadoEFiltrado(String termoBusca, int pagina, int limitePorPagina) {
        List<Curso> cursos = new ArrayList<>();
        
        String sqlBase = "SELECT * FROM cursos ";
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE nome_curso LIKE ? OR nivel LIKE ? OR CAST(duracao_semestres AS CHAR) LIKE ? ";
        
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
            
            while (rs.next()) {
                 Curso curso = new Curso(
                     rs.getString("nome_curso"),
                     rs.getString("nivel"),
                     rs.getInt("duracao_semestres")
                 );
                 curso.setId(rs.getInt("id"));
                 cursos.add(curso);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cursos;
    }

    /**
     * Calcula a média de todas as notas lançadas para cada curso.
     * @return Mapa com Nome do Curso -> Média (Double)
     */
    public java.util.Map<String, Double> getMediaNotasPorCurso() {
        // SQL corrigida para usar LEFT JOIN (pega cursos mesmo se a média for null, tratamos depois)
        // VERIFIQUE: A sua tabela de notas tem a coluna 'valor' ou 'nota'? 
        // Estou assumindo 'valor' baseada no seu código anterior.
        String sql = "SELECT c.nome_curso, AVG(n.valor) as media " +
                     "FROM cursos c " +
                     "JOIN turmas t ON c.id = t.id_curso " +
                     "JOIN matriculas m ON t.id = m.id_turma " +
                     "JOIN notas n ON m.id_matricula = n.id_matricula " +
                     "GROUP BY c.id, c.nome_curso";
        
        java.util.Map<String, Double> dados = new java.util.HashMap<>();
        
        try (java.sql.Connection conn = com.example.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("--- DEBUG GRÁFICO CURSOS ---"); // Debug
            boolean temDados = false;

            while(rs.next()) {
                temDados = true;
                String curso = rs.getString("nome_curso");
                double media = rs.getDouble("media");
                
                // Arredonda
                media = Math.round(media * 10.0) / 10.0;
                
                System.out.println("Encontrado: " + curso + " - Média: " + media); // Debug
                dados.put(curso, media);
            }

            if (!temDados) {
                System.out.println("A consulta SQL rodou, mas não retornou nenhuma linha (ZERO notas encontradas).");
            }
            
        } catch (java.sql.SQLException e) {
            System.err.println("ERRO SQL AO BUSCAR MÉDIAS:");
            e.printStackTrace();
        }
        return dados;
    }
}
