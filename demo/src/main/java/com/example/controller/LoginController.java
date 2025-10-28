package com.example.controller;

import com.example.SceneNavigator; // Precisamos importar o nosso navegador
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    @FXML
    protected void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Lógica de autenticação (exemplo simples)
        if (username.equals("admin") && password.equals("admin")) {
            // Se o login for bem-sucedido, navega para a tela inicial
            System.out.println("Login bem-sucedido!");
            SceneNavigator.switchTo("TelaInicial"); // Usamos o navegador para trocar de tela
        } else {
            // Se falhar, mostra um alerta
            showAlert(Alert.AlertType.ERROR, "Falha no Login", "Usuário ou senha inválidos.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}