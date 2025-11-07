package com.example.controller;

import com.example.model.Matricula;
import com.example.repository.MatriculaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConsultaMatriculasController implements Initializable {

    @FXML
    private TableView<Matricula> matriculasTableView;
    @FXML
    private TextField searchField;

    private MatriculaDAO matriculaDAO;
    private final ObservableList<Matricula> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.matriculaDAO = new MatriculaDAO();
        carregarMatriculas();

        // 1. Configura a lista filtrada para a pesquisa
        FilteredList<Matricula> filteredData = new FilteredList<>(masterData, p -> true);

        // 2. Adiciona o listener no campo de pesquisa
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(matricula -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Mostra tudo se a barra estiver vazia
                }
                String lowerCaseFilter = newValue.toLowerCase();

                if (matricula.getNomeAluno().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filtra por nome do aluno
                } else if (matricula.getNomeTurma().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filtra por nome da turma
                } else if (matricula.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filtra por status
                }
                return false; // Não encontrou
            });
        });

        // 3. Liga a tabela à lista filtrada
        matriculasTableView.setItems(filteredData);
    }

    /**
     * Busca os dados mais recentes do banco de dados e atualiza a lista masterData.
     */
    private void carregarMatriculas() {
        masterData.clear();
        List<Matricula> matriculasList = matriculaDAO.getAll(); // Usa o DAO existente
        masterData.addAll(matriculasList);
    }

    /**
     * Chamado ao clicar no botão 'Excluir'.
     */
    @FXML
    private void onExcluir() {
        Matricula selecionada = matriculasTableView.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma matrícula para excluir.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir a matrícula de " + selecionada.getNomeAluno() + "?");
        alert.setContentText("Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                matriculaDAO.delete(selecionada.getId()); // Usa o DAO para apagar
                masterData.remove(selecionada); // Remove da lista local (atualiza a UI)
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Matrícula excluída.");
            } catch (Exception e) { // Mudado para Exception genérica caso delete() não lance SQLException
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir a matrícula. Verifique se ela não possui notas associadas.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Chamado ao clicar no botão 'Fechar'.
     */
    @FXML
    private void onFechar() {
        Stage stage = (Stage) matriculasTableView.getScene().getWindow();
        stage.close();
    }

    /**
     * Método utilitário para mostrar alertas.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}