package com.example.controller;

import com.example.model.Turma;
import com.example.repository.TurmaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

// --- NOVAS IMPORTAÇÕES ---
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;


public class ConsultaTurmasController implements Initializable {

    @FXML
    private TableView<Turma> turmasTableView;
    @FXML
    private TextField searchField;

    private TurmaDAO turmaDAO;
    private final ObservableList<Turma> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.turmaDAO = new TurmaDAO();
        carregarTurmas();

        FilteredList<Turma> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(turma -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                if (turma.getNome().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (turma.getCurso().getNomeCurso().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (turma.getTurno().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (turma.getPeriodoLetivo().getNome().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        turmasTableView.setItems(filteredData);
    }

    private void carregarTurmas() {
        masterData.clear();
        List<Turma> turmasList = turmaDAO.getAll();
        masterData.addAll(turmasList);
    }

    // --- NOVO MÉTODO ONEDITAR ---
    @FXML
    private void onEditar() {
        Turma turmaSelecionada = turmasTableView.getSelectionModel().getSelectedItem();
        if (turmaSelecionada == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma turma para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroTurma.fxml"));
            Parent root = loader.load();

            // Pega o controlador da tela de cadastro
            CadastroTurmaController controller = loader.getController();
            // Envia a turma selecionada para ele
            controller.setTurmaParaEdicao(turmaSelecionada);

            Stage stage = new Stage();
            stage.setTitle("Editar Turma");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // showAndWait() espera a janela de edição fechar
            stage.showAndWait();

            // Atualiza a tabela após a edição
            carregarTurmas(); 
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição de turma.");
        }
    }

    // --- NOVO MÉTODO ONEXCLUIR ---
    @FXML
    private void onExcluir() {
        Turma turmaSelecionada = turmasTableView.getSelectionModel().getSelectedItem();
        if (turmaSelecionada == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma turma para excluir.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir a turma '" + turmaSelecionada.getNome() + "'?");
        alert.setContentText("Atenção: Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                turmaDAO.delete(turmaSelecionada.getId());
                masterData.remove(turmaSelecionada); // Remove da lista na UI
            } catch (SQLException e) {
                // Erro comum: tentar excluir uma turma que tem alunos matriculados
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir a turma. Verifique se ela não está associada a matrículas ou horários.");
            }
        }
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) turmasTableView.getScene().getWindow();
        stage.close();
    }

    // --- NOVO MÉTODO SHOWALERT ---
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}