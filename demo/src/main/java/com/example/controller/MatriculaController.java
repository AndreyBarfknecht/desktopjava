package com.example.controller;

import com.example.model.Aluno;
import com.example.model.Matricula;
import com.example.model.Turma;
import com.example.repository.AlunoDAO;
import com.example.repository.MatriculaDAO;
import com.example.repository.TurmaDAO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List; // NOVO
import java.util.ArrayList; // NOVO
import java.util.ResourceBundle;

public class MatriculaController implements Initializable {

    @FXML private ComboBox<Aluno> alunoComboBox;
    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private DatePicker dataMatriculaPicker;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton; 
    @FXML private ComboBox<String> statusComboBox;

    private AlunoDAO alunoDAO;
    private TurmaDAO turmaDAO;
    private MatriculaDAO matriculaDAO;

    private Matricula matriculaParaEditar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.alunoDAO = new AlunoDAO();
        this.turmaDAO = new TurmaDAO();
        this.matriculaDAO = new MatriculaDAO();

        // --- MUDANÇA PRINCIPAL ---
        // Removemos o carregamento de "getAll()"
        // carregarDados(); // Método antigo removido
        
        // Configura os ComboBoxes para o autocompletar
        setupAutocompleteAluno(alunoComboBox);
        setupAutocompleteTurma(turmaComboBox);

        statusComboBox.getItems().addAll("Ativo", "Trancado", "Concluído", "Cancelado");
        dataMatriculaPicker.setValue(LocalDate.now());
    }
    
    /**
     * NOVO: Configura o autocompletar para o ComboBox de Alunos.
     */
    private void setupAutocompleteAluno(ComboBox<Aluno> comboBox) {
        comboBox.setEditable(true);
        
        comboBox.setConverter(new StringConverter<Aluno>() {
            @Override
            public String toString(Aluno object) {
                return object == null ? "" : object.getNomeCompleto();
            }
            @Override
            public Aluno fromString(String string) {
                return comboBox.getItems().stream()
                         .filter(item -> item.getNomeCompleto().equals(string))
                         .findFirst().orElse(null);
            }
        });

        comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                comboBox.setItems(FXCollections.observableArrayList());
                return;
            }

            Aluno selecionado = comboBox.getSelectionModel().getSelectedItem();
            if (selecionado != null && selecionado.toString().equals(newText)) {
                return;
            }

            List<Aluno> sugestoes = alunoDAO.searchByName(newText);
            Aluno itemSelecionado = comboBox.getSelectionModel().getSelectedItem();
            comboBox.setItems(FXCollections.observableArrayList(sugestoes));
            
            if (itemSelecionado != null && sugestoes.contains(itemSelecionado)) {
                comboBox.getSelectionModel().select(itemSelecionado);
            }
            comboBox.show();
        });
    }

    /**
     * NOVO: Configura o autocompletar para o ComboBox de Turmas.
     */
    private void setupAutocompleteTurma(ComboBox<Turma> comboBox) {
        comboBox.setEditable(true);
        
        comboBox.setConverter(new StringConverter<Turma>() {
            @Override
            public String toString(Turma object) {
                return object == null ? "" : object.getNome();
            }
            @Override
            public Turma fromString(String string) {
                return comboBox.getItems().stream()
                         .filter(item -> item.getNome().equals(string))
                         .findFirst().orElse(null);
            }
        });

        comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                comboBox.setItems(FXCollections.observableArrayList());
                return;
            }

            Turma selecionada = comboBox.getSelectionModel().getSelectedItem();
            if (selecionada != null && selecionada.toString().equals(newText)) {
                return;
            }

            List<Turma> sugestoes = turmaDAO.searchByName(newText);
            Turma itemSelecionado = comboBox.getSelectionModel().getSelectedItem();
            comboBox.setItems(FXCollections.observableArrayList(sugestoes));
            
            if (itemSelecionado != null && sugestoes.contains(itemSelecionado)) {
                comboBox.getSelectionModel().select(itemSelecionado);
            }
            comboBox.show();
        });
    }

    /**
     * ATUALIZADO: Chamado pelo GestaoAlunosController para pré-selecionar o aluno.
     */
    public void setAlunoParaMatricular(Aluno aluno) {
        if (alunoComboBox != null && aluno != null) {
            // Adiciona o aluno à lista (que estaria vazia) e seleciona-o
            alunoComboBox.getItems().setAll(aluno);
            alunoComboBox.getSelectionModel().select(aluno);
            
            alunoComboBox.setDisable(true); // Bloqueia a troca
            statusComboBox.setValue("Ativo"); // Define status padrão
        }
    }

    /**
     * ATUALIZADO: Usado pelo ConsultaMatriculasController para o modo de edição.
     */
    public void setMatriculaParaEdicao(Matricula matricula) {
        this.matriculaParaEditar = matricula;
        if (matricula == null) return;

        // Preenche todos os campos
        if (matricula.getAluno() != null) {
            alunoComboBox.getItems().setAll(matricula.getAluno());
            alunoComboBox.getSelectionModel().select(matricula.getAluno());
        }
        if (matricula.getTurma() != null) {
            turmaComboBox.getItems().setAll(matricula.getTurma());
            turmaComboBox.getSelectionModel().select(matricula.getTurma());
        }
        
        dataMatriculaPicker.setValue(matricula.getDataMatricula());
        statusComboBox.setValue(matricula.getStatus());

        // Bloqueia campos que não devem ser editados
        alunoComboBox.setDisable(true);
        turmaComboBox.setDisable(true);
        dataMatriculaPicker.setDisable(true);
        
        // Permite apenas a alteração do Status
        statusComboBox.setDisable(false);
        salvarButton.setText("Atualizar Status");
    }

    // REMOVIDO: carregarDados() não é mais necessário

    @FXML
    private void onSalvar() {
        // --- CORREÇÃO: Usar .getSelectedItem() ---
        Aluno aluno = alunoComboBox.getSelectionModel().getSelectedItem();
        Turma turma = turmaComboBox.getSelectionModel().getSelectedItem();
        LocalDate data = dataMatriculaPicker.getValue();
        String status = statusComboBox.getValue();

        try {
            // --- MODO DE EDIÇÃO ---
            if (matriculaParaEditar != null) {
                if (status == null) {
                    showAlert(Alert.AlertType.WARNING, "Dados Incompletos", "Por favor, selecione um status.");
                    return;
                }
                matriculaDAO.updateStatus(matriculaParaEditar.getId(), status);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Status da matrícula atualizado!");
            
            // --- MODO DE CRIAÇÃO ---
            } else {
                if (aluno == null || turma == null || data == null || status == null) {
                     // Feedback melhor se o utilizador digitou mas não selecionou
                    if (aluno == null && !alunoComboBox.getEditor().getText().isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Aluno inválido. Por favor, selecione um aluno da lista.");
                        return;
                    }
                    if (turma == null && !turmaComboBox.getEditor().getText().isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Turma inválida. Por favor, selecione uma turma da lista.");
                        return;
                    }
                    showAlert(Alert.AlertType.WARNING, "Dados Incompletos", "Todos os campos são obrigatórios.");
                    return;
                }

                Matricula novaMatricula = new Matricula(aluno, turma, data, status);
                matriculaDAO.save(novaMatricula);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Aluno matriculado com sucesso!");
            }
            
            fecharJanela(); // Fecha a janela após salvar ou atualizar
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { 
                showAlert(Alert.AlertType.ERROR, "Erro", "Este aluno já está matriculado nesta turma.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar a matrícula.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onCancelar() {
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}