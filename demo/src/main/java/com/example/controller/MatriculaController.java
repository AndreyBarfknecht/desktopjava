package com.example.controller;

import com.example.model.Aluno;
import com.example.model.Turma;
import com.example.service.AcademicService;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class MatriculaController implements Initializable {

    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private ListView<Aluno> alunosDisponiveisListView;
    @FXML private ListView<Aluno> alunosMatriculadosListView;
    @FXML private Button matricularButton;
    @FXML private Button desmatricularButton;

    private AcademicService service = AcademicService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        turmaComboBox.setItems(service.getTurmas());

        turmaComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    carregarListasDeAlunos(newValue);
                }
            }
        );
    }

    private void carregarListasDeAlunos(Turma turmaSelecionada) {
        alunosMatriculadosListView.setItems(turmaSelecionada.getAlunosMatriculados());

        ObservableList<Aluno> alunosDisponiveis = service.getAlunos().stream()
                .filter(aluno -> !turmaSelecionada.getAlunosMatriculados().contains(aluno))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        
        alunosDisponiveisListView.setItems(alunosDisponiveis);
    }

    @FXML
    private void matricularAluno() {
        Aluno alunoSelecionado = alunosDisponiveisListView.getSelectionModel().getSelectedItem();
        Turma turmaSelecionada = turmaComboBox.getSelectionModel().getSelectedItem();

        if (alunoSelecionado != null && turmaSelecionada != null) {
            turmaSelecionada.matricular(alunoSelecionado);
            carregarListasDeAlunos(turmaSelecionada);
        }
    }

    @FXML
    private void desmatricularAluno() {
        Aluno alunoSelecionado = alunosMatriculadosListView.getSelectionModel().getSelectedItem();
        Turma turmaSelecionada = turmaComboBox.getSelectionModel().getSelectedItem();

        if (alunoSelecionado != null && turmaSelecionada != null) {
            turmaSelecionada.desmatricular(alunoSelecionado);
            carregarListasDeAlunos(turmaSelecionada);
        }
    }

    @FXML
    private void onCancelar() {
        Stage stage = (Stage) turmaComboBox.getScene().getWindow();
        stage.close();
    }
}