package com.example.repository;

import com.example.util.DatabaseConnection; // Importa a nossa classe de ligação
import com.example.model.Professor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProfessorDAO {

    public void save(Professor professor) {
        String sql = "INSERT INTO professores (nome_completo, cpf, data_nascimento, email, telefone) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, professor.getNomeCompleto());
            pstmt.setString(2, professor.getCpf());
            // O campo data_nascimento não está no modelo Professor, vamos adicionar depois. Por agora, fica nulo.
            if (professor.getDataNascimento() != null) {
                 pstmt.setDate(3, Date.valueOf(professor.getDataNascimento()));
            } else {
                 pstmt.setNull(3, java.sql.Types.DATE);
            }
            pstmt.setString(4, professor.getEmail());
            pstmt.setString(5, professor.getTelefone());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao salvar professor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // metodo para listar os
    public List<Professor> getAll() {
        String sql = "SELECT * FROM professores";
        List<Professor> professores = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Itera sobre cada linha que a base de dados retornou
            while (rs.next()) {
                // Extrai os dados de cada coluna da linha atual
                int id = rs.getInt("id");
                String nome = rs.getString("nome_completo");
                String cpf = rs.getString("cpf");
                // Lida com a possibilidade de data_nascimento ser NULL na base de dados
                Date sqlDate = rs.getDate("data_nascimento");
                LocalDate dataNascimento = (sqlDate != null) ? sqlDate.toLocalDate() : null;


                String email = rs.getString("email");
                String telefone = rs.getString("telefone");

                // Cria um objeto Professor com os dados extraídos
                Professor professor = new Professor(nome, cpf, dataNascimento, email, telefone);
                professor.setId(id); // Define o ID que veio da base de dados

                // Adiciona o professor à lista
                professores.add(professor);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar professores: " + e.getMessage());
            e.printStackTrace();
        }
        
        return professores; // Retorna a lista completa de professores
    }

    public void update(Professor professor) {
        String sql = "UPDATE professores SET nome_completo = ?, cpf = ?, data_nascimento = ?, email = ?, telefone = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Lida com a possibilidade de dataNascimento ser NULL no objeto Professor
            if (professor.getDataNascimento() != null) {
                 pstmt.setDate(3, Date.valueOf(professor.getDataNascimento()));
            } else {
                 pstmt.setNull(3, java.sql.Types.DATE);
            }
            pstmt.setString(1, professor.getNomeCompleto());
            pstmt.setString(2, professor.getCpf());
            pstmt.setString(4, professor.getEmail());
            pstmt.setString(5, professor.getTelefone());
            pstmt.setInt(6, professor.getId()); // O ID é usado no 'WHERE'

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar professor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- NOVO MÉTODO PARA APAGAR ---
    public void delete(int id) {
        String sql = "DELETE FROM professores WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id); // Define o ID do professor a ser apagado
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao apagar professor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Professor> searchByName(String nome) {
        String sql = "SELECT * FROM professores WHERE nome_completo LIKE ? LIMIT 10";
        List<Professor> professores = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + nome + "%"); 

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Suposição de que o construtor do Professor lida com data nula
                java.sql.Date sqlDate = rs.getDate("data_nascimento");
                LocalDate dataNascimento = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                Professor professor = new Professor(
                    rs.getString("nome_completo"), 
                    rs.getString("cpf"), 
                    dataNascimento, 
                    rs.getString("email"), 
                    rs.getString("telefone")
                );
                professor.setId(rs.getInt("id"));
                professores.add(professor);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar professores por nome: " + e.getMessage());
            e.printStackTrace();
        }
        return professores; 
    }

    public int countProfessoresFiltrados(String termoBusca) {
        String sqlBase = "SELECT COUNT(*) FROM professores ";
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE nome_completo LIKE ? OR cpf LIKE ? OR email LIKE ? OR telefone LIKE ?";
        
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

    public List<Professor> getProfessoresPaginadoEFiltrado(String termoBusca, int pagina, int limitePorPagina) {
        List<Professor> professores = new ArrayList<>();
        
        String sqlBase = "SELECT * FROM professores ";
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE nome_completo LIKE ? OR cpf LIKE ? OR email LIKE ? OR telefone LIKE ? ";
        
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
                 java.sql.Date sqlDate = rs.getDate("data_nascimento");
                 LocalDate dataNascimento = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                 Professor professor = new Professor(
                     rs.getString("nome_completo"), 
                     rs.getString("cpf"), 
                     dataNascimento, 
                     rs.getString("email"), 
                     rs.getString("telefone")
                 );
                 professor.setId(rs.getInt("id"));
                 professores.add(professor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return professores;
    }
}
