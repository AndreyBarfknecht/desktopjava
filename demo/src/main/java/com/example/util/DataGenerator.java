package com.example.util;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Ferramenta auxiliar para gerar dados fictícios REALISTAS em massa E RELACIONADOS.
 * Para executar, clique com o botão direito neste ficheiro e selecione "Run".
 */
public class DataGenerator {

    private static Random random = new Random();

    // --- Podes mudar estes intervalos! ---
    private static final int TOTAL_CURSOS = randomBetween(450, 550);
    private static final int TOTAL_DISCIPLINAS = randomBetween(950, 1050);
    private static final int TOTAL_PROFESSORES = randomBetween(180, 220);
    private static final int TOTAL_PERIODOS = 5; // 5 é um bom número (ex: 2023/1 a 2025/1)
    private static final int TOTAL_ALUNOS_E_RESPONSAVEIS = randomBetween(1800, 2200);
    private static final int TOTAL_TURMAS = randomBetween(90, 110);
    private static final int TOTAL_MATRICULAS = randomBetween(4500, 5500);
    private static final int TOTAL_HORARIOS = randomBetween(900, 1100);
    private static final int TOTAL_NOTAS = randomBetween(4500, 5500);
    // ---

    // Listas para gerar nomes realistas
    private static final String[] NOMES = {"Miguel", "Arthur", "Gael", "Heitor", "Theo", "Davi", "Gabriel", "Bernardo", "Samuel", "João", "Helena", "Alice", "Laura", "Maria", "Valentina", "Heloísa", "Maite", "Júlia", "Sophia", "Isabella"};
    private static final String[] SOBRENOMES = {"Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima", "Gomes", "Costa", "Ribeiro", "Martins", "Carvalho", "Almeida", "Lopes", "Dias"};
    private static final String[] CURSOS = {"Análise e Desenv. de Sistemas", "Engenharia de Software", "Ciência da Computação", "Redes de Computadores", "Ciência de Dados", "Administração", "Direito", "Medicina", "Engenharia Civil", "Psicologia", "Arquitetura"};
    private static final String[] DISCIPLINAS = {"Cálculo I", "Algoritmos", "Banco de Dados", "Programação Orientada a Objetos", "Sistemas Operacionais", "Inteligência Artificial", "Direito Constitucional", "Anatomia Humana", "Gestão de Projetos", "Marketing Digital"};

    // Listas para guardar os IDs gerados
    private static List<Integer> idsCursos = new ArrayList<>();
    private static List<Integer> idsDisciplinas = new ArrayList<>();
    private static List<Integer> idsProfessores = new ArrayList<>();
    private static List<Integer> idsPeriodos = new ArrayList<>();
    private static List<Integer> idsAlunos = new ArrayList<>();
    private static List<Integer> idsTurmas = new ArrayList<>();
    private static List<Integer> idsMatriculas = new ArrayList<>();

    // Função auxiliar para gerar números "sortidos"
    private static int randomBetween(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    // Função auxiliar para gerar nomes
    private static String getNomeAleatorio() {
        return NOMES[random.nextInt(NOMES.length)] + " " + SOBRENOMES[random.nextInt(SOBRENOMES.length)];
    }


    public static void main(String[] args) {
        System.out.println("--- Iniciando Gerador de Dados Fictícios REALISTAS ---");
        
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Erro: Driver MariaDB não encontrado!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Falha ao conectar à base de dados!");
                return;
            }
            
            conn.setAutoCommit(false);
            System.out.println("Conectado! A inserir dados... (Isto pode demorar um pouco)");

            generateCursos(conn);
            generateDisciplinas(conn);
            generateProfessores(conn);
            generatePeriodosLetivos(conn);
            generateResponsaveisEAlunos(conn);
            generateTurmas(conn);
            generateMatriculas(conn);
            generateHorarios(conn);
            generateNotas(conn);
            
            conn.commit();
            System.out.println("--- SUCESSO! ---");
            System.out.println("Dados fictícios inseridos:");
            System.out.println("Total Cursos: " + idsCursos.size());
            System.out.println("Total Professores: " + idsProfessores.size());
            System.out.println("Total Alunos: " + idsAlunos.size());
            System.out.println("Total Turmas: " + idsTurmas.size());
            System.out.println("Total Matrículas: " + idsMatriculas.size());
            System.out.println("Total Horários: " + TOTAL_HORARIOS);
            System.out.println("Total Notas: " + TOTAL_NOTAS);
            System.out.println("Podes agora fechar este processo e executar a aplicação principal (App.java).");

        } catch (SQLException e) {
            System.err.println("Erro de SQL durante a inserção em massa.");
            e.printStackTrace();
        }
    }

    // --- MÉTODOS GERADORES (Atualizados) ---

    private static void generateCursos(Connection conn) throws SQLException {
        System.out.println("A inserir " + TOTAL_CURSOS + " cursos...");
        String sql = "INSERT INTO cursos (nome_curso, nivel, duracao_semestres) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < TOTAL_CURSOS; i++) {
                pstmt.setString(1, CURSOS[random.nextInt(CURSOS.length)] + " Fictício " + i);
                pstmt.setString(2, "Graduação");
                pstmt.setInt(3, random.nextInt(4) + 6);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                while (rs.next()) idsCursos.add(rs.getInt(1));
            }
        }
    }

    private static void generateDisciplinas(Connection conn) throws SQLException {
        System.out.println("A inserir " + TOTAL_DISCIPLINAS + " disciplinas...");
        String sql = "INSERT INTO disciplinas (nome_disciplina, carga_horaria) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < TOTAL_DISCIPLINAS; i++) {
                pstmt.setString(1, DISCIPLINAS[random.nextInt(DISCIPLINAS.length)] + " Fictício " + i);
                pstmt.setInt(2, (random.nextInt(3) + 1) * 40);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                while (rs.next()) idsDisciplinas.add(rs.getInt(1));
            }
        }
    }

    private static void generateProfessores(Connection conn) throws SQLException {
        System.out.println("A inserir " + TOTAL_PROFESSORES + " professores...");
        String sql = "INSERT INTO professores (nome_completo, cpf, data_nascimento, email, telefone) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < TOTAL_PROFESSORES; i++) {
                pstmt.setString(1, "Prof. " + getNomeAleatorio());
                pstmt.setString(2, "111.222." + String.format("%03d-%02d", i / 100, i % 100)); // CPF Único
                pstmt.setDate(3, Date.valueOf(LocalDate.of(1980, 1, 1).plusDays(random.nextInt(10000))));
                pstmt.setString(4, "professor" + i + "@teste.com");
                pstmt.setString(5, "(55) 99999-" + String.format("%04d", i));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                while (rs.next()) idsProfessores.add(rs.getInt(1));
            }
        }
    }
    
    private static void generatePeriodosLetivos(Connection conn) throws SQLException {
        System.out.println("A inserir " + TOTAL_PERIODOS + " períodos letivos...");
        String sql = "INSERT INTO periodos_letivos (nome, data_inicio, data_fim, status) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < TOTAL_PERIODOS; i++) {
                int ano = 2023 + (i / 2);
                int semestre = (i % 2) + 1;
                pstmt.setString(1, ano + "/" + semestre);
                pstmt.setDate(2, Date.valueOf(LocalDate.of(ano, (semestre == 1 ? 2 : 7), 1)));
                pstmt.setDate(3, Date.valueOf(LocalDate.of(ano, (semestre == 1 ? 6 : 12), 15)));
                pstmt.setString(4, (i == TOTAL_PERIODOS - 1) ? "Ativo" : "Concluído"); // Só o último é ativo
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                while (rs.next()) idsPeriodos.add(rs.getInt(1));
            }
        }
    }

    private static void generateResponsaveisEAlunos(Connection conn) throws SQLException {
        System.out.println("A inserir " + TOTAL_ALUNOS_E_RESPONSAVEIS + " responsáveis e alunos...");
        String sqlResp = "INSERT INTO responsaveis (nome_completo, cpf, email, telefone) VALUES (?, ?, ?, ?)";
        String sqlAluno = "INSERT INTO estudantes (nome_completo, cpf, data_nascimento, id_responsavel, email, telefone) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmtResp = conn.prepareStatement(sqlResp, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmtAluno = conn.prepareStatement(sqlAluno, Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < TOTAL_ALUNOS_E_RESPONSAVEIS; i++) {
                
                String nomeResponsavel = "Resp. " + getNomeAleatorio();
                pstmtResp.setString(1, nomeResponsavel);
                pstmtResp.setString(2, "444.555." + String.format("%03d-%02d", i / 100, i % 100)); // CPF Único
                pstmtResp.setString(3, "responsavel" + i + "@teste.com");
                pstmtResp.setString(4, "(55) 88888-" + String.format("%04d", i));
                pstmtResp.executeUpdate();

                int responsavelId = -1;
                try (ResultSet rs = pstmtResp.getGeneratedKeys()) {
                    if (rs.next()) responsavelId = rs.getInt(1);
                }

                if (responsavelId == -1) throw new SQLException("Falha ao obter ID do responsável.");

                pstmtAluno.setString(1, getNomeAleatorio()); // Nome real de aluno
                pstmtAluno.setString(2, "777.888." + String.format("%03d-%02d", i / 100, i % 100)); // CPF Único
                pstmtAluno.setDate(3, Date.valueOf(LocalDate.of(2000, 1, 1).plusDays(random.nextInt(1000))));
                pstmtAluno.setInt(4, responsavelId);
                pstmtAluno.setString(5, "aluno" + i + "@teste.com");
                pstmtAluno.setString(6, "(55) 77777-" + String.format("%04d", i));
                pstmtAluno.executeUpdate();
                
                try (ResultSet rs = pstmtAluno.getGeneratedKeys()) {
                    if (rs.next()) idsAlunos.add(rs.getInt(1));
                }
            }
        }
    }
    
    private static void generateTurmas(Connection conn) throws SQLException {
        System.out.println("A inserir " + TOTAL_TURMAS + " turmas...");
        String sql = "INSERT INTO turmas (id_curso, id_periodo_letivo, nome_turma, turno, salaAula) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String[] turnos = {"Manhã", "Tarde", "Noite"};
            for (int i = 0; i < TOTAL_TURMAS; i++) {
                pstmt.setInt(1, idsCursos.get(random.nextInt(idsCursos.size())));
                pstmt.setInt(2, idsPeriodos.get(random.nextInt(idsPeriodos.size())));
                pstmt.setString(3, "Turma " + (2020 + i)); // Nome da Turma
                pstmt.setString(4, turnos[random.nextInt(turnos.length)]);
                pstmt.setString(5, "Sala " + (100 + i));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                while (rs.next()) idsTurmas.add(rs.getInt(1));
            }
        }
    }
    
    private static void generateMatriculas(Connection conn) throws SQLException {
        System.out.println("A inserir " + TOTAL_MATRICULAS + " matrículas (com tratamento de duplicados)...");
        String sql = "INSERT INTO matriculas (id_aluno, id_turma, data_matricula, status) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE status=status";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < TOTAL_MATRICULAS; i++) {
                pstmt.setInt(1, idsAlunos.get(random.nextInt(idsAlunos.size())));
                pstmt.setInt(2, idsTurmas.get(random.nextInt(idsTurmas.size())));
                pstmt.setDate(3, Date.valueOf(LocalDate.of(2024, 1, 1).plusDays(random.nextInt(365))));
                pstmt.setString(4, "Ativo");
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    // SÓ ADICIONA SE FOR UM ID VÁLIDO (maior que 0)
                    if (id > 0) {
                        idsMatriculas.add(id);
                    }
                }
            }
        }
    }
    
    private static void generateHorarios(Connection conn) throws SQLException {
        System.out.println("A inserir " + TOTAL_HORARIOS + " horários (com tratamento de duplicados)...");
        String sql = "INSERT INTO horarios (id_disciplina, id_turma, id_professor, dia_semana, hora_inicio, hora_fim) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE id_professor=id_professor";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[] dias = {"Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira"};
            String[] inicios = {"08:00", "10:00", "13:30", "15:30", "19:00", "21:00"};
            String[] fins = {"10:00", "12:00", "15:30", "17:30", "21:00", "23:00"};
            
            for (int i = 0; i < TOTAL_HORARIOS; i++) {
                int randIndex = random.nextInt(dias.length);
                pstmt.setInt(1, idsDisciplinas.get(random.nextInt(idsDisciplinas.size())));
                pstmt.setInt(2, idsTurmas.get(random.nextInt(idsTurmas.size())));
                pstmt.setInt(3, idsProfessores.get(random.nextInt(idsProfessores.size())));
                pstmt.setString(4, dias[randIndex]);
                pstmt.setString(5, inicios[randIndex % inicios.length]);
                pstmt.setString(6, fins[randIndex % fins.length]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    
    private static void generateNotas(Connection conn) throws SQLException {
        System.out.println("A inserir " + TOTAL_NOTAS + " notas...");
        String sql = "INSERT INTO notas (id_matricula, id_disciplina, valor, avaliacao) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < TOTAL_NOTAS; i++) {
                if (idsMatriculas.isEmpty()) {
                    System.out.println("AVISO: Nenhuma matrícula criada, impossível gerar notas.");
                    break;
                }
                
                pstmt.setInt(1, idsMatriculas.get(random.nextInt(idsMatriculas.size())));
                pstmt.setInt(2, idsDisciplinas.get(random.nextInt(idsDisciplinas.size())));
                pstmt.setDouble(3, (random.nextInt(100) + 1) / 10.0); // Nota de 0.1 a 10.0
                pstmt.setString(4, "Prova " + (random.nextInt(2) + 1));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
}