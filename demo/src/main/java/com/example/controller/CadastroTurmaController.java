package com.example.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.example.model.Curso;
import com.example.model.PeriodoLetivo;
import com.example.model.Turma; // Importa o modelo
import com.example.repository.CursoDAO;
import com.example.repository.PeriodoLetivoDAO;
import com.example.repository.TurmaDAO; // Importa o DAO

import javafx.collections.FXCollections;
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
    // --- CAMPOS ALTERADOS ---
    @FXML private ComboBox<Curso> cursoComboBox;
    @FXML private ComboBox<PeriodoLetivo> periodoLetivoComboBox;
    @FXML private ComboBox<String> turnoComboBox;
    @FXML private TextField salaField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    private TurmaDAO turmaDAO; // Adiciona uma instância do DAO
    // --- DAOs ADICIONAIS ---
    private CursoDAO cursoDAO;
    private PeriodoLetivoDAO periodoLetivoDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa os DAOs
        this.turmaDAO = new TurmaDAO();
        this.cursoDAO = new CursoDAO();
        this.periodoLetivoDAO = new PeriodoLetivoDAO();

        // Preenche o ComboBox com as opções de turno
        turnoComboBox.getItems().addAll("Manhã", "Tarde", "Noite");

        // Carrega os dados dos cursos e períodos letivos nos ComboBoxes
        cursoComboBox.setItems(FXCollections.observableArrayList(cursoDAO.getAll()));
        periodoLetivoComboBox.setItems(FXCollections.observableArrayList(periodoLetivoDAO.getAll()));
    }

    @FXML
        private void onSalvar() {
        if (!isTurmaDataValid()) {
            return;
        }

        Turma novaTurma = new Turma(
            nomeTurmaField.getText(),
            cursoComboBox.getValue(),
            periodoLetivoComboBox.getValue(),
            turnoComboBox.getValue(),
            salaField.getText()
        );

        try {
            turmaDAO.save(novaTurma); // Usa o DAO para salvar no banco de dados
            System.out.println("Turma " + novaTurma.getNome() + " salva no banco de dados.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar a turma.");
            e.printStackTrace();
        }

        showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Turma salva com sucesso!");
        fecharJanela();
    }
    
    private boolean isTurmaDataValid() {
        if (nomeTurmaField.getText().trim().isEmpty() || 
            cursoComboBox.getValue() == null ||
            periodoLetivoComboBox.getValue() == null ||
            turnoComboBox.getValue() == null ||
            salaField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Todos os campos são obrigatórios.");
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