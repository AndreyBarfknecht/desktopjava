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
}