package com.example.controller;

import com.example.model.Curso;
import com.example.repository.CursoDAO;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CadastroCursoController implements Initializable {

    @FXML private TextField nomeCursoField;
    @FXML private ComboBox<String> nivelComboBox;
    @FXML private TextField duracaoField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    private CursoDAO cursoDAO;
    private Curso cursoParaEditar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.cursoDAO = new CursoDAO();

        // Preenche o ComboBox com as opções de nível
        nivelComboBox.getItems().addAll("Técnico", "Graduação", "Pós-Graduação");

        // Adiciona um listener para permitir apenas números na Duração
        duracaoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                duracaoField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    public void setCursoParaEdicao(Curso curso) {
        this.cursoParaEditar = curso;
        nomeCursoField.setText(curso.getNomeCurso());
        nivelComboBox.setValue(curso.getNivel());
        duracaoField.setText(String.valueOf(curso.getDuracaoSemestres()));
        salvarButton.setText("Atualizar");
    }

    @FXML
    private void onSalvar() {
        if (!isDataValid()) {
            return;
        }

        String nome = nomeCursoField.getText();
        String nivel = nivelComboBox.getValue();
        int duracao = Integer.parseInt(duracaoField.getText());

        try {
            if (cursoParaEditar == null) { // Modo de Criação
                Curso novoCurso = new Curso(nome, nivel, duracao);
                cursoDAO.save(novoCurso);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Curso salvo com sucesso!");
            } else { // Modo de Edição
                cursoParaEditar.setNomeCurso(nome);
                cursoParaEditar.setNivel(nivel);
                cursoParaEditar.setDuracaoSemestres(duracao);
                cursoDAO.update(cursoParaEditar);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Curso atualizado com sucesso!");
            }
            fecharJanela();

        } catch (SQLException e) {
            System.err.println("Erro ao salvar curso: " + e.getMessage());
            if (e.getErrorCode() == 1062) {
                 showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Já existe um curso com este nome.");
            } else {
                 showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar/atualizar o curso.");
            }
        }
    }

    private boolean isDataValid() {
        if (nomeCursoField.getText().trim().isEmpty() || 
            nivelComboBox.getValue() == null ||
            duracaoField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Todos os campos são obrigatórios.");
            return false;
        }
         try {
            Integer.parseInt(duracaoField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "A duração deve ser um número válido.");
            return false;
        }
        return true;
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