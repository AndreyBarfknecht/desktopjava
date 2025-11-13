package com.example.controller;

import com.example.model.Matricula;
import com.example.repository.MatriculaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// REMOVIDO: FilteredList
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button; // NOVO
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label; // NOVO
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConsultaMatriculasController implements Initializable {

    @FXML private TableView<Matricula> matriculasTableView;
    @FXML private TextField searchField;

    // --- NOVOS FXML DE PAGINAÇÃO ---
    @FXML private Button btnPaginaAnterior;
    @FXML private Button btnPaginaProxima;
    @FXML private Label lblStatusPaginacao;

    private MatriculaDAO matriculaDAO;
    private final ObservableList<Matricula> masterData = FXCollections.observableArrayList();

    // --- NOVAS VARIÁVEIS DE ESTADO DA PAGINAÇÃO ---
    private int paginaAtual = 1;
    private final int limitePorPagina = 10; // 10 por página no popup
    private String termoBuscaAtual = "";
    private int totalPaginas = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.matriculaDAO = new MatriculaDAO();
        
        // --- LÓGICA DE FILTRO REMOVIDA ---
        
        matriculasTableView.setItems(masterData);
        carregarMatriculas(); // Carregamento inicial
    }

    /**
     * MÉTODO CENTRAL REESCRITO
     */
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
    
    /**
     * NOVO: Atualiza a legenda e os botões de paginação.
     */
    private void atualizarControlesPaginacao() {
        lblStatusPaginacao.setText("Página " + paginaAtual + " de " + totalPaginas);
        btnPaginaAnterior.setDisable(paginaAtual == 1);
        btnPaginaProxima.setDisable(paginaAtual >= totalPaginas);
    }

    /**
     * NOVO: Chamado quando o utilizador pressiona Enter no campo de busca.
     */
    @FXML
    private void onBuscar() {
        termoBuscaAtual = searchField.getText();
        paginaAtual = 1; // Volta para a página 1
        carregarMatriculas();
    }

    /**
     * NOVO: Chamado pelo botão "Anterior".
     */
    @FXML
    private void onPaginaAnterior() {
        if (paginaAtual > 1) {
            paginaAtual--;
            carregarMatriculas();
        }
    }

    /**
     * NOVO: Chamado pelo botão "Próxima".
     */
    @FXML
    private void onPaginaProxima() {
        if (paginaAtual < totalPaginas) {
            paginaAtual++;
            carregarMatriculas();
        }
    }

    /**
     * REMOVIDO: A lógica de exclusão foi movida para as telas de gestão.
     */
    // @FXML private void onExcluir() { ... }

    /**
     * Chamado ao clicar no botão 'Fechar'.
     */
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