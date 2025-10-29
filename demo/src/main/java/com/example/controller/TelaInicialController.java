package com.example.controller;

import com.example.SceneNavigator;
import com.example.repository.ProfessorDAO; 
import com.example.repository.TurmaDAO; // NOVO: Importar o TurmaDAO
import com.example.repository.AlunoDAO; // NOVO: Importar o AlunoDAO

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; 
import javafx.scene.control.Button;
import javafx.scene.control.Label;     
import javafx.scene.layout.VBox;      

public class TelaInicialController implements Initializable {

    // --- Botões da barra lateral (sem alterações) ---
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

    // --- Widgets do Painel (Professores já existia) ---
    @FXML private VBox professoresWidget;            
    @FXML private Label professoresCountLabel;       

    // --- NOVAS VARIÁVEIS FXML PARA OS OUTROS WIDGETS ---
    @FXML private VBox cursosWidget;
    @FXML private Label cursosCountLabel;
    @FXML private VBox turmasWidget;
    @FXML private Label turmasCountLabel;
    @FXML private VBox alunosWidget;
    @FXML private Label alunosCountLabel;

    // --- NOVOS DAOs ---
    private ProfessorDAO professorDAO; 
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa todos os DAOs necessários
        this.professorDAO = new ProfessorDAO();
        this.turmaDAO = new TurmaDAO(); // NOVO
        this.alunoDAO = new AlunoDAO(); // NOVO
        
        // Chama os métodos para carregar os dados
        atualizarContagemProfessores(); 
        atualizarContagemCursos();      // NOVO
        atualizarContagemTurmas();      // NOVO
        atualizarContagemAlunos();      // NOVO
    }

    // --- NOVOS MÉTODOS DE ATUALIZAÇÃO DE CONTAGEM ---

    private void atualizarContagemCursos() {
        // Como não temos um DAO para "Cursos", definimos como 0.
        // (Ver explicação abaixo)
        cursosCountLabel.setText("0");
    }

    private void atualizarContagemTurmas() {
        try {
            // Usa o DAO para buscar todas as turmas e obtém o tamanho da lista
            int contagem = turmaDAO.getAll().size();
            turmasCountLabel.setText(String.valueOf(contagem));
        } catch (Exception e) {
            turmasCountLabel.setText("!"); // Mostra '!' em caso de erro
            e.printStackTrace();
        }
    }

    private void atualizarContagemAlunos() {
        try {
            // Usa o DAO para buscar todos os alunos e obtém o tamanho da lista
            int contagem = alunoDAO.getAll().size();
            alunosCountLabel.setText(String.valueOf(contagem));
        } catch (Exception e) {
            alunosCountLabel.setText("!"); // Mostra '!' em caso de erro
            e.printStackTrace();
        }
    }

    // --- Método de Professores (sem alteração) ---
    private void atualizarContagemProfessores() {
        try {
            int contagem = professorDAO.getAll().size();
            professoresCountLabel.setText(String.valueOf(contagem));
        } catch (Exception e) {
            professoresCountLabel.setText("!"); 
            e.printStackTrace();
        }
    }

    // --- Handlers dos botões (sem alteração) ---
    @FXML
    private void handleCadastroAlunoButton() {
        SceneNavigator.openNewWindow("tela_cadastro_aluno", "Cadastro de Novo Aluno");
    }
    @FXML
    private void handleCadastroProfessorButton() {
        SceneNavigator.openNewWindow("CadastroProfessor", "Cadastro de Novo Professor");
    }
    @FXML
    private void handleCadastroTurmaButton() {
        SceneNavigator.openNewWindow("CadastroTurma", "Cadastro de Nova Turma");
    }
    @FXML
    private void handleCadastroPeriodoLetivoButton() {
        SceneNavigator.openNewWindow("CadastroPeriodoLetivo", "Cadastro de Período Letivo");
    }
    @FXML
    private void handleCadastroHorarioButton() {
        SceneNavigator.openNewWindow("CadastroHorario", "Cadastro de Horário");
    }
    @FXML
    private void handleCadastroUsuarioButton() {
        SceneNavigator.openNewWindow("Register", "Cadastro de Novo Usuário");
    }
    @FXML
    private void handleSairButton() {
        Platform.exit();
    }
    @FXML
    private void handleMatriculaButton() {
        SceneNavigator.openNewWindow("Matricula", "Matrícula de Alunos");
    }
    @FXML
    private void handleRegistroNotasButton() {
        SceneNavigator.openNewWindow("RegistroNotas", "Registro de Notas");
    }
    @FXML
    private void handleConsultarAlunosButton() {
        SceneNavigator.openNewWindow("ConsultaAlunos", "Consulta de Alunos");
    }
    @FXML
    private void handleConsultarProfessoresButton() {
        SceneNavigator.openNewWindow("ConsultaProfessores", "Consulta de Professores");
    }
    
    // --- Handlers dos Widgets Clicáveis ---

    @FXML
    private void handleConsultarProfessoresWidget() {
        handleConsultarProfessoresButton(); // Reutiliza o método do botão
    }
    
    // NOVO MÉTODO: Chamado pelo widget de Alunos
    @FXML
    private void handleConsultarAlunosWidget() {
        handleConsultarAlunosButton(); // Reutiliza o método do botão
    }
}