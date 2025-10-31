package com.example.controller;

import com.example.SceneNavigator; //
import com.example.repository.ProfessorDAO; //
import com.example.repository.TurmaDAO;    //
import com.example.repository.AlunoDAO;    //

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion; // Importa Accordion
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane; // Importa TitledPane
import javafx.scene.layout.VBox;

// REMOVIDOS imports de Animação, Map, Tooltip, ContentDisplay, etc.

public class TelaInicialController implements Initializable {

    // --- Botões da barra lateral ---
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
    @FXML private Button cadastrarDisciplinaButton;
    @FXML private Button gestaoGradeButton; // Botão da Gestão de Grade

    // --- Widgets do Painel ---
    @FXML private VBox professoresWidget;
    @FXML private Label professoresCountLabel;
    @FXML private VBox cursosWidget;
    @FXML private Label cursosCountLabel;
    @FXML private VBox turmasWidget;
    @FXML private Label turmasCountLabel;
    @FXML private VBox alunosWidget;
    @FXML private Label alunosCountLabel;

    // --- Componentes da Sidebar (Simplificado) ---
    @FXML private Accordion sidebarAccordion;
    @FXML private TitledPane cadastrosPane;
    @FXML private TitledPane academicoPane;
    @FXML private TitledPane consultasPane;
    @FXML private TitledPane sistemaPane;
    // REMOVIDOS: sidebarContainer, toggleSidebarButton, toggleIcon

    // --- DAOs ---
    private ProfessorDAO professorDAO;
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO;
    // private CursoDAO cursoDAO; 

    // REMOVIDAS: Variáveis de controle da Sidebar (isSidebarExpanded, width, maps)

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa DAOs
        this.professorDAO = new ProfessorDAO(); //
        this.turmaDAO = new TurmaDAO();       //
        this.alunoDAO = new AlunoDAO();       //
        // this.cursoDAO = new CursoDAO();

        // Carrega contagens nos widgets
        atualizarContagemProfessores();
        atualizarContagemCursos();
        atualizarContagemTurmas();
        atualizarContagemAlunos();

        // Garante que o primeiro painel esteja expandido inicialmente
        Platform.runLater(() -> {
            if (sidebarAccordion != null && !sidebarAccordion.getPanes().isEmpty()) {
                sidebarAccordion.setExpandedPane(sidebarAccordion.getPanes().get(0));
            }
        });
    }

    // REMOVIDOS: storeOriginalTexts(), handleToggleSidebar(), updateSidebarItemsDisplay(), getButtonShortTextFromFXML()

    // --- Métodos de Contagem (Mantidos) ---
    private void atualizarContagemCursos() {
        // TODO: Implementar com CursoDAO quando disponível
        cursosCountLabel.setText("0"); // Placeholder
    }

    private void atualizarContagemTurmas() {
        try {
            int contagem = turmaDAO.getAll().size(); //
            turmasCountLabel.setText(String.valueOf(contagem));
        } catch (Exception e) {
            turmasCountLabel.setText("!");
            e.printStackTrace();
        }
    }

    private void atualizarContagemAlunos() {
        try {
            int contagem = alunoDAO.getAll().size(); //
            alunosCountLabel.setText(String.valueOf(contagem));
        } catch (Exception e) {
            alunosCountLabel.setText("!");
            e.printStackTrace();
        }
    }

    private void atualizarContagemProfessores() {
        try {
            int contagem = professorDAO.getAll().size(); //
            professoresCountLabel.setText(String.valueOf(contagem));
        } catch (Exception e) {
            professoresCountLabel.setText("!");
            e.printStackTrace();
        }
    }

    // --- Handlers dos botões (Mantidos) ---
    @FXML private void handleCadastroAlunoButton() { SceneNavigator.openNewWindow("tela_cadastro_aluno", "Cadastro de Novo Aluno"); atualizarContagemAlunos();} //
    @FXML private void handleCadastroProfessorButton() { SceneNavigator.openNewWindow("CadastroProfessor", "Cadastro de Novo Professor"); atualizarContagemProfessores(); } //
    @FXML private void handleCadastroCursoButton() { SceneNavigator.openNewWindow("CadastroCurso", "Cadastro de Novo Curso"); atualizarContagemCursos(); } //
    @FXML private void handleCadastroDisciplinaButton() { SceneNavigator.openNewWindow("CadastroDisciplina", "Cadastro de Disciplina"); } //
    @FXML private void handleCadastroTurmaButton() { SceneNavigator.openNewWindow("CadastroTurma", "Cadastro de Nova Turma"); atualizarContagemTurmas(); } //
    @FXML private void handleCadastroPeriodoLetivoButton() { SceneNavigator.openNewWindow("CadastroPeriodoLetivo", "Cadastro de Período Letivo"); } //
    @FXML private void handleCadastroHorarioButton() { SceneNavigator.openNewWindow("CadastroHorario", "Cadastro de Horário"); } //
    @FXML private void handleCadastroUsuarioButton() { SceneNavigator.openNewWindow("RegisterUser", "Cadastro de Novo Usuário"); } //
    @FXML private void handleSairButton() { Platform.exit(); }
    @FXML private void handleMatriculaButton() { SceneNavigator.openNewWindow("Matricula", "Matrícula de Alunos"); atualizarContagemAlunos(); } //
    @FXML private void handleGestaoGradeButton() { SceneNavigator.openNewWindow("GestaoGrade", "Gerenciar Grade Curricular"); } //
    @FXML private void handleRegistroNotasButton() { SceneNavigator.openNewWindow("RegistroNotas", "Registro de Notas"); } //
    @FXML private void handleConsultarAlunosButton() { SceneNavigator.openNewWindow("ConsultaAlunos", "Consulta de Alunos"); atualizarContagemAlunos(); } //
    @FXML private void handleConsultarProfessoresButton() { SceneNavigator.openNewWindow("ConsultaProfessores", "Consulta de Professores"); atualizarContagemProfessores(); } //

    // --- Handlers dos Widgets Clicáveis (Mantidos) ---
    @FXML private void handleConsultarProfessoresWidget() { handleConsultarProfessoresButton(); }
    @FXML private void handleConsultarAlunosWidget() { handleConsultarAlunosButton(); }
}