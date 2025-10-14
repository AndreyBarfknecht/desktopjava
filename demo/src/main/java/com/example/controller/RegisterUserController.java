package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class RegisterUserController {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    
    // VARIÁVEL NOVA: Ligada ao novo campo FXML
    @FXML private PasswordField repeatPasswordField; 

    @FXML private Label lblMessage;
    @FXML private Button cancelarButton;

    @FXML
    private void onSalvar() {
        String name = txtName.getText();
        String email = txtEmail.getText();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        
        // CÓDIGO NOVO: Obter o texto do campo de repetição
        String repeatPassword = repeatPasswordField.getText();

        // --- VALIDAÇÃO ---

        // 1. Verifica se algum campo está em branco
        if (name.isBlank() || email.isBlank() || username.isBlank() || password.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Todos os campos são obrigatórios.");
            return;
        }

        // 2. LÓGICA NOVA: Verifica se as senhas são iguais
        if (!password.equals(repeatPassword)) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "As senhas não são iguais. Por favor, tente novamente.");
            return; // Interrompe o processo de salvar
        }

        // Se todas as validações passarem, continua com o salvamento
        System.out.println("Utilizador válido! Salvando dados...");
        lblMessage.setText("Utilizador " + name + " registado com sucesso!");
        lblMessage.setTextFill(Color.GREEN);
        
        clearFields();
    }

    @FXML
    private void onCancelar() {
        System.out.println("Ação de cadastro cancelada.");
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.close();
    }

    private void clearFields() {
        txtName.clear();
        txtEmail.clear();
        txtUsername.clear();
        txtPassword.clear();
        repeatPasswordField.clear(); // Limpa o novo campo também
    }
    
    // Método auxiliar para mostrar alertas, como fizemos nos outros controllers
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}