package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Disciplina;
import com.example.repository.DisciplinaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConsultaDisciplinaController implements Initializable {

    @FXML
    private TableView<Disciplina> disciplinasTableView;
    @FXML
    private TextField searchField;

    private DisciplinaDAO disciplinaDAO;
    private final ObservableList<Disciplina> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.disciplinaDAO = new DisciplinaDAO();
        carregarDisciplinas();

        FilteredList<Disciplina> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(disciplina -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                if (disciplina.getNomeDisciplina().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(disciplina.getCargaHoraria()).contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        disciplinasTableView.setItems(filteredData);
    }

    private void carregarDisciplinas() {
        masterData.clear();
        List<Disciplina> disciplinaList = disciplinaDAO.getAll();
        masterData.addAll(disciplinaList);
    }

    @FXML
    private void onEditar() {
        Disciplina disciplinaSelecionado = disciplinasTableView.getSelectionModel().getSelectedItem();
        if (disciplinaSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma disciplina para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroDisciplina.fxml"));
            Parent root = loader.load();

            CadastroDisciplinaController controller = loader.getController();
            controller.setDisciplinaParaEdicao(disciplinaSelecionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Disciplina");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            carregarDisciplinas(); // Atualiza a tabela após a edição
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição de disciplinas.");
        }
    }

    @FXML
    private void onExcluir() {
        Disciplina disciplinaSelecionado = disciplinasTableView.getSelectionModel().getSelectedItem();
        if (disciplinaSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma disciplina para excluir.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir a disciplina '" + disciplinaSelecionado.getNomeDisciplina() + "'?");
        alert.setContentText("Atenção: Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                disciplinaDAO.delete(disciplinaSelecionado.getId());
                masterData.remove(disciplinaSelecionado); // Remove da lista na UI
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir a disciplina. Verifique se ela não está associada a grades curriculares.");
            }
        }
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) disciplinasTableView.getScene().getWindow();
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