package com.example.repository;

import com.example.model.Curso;
import com.example.model.Disciplina; // <-- IMPORTAR DISCIPLINA
import com.example.model.Horario;
import com.example.model.PeriodoLetivo;
import com.example.model.Professor;
import com.example.model.Turma;
import com.example.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HorarioDAO {

    public void save(Horario horario) throws SQLException {
        // --- SQL ALTERADO (usa id_disciplina) ---
        String sql = "INSERT INTO horarios (id_disciplina, id_turma, id_professor, dia_semana, hora_inicio, hora_fim) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // --- LÓGICA ALTERADA (usa getDisciplina) ---
            pstmt.setInt(1, horario.getDisciplina().getId()); 
            pstmt.setInt(2, horario.getTurma().getId());
            pstmt.setInt(3, horario.getProfessor().getId());
            pstmt.setString(4, horario.getDiaSemana());
            pstmt.setTime(5, Time.valueOf(horario.getHoraInicio() + ":00"));
            pstmt.setTime(6, Time.valueOf(horario.getHoraFim() + ":00"));

            pstmt.executeUpdate();
        }
    }

    public List<Horario> getAll() {
        // --- SQL TOTALMENTE REESCRITO ---
        // Esta query agora busca a DISCIPLINA (d.*) e busca o PERÍODO LETIVO (pl.*)
        // através da TURMA (t.id_periodo_letivo)
        String sql = "SELECT " +
                     "    h.id as horario_id, h.dia_semana, h.hora_inicio, h.hora_fim, " +
                     "    p.id as prof_id, p.nome_completo as prof_nome, p.cpf as prof_cpf, p.data_nascimento as prof_nasc, p.email as prof_email, p.telefone as prof_tel, " +
                     "    t.id as turma_id, t.nome_turma, t.turno, t.salaAula, " +
                     "    d.id as disc_id, d.nome_disciplina, d.carga_horaria, " + // <-- Buscamos a Disciplina
                     "    pl.id as pl_id, pl.nome as pl_nome, pl.data_inicio, pl.data_fim, pl.status, " + // <-- Buscamos o Período via Turma
                     "    c.id as curso_id, c.nome_curso, c.nivel, c.duracao_semestres " +
                     "FROM horarios h " +
                     "LEFT JOIN professores p ON h.id_professor = p.id " +
                     "LEFT JOIN turmas t ON h.id_turma = t.id " +
                     "LEFT JOIN disciplinas d ON h.id_disciplina = d.id " + // <-- JOIN na tabela disciplinas
                     "LEFT JOIN periodos_letivos pl ON t.id_periodo_letivo = pl.id " + // <-- JOIN no período da Turma
                     "LEFT JOIN cursos c ON t.id_curso = c.id";

        List<Horario> horarios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // 1. Monta o objeto Professor
                Professor professor = new Professor(
                    rs.getString("prof_nome"), rs.getString("prof_cpf"),
                    rs.getDate("prof_nasc") != null ? rs.getDate("prof_nasc").toLocalDate() : null,
                    rs.getString("prof_email"), rs.getString("prof_tel")
                );
                professor.setId(rs.getInt("prof_id"));

                // 2. Monta o objeto Curso
                Curso curso = new Curso(rs.getString("nome_curso"), rs.getString("nivel"), rs.getInt("duracao_semestres"));
                curso.setId(rs.getInt("curso_id"));

                // 3. Monta o objeto PeriodoLetivo (CORRIGIDO PARA DATAS NULAS)
                java.sql.Date sqlDataInicio = rs.getDate("data_inicio");
                java.sql.Date sqlDataFim = rs.getDate("data_fim");
                LocalDate dataInicio = (sqlDataInicio != null) ? sqlDataInicio.toLocalDate() : null;
                LocalDate dataFim = (sqlDataFim != null) ? sqlDataFim.toLocalDate() : null;

                PeriodoLetivo periodoLetivo = new PeriodoLetivo(
                    rs.getInt("pl_id"), rs.getString("pl_nome"),
                    dataInicio, dataFim, rs.getString("status")
                );

                // 4. Monta o objeto Turma
                Turma turma = new Turma(
                    rs.getString("nome_turma"), curso, periodoLetivo,
                    rs.getString("turno"), rs.getString("salaAula")
                );
                turma.setId(rs.getInt("turma_id"));

                // --- 5. (NOVO) Monta o objeto Disciplina ---
                Disciplina disciplina = new Disciplina(
                    rs.getString("nome_disciplina"),
                    rs.getInt("carga_horaria")
                );
                disciplina.setId(rs.getInt("disc_id"));


                // --- 6. (ALTERADO) Monta o objeto Horario ---
                Horario horario = new Horario(
                    disciplina, // <-- Passa a disciplina
                    turma, 
                    professor,
                    rs.getString("dia_semana"),
                    rs.getTime("hora_inicio").toString().substring(0, 5),
                    rs.getTime("hora_fim").toString().substring(0, 5)
                );
                horario.setId(rs.getInt("horario_id"));

                horarios.add(horario);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar horários: " + e.getMessage());
            e.printStackTrace();
        }
        return horarios;
    }

    // (Importações e outros métodos da tua classe HorarioDAO)

// --- NOVO MÉTODO ---
/**
 * Busca todos os horários de uma turma específica.
 * @param idTurma O ID da turma.
 * @return Uma lista de Horarios.
 */
public List<Horario> getHorariosByTurmaId(int idTurma) {
    // A query é a mesma do getAll(), mas com um WHERE
    String sql = "SELECT " +
                 "    h.id as horario_id, h.dia_semana, h.hora_inicio, h.hora_fim, " +
                 "    p.id as prof_id, p.nome_completo as prof_nome, p.cpf as prof_cpf, p.data_nascimento as prof_nasc, p.email as prof_email, p.telefone as prof_tel, " +
                 "    t.id as turma_id, t.nome_turma, t.turno, t.salaAula, " +
                 "    d.id as disc_id, d.nome_disciplina, d.carga_horaria, " + 
                 "    pl.id as pl_id, pl.nome as pl_nome, pl.data_inicio, pl.data_fim, pl.status, " + 
                 "    c.id as curso_id, c.nome_curso, c.nivel, c.duracao_semestres " +
                 "FROM horarios h " +
                 "LEFT JOIN professores p ON h.id_professor = p.id " +
                 "LEFT JOIN turmas t ON h.id_turma = t.id " +
                 "LEFT JOIN disciplinas d ON h.id_disciplina = d.id " + 
                 "LEFT JOIN periodos_letivos pl ON t.id_periodo_letivo = pl.id " + 
                 "LEFT JOIN cursos c ON t.id_curso = c.id " +
                 "WHERE h.id_turma = ?"; // <-- Filtro adicionado

    List<Horario> horarios = new ArrayList<>();

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, idTurma); // Define o parâmetro do WHERE
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            // (O código de montagem dos objetos é idêntico ao do getAll())
            Professor professor = new Professor(
                rs.getString("prof_nome"), rs.getString("prof_cpf"),
                rs.getDate("prof_nasc") != null ? rs.getDate("prof_nasc").toLocalDate() : null,
                rs.getString("prof_email"), rs.getString("prof_tel")
            );
            professor.setId(rs.getInt("prof_id"));

            Curso curso = new Curso(rs.getString("nome_curso"), rs.getString("nivel"), rs.getInt("duracao_semestres"));
            curso.setId(rs.getInt("curso_id"));

            java.sql.Date sqlDataInicio = rs.getDate("data_inicio");
            java.sql.Date sqlDataFim = rs.getDate("data_fim");
            LocalDate dataInicio = (sqlDataInicio != null) ? sqlDataInicio.toLocalDate() : null;
            LocalDate dataFim = (sqlDataFim != null) ? sqlDataFim.toLocalDate() : null;

            PeriodoLetivo periodoLetivo = new PeriodoLetivo(
                rs.getInt("pl_id"), rs.getString("pl_nome"),
                dataInicio, dataFim, rs.getString("status")
            );

            Turma turma = new Turma(
                rs.getString("nome_turma"), curso, periodoLetivo,
                rs.getString("turno"), rs.getString("salaAula")
            );
            turma.setId(rs.getInt("turma_id"));

            Disciplina disciplina = new Disciplina(
                rs.getString("nome_disciplina"),
                rs.getInt("carga_horaria")
            );
            disciplina.setId(rs.getInt("disc_id"));

            Horario horario = new Horario(
                disciplina, turma, professor,
                rs.getString("dia_semana"),
                rs.getTime("hora_inicio").toString().substring(0, 5),
                rs.getTime("hora_fim").toString().substring(0, 5)
            );
            horario.setId(rs.getInt("horario_id"));

            horarios.add(horario);
        }
    } catch (SQLException e) {
        System.err.println("Erro ao buscar horários por turma: " + e.getMessage());
        e.printStackTrace();
    }
    return horarios;
}

// (O resto da tua classe HorarioDAO.java continua aqui)

    public void update(Horario horario) throws SQLException {
        // --- SQL ALTERADO (usa id_disciplina) ---
        String sql = "UPDATE horarios SET id_disciplina = ?, id_turma = ?, id_professor = ?, dia_semana = ?, hora_inicio = ?, hora_fim = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // --- LÓGICA ALTERADA (usa getDisciplina) ---
            pstmt.setInt(1, horario.getDisciplina().getId());
            pstmt.setInt(2, horario.getTurma().getId());
            pstmt.setInt(3, horario.getProfessor().getId());
            pstmt.setString(4, horario.getDiaSemana());
            pstmt.setTime(5, Time.valueOf(horario.getHoraInicio() + ":00"));
            pstmt.setTime(6, Time.valueOf(horario.getHoraFim() + ":00"));
            pstmt.setInt(7, horario.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM horarios WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public int countHorariosFiltrados(String termoBusca) {
        // SQL base para contagem com todos os JOINs necessários
        String sqlBase = "SELECT COUNT(h.id) " +
                         "FROM horarios h " +
                         "LEFT JOIN professores p ON h.id_professor = p.id " +
                         "LEFT JOIN turmas t ON h.id_turma = t.id " +
                         "LEFT JOIN disciplinas d ON h.id_disciplina = d.id ";
        
        String termoLike = "%" + termoBusca + "%";
        // Cláusula WHERE baseada na sua lógica de filtro antiga
        String sqlWhere = "WHERE t.nome_turma LIKE ? " +
                          "OR p.nome_completo LIKE ? " +
                          "OR d.nome_disciplina LIKE ? " +
                          "OR h.dia_semana LIKE ? ";

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

    public List<Horario> getHorariosPaginadoEFiltrado(String termoBusca, int pagina, int limitePorPagina) {
        List<Horario> horarios = new ArrayList<>();
        
        // O SQL base é o mesmo do seu método getAll()
        String sqlBase = "SELECT " +
                     "    h.id as horario_id, h.dia_semana, h.hora_inicio, h.hora_fim, " +
                     "    p.id as prof_id, p.nome_completo as prof_nome, p.cpf as prof_cpf, p.data_nascimento as prof_nasc, p.email as prof_email, p.telefone as prof_tel, " +
                     "    t.id as turma_id, t.nome_turma, t.turno, t.salaAula, " +
                     "    d.id as disc_id, d.nome_disciplina, d.carga_horaria, " +
                     "    pl.id as pl_id, pl.nome as pl_nome, pl.data_inicio, pl.data_fim, pl.status, " +
                     "    c.id as curso_id, c.nome_curso, c.nivel, c.duracao_semestres " +
                     "FROM horarios h " +
                     "LEFT JOIN professores p ON h.id_professor = p.id " +
                     "LEFT JOIN turmas t ON h.id_turma = t.id " +
                     "LEFT JOIN disciplinas d ON h.id_disciplina = d.id " +
                     "LEFT JOIN periodos_letivos pl ON t.id_periodo_letivo = pl.id " +
                     "LEFT JOIN cursos c ON t.id_curso = c.id ";
        
        String termoLike = "%" + termoBusca + "%";
        String sqlWhere = "WHERE t.nome_turma LIKE ? " +
                          "OR p.nome_completo LIKE ? " +
                          "OR d.nome_disciplina LIKE ? " +
                          "OR h.dia_semana LIKE ? ";

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
            
            // Este loop é idêntico ao do seu getAll()
            while (rs.next()) {
                // (O código de montagem dos objetos é complexo e idêntico ao 
                // seu getAll(), então vamos reutilizá-lo)
                Professor professor = new Professor(
                    rs.getString("prof_nome"), rs.getString("prof_cpf"),
                    rs.getDate("prof_nasc") != null ? rs.getDate("prof_nasc").toLocalDate() : null,
                    rs.getString("prof_email"), rs.getString("prof_tel")
                );
                professor.setId(rs.getInt("prof_id"));

                Curso curso = new Curso(rs.getString("nome_curso"), rs.getString("nivel"), rs.getInt("duracao_semestres"));
                curso.setId(rs.getInt("curso_id"));

                java.sql.Date sqlDataInicio = rs.getDate("data_inicio");
                java.sql.Date sqlDataFim = rs.getDate("data_fim");
                LocalDate dataInicio = (sqlDataInicio != null) ? sqlDataInicio.toLocalDate() : null;
                LocalDate dataFim = (sqlDataFim != null) ? sqlDataFim.toLocalDate() : null;

                PeriodoLetivo periodoLetivo = new PeriodoLetivo(
                    rs.getInt("pl_id"), rs.getString("pl_nome"),
                    dataInicio, dataFim, rs.getString("status")
                );

                Turma turma = new Turma(
                    rs.getString("nome_turma"), curso, periodoLetivo,
                    rs.getString("turno"), rs.getString("salaAula")
                );
                turma.setId(rs.getInt("turma_id"));

                Disciplina disciplina = new Disciplina(
                    rs.getString("nome_disciplina"),
                    rs.getInt("carga_horaria")
                );
                disciplina.setId(rs.getInt("disc_id"));

                Horario horario = new Horario(
                    disciplina, turma, professor,
                    rs.getString("dia_semana"),
                    rs.getTime("hora_inicio").toString().substring(0, 5),
                    rs.getTime("hora_fim").toString().substring(0, 5)
                );
                horario.setId(rs.getInt("horario_id"));

                horarios.add(horario);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar horários paginados: " + e.getMessage());
            e.printStackTrace();
        }
        return horarios;
    }

    /**
     * Busca os horários de um dia específico da semana.
     * @param diaSemana Ex: "Segunda-feira", "Terça-feira"...
     */
    public List<Horario> getHorariosDoDia(String diaSemana) {
        String sql = "SELECT " +
                     "    h.id as horario_id, h.dia_semana, h.hora_inicio, h.hora_fim, " +
                     "    p.id as prof_id, p.nome_completo as prof_nome, p.cpf as prof_cpf, p.data_nascimento as prof_nasc, p.email as prof_email, p.telefone as prof_tel, " +
                     "    t.id as turma_id, t.nome_turma, t.turno, t.salaAula, " +
                     "    d.id as disc_id, d.nome_disciplina, d.carga_horaria, " + 
                     "    pl.id as pl_id, pl.nome as pl_nome, pl.data_inicio, pl.data_fim, pl.status, " + 
                     "    c.id as curso_id, c.nome_curso, c.nivel, c.duracao_semestres " +
                     "FROM horarios h " +
                     "LEFT JOIN professores p ON h.id_professor = p.id " +
                     "LEFT JOIN turmas t ON h.id_turma = t.id " +
                     "LEFT JOIN disciplinas d ON h.id_disciplina = d.id " + 
                     "LEFT JOIN periodos_letivos pl ON t.id_periodo_letivo = pl.id " + 
                     "LEFT JOIN cursos c ON t.id_curso = c.id " +
                     "WHERE h.dia_semana = ? " +
                     "ORDER BY h.hora_inicio ASC"; // Ordena pela hora

        List<Horario> horarios = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, diaSemana);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // --- REUTILIZAR A LÓGICA DE CRIAÇÃO DE OBJETOS DO SEU getAll() AQUI ---
                // (Para poupar espaço, assumo que você copiará a lógica de instanciação
                // de Professor, Curso, PeriodoLetivo, Turma, Disciplina e Horario do método getAll)
                
                // ... (Copie o bloco 'while' do seu getAll() aqui, mas use a lista 'horarios' local)
                
                // Exemplo resumido da montagem final:
                // Horario horario = new Horario(...);
                // horarios.add(horario);
                
                // --- SE PRECISAR, EU REESCREVO O BLOCO COMPLETO, MAS É IGUAL AO getAll() ---
                // Vou colocar aqui a montagem para garantir que não haja erros:
                 Professor professor = new Professor(
                    rs.getString("prof_nome"), rs.getString("prof_cpf"),
                    rs.getDate("prof_nasc") != null ? rs.getDate("prof_nasc").toLocalDate() : null,
                    rs.getString("prof_email"), rs.getString("prof_tel")
                );
                professor.setId(rs.getInt("prof_id"));

                Curso curso = new Curso(rs.getString("nome_curso"), rs.getString("nivel"), rs.getInt("duracao_semestres"));
                curso.setId(rs.getInt("curso_id"));

                java.sql.Date sqlDataInicio = rs.getDate("data_inicio");
                java.sql.Date sqlDataFim = rs.getDate("data_fim");
                LocalDate dataInicio = (sqlDataInicio != null) ? sqlDataInicio.toLocalDate() : null;
                LocalDate dataFim = (sqlDataFim != null) ? sqlDataFim.toLocalDate() : null;

                PeriodoLetivo periodoLetivo = new PeriodoLetivo(
                    rs.getInt("pl_id"), rs.getString("pl_nome"),
                    dataInicio, dataFim, rs.getString("status")
                );

                Turma turma = new Turma(
                    rs.getString("nome_turma"), curso, periodoLetivo,
                    rs.getString("turno"), rs.getString("salaAula")
                );
                turma.setId(rs.getInt("turma_id"));

                Disciplina disciplina = new Disciplina(
                    rs.getString("nome_disciplina"),
                    rs.getInt("carga_horaria")
                );
                disciplina.setId(rs.getInt("disc_id"));

                Horario horario = new Horario(
                    disciplina, turma, professor,
                    rs.getString("dia_semana"),
                    rs.getTime("hora_inicio").toString().substring(0, 5),
                    rs.getTime("hora_fim").toString().substring(0, 5)
                );
                horario.setId(rs.getInt("horario_id"));
                horarios.add(horario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return horarios;
    }
}