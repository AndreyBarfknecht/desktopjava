package com.example.controller;

import com.example.SceneNavigator;
import com.example.repository.ProfessorDAO; // Importação necessária

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // ALTERAÇÃO: Importamos a interface Initializable
import javafx.scene.control.Button;
import javafx.scene.control.Label;     // ALTERAÇÃO: Importamos Label
import javafx.scene.layout.VBox;      // ALTERAÇÃO: Importamos VBox

// ALTERAÇÃO: A classe agora implementa Initializable
public class TelaInicialController implements Initializable {

    // --- Botões que já tinhas ---
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

    // --- NOVAS VARIÁVEIS PARA OS ELEMENTOS QUE CRIÁMOS ---
    @FXML private Button consultarProfessoresButton; // O novo botão da barra lateral
    @FXML private VBox professoresWidget;            // O widget clicável
    @FXML private Label professoresCountLabel;       // O número '0' dentro do widget

    private ProfessorDAO professorDAO; // Variável para comunicar com a base de dados

    // ALTERAÇÃO: Este método é executado assim que a janela é carregada
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.professorDAO = new ProfessorDAO();
        atualizarContagemProfessores(); // Chama o método para buscar o número de professores
    }

    // NOVO MÉTODO: Busca os dados e atualiza o widget
    private void atualizarContagemProfessores() {
        try {
            int contagem = professorDAO.getAll().size();
            professoresCountLabel.setText(String.valueOf(contagem));
        } catch (Exception e) {
            professoresCountLabel.setText("!"); // Mostra '!' em caso de erro
            e.printStackTrace();
        }
    }

    // --- Handlers que já tinhas ---
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

    // --- NOVOS MÉTODOS PARA OS NOVOS ELEMENTOS ---
    
    @FXML
    private void handleConsultarProfessoresButton() {
        SceneNavigator.openNewWindow("ConsultaProfessores", "Consulta de Professores");
    }
    
    @FXML
    private void handleConsultarProfessoresWidget() {
        // Apenas chama o outro método para não repetir código
        handleConsultarProfessoresButton();
    }
}