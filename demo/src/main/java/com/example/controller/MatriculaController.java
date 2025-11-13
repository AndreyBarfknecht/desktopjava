package com.example.controller;

import com.example.model.Aluno;
import com.example.model.Matricula;
import com.example.model.Turma;
import com.example.repository.AlunoDAO;
import com.example.repository.MatriculaDAO;
import com.example.repository.TurmaDAO;
import javafx.collections.FXCollections;
// REMOVIDO: ObservableList (não precisamos mais da lista da tabela)
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
// REMOVIDO: Optional (não precisamos do alerta de exclusão)
import java.util.ResourceBundle;

public class MatriculaController implements Initializable {

    @FXML private ComboBox<Aluno> alunoComboBox;
    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private DatePicker dataMatriculaPicker;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton; // NOVO

    // --- REMOVIDO ---
    // @FXML private TableView<Matricula> matriculasTableView;
    // @FXML private Button excluirButton;
    // private final ObservableList<Matricula> matriculasList = FXCollections.observableArrayList();

    private AlunoDAO alunoDAO;
    private TurmaDAO turmaDAO;
    private MatriculaDAO matriculaDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.alunoDAO = new AlunoDAO();
        this.turmaDAO = new TurmaDAO();
        this.matriculaDAO = new MatriculaDAO();

        configurarComboBoxes();
        carregarDados();

        // REMOVIDO: matriculasTableView.setItems(matriculasList);
        dataMatriculaPicker.setValue(LocalDate.now());
    }
    
    /**
     * NOVO MÉTODO: Chamado pelo GestaoAlunosController para pré-selecionar o aluno.
     */
    public void setAlunoParaMatricular(Aluno aluno) {
        if (alunoComboBox != null) {
            alunoComboBox.setValue(aluno);
            alunoComboBox.setDisable(true); // Bloqueia a troca
        }
    }

    private void configurarComboBoxes() {
        alunoComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Aluno aluno) {
                return aluno == null ? "" : aluno.getNomeCompleto();
            }
            @Override public Aluno fromString(String string) { return null; }
        });

        turmaComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Turma turma) {
                return turma == null ? "" : turma.getNome();
            }
            @Override public Turma fromString(String string) { return null; }
        });
    }

    private void carregarDados() {
        // Carrega apenas os ComboBoxes
        alunoComboBox.setItems(FXCollections.observableArrayList(alunoDAO.getAll()));
        turmaComboBox.setItems(FXCollections.observableArrayList(turmaDAO.getAll()));
        // REMOVIDO: Carregamento da matriculasList
    }

    @FXML
    private void onSalvar() {
        Aluno aluno = alunoComboBox.getSelectionModel().getSelectedItem();
        Turma turma = turmaComboBox.getSelectionModel().getSelectedItem();
        LocalDate data = dataMatriculaPicker.getValue();

        if (aluno == null || turma == null || data == null) {
            showAlert(Alert.AlertType.WARNING, "Dados Incompletos", "Por favor, selecione um aluno, uma turma e uma data.");
            return;
        }

        Matricula novaMatricula = new Matricula(aluno, turma, data, "Matriculado");

        try {
            matriculaDAO.save(novaMatricula);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Aluno matriculado com sucesso!");
            fecharJanela(); // Fecha a janela após salvar
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { 
                showAlert(Alert.AlertType.ERROR, "Erro", "Este aluno já está matriculado nesta turma.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Ocorreu um erro ao salvar a matrícula.");
                e.printStackTrace();
            }
        }
    }

    // --- REMOVIDO: onExcluir() ---

    // NOVO: Método para o botão Cancelar
    @FXML
    private void onCancelar() {
        fecharJanela();
    }

    // Método `fecharJanela` atualizado para usar `cancelarButton`
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