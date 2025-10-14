package com.example.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.example.model.PeriodoLetivo;
import com.example.service.AcademicService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CadastroPeriodoLetivoController implements Initializable {

    @FXML private TextField nomePeriodoField;
    @FXML private DatePicker dataInicioPicker;
    @FXML private DatePicker dataFimPicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Preenche o ComboBox com as opções de status
        statusComboBox.getItems().addAll("Aberto para Matrículas", "Em Andamento", "Encerrado");
    }

    @FXML
        private void onSalvar() {
        if (!isDataValid()) {
            return;
        }

        PeriodoLetivo novoPeriodo = new PeriodoLetivo(
            nomePeriodoField.getText()
        );

        AcademicService.getInstance().addPeriodoLetivo(novoPeriodo);
        System.out.println("Período Letivo " + novoPeriodo.getNome() + " salvo no serviço.");

        showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Período Letivo salvo com sucesso!");
        fecharJanela();
    }

    private boolean isDataValid() {
        if (nomePeriodoField.getText().trim().isEmpty() || 
            dataInicioPicker.getValue() == null ||
            dataFimPicker.getValue() == null ||
            statusComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Todos os campos são obrigatórios.");
            return false;
        }

        if (dataFimPicker.getValue().isBefore(dataInicioPicker.getValue())) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "A data de fim não pode ser anterior à data de início.");
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