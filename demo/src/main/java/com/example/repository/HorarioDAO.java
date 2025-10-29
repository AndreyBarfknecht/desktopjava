package com.example.repository;

import com.example.model.Horario;
import com.example.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time; 

public class HorarioDAO {

    public void save(Horario horario) throws SQLException {
        String sql = "INSERT INTO horarios (id_periodo_letivo, id_turma, id_professor, dia_semana, hora_inicio, hora_fim) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, horario.getPeriodoLetivo().getId());
            pstmt.setInt(2, horario.getTurma().getId());
            pstmt.setInt(3, horario.getProfessor().getId());
            pstmt.setString(4, horario.getDiaSemana());
            pstmt.setTime(5, Time.valueOf(horario.getHoraInicio() + ":00"));
            pstmt.setTime(6, Time.valueOf(horario.getHoraFim() + ":00"));

            pstmt.executeUpdate();
        }
    }
}