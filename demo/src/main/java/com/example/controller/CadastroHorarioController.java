package com.example.controller;

import com.example.model.PeriodoLetivo;
import com.example.model.Professor;
import com.example.model.Turma;
import com.example.service.AcademicService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CadastroHorarioController implements Initializable {

    // CAMPOS NOVOS/ATUALIZADOS
    @FXML private ComboBox<PeriodoLetivo> periodoLetivoComboBox;
    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private ComboBox<Professor> professorComboBox;
    
    @FXML private ComboBox<String> diaSemanaComboBox;
    @FXML private TextField horaInicioField;
    @FXML private TextField horaFimField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Pega a instância do nosso serviço de dados
        AcademicService service = AcademicService.getInstance();

        // Preenche as ComboBoxes com os dados do serviço
        periodoLetivoComboBox.setItems(service.getPeriodosLetivos());
        turmaComboBox.setItems(service.getTurmas());
        professorComboBox.setItems(service.getProfessores());

        diaSemanaComboBox.getItems().addAll(
            "Segunda-feira", "Terça-feira", "Quarta-feira", 
            "Quinta-feira", "Sexta-feira", "Sábado"
        );
    }

    @FXML
    private void onSalvar() {
        if (!isDataValid()) {
            return;
        }

        System.out.println("--- Dados do Horário a Salvar ---");
        System.out.println("Período: " + periodoLetivoComboBox.getValue());
        System.out.println("Turma: " + turmaComboBox.getValue());
        System.out.println("Professor: " + professorComboBox.getValue());
        System.out.println("Dia: " + diaSemanaComboBox.getValue());
        System.out.println("Início: " + horaInicioField.getText());
        System.out.println("Fim: " + horaFimField.getText());
        System.out.println("---------------------------------");
        
        showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Horário salvo com sucesso!");
        fecharJanela();
    }

    private boolean isDataValid() {
        if (periodoLetivoComboBox.getValue() == null ||
            turmaComboBox.getValue() == null ||
            professorComboBox.getValue() == null ||
            diaSemanaComboBox.getValue() == null ||
            horaInicioField.getText().trim().isEmpty() ||
            horaFimField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Todos os campos são obrigatórios.");
            return false;
        }
        
        String timeRegex = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        if (!horaInicioField.getText().matches(timeRegex) || !horaFimField.getText().matches(timeRegex)) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O formato da hora deve ser HH:MM (ex: 08:30).");
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