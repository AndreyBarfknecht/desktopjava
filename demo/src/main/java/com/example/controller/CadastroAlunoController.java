package com.example.controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.example.model.Aluno;
import com.example.model.Responsavel;
import com.example.service.AcademicService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CadastroAlunoController implements Initializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    // --- PAINEIS DE NAVEGAÇÃO ---
    @FXML private VBox guardianPane;
    @FXML private VBox studentPane;

    // --- CAMPOS DO RESPONSÁVEL ---
    @FXML private TextField guardianNameField;
    @FXML private TextField guardianCpfField;
    @FXML private TextField guardianEmailField;
    @FXML private TextField guardianPhoneField;

    // --- CAMPOS DO ALUNO ---
    @FXML private TextField nomeCompletoField;
    @FXML private TextField cpfField;
    @FXML private DatePicker dataNascimentoPicker;
    @FXML private TextField emailField;
    @FXML private TextField telefoneField;

    // --- BOTÕES ---
    @FXML private Button salvarButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        guardianPane.setVisible(true);
        studentPane.setVisible(false);

        addCpfMask(cpfField);
        addPhoneMask(telefoneField);
        addEmailValidation(emailField);

        addCpfMask(guardianCpfField);
        addPhoneMask(guardianPhoneField);
        addEmailValidation(guardianEmailField);
    }

    // --- MÉTODOS DE NAVEGAÇÃO ---

    @FXML
    private void onProximo() {
        if (!isGuardianDataValid()) {
            return; 
        }
        guardianPane.setVisible(false);
        studentPane.setVisible(true);
    }

    @FXML
    private void onVoltar() {
        studentPane.setVisible(false);
        guardianPane.setVisible(true);
    }

    // --- LÓGICA PRINCIPAL ---

    @FXML
    private void onSalvar() {
        if (!isStudentDataValid()) {
            return;
        }

        Responsavel responsavel = new Responsavel(guardianNameField.getText(), guardianCpfField.getText(), guardianPhoneField.getText());

        // 2. Cria o objeto Aluno, associando o responsável
        Aluno novoAluno = new Aluno(
            nomeCompletoField.getText(),
            cpfField.getText(),
            dataNascimentoPicker.getValue(),
            responsavel
        );

        // 3. Adiciona o novo aluno ao nosso serviço de dados
        AcademicService.getInstance().addAluno(novoAluno);
        
        System.out.println("Aluno " + novoAluno.getNomeCompleto() + " salvo no serviço.");
        
        showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Aluno e Responsável salvos com sucesso!");
        fecharJanela();
        }
    
    // --- MÉTODOS DE VALIDAÇÃO ---

    private boolean isGuardianDataValid() {
        if (guardianNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O campo 'Nome Completo' do responsável é obrigatório.");
            return false;
        }
        String cpfDigitsOnly = guardianCpfField.getText().replaceAll("\\D", "");
        if (cpfDigitsOnly.length() != 11) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O CPF do responsável está incompleto.");
            return false;
        }
        String phoneDigitsOnly = guardianPhoneField.getText().replaceAll("\\D", "");
        if (phoneDigitsOnly.length() < 10) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O campo 'Telefone' do responsável é obrigatório e deve estar completo.");
            return false;
        }
        return true;
    }

    private boolean isStudentDataValid() {
        if (nomeCompletoField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O campo 'Nome Completo do Aluno' é obrigatório.");
            return false;
        }

        if (dataNascimentoPicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O campo 'Data de Nascimento' do aluno é obrigatório.");
            return false;
        }

        // --- ALTERAÇÃO AQUI ---
        // Validação do CPF do Aluno (agora obrigatório)
        String cpfDigitsOnly = cpfField.getText().replaceAll("\\D", "");
        if (cpfDigitsOnly.length() != 11) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O CPF do aluno é obrigatório e deve estar completo.");
            return false;
        }
        
        String email = emailField.getText();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O formato do e-mail do aluno é inválido.");
            return false;
        }

        String telefoneDigitsOnly = telefoneField.getText().replaceAll("\\D", "");
        if (!telefoneDigitsOnly.isEmpty() && telefoneDigitsOnly.length() < 10) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O Telefone do aluno está incompleto.");
            return false;
        }

        return true;
    }
    
    // --- MÉTODOS AUXILIARES (Máscaras, Alertas, etc.) ---

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
                } else { // Telemóvel
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
            if (wasFocused && !isNowFocused) {
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

    @FXML
    private void onCancelar() {
        System.out.println("Ação de cadastro cancelada.");
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) guardianPane.getScene().getWindow();
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