package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Turma;
import com.example.repository.ProfessorDAO;
import com.example.repository.TurmaDAO;
import com.example.repository.AlunoDAO;
import com.example.repository.CursoDAO;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

// --- NOVAS IMPORTAÇÕES ---
import javafx.animation.PauseTransition;
import javafx.util.Duration;
// --- FIM DAS NOVAS IMPORTAÇÕES ---

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
// --- NOVA IMPORTAÇÃO ---
import javafx.scene.control.ScrollPane;



public class TelaInicialController implements Initializable {

    // --- Botões da barra lateral (Existentes) ---
    @FXML private Button cadastrarAlunoButton;
    @FXML private Button sairButton;
    @FXML private Button consultarHorariosButton;
    @FXML private Button consultarMatriculasButton;
    
    // ... (todos os seus outros botões FXML) ...
    @FXML private Button associarDisciplinaButton;

    // --- @FXML PARA OS GRÁFICOS (Mantidos) ---
    @FXML private PieChart alunosPorCursoChart;
    @FXML private BarChart<String, Number> turmasPorTurnoChart;

    // --- Componentes da Sidebar (Mantidos) ---
    @FXML private Accordion sidebarAccordion;
    @FXML private TitledPane cadastrosPane;

    // --- NOVO @FXML PARA O SCROLLPANE DO MENU ---
    /**
     * Este é o ScrollPane que envolve o Accordion do menu.
     * Vamos controlar a sua largura para expandir e encolher.
     */
    @FXML private ScrollPane sidebarScrollPane;
    
    // --- NOVO: Temporizador para encolher o menu ---
    private PauseTransition collapseTimer;


    // --- DAOs (Mantidos) ---
    private ProfessorDAO professorDAO;
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO;
    private CursoDAO cursoDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa DAOs
        this.professorDAO = new ProfessorDAO();
        this.turmaDAO = new TurmaDAO();
        this.alunoDAO = new AlunoDAO();
        this.cursoDAO = new CursoDAO();
        
        // --- GRÁFICOS (Mantidos) ---
        carregarGraficoAlunosPorCurso();
        carregarGraficoTurmasPorTurno();

        // Garante que o primeiro painel esteja expandido inicialmente
        Platform.runLater(() -> {
            if (sidebarAccordion != null && !sidebarAccordion.getPanes().isEmpty()) {
                sidebarAccordion.setExpandedPane(sidebarAccordion.getPanes().get(0));
            }
        });
        
        // --- LÓGICA NOVA: Configurar o menu retrátil ---
        configurarMenuRetratil();
    }
    
    /**
     * NOVO MÉTODO
     * Configura o temporizador para encolher o menu lateral.
     */
    private void configurarMenuRetratil() {
        // Define o estado inicial como encolhido
        if (sidebarScrollPane != null) {
            sidebarScrollPane.setPrefWidth(70); // Largura encolhida
            if (!sidebarScrollPane.getStyleClass().contains("sidebar-collapsed")) {
                 sidebarScrollPane.getStyleClass().add("sidebar-collapsed");
            }
        }
        
        // Cria um temporizador de 300ms
        collapseTimer = new PauseTransition(Duration.millis(300));
        collapseTimer.setOnFinished(e -> {
            // Esta ação só é executada se o rato AINDA estiver fora do menu
            if (sidebarScrollPane != null && !sidebarScrollPane.isHover()) {
                sidebarScrollPane.setPrefWidth(70); // Largura encolhida
                sidebarScrollPane.getStyleClass().add("sidebar-collapsed");
            }
        });
    }

    /**
     * NOVO MÉTODO
     * Chamado quando o rato entra na área do menu.
     */
    @FXML
    private void handleMenuExpand() {
        // Para qualquer temporizador de encolher que esteja a correr
        collapseTimer.stop();
        
        // Expande o menu
        sidebarScrollPane.setPrefWidth(260); // Largura expandida
        sidebarScrollPane.getStyleClass().remove("sidebar-collapsed");
    }

    /**
     * NOVO MÉTODO
     * Chamado quando o rato sai da área do menu.
     */
    @FXML
    private void handleMenuCollapse() {
        // Inicia a contagem decrescente para encolher o menu
        collapseTimer.playFromStart();
    }


    // --- MÉTODOS DOS GRÁFICOS (Mantidos) ---
    // (O código dos métodos carregarGraficoAlunosPorCurso() e 
    // carregarGraficoTurmasPorTurno() permanece aqui, inalterado)
    
    private void carregarGraficoAlunosPorCurso() {
        try {
            Map<String, Integer> contagem = cursoDAO.getContagemAlunosPorCurso(); //
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(); //
            
            for (Map.Entry<String, Integer> entry : contagem.entrySet()) {
                if (entry.getValue() > 0) {
                    pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }
            
            alunosPorCursoChart.setData(pieChartData); //
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarGraficoTurmasPorTurno() {
        try {
            Map<String, Integer> contagem = new HashMap<>(); //
            contagem.put("Manhã", 0); //
            contagem.put("Tarde", 0); //
            contagem.put("Noite", 0); //

            for (Turma turma : turmaDAO.getAll()) { //
                if (turma.getTurno() != null) { //
                    contagem.merge(turma.getTurno(), 1, Integer::sum); //
                }
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>(); //
            series.getData().add(new XYChart.Data<>("Manhã", contagem.get("Manhã"))); //
            series.getData().add(new XYChart.Data<>("Tarde", contagem.get("Tarde"))); //
            series.getData().add(new XYChart.Data<>("Noite", contagem.get("Noite"))); //
            
            turmasPorTurnoChart.getData().add(series); //
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Handlers dos botões (Mantidos) ---
    // (Todos os seus métodos handleCadastro...() e handleConsultar...() 
    // permanecem aqui, inalterados)
    @FXML private void handleCadastroAlunoButton() { SceneNavigator.openNewWindow("tela_cadastro_aluno", "Cadastro de Novo Aluno"); carregarGraficoAlunosPorCurso(); } //
    @FXML private void handleCadastroProfessorButton() { SceneNavigator.openNewWindow("CadastroProfessor", "Cadastro de Novo Professor"); } //
    @FXML private void handleCadastroCursoButton() { SceneNavigator.openNewWindow("CadastroCurso", "Cadastro de Novo Curso"); carregarGraficoAlunosPorCurso(); } //
    @FXML private void handleCadastroDisciplinaButton() { SceneNavigator.openNewWindow("CadastroDisciplina", "Cadastro de Disciplina"); } //
    @FXML private void handleCadastroTurmaButton() { SceneNavigator.openNewWindow("CadastroTurma", "Cadastro de Nova Turma"); carregarGraficoTurmasPorTurno(); } //
    @FXML private void handleCadastroPeriodoLetivoButton() { SceneNavigator.openNewWindow("CadastroPeriodoLetivo", "Cadastro de Período Letivo"); } //
    @FXML private void handleCadastroHorarioButton() { SceneNavigator.openNewWindow("CadastroHorario", "Cadastro de Horário"); } //
    @FXML private void handleCadastroUsuarioButton() { SceneNavigator.openNewWindow("RegisterUser", "Cadastro de Novo Usuário"); } //
    @FXML private void handleSairButton() { Platform.exit(); } //
    @FXML private void handleMatriculaButton() { SceneNavigator.openNewWindow("Matricula", "Matrícula de Alunos"); carregarGraficoAlunosPorCurso(); } //
    @FXML private void handleGestaoGradeButton() { SceneNavigator.openNewWindow("GestaoGrade", "Gerenciar Grade Curricular"); } //
    
    @FXML private void handleAssociarDisciplinaButton() {
        SceneNavigator.openNewWindow("GestaoProfessorDisciplina", "Associar Disciplinas ao Professor"); //
    }

    @FXML private void handleRegistroNotasButton() { SceneNavigator.openNewWindow("RegistroNotas", "Registro de Notas"); } //
    @FXML private void handleConsultarAlunosButton() { SceneNavigator.openNewWindow("ConsultaAlunos", "Consulta de Alunos"); } //
    @FXML private void handleConsultarCursosButton() { SceneNavigator.openNewWindow("ConsultaCursos", "Consulta de Cursos"); } //
    @FXML private void handleConsultarTurmasButton() { SceneNavigator.openNewWindow("ConsultaTurmas", "Consulta de Turmas"); } //
    @FXML private void handleConsultarProfessoresButton() { SceneNavigator.openNewWindow("ConsultaProfessores", "Consulta de Professores"); } //

    @FXML
    private void handleConsultarPeriodosLetivosButton() {
        SceneNavigator.openNewWindow("ConsultaPeriodosLetivos", "Consulta de Períodos Letivos");
    }

    @FXML
    private void handleConsultarHorariosButton() {
        SceneNavigator.openNewWindow("ConsultaHorarios", "Consulta de Horários");
    }
    @FXML
private void handleConsultarMatriculasButton() {
    SceneNavigator.openNewWindow("ConsultaMatriculas", "Consulta de Matrículas");
}
    
}
