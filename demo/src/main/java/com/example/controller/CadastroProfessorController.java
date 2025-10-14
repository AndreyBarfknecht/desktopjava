package com.example.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.example.model.Professor;
import com.example.service.AcademicService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class CadastroProfessorController implements Initializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    // --- CAMPOS DO FXML ---
    @FXML private TextField nomeCompletoField;
    @FXML private TextField cpfField;
    @FXML private DatePicker dataNascimentoPicker;
    @FXML private TextField emailField;
    @FXML private TextField telefoneField;
    @FXML private TextField disciplinaField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Adiciona máscaras e validações aos campos
        addCpfMask(cpfField);
        addPhoneMask(telefoneField);
        addEmailValidation(emailField);
    }

    @FXML
        private void onSalvar() {
        if (!isProfessorDataValid()) {
            return;
        }

        // Cria um novo objeto Professor
        Professor novoProfessor = new Professor(
            nomeCompletoField.getText(),
            cpfField.getText()
        );

        // Adiciona o professor ao nosso serviço de dados
        AcademicService.getInstance().addProfessor(novoProfessor);

        System.out.println("Professor " + novoProfessor.getNomeCompleto() + " salvo no serviço.");
        
        showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Professor salvo com sucesso!");
        fecharJanela();
    }

    
    private boolean isProfessorDataValid() {
        if (nomeCompletoField.getText().trim().isEmpty() || 
            dataNascimentoPicker.getValue() == null ||
            disciplinaField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Nome, Data de Nascimento e Disciplina são obrigatórios.");
            return false;
        }

        String cpfDigitsOnly = cpfField.getText().replaceAll("\\D", "");
        if (cpfDigitsOnly.length() != 11) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O CPF é obrigatório e deve estar completo.");
            return false;
        }

        String email = emailField.getText();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O formato do e-mail é inválido.");
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
    
    // --- MÉTODOS AUXILIARES (Máscaras, Alertas, etc.) ---
    // (Estes métodos são reutilizados do CadastroAlunoController)

    private void addCpfMask(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            String digitsOnly = newValue.replaceAll("\\D", "");
            if (digitsOnly.length() > 11) digitsOnly = digitsOnly.substring(0, 11);
            
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digitsOnly.length(); i++) {
                formatted.append(digitsOnly.charAt(i));
                if ((i == 2 || i == 5) && i < digitsOnly.length() - 1) formatted.append(".");
                else if (i == 8 && i < digitsOnly.length() - 1) formatted.append("-");
            }
            
            Platform.runLater(() -> {
                textField.setText(formatted.toString());
                textField.positionCaret(formatted.length());
            });
        });
    }

    private void addPhoneMask(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            String digitsOnly = newValue.replaceAll("\\D", "");
            if (digitsOnly.length() > 11) digitsOnly = digitsOnly.substring(0, 11);

            StringBuilder formatted = new StringBuilder();
            if (digitsOnly.length() > 0) formatted.append("(").append(digitsOnly.substring(0, Math.min(2, digitsOnly.length())));
            if (digitsOnly.length() > 2) {
                formatted.append(") ");
                if (digitsOnly.length() <= 10) { // Fixo
                    formatted.append(digitsOnly.substring(2, Math.min(6, digitsOnly.length())));
                    if (digitsOnly.length() > 6) formatted.append("-").append(digitsOnly.substring(6, Math.min(10, digitsOnly.length())));
                } else { // Celular
                    formatted.append(digitsOnly.substring(2, Math.min(7, digitsOnly.length())));
                    if (digitsOnly.length() > 7) formatted.append("-").append(digitsOnly.substring(7, Math.min(11, digitsOnly.length())));
                }
            }
            
            Platform.runLater(() -> {
                textField.setText(formatted.toString());
                textField.positionCaret(formatted.length());
            });
        });
    }
    
    private void addEmailValidation(TextField textField) {
        textField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused) { // Se o campo perdeu o foco
                if (!textField.getText().isEmpty()) {
                    if (EMAIL_PATTERN.matcher(textField.getText()).matches()) {
                        textField.getStyleClass().remove("text-field-error");
                    } else {
                        if (!textField.getStyleClass().contains("text-field-error")) {
                            textField.getStyleClass().add("text-field-error");
                        }
                    }
                } else {
                    textField.getStyleClass().remove("text-field-error");
                }
            }
        });
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}