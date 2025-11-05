package com.example.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import com.example.model.Aluno;
import com.example.repository.AlunoDAO;
import com.example.model.Responsavel;
import com.example.repository.ResponsavelDAO;
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

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    // --- CAMPOS FXML ---
    @FXML private VBox guardianPane;
    @FXML private VBox studentPane;
    @FXML private TextField guardianNameField;
    @FXML private TextField guardianCpfField;
    @FXML private TextField guardianEmailField;
    @FXML private TextField guardianPhoneField;
    @FXML private TextField nomeCompletoField;
    @FXML private TextField cpfField;
    @FXML private DatePicker dataNascimentoPicker;
    @FXML private TextField emailField; // Campo para o email do aluno
    @FXML private TextField telefoneField; // Campo para o telefone do aluno
    @FXML private Button salvarButton;

    // --- DAOs para acesso à base de dados ---
    private AlunoDAO alunoDAO;
    private ResponsavelDAO responsavelDAO;

    // --- Variável para o modo de edição ---
    private Aluno alunoParaEditar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        guardianPane.setVisible(true);
        studentPane.setVisible(false);
        this.alunoDAO = new AlunoDAO();
        this.responsavelDAO = new ResponsavelDAO();
        
        // Configura máscaras e validações
        addCpfMask(cpfField);
        addPhoneMask(telefoneField);
        addEmailValidation(emailField);
        addCpfMask(guardianCpfField);
        addPhoneMask(guardianPhoneField);
        addEmailValidation(guardianEmailField);
    }

    // Método chamado para preencher o formulário no modo de edição
    public void setAlunoParaEdicao(Aluno aluno) {
        this.alunoParaEditar = aluno;
        Responsavel responsavel = aluno.getResponsavel();

        // Preenche campos do responsável
        guardianNameField.setText(responsavel.getNomeCompleto());
        guardianCpfField.setText(responsavel.getCpf());
        guardianEmailField.setText(responsavel.getEmail());
        guardianPhoneField.setText(responsavel.getTelefone());

        // Preenche campos do aluno
        nomeCompletoField.setText(aluno.getNomeCompleto());
        cpfField.setText(aluno.getCpf());
        dataNascimentoPicker.setValue(aluno.getDataNascimento());
        telefoneField.setText(aluno.getTelefone());
        emailField.setText(aluno.getEmail());
    
        
        // Nota: Os campos de email e telefone do aluno não estão no modelo,
        // mas preenchemos se eles existirem na UI.
        // Se quiseres guardar estes dados, terias de os adicionar ao modelo Aluno e à tabela 'estudantes'.
    }

    @FXML
    private void onSalvar() {
        if (!isStudentDataValid()) { return; }

        try {
            if (alunoParaEditar == null) { // --- MODO DE CRIAÇÃO ---
                Responsavel responsavel = new Responsavel(
                    guardianNameField.getText(), guardianCpfField.getText(),
                    guardianPhoneField.getText(), guardianEmailField.getText()
                );
                int responsavelId = responsavelDAO.saveAndReturnId(responsavel);
                if (responsavelId == -1) {
                    showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar o responsável.");
                    return;
                }
                responsavel.setId(responsavelId);

                Aluno novoAluno = new Aluno(
                    nomeCompletoField.getText(), cpfField.getText(),
                    dataNascimentoPicker.getValue(), responsavel,
                    telefoneField.getText(),
                    emailField.getText() // Adiciona o telefone do aluno
                );

                alunoDAO.save(novoAluno);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Aluno e Responsável salvos com sucesso!");

            } else { // --- MODO DE EDIÇÃO ---
                Responsavel responsavel = alunoParaEditar.getResponsavel();
                responsavel.setNomeCompleto(guardianNameField.getText());
                responsavel.setCpf(guardianCpfField.getText());
                responsavel.setTelefone(guardianPhoneField.getText());
                responsavel.setEmail(guardianEmailField.getText());
                responsavelDAO.update(responsavel);

                alunoParaEditar.setNomeCompleto(nomeCompletoField.getText());
                alunoParaEditar.setCpf(cpfField.getText());
                alunoParaEditar.setDataNascimento(dataNascimentoPicker.getValue());
                alunoDAO.update(alunoParaEditar);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Aluno e Responsável atualizados com sucesso!");
            }
            fecharJanela();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar os dados.");
            e.printStackTrace();
        }
    }

    // ... (Todos os outros métodos como onProximo, onVoltar, validações, máscaras, etc. permanecem aqui sem alterações)
    @FXML
    private void onProximo() {
        if (!isGuardianDataValid()) { return; }
        guardianPane.setVisible(false);
        studentPane.setVisible(true);
    }

    @FXML
    private void onVoltar() {
        studentPane.setVisible(false);
        guardianPane.setVisible(true);
    }
    
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