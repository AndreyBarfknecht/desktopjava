package com.example.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.example.model.Turma; // Importa o modelo
import com.example.repository.TurmaDAO; // Importa o DAO

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CadastroTurmaController implements Initializable {

    // --- CAMPOS DO FXML ---
    @FXML private TextField nomeTurmaField;
    @FXML private TextField anoLetivoField;
    @FXML private ComboBox<String> turnoComboBox;
    @FXML private TextField salaField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    private TurmaDAO turmaDAO; // Adiciona uma instância do DAO

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Preenche o ComboBox com as opções de turno
        turnoComboBox.getItems().addAll("Manhã", "Tarde", "Noite");

        // Adiciona um listener para permitir apenas números no ano letivo
        anoLetivoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                anoLetivoField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        this.turmaDAO = new TurmaDAO(); // Inicializa o DAO
    }

    @FXML
        private void onSalvar() {
        if (!isTurmaDataValid()) {
            return;
        }
        // CORREÇÃO: Passa o valor do turnoComboBox para o construtor
        Turma novaTurma = new Turma(
            nomeTurmaField.getText(),
            anoLetivoField.getText(),
            turnoComboBox.getValue(),
            salaField.getText() // Adiciona o valor do campo sala
        );

        turmaDAO.save(novaTurma); // Usa o DAO para salvar no banco de dados
        System.out.println("Turma " + novaTurma.getNome() + " salva no banco de dados.");

        showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Turma salva com sucesso!");
        fecharJanela();
    }
    
    private boolean isTurmaDataValid() {
        if (nomeTurmaField.getText().trim().isEmpty() || 
            anoLetivoField.getText().trim().isEmpty() ||
            turnoComboBox.getValue() == null ||
            salaField.getText().trim().isEmpty()) { // Adiciona validação para a sala
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Todos os campos são obrigatórios.");
            return false;
        }

        if (anoLetivoField.getText().length() != 4) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O Ano Letivo deve ter 4 dígitos.");
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