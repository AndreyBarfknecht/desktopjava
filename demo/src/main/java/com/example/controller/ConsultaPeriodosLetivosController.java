package com.example.controller;

import com.example.model.PeriodoLetivo;
import com.example.repository.PeriodoLetivoDAO;
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

public class ConsultaPeriodosLetivosController implements Initializable {

    @FXML
    private TableView<PeriodoLetivo> periodosTableView;
    @FXML
    private TextField searchField;

    private PeriodoLetivoDAO periodoLetivoDAO;
    private final ObservableList<PeriodoLetivo> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.periodoLetivoDAO = new PeriodoLetivoDAO();
        carregarPeriodos();

        FilteredList<PeriodoLetivo> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(periodo -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (periodo.getNome().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return periodo.getStatus().toLowerCase().contains(lowerCaseFilter);
            });
        });

        periodosTableView.setItems(filteredData);
    }

    private void carregarPeriodos() {
        masterData.clear();
        List<PeriodoLetivo> periodosList = periodoLetivoDAO.getAll();
        masterData.addAll(periodosList);
    }

    @FXML
    private void onEditar() {
        PeriodoLetivo periodoSelecionado = periodosTableView.getSelectionModel().getSelectedItem();
        if (periodoSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um período para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroPeriodoLetivo.fxml"));
            Parent root = loader.load();

            CadastroPeriodoLetivoController controller = loader.getController();
            controller.setPeriodoParaEdicao(periodoSelecionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Período Letivo");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            carregarPeriodos(); // Atualiza a tabela
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

    @FXML
    private void onExcluir() {
        PeriodoLetivo periodoSelecionado = periodosTableView.getSelectionModel().getSelectedItem();
        if (periodoSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um período para excluir.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja realmente excluir o período '" + periodoSelecionado.getNome() + "'?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar Exclusão");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                periodoLetivoDAO.delete(periodoSelecionado.getId());
                masterData.remove(periodoSelecionado);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir o período. Verifique se ele não está associado a turmas.");
            }
        }
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) periodosTableView.getScene().getWindow();
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
