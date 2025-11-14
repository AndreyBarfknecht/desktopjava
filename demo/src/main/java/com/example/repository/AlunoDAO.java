package com.example.repository;

import com.example.model.Aluno;
import com.example.model.Responsavel;
import com.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AlunoDAO {

    public void save(Aluno aluno) {
        String sql = "INSERT INTO estudantes (nome_completo, cpf, data_nascimento, id_responsavel, telefone, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, aluno.getNomeCompleto());
            pstmt.setString(2, aluno.getCpf());
            pstmt.setDate(3, Date.valueOf(aluno.getDataNascimento()));
            pstmt.setInt(4, aluno.getResponsavel().getId());
            pstmt.setString(5, aluno.getTelefone());
            pstmt.setString(6, aluno.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODO GETALL CORRIGIDO ---
    public List<Aluno> getAll() {
        String sql = "SELECT e.id as aluno_id, e.nome_completo as aluno_nome, e.cpf as aluno_cpf, e.data_nascimento, e.telefone as aluno_telefone, e.email as aluno_email," +
                     "r.id as resp_id, r.nome_completo as resp_nome, r.cpf as resp_cpf, r.telefone as resp_telefone, r.email as resp_email " +
                     "FROM estudantes e JOIN responsaveis r ON e.id_responsavel = r.id";
        
        List<Aluno> alunos = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Responsavel responsavel = new Responsavel(
                    rs.getString("resp_nome"), rs.getString("resp_cpf"),
                    rs.getString("resp_telefone"), rs.getString("resp_email")
                );
                responsavel.setId(rs.getInt("resp_id"));

                // CORREÇÃO: Usamos o construtor de 5 parâmetros, incluindo o telefone do aluno.
                Aluno aluno = new Aluno(
                    rs.getString("aluno_nome"),
                    rs.getString("aluno_cpf"),
                    rs.getDate("data_nascimento").toLocalDate(),
                    responsavel,
                    rs.getString("aluno_telefone"),
                    rs.getString("aluno_email") // Passamos o telefone do aluno
                );
                aluno.setId(rs.getInt("aluno_id"));
                alunos.add(aluno);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alunos;
    }

    public List<Aluno> getAlunosByTurmaId(int turmaId) {
        // Este SQL junta Alunos (estudantes) e Matrículas
        // e filtra pela turma (id_turma)
        String sql = "SELECT e.*, r.id as resp_id, r.nome_completo as resp_nome, r.cpf as resp_cpf, r.telefone as resp_telefone, r.email as resp_email " +
                     "FROM estudantes e " +
                     "JOIN responsaveis r ON e.id_responsavel = r.id " +
                     "JOIN matriculas m ON e.id = m.id_aluno " +
                     "WHERE m.id_turma = ?";
        
        List<Aluno> alunos = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, turmaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // (Este código é igual ao do teu método getAll())
                Responsavel responsavel = new Responsavel(
                    rs.getString("resp_nome"), rs.getString("resp_cpf"),
                    rs.getString("resp_telefone"), rs.getString("resp_email")
                );
                responsavel.setId(rs.getInt("resp_id"));

                Aluno aluno = new Aluno(
                    rs.getString("nome_completo"),
                    rs.getString("cpf"),
                    rs.getDate("data_nascimento").toLocalDate(),
                    responsavel,
                    rs.getString("telefone"),
                    rs.getString("email")
                );
                aluno.setId(rs.getInt("id"));
                alunos.add(aluno);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alunos;
    }

    public void update(Aluno aluno) {
        String sql = "UPDATE estudantes SET nome_completo = ?, cpf = ?, data_nascimento = ?, id_responsavel = ?, telefone = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, aluno.getNomeCompleto());
            pstmt.setString(2, aluno.getCpf());
            pstmt.setDate(3, Date.valueOf(aluno.getDataNascimento()));
            pstmt.setInt(4, aluno.getResponsavel().getId());
            pstmt.setString(5, aluno.getTelefone());
            pstmt.setString(6, aluno.getEmail());
            pstmt.setInt(7, aluno.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM estudantes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int countAlunosByResponsavelId(int responsavelId) {
        String sql = "SELECT COUNT(*) FROM estudantes WHERE id_responsavel = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, responsavelId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countAlunosFiltrados(String termoBusca) {
        // SQL base para contagem
        String sqlBase = "SELECT COUNT(DISTINCT e.id) " +
                         "FROM estudantes e " +
                         "JOIN responsaveis r ON e.id_responsavel = r.id ";
        
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE e.nome_completo LIKE ? OR e.cpf LIKE ? OR r.nome_completo LIKE ?";

        // Usa o SQL completo apenas se houver um termo de busca
        String sqlFinal = termoBusca.isEmpty() ? sqlBase : sqlBase + sqlWhere;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlFinal)) {

            // Define os parâmetros apenas se a cláusula WHERE existir
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

    public List<Aluno> getAlunosPaginadoEFiltrado(String termoBusca, int pagina, int limitePorPagina) {
        List<Aluno> alunos = new ArrayList<>();
        
        // SQL base (o mesmo do seu getAll())
        String sqlBase = "SELECT e.id as aluno_id, e.nome_completo as aluno_nome, e.cpf as aluno_cpf, " +
                         "e.data_nascimento, e.telefone as aluno_telefone, e.email as aluno_email, " +
                         "r.id as resp_id, r.nome_completo as resp_nome, r.cpf as resp_cpf, " +
                         "r.telefone as resp_telefone, r.email as resp_email " +
                         "FROM estudantes e JOIN responsaveis r ON e.id_responsavel = r.id ";
        
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE e.nome_completo LIKE ? OR e.cpf LIKE ? OR r.nome_completo LIKE ? ";
        
        // Cálculo do OFFSET (quantos registos pular)
        int offset = (pagina - 1) * limitePorPagina;
        
        // SQL final com filtros e paginação
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
            
            // Este loop é idêntico ao do seu getAll()
            while (rs.next()) {
                Responsavel responsavel = new Responsavel(
                    rs.getString("resp_nome"), rs.getString("resp_cpf"),
                    rs.getString("resp_telefone"), rs.getString("resp_email")
                );
                responsavel.setId(rs.getInt("resp_id"));

                Aluno aluno = new Aluno(
                    rs.getString("aluno_nome"),
                    rs.getString("aluno_cpf"),
                    rs.getDate("data_nascimento").toLocalDate(),
                    responsavel,
                    rs.getString("aluno_telefone"),
                    rs.getString("aluno_email")
                );
                aluno.setId(rs.getInt("aluno_id"));
                alunos.add(aluno);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alunos;
    }

    public List<Aluno> searchByName(String nome) {
        List<Aluno> alunos = new ArrayList<>();
        
        // SQL base (o mesmo do seu getAll() para garantir que o Responsável vem junto)
        String sql = "SELECT e.id as aluno_id, e.nome_completo as aluno_nome, e.cpf as aluno_cpf, " +
                     "e.data_nascimento, e.telefone as aluno_telefone, e.email as aluno_email, " +
                     "r.id as resp_id, r.nome_completo as resp_nome, r.cpf as resp_cpf, " +
                     "r.telefone as resp_telefone, r.email as resp_email " +
                     "FROM estudantes e " +
                     "JOIN responsaveis r ON e.id_responsavel = r.id " +
                     "WHERE e.nome_completo LIKE ? LIMIT 10";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + nome + "%"); 

            ResultSet rs = pstmt.executeQuery();
            
            // Este loop é idêntico ao do seu getAll()
            while (rs.next()) {
                Responsavel responsavel = new Responsavel(
                    rs.getString("resp_nome"), rs.getString("resp_cpf"),
                    rs.getString("resp_telefone"), rs.getString("resp_email")
                );
                responsavel.setId(rs.getInt("resp_id"));

                java.sql.Date sqlDate = rs.getDate("data_nascimento");
                LocalDate dataNascimento = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                Aluno aluno = new Aluno(
                    rs.getString("aluno_nome"),
                    rs.getString("aluno_cpf"),
                    dataNascimento,
                    responsavel,
                    rs.getString("aluno_telefone"),
                    rs.getString("aluno_email")
                );
                aluno.setId(rs.getInt("aluno_id"));
                alunos.add(aluno);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar alunos por nome: " + e.getMessage());
            e.printStackTrace();
        }
        return alunos;
    }
}