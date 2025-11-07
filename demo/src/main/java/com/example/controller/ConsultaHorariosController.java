package com.example.controller;

import com.example.model.Horario;
import com.example.repository.HorarioDAO;
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

public class ConsultaHorariosController implements Initializable {

    @FXML
    private TableView<Horario> horariosTableView;
    @FXML
    private TextField searchField;

    private HorarioDAO horarioDAO;
    private final ObservableList<Horario> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.horarioDAO = new HorarioDAO();
        carregarHorarios();

        FilteredList<Horario> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(horario -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                if (horario.getTurma().getNome().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (horario.getProfessor().getNomeCompleto().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (horario.getNomeDisciplina().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (horario.getDiaSemana().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        horariosTableView.setItems(filteredData);
    }

    private void carregarHorarios() {
        masterData.clear();
        List<Horario> horariosList = horarioDAO.getAll();
        masterData.addAll(horariosList);
    }

    @FXML
    private void onEditar() {
        Horario horarioSelecionado = horariosTableView.getSelectionModel().getSelectedItem();
        if (horarioSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um horário para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroHorario.fxml"));
            Parent root = loader.load();

            // Pega o controlador da tela de cadastro e passa o objeto para edição
            CadastroHorarioController controller = loader.getController();
            controller.setHorarioParaEdicao(horarioSelecionado);
            
            Stage stage = new Stage();
            stage.setTitle("Editar Horário");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            carregarHorarios(); // Atualiza a tabela
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de cadastro de horário.");
        }
    }

    @FXML
    private void onExcluir() {
        Horario horarioSelecionado = horariosTableView.getSelectionModel().getSelectedItem();
        if (horarioSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um horário para excluir.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja realmente excluir este horário?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar Exclusão");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                horarioDAO.delete(horarioSelecionado.getId());
                masterData.remove(horarioSelecionado);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir o horário.");
            }
        }
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) horariosTableView.getScene().getWindow();
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