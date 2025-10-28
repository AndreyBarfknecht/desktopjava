package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtusername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    @FXML
    private void onSalvar(){
        String nome = txtName.getText();
        String email = txtEmail.getText();
        String username = txtusername.getText();
        String password = txtPassword.getText();

        // Validação: Verifica se algum campo essencial está em branco
        if (nome.isBlank() || email.isBlank() || username.isBlank() || password.isBlank()) {
            lblMessage.setText("Erro: Todos os campos são obrigatórios.");
            lblMessage.setTextFill(Color.RED); // Muda a cor do texto para vermelho
            return; // Interrompe a execução do método
        }

        // Lógica de "salvamento" (aqui apenas simulamos)
        System.out.println("Salvando usuário: " + username);

        // Feedback de sucesso para o usuário
        lblMessage.setText("Usuário " + nome + " cadastrado com sucesso!");
        lblMessage.setTextFill(Color.GREEN); // Muda a cor do texto para verde

        // Limpa os campos após o cadastro bem-sucedido
        clearFields();
    }

    private void clearFields() {
        txtName.clear();
        txtEmail.clear();
        txtusername.clear();
        txtPassword.clear();
    }
}
