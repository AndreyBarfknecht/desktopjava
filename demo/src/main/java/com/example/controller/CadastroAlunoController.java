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
import javafx.scene.control.Label; // <-- IMPORTA O LABEL

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
    @FXML private Label lblTituloJanela;
    

    // --- DAOs para acesso à base de dados ---
    private AlunoDAO alunoDAO;
    private ResponsavelDAO responsavelDAO;

    // --- Variável para o modo de edição ---
    private Aluno alunoParaEditar;
    private Responsavel responsavelExistente = null;
    
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
        lblTituloJanela.setText("Editar Aluno");

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
        // --- LÓGICA DO RESPONSÁVEL ---
        Responsavel responsavelFinal;

        if (alunoParaEditar != null) {
            // Edição de aluno existente: mantém o responsável atual (ou atualiza os dados dele)
            responsavelFinal = alunoParaEditar.getResponsavel();
            // Atualiza os dados do objeto com o que está na tela
            responsavelFinal.setNomeCompleto(guardianNameField.getText());
            responsavelFinal.setCpf(guardianCpfField.getText());
            responsavelFinal.setTelefone(guardianPhoneField.getText());
            responsavelFinal.setEmail(guardianEmailField.getText());
            responsavelDAO.update(responsavelFinal);
            
        } else {
            // Novo Cadastro de Aluno
            if (responsavelExistente != null) {
                // CASO 1: Responsável JÁ EXISTE no banco (foi buscado pelo botão)
                responsavelFinal = responsavelExistente;
                
                // Opcional: Atualizar os dados do responsável existente caso o usuário tenha mudado o telefone/email na tela
                responsavelFinal.setNomeCompleto(guardianNameField.getText());
                responsavelFinal.setTelefone(guardianPhoneField.getText());
                responsavelFinal.setEmail(guardianEmailField.getText());
                responsavelDAO.update(responsavelFinal);
                
            } else {
                // CASO 2: Responsável NOVO (não existe no banco)
                responsavelFinal = new Responsavel(
                    guardianNameField.getText(), guardianCpfField.getText(),
                    guardianPhoneField.getText(), guardianEmailField.getText()
                );
                int novoId = responsavelDAO.saveAndReturnId(responsavelFinal);
                if (novoId == -1) throw new Exception("Falha ao criar responsável");
                responsavelFinal.setId(novoId);
            }
        }

        // --- LÓGICA DO ALUNO (Praticamente igual, mas usa o responsavelFinal) ---
        if (alunoParaEditar == null) {
            Aluno novoAluno = new Aluno(
                nomeCompletoField.getText(), cpfField.getText(),
                dataNascimentoPicker.getValue(), 
                responsavelFinal, // Usa o responsável correto (novo ou existente)
                telefoneField.getText(),
                emailField.getText()
            );
            alunoDAO.save(novoAluno);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Aluno matriculado com sucesso!");
        } else {
            // Lógica de update do aluno (mantém-se igual)
            alunoParaEditar.setNomeCompleto(nomeCompletoField.getText());
            alunoParaEditar.setCpf(cpfField.getText());
            alunoParaEditar.setDataNascimento(dataNascimentoPicker.getValue());
            alunoParaEditar.setTelefone(telefoneField.getText());
            alunoParaEditar.setEmail(emailField.getText());
            // Se quiser permitir trocar o responsável na edição, faria: alunoParaEditar.setResponsavel(responsavelFinal);
            
            alunoDAO.update(alunoParaEditar);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Dados atualizados com sucesso!");
        }
        fecharJanela();

    } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao salvar: " + e.getMessage());
        e.printStackTrace();
    }
}

    @FXML
    private void onBuscarResponsavel() {
    String cpf = guardianCpfField.getText().replaceAll("\\D", ""); // Remove a máscara para buscar
    
    // Se estiver usando máscara na tela, o getText pode vir formatado (ex: 111.222...), 
    // certifique-se de buscar como está salvo no banco (com ou sem pontuação). 
    // Se no banco tem pontuação, use guardianCpfField.getText().
    
    if (cpf.length() < 11) {
        showAlert(Alert.AlertType.WARNING, "Aviso", "Digite o CPF completo para buscar.");
        return;
    }

    // Busca no banco (tente buscar com formatação e sem, dependendo de como salvou)
    Responsavel encontrado = responsavelDAO.findByCpf(guardianCpfField.getText());

    if (encontrado != null) {
        // SE ENCONTROU: Preenche os campos e guarda o objeto
        responsavelExistente = encontrado;
        
        guardianNameField.setText(encontrado.getNomeCompleto());
        guardianEmailField.setText(encontrado.getEmail());
        guardianPhoneField.setText(encontrado.getTelefone());
        
        showAlert(Alert.AlertType.INFORMATION, "Encontrado", "Responsável já cadastrado! Os dados foram carregados.");
    } else {
        // SE NÃO ENCONTROU: Limpa a variável de controle (mas mantém o CPF digitado)
        responsavelExistente = null;
        // Opcional: Limpar os outros campos para garantir que não há lixo
        guardianNameField.clear();
        guardianEmailField.clear();
        guardianPhoneField.clear();
        
        showAlert(Alert.AlertType.INFORMATION, "Não Encontrado", "Responsável não encontrado. Por favor, preencha os dados para cadastrar um novo.");
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