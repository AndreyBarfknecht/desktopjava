package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Turma; // <-- IMPORTAÇÃO ADICIONADA
import com.example.repository.ProfessorDAO;
import com.example.repository.TurmaDAO;
import com.example.repository.AlunoDAO;
import com.example.repository.CursoDAO; // <-- IMPORTAÇÃO CORRIGIDA

import java.net.URL;
import java.util.HashMap; // <-- NOVA IMPORTAÇÃO
import java.util.Map; // <-- NOVA IMPORTAÇÃO
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections; // <-- NOVA IMPORTAÇÃO
import javafx.collections.ObservableList; // <-- NOVA IMPORTAÇÃO
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart; // <-- NOVA IMPORTAÇÃO
import javafx.scene.chart.PieChart; // <-- NOVA IMPORTAÇÃO
import javafx.scene.chart.XYChart; // <-- NOVA IMPORTAÇÃO
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class TelaInicialController implements Initializable {

    // --- Botões da barra lateral (Existentes) ---
    @FXML private Button cadastrarAlunoButton;
    @FXML private Button sairButton;
    @FXML private Button cadastrarUsuarioButton;
    @FXML private Button cadastrarProfessorButton;
    @FXML private Button cadastrarTurmaButton;
    @FXML private Button registroNotasButton;
    @FXML private Button consultarAlunosButton;
    @FXML private Button cadastrarPeriodoLetivoButton;
    @FXML private Button cadastrarHorarioButton;
    @FXML private Button matriculaButton;
    @FXML private Button consultarProfessoresButton;
    @FXML private Button cadastrarCursoButton;
    @FXML private Button consultarCursosButton;
    @FXML private Button consultarTurmasButton;
    @FXML private Button cadastrarDisciplinaButton;
    @FXML private Button gestaoGradeButton;
    @FXML private Button associarDisciplinaButton;

    // --- Widgets do Painel (Existentes) ---
    @FXML private VBox professoresWidget;
    @FXML private Label professoresCountLabel;
    @FXML private VBox cursosWidget;
    @FXML private Label cursosCountLabel;
    @FXML private VBox turmasWidget;
    @FXML private Label turmasCountLabel;
    @FXML private VBox alunosWidget;
    @FXML private Label alunosCountLabel;
    
    // --- NOVOS @FXML PARA OS GRÁFICOS ---
    @FXML private PieChart alunosPorCursoChart;
    @FXML private BarChart<String, Number> turmasPorTurnoChart;


    // --- Componentes da Sidebar (Existentes) ---
    @FXML private Accordion sidebarAccordion;
    @FXML private TitledPane cadastrosPane;

    // --- DAOs (Existentes) ---
    private ProfessorDAO professorDAO;
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO;
    private CursoDAO cursoDAO; // <-- Tipo corrigido

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa DAOs
        this.professorDAO = new ProfessorDAO();
        this.turmaDAO = new TurmaDAO();
        this.alunoDAO = new AlunoDAO();
        this.cursoDAO = new CursoDAO(); // <-- Instância corrigida

        // Carrega contagens nos widgets
        atualizarContagemProfessores();
        atualizarContagemCursos();
        atualizarContagemTurmas();
        atualizarContagemAlunos();
        
        // --- NOVAS CHAMADAS PARA CARREGAR OS GRÁFICOS ---
        carregarGraficoAlunosPorCurso();
        carregarGraficoTurmasPorTurno();

        // Garante que o primeiro painel esteja expandido inicialmente
        Platform.runLater(() -> {
            if (sidebarAccordion != null && !sidebarAccordion.getPanes().isEmpty()) {
                sidebarAccordion.setExpandedPane(sidebarAccordion.getPanes().get(0));
            }
        });
    }

    // --- Métodos de Contagem (Existentes, sem alteração) ---
    private void atualizarContagemCursos() {
        try {
            int contagem = cursoDAO.getAll().size();
            cursosCountLabel.setText(String.valueOf(contagem));
        } catch (Exception e) {
            cursosCountLabel.setText("!");
            e.printStackTrace();
        }
    }

    private void atualizarContagemTurmas() {
        try {
            int contagem = turmaDAO.getAll().size();
            turmasCountLabel.setText(String.valueOf(contagem));
        } catch (Exception e) {
            turmasCountLabel.setText("!");
            e.printStackTrace();
        }
    }

    private void atualizarContagemAlunos() {
        try {
            int contagem = alunoDAO.getAll().size();
            alunosCountLabel.setText(String.valueOf(contagem));
        } catch (Exception e) {
            alunosCountLabel.setText("!");
            e.printStackTrace();
        }
    }

    private void atualizarContagemProfessores() {
        try {
            int contagem = professorDAO.getAll().size();
            professoresCountLabel.setText(String.valueOf(contagem));
        } catch (Exception e) {
            professoresCountLabel.setText("!");
            e.printStackTrace();
        }
    }
    
    // --- NOVOS MÉTODOS PARA OS GRÁFICOS ---

    /**
     * Busca os dados no CursoDAO e preenche o gráfico de tarte.
     */
    private void carregarGraficoAlunosPorCurso() {
        try {
            Map<String, Integer> contagem = cursoDAO.getContagemAlunosPorCurso();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            for (Map.Entry<String, Integer> entry : contagem.entrySet()) {
                // Adiciona apenas cursos que têm alunos
                if (entry.getValue() > 0) {
                    pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }
            
            alunosPorCursoChart.setData(pieChartData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Busca os dados no TurmaDAO e preenche o gráfico de barras.
     */
    private void carregarGraficoTurmasPorTurno() {
        try {
            Map<String, Integer> contagem = new HashMap<>();
            contagem.put("Manhã", 0);
            contagem.put("Tarde", 0);
            contagem.put("Noite", 0);

            // Processa os dados das turmas
            for (Turma turma : turmaDAO.getAll()) {
                if (turma.getTurno() != null) {
                    contagem.merge(turma.getTurno(), 1, Integer::sum);
                }
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Manhã", contagem.get("Manhã")));
            series.getData().add(new XYChart.Data<>("Tarde", contagem.get("Tarde")));
            series.getData().add(new XYChart.Data<>("Noite", contagem.get("Noite")));
            
            turmasPorTurnoChart.getData().add(series);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // --- Handlers dos botões (Existentes, sem alteração) ---
    @FXML private void handleCadastroAlunoButton() { SceneNavigator.openNewWindow("tela_cadastro_aluno", "Cadastro de Novo Aluno"); atualizarContagemAlunos(); carregarGraficoAlunosPorCurso(); }
    @FXML private void handleCadastroProfessorButton() { SceneNavigator.openNewWindow("CadastroProfessor", "Cadastro de Novo Professor"); atualizarContagemProfessores(); }
    @FXML private void handleCadastroCursoButton() { SceneNavigator.openNewWindow("CadastroCurso", "Cadastro de Novo Curso"); atualizarContagemCursos(); carregarGraficoAlunosPorCurso(); }
    @FXML private void handleCadastroDisciplinaButton() { SceneNavigator.openNewWindow("CadastroDisciplina", "Cadastro de Disciplina"); }
    @FXML private void handleCadastroTurmaButton() { SceneNavigator.openNewWindow("CadastroTurma", "Cadastro de Nova Turma"); atualizarContagemTurmas(); carregarGraficoTurmasPorTurno(); }
    @FXML private void handleCadastroPeriodoLetivoButton() { SceneNavigator.openNewWindow("CadastroPeriodoLetivo", "Cadastro de Período Letivo"); }
    @FXML private void handleCadastroHorarioButton() { SceneNavigator.openNewWindow("CadastroHorario", "Cadastro de Horário"); }
    @FXML private void handleCadastroUsuarioButton() { SceneNavigator.openNewWindow("RegisterUser", "Cadastro de Novo Usuário"); }
    @FXML private void handleSairButton() { Platform.exit(); }
    @FXML private void handleMatriculaButton() { SceneNavigator.openNewWindow("Matricula", "Matrícula de Alunos"); atualizarContagemAlunos(); carregarGraficoAlunosPorCurso(); }
    @FXML private void handleGestaoGradeButton() { SceneNavigator.openNewWindow("GestaoGrade", "Gerenciar Grade Curricular"); }
    
    @FXML private void handleAssociarDisciplinaButton() {
        SceneNavigator.openNewWindow("GestaoProfessorDisciplina", "Associar Disciplinas ao Professor");
    }

    @FXML private void handleRegistroNotasButton() { SceneNavigator.openNewWindow("RegistroNotas", "Registro de Notas"); }
    @FXML private void handleConsultarAlunosButton() { SceneNavigator.openNewWindow("ConsultaAlunos", "Consulta de Alunos"); }
    @FXML private void handleConsultarCursosButton() { SceneNavigator.openNewWindow("ConsultaCursos", "Consulta de Cursos"); }
    @FXML private void handleConsultarTurmasButton() { SceneNavigator.openNewWindow("ConsultaTurmas", "Consulta de Turmas"); }
    @FXML private void handleConsultarProfessoresButton() { SceneNavigator.openNewWindow("ConsultaProfessores", "Consulta de Professores"); }

    // --- Handlers dos Widgets Clicáveis (Existentes, sem alteração) ---
    @FXML private void handleConsultarProfessoresWidget() { handleConsultarProfessoresButton(); }
    @FXML private void handleConsultarAlunosWidget() { handleConsultarAlunosButton(); }
    @FXML private void handleConsultarCursosWidget() { handleConsultarCursosButton(); }
    @FXML private void handleConsultarTurmasWidget() { handleConsultarTurmasButton(); }
}