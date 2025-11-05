package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Curso;
import com.example.repository.CursoDAO;
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

public class ConsultaCursosController implements Initializable {

    @FXML
    private TableView<Curso> cursosTableView;
    @FXML
    private TextField searchField;

    private CursoDAO cursoDAO;
    private final ObservableList<Curso> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.cursoDAO = new CursoDAO();
        carregarCursos();

        FilteredList<Curso> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(curso -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                if (curso.getNomeCurso().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (curso.getNivel().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        cursosTableView.setItems(filteredData);
    }

    private void carregarCursos() {
        masterData.clear();
        List<Curso> cursosList = cursoDAO.getAll();
        masterData.addAll(cursosList);
    }

    @FXML
    private void onEditar() {
        Curso cursoSelecionado = cursosTableView.getSelectionModel().getSelectedItem();
        if (cursoSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um curso para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroCurso.fxml"));
            Parent root = loader.load();

            CadastroCursoController controller = loader.getController();
            controller.setCursoParaEdicao(cursoSelecionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Curso");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            carregarCursos(); // Atualiza a tabela após a edição
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição de curso.");
        }
    }

    @FXML
    private void onExcluir() {
        Curso cursoSelecionado = cursosTableView.getSelectionModel().getSelectedItem();
        if (cursoSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um curso para excluir.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir o curso '" + cursoSelecionado.getNomeCurso() + "'?");
        alert.setContentText("Atenção: Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                cursoDAO.delete(cursoSelecionado.getId());
                masterData.remove(cursoSelecionado); // Remove da lista na UI
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir o curso. Verifique se ele não está associado a turmas ou grades curriculares.");
            }
        }
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) cursosTableView.getScene().getWindow();
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