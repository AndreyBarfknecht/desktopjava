package com.example.controller;

import com.example.model.Disciplina;
import com.example.repository.DisciplinaDAO; // O DAO que criámos
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CadastroDisciplinaController implements Initializable {

    @FXML private TextField nomeDisciplinaField;
    @FXML private TextField cargaHorariaField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    private DisciplinaDAO disciplinaDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.disciplinaDAO = new DisciplinaDAO();

        // Adiciona um listener para permitir apenas números na Carga Horária
        // (Similar ao que fizemos no anoLetivoField em CadastroTurmaController)
        cargaHorariaField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                cargaHorariaField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void onSalvar() {
        if (!isDataValid()) {
            return;
        }

        String nome = nomeDisciplinaField.getText();
        int cargaHoraria = Integer.parseInt(cargaHorariaField.getText());

        Disciplina novaDisciplina = new Disciplina(nome, cargaHoraria);

        try {
            disciplinaDAO.save(novaDisciplina);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Disciplina salva com sucesso!");
            fecharJanela();

        } catch (SQLException e) {
            System.err.println("Erro ao salvar disciplina: " + e.getMessage());
            // Código 1062 é "Entrada Duplicada" (UNIQUE)
            if (e.getErrorCode() == 1062) {
                 showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Já existe uma disciplina com este nome.");
            } else {
                 showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar a disciplina.");
            }
        }
    }

    private boolean isDataValid() {
        if (nomeDisciplinaField.getText().trim().isEmpty() || cargaHorariaField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Todos os campos são obrigatórios.");
            return false;
        }
        try {
            Integer.parseInt(cargaHorariaField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "A carga horária deve ser um número válido.");
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