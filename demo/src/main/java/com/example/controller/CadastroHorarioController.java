package com.example.controller;

import com.example.model.Horario; 
import com.example.model.PeriodoLetivo; // (Vamos remover esta importação)
import com.example.model.Professor;
import com.example.model.Turma;
import com.example.model.Disciplina; // <-- IMPORTAR DISCIPLINA
import com.example.repository.PeriodoLetivoDAO; // (Vamos remover este)
import com.example.repository.ProfessorDAO;      
import com.example.repository.TurmaDAO;         
import com.example.repository.HorarioDAO;       
import com.example.repository.TurmaProfessorDAO; 
import com.example.repository.DisciplinaDAO; // <-- IMPORTAR O NOVO DAO

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

    // --- FXML ALTERADO ---
    @FXML private ComboBox<Disciplina> disciplinaComboBox; // Alterado de PeriodoLetivo
    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private ComboBox<Professor> professorComboBox;
    @FXML private ComboBox<String> diaSemanaComboBox;
    @FXML private TextField horaInicioField;
    @FXML private TextField horaFimField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    // --- DAOs ALTERADOS ---
    private DisciplinaDAO disciplinaDAO; // Alterado
    private TurmaDAO turmaDAO;
    private ProfessorDAO professorDAO;
    private TurmaProfessorDAO turmaProfessorDAO;
    private HorarioDAO horarioDAO;

    private Horario horarioParaEditar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // --- LÓGICA ALTERADA ---
        this.disciplinaDAO = new DisciplinaDAO(); // Alterado
        this.turmaDAO = new TurmaDAO();
        this.professorDAO = new ProfessorDAO();
        this.turmaProfessorDAO = new TurmaProfessorDAO();
        this.horarioDAO = new HorarioDAO();

        // --- PREENCHIMENTO ALTERADO ---
        disciplinaComboBox.setItems(FXCollections.observableArrayList(disciplinaDAO.getAll())); // Alterado
        turmaComboBox.setItems(FXCollections.observableArrayList(turmaDAO.getAll()));
        professorComboBox.setItems(FXCollections.observableArrayList(professorDAO.getAll()));

        diaSemanaComboBox.getItems().addAll(
            "Segunda-feira", "Terça-feira", "Quarta-feira", 
            "Quinta-feira", "Sexta-feira", "Sábado"
        );
    }

    public void setHorarioParaEdicao(Horario horario) {
        this.horarioParaEditar = horario;

        horaInicioField.setText(horario.getHoraInicio());
        horaFimField.setText(horario.getHoraFim());
        diaSemanaComboBox.setValue(horario.getDiaSemana());

        // --- LÓGICA ALTERADA (para Disciplina) ---
        disciplinaComboBox.getItems().stream()
            .filter(d -> d.getId() == horario.getDisciplina().getId())
            .findFirst()
            .ifPresent(disciplinaComboBox::setValue);

        // (Lógica da Turma e Professor continua igual)
        turmaComboBox.getItems().stream()
            .filter(t -> t.getId() == horario.getTurma().getId())
            .findFirst()
            .ifPresent(turmaComboBox::setValue);

        professorComboBox.getItems().stream()
            .filter(p -> p.getId() == horario.getProfessor().getId())
            .findFirst()
            .ifPresent(professorComboBox::setValue);

        salvarButton.setText("Atualizar");
    }

    @FXML
    private void onSalvar() {
        if (!isDataValid()) {
            return;
        }

        // --- LÓGICA ALTERADA ---
        Disciplina disciplina = disciplinaComboBox.getValue(); // Alterado
        Turma turma = turmaComboBox.getValue();
        Professor professor = professorComboBox.getValue();
        String dia = diaSemanaComboBox.getValue();
        String inicio = horaInicioField.getText();
        String fim = horaFimField.getText();

        try {
            if (horarioParaEditar == null) {
                // Modo Criação
                // --- CONSTRUTOR ALTERADO ---
                Horario novoHorario = new Horario(disciplina, turma, professor, dia, inicio, fim);
                
                horarioDAO.save(novoHorario);

                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Horário salvo com sucesso!");
            } else {
                // Modo Edição
                // --- SETTERS ALTERADOS ---
                horarioParaEditar.setDisciplina(disciplina); // Alterado
                horarioParaEditar.setTurma(turma);
                horarioParaEditar.setProfessor(professor);
                horarioParaEditar.setDiaSemana(dia);
                horarioParaEditar.setHoraInicio(inicio);
                horarioParaEditar.setHoraFim(fim);

                horarioDAO.update(horarioParaEditar);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Horário atualizado com sucesso!");
            }

            fecharJanela();

        } catch (SQLException e) {
            System.err.println("Erro ao salvar horário: " + e.getMessage());
            if (e.getErrorCode() == 1062) { 
                showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Conflito de horário detectado. Verifique os dados.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar o horário.");
            }
            e.printStackTrace();
        }
    }

    private boolean isDataValid() {
        // --- VALIDAÇÃO ALTERADA ---
        if (disciplinaComboBox.getValue() == null || // Alterado
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