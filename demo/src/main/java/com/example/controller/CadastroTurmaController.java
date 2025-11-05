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
    @FXML private ComboBox<Curso> cursoComboBox;
    @FXML private ComboBox<PeriodoLetivo> periodoLetivoComboBox;
    @FXML private ComboBox<String> turnoComboBox;
    @FXML private TextField salaField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    private TurmaDAO turmaDAO; 
    private CursoDAO cursoDAO;
    private PeriodoLetivoDAO periodoLetivoDAO;

    // --- NOVO: Variável para Edição ---
    private Turma turmaParaEditar;

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

    // --- NOVO MÉTODO: Para popular os campos no modo de edição ---
    public void setTurmaParaEdicao(Turma turma) {
        this.turmaParaEditar = turma;
        
        nomeTurmaField.setText(turma.getNome());
        salaField.setText(turma.getSala());
        turnoComboBox.setValue(turma.getTurno());
        salvarButton.setText("Atualizar");

        // Para ComboBoxes de objetos, precisamos encontrar o objeto correspondente na lista
        // (Não podemos simplesmente usar setValue(turma.getCurso()) se a instância for diferente)
        cursoComboBox.getItems().stream()
            .filter(c -> c.getId() == turma.getCurso().getId())
            .findFirst()
            .ifPresent(cursoComboBox::setValue);

        periodoLetivoComboBox.getItems().stream()
            .filter(p -> p.getId() == turma.getPeriodoLetivo().getId())
            .findFirst()
            .ifPresent(periodoLetivoComboBox::setValue);
    }

    @FXML
    private void onSalvar() {
        if (!isTurmaDataValid()) {
            return;
        }

        try {
            if (turmaParaEditar == null) { 
                // --- MODO CRIAÇÃO ---
                Turma novaTurma = new Turma(
                    nomeTurmaField.getText(),
                    cursoComboBox.getValue(),
                    periodoLetivoComboBox.getValue(),
                    turnoComboBox.getValue(),
                    salaField.getText()
                );
                turmaDAO.save(novaTurma);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Turma salva com sucesso!");
            
            } else {
                // --- MODO ATUALIZAÇÃO ---
                turmaParaEditar.setNome(nomeTurmaField.getText());
                turmaParaEditar.setCurso(cursoComboBox.getValue());
                turmaParaEditar.setPeriodoLetivo(periodoLetivoComboBox.getValue());
                turmaParaEditar.setTurno(turnoComboBox.getValue());
                turmaParaEditar.setSala(salaField.getText());
                
                turmaDAO.update(turmaParaEditar);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Turma atualizada com sucesso!");
            }
            
            fecharJanela();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar a turma.");
            e.printStackTrace();
        }
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