package com.example.controller;

import com.example.model.Matricula;
import com.example.repository.MatriculaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // NOVO
import javafx.fxml.Initializable;
import javafx.scene.Parent; // NOVO
import javafx.scene.Scene; // NOVO
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality; // NOVO
import javafx.stage.Stage;

import java.io.IOException; // NOVO
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConsultaMatriculasController implements Initializable {

    @FXML private TableView<Matricula> matriculasTableView;
    @FXML private TextField searchField;
    @FXML private Button btnPaginaAnterior;
    @FXML private Button btnPaginaProxima;
    @FXML private Label lblStatusPaginacao;

    private MatriculaDAO matriculaDAO;
    private final ObservableList<Matricula> masterData = FXCollections.observableArrayList();

    private int paginaAtual = 1;
    private final int limitePorPagina = 10;
    private String termoBuscaAtual = "";
    private int totalPaginas = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.matriculaDAO = new MatriculaDAO();
        matriculasTableView.setItems(masterData);
        carregarMatriculas();
    }

    private void carregarMatriculas() {
        try {
            int totalMatriculas = matriculaDAO.countMatriculasFiltradas(termoBuscaAtual);
            totalPaginas = (int) Math.ceil((double) totalMatriculas / limitePorPagina);
            if (totalPaginas == 0) totalPaginas = 1;
            
            List<Matricula> matriculas = matriculaDAO.getMatriculasPaginadoEFiltrado(
                termoBuscaAtual, paginaAtual, limitePorPagina
            );
            
            masterData.setAll(matriculas);
            atualizarControlesPaginacao();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Não foi possível carregar as matrículas.");
        }
    }
    
    private void atualizarControlesPaginacao() {
        lblStatusPaginacao.setText("Página " + paginaAtual + " de " + totalPaginas);
        btnPaginaAnterior.setDisable(paginaAtual == 1);
        btnPaginaProxima.setDisable(paginaAtual >= totalPaginas);
    }

    @FXML
    private void onBuscar() {
        termoBuscaAtual = searchField.getText();
        paginaAtual = 1; 
        carregarMatriculas();
    }

    @FXML
    private void onPaginaAnterior() {
        if (paginaAtual > 1) {
            paginaAtual--;
            carregarMatriculas();
        }
    }

    @FXML
    private void onPaginaProxima() {
        if (paginaAtual < totalPaginas) {
            paginaAtual++;
            carregarMatriculas();
        }
    }

    /**
     * NOVO: Método onEditar (baseado no de ConsultaHorarios)
     */
    @FXML
    private void onEditar() {
        Matricula selecionada = matriculasTableView.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma matrícula para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/Matricula.fxml"));
            Parent root = loader.load();
            
            // Pega o controlador do popup
            MatriculaController controller = loader.getController();
            
            // Injeta a matrícula no modo de edição
            controller.setMatriculaParaEdicao(selecionada);

            Stage stage = new Stage();
            stage.setTitle("Editar Status da Matrícula");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(matriculasTableView.getScene().getWindow()); // Correção do Tiling
            stage.showAndWait();

            carregarMatriculas(); // Recarrega a página atual

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de matrícula.");
        }
    }

    /**
     * NOVO: Método onExcluir (restaurado)
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
                matriculaDAO.delete(selecionada.getId());
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Matrícula excluída.");
                
                // Recarrega a página (verifica se a página ficou vazia)
                if (masterData.size() == 1 && paginaAtual > 1) {
                    paginaAtual--;
                }
                carregarMatriculas();
                
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir a matrícula. Verifique se ela não possui notas associadas.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) matriculasTableView.getScene().getWindow();
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