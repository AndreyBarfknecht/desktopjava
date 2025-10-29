package com.example.controller;

import com.example.model.Aluno;
import com.example.model.Matricula;
import com.example.model.Turma;
import com.example.repository.AlunoDAO;
import com.example.repository.MatriculaDAO;
import com.example.repository.TurmaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class MatriculaController implements Initializable {

    @FXML private ComboBox<Aluno> alunoComboBox;
    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private DatePicker dataMatriculaPicker;
    @FXML private Button salvarButton;
    @FXML private TableView<Matricula> matriculasTableView;
    @FXML private Button excluirButton;

    private AlunoDAO alunoDAO;
    private TurmaDAO turmaDAO;
    private MatriculaDAO matriculaDAO;

    private final ObservableList<Matricula> matriculasList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.alunoDAO = new AlunoDAO();
        this.turmaDAO = new TurmaDAO();
        this.matriculaDAO = new MatriculaDAO();

        configurarComboBoxes();
        carregarDados();

        matriculasTableView.setItems(matriculasList);
        dataMatriculaPicker.setValue(LocalDate.now());
    }

    private void configurarComboBoxes() {
        // Configura como o nome do Aluno será exibido no ComboBox
        alunoComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Aluno aluno) {
                return aluno == null ? "" : aluno.getNomeCompleto();
            }
            @Override
            public Aluno fromString(String string) { return null; }
        });

        // Configura como o nome da Turma será exibido no ComboBox
        turmaComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Turma turma) {
                return turma == null ? "" : turma.getNome();
            }
            @Override
            public Turma fromString(String string) { return null; }
        });
    }

    private void carregarDados() {
        // Carrega alunos e turmas nos ComboBoxes
        alunoComboBox.setItems(FXCollections.observableArrayList(alunoDAO.getAll()));
        turmaComboBox.setItems(FXCollections.observableArrayList(turmaDAO.getAll()));

        // Carrega matrículas existentes na tabela
        matriculasList.setAll(matriculaDAO.getAll());
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
            carregarDados(); // Recarrega a lista da tabela
            limparCampos();
        } catch (SQLException e) {
            // Verifica se o erro é de chave única duplicada
            if (e.getErrorCode() == 1062) { // Código de erro para 'duplicate entry' no MariaDB/MySQL
                showAlert(Alert.AlertType.ERROR, "Erro", "Este aluno já está matriculado nesta turma.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Ocorreu um erro ao salvar a matrícula.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onExcluir() {
        Matricula selecionada = matriculasTableView.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Selecione uma matrícula na tabela para excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Deseja realmente excluir a matrícula de " + selecionada.getNomeAluno() + "?");
        Optional<ButtonType> resultado = confirmacao.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            matriculaDAO.delete(selecionada.getId());
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Matrícula excluída.");
            carregarDados(); // Recarrega a lista
        }
    }

    private void limparCampos() {
        alunoComboBox.getSelectionModel().clearSelection();
        turmaComboBox.getSelectionModel().clearSelection();
        dataMatriculaPicker.setValue(LocalDate.now());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}