package com.example.controller;

import java.net.URL;
import java.sql.SQLException; 
import java.util.ResourceBundle;

import com.example.model.PeriodoLetivo;
import com.example.repository.PeriodoLetivoDAO; 

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Label; // <-- IMPORTA O LABEL

public class CadastroPeriodoLetivoController implements Initializable {

    @FXML private TextField nomePeriodoField;
    @FXML private DatePicker dataInicioPicker;
    @FXML private DatePicker dataFimPicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;
    @FXML private Label lblTituloJanela;
    private PeriodoLetivoDAO periodoDAO; 

    private PeriodoLetivo periodoParaEditar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusComboBox.getItems().addAll("Aberto para Matrículas", "Em Andamento", "Encerrado");
        this.periodoDAO = new PeriodoLetivoDAO(); 
    }

    public void setPeriodoParaEdicao(PeriodoLetivo periodo) {
        this.periodoParaEditar = periodo;
        lblTituloJanela.setText("Editar Período Letivo");
        nomePeriodoField.setText(periodo.getNome());
        dataInicioPicker.setValue(periodo.getDataInicio());
        dataFimPicker.setValue(periodo.getDataFim());
        statusComboBox.setValue(periodo.getStatus());

        salvarButton.setText("Atualizar");
    }

    @FXML
    private void onSalvar() {
        if (!isDataValid()) {
            return;
        }

        try {
            if (periodoParaEditar == null) {
                // Modo Criação
                PeriodoLetivo novoPeriodo = new PeriodoLetivo(
                    nomePeriodoField.getText(),
                    dataInicioPicker.getValue(),
                    dataFimPicker.getValue(),
                    statusComboBox.getValue()
                );
                periodoDAO.save(novoPeriodo);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Período Letivo salvo com sucesso!");
            } else {
                // Modo Edição
                periodoParaEditar.setNome(nomePeriodoField.getText());
                periodoParaEditar.setDataInicio(dataInicioPicker.getValue());
                periodoParaEditar.setDataFim(dataFimPicker.getValue());
                periodoParaEditar.setStatus(statusComboBox.getValue());
                periodoDAO.update(periodoParaEditar);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Período Letivo atualizado com sucesso!");
            }

            fecharJanela();
            
        } catch (SQLException e) {
            System.err.println("Erro ao salvar período letivo: " + e.getMessage());
            if (e.getErrorCode() == 1062) { 
                 showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Já existe um período letivo com este nome.");
            } else {
                 showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar/atualizar o período.");
            }
        }
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