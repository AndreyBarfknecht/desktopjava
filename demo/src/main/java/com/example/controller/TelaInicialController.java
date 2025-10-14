package com.example.controller;

import com.example.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;


public class TelaInicialController {

    @FXML private Button cadastrarAlunoButton;
    @FXML private Button sairButton;
    @FXML private Button cadastrarUsuarioButton;
    @FXML private Button cadastrarProfessorButton;
    @FXML private Button cadastrarTurmaButton;
    @FXML private Button registroNotasButton;
    @FXML private Button consultarAlunosButton;
    
    
    // --- NOVAS VARIÁVEIS ---
    @FXML private Button cadastrarPeriodoLetivoButton;
    @FXML private Button cadastrarHorarioButton;
    @FXML private Button matriculaButton;

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

    // --- NOVOS MÉTODOS ---
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

}