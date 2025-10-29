package com.example.controller;

import com.example.model.Horario; 
import com.example.model.PeriodoLetivo;
import com.example.model.Professor;
import com.example.model.Turma;
import com.example.repository.PeriodoLetivoDAO; 
import com.example.repository.ProfessorDAO;      
import com.example.repository.TurmaDAO;         
import com.example.repository.HorarioDAO;       
import com.example.repository.TurmaProfessorDAO; 

import java.net.URL;
import java.sql.SQLException; 
import java.util.ResourceBundle;

import javafx.collections.FXCollections; 
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CadastroHorarioController implements Initializable {

    @FXML private ComboBox<PeriodoLetivo> periodoLetivoComboBox;
    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private ComboBox<Professor> professorComboBox;
    @FXML private ComboBox<String> diaSemanaComboBox;
    @FXML private TextField horaInicioField;
    @FXML private TextField horaFimField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    private PeriodoLetivoDAO periodoLetivoDAO;
    private TurmaDAO turmaDAO;
    private ProfessorDAO professorDAO;
    private TurmaProfessorDAO turmaProfessorDAO;
    private HorarioDAO horarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.periodoLetivoDAO = new PeriodoLetivoDAO();
        this.turmaDAO = new TurmaDAO();
        this.professorDAO = new ProfessorDAO();
        this.turmaProfessorDAO = new TurmaProfessorDAO();
        this.horarioDAO = new HorarioDAO();

        periodoLetivoComboBox.setItems(FXCollections.observableArrayList(periodoLetivoDAO.getAll()));
        turmaComboBox.setItems(FXCollections.observableArrayList(turmaDAO.getAll()));
        professorComboBox.setItems(FXCollections.observableArrayList(professorDAO.getAll()));

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

        PeriodoLetivo periodo = periodoLetivoComboBox.getValue();
        Turma turma = turmaComboBox.getValue();
        Professor professor = professorComboBox.getValue();
        String dia = diaSemanaComboBox.getValue();
        String inicio = horaInicioField.getText();
        String fim = horaFimField.getText();

        Horario novoHorario = new Horario(periodo, turma, professor, dia, inicio, fim);

        try {
            turmaProfessorDAO.adicionarProfessorNaTurma(turma.getId(), professor.getId());
            horarioDAO.save(novoHorario);

            System.out.println("Horário e Associação Professor-Turma salvos!");
            showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Horário salvo com sucesso!");
            fecharJanela();

        } catch (SQLException e) {
            System.err.println("Erro ao salvar horário: " + e.getMessage());
            if (e.getErrorCode() == 1062) { 
                showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Este horário já existe (Mesma turma, dia e hora de início).");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar o horário.");
            }
            e.printStackTrace();
        }
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