package com.example.controller;

import com.example.model.Horario;
import com.example.repository.HorarioDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// REMOVIDO: FilteredList
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button; // NOVO
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label; // NOVO
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

    @FXML private TableView<Horario> horariosTableView;
    @FXML private TextField searchField;

    // --- NOVOS FXML DE PAGINAÇÃO ---
    @FXML private Button btnPaginaAnterior;
    @FXML private Button btnPaginaProxima;
    @FXML private Label lblStatusPaginacao;

    private HorarioDAO horarioDAO;
    // masterData agora guarda apenas a PÁGINA ATUAL
    private final ObservableList<Horario> masterData = FXCollections.observableArrayList();

    // --- NOVAS VARIÁVEIS DE ESTADO DA PAGINAÇÃO ---
    private int paginaAtual = 1;
    private final int limitePorPagina = 10; // 10 é um bom número para um popup
    private String termoBuscaAtual = "";
    private int totalPaginas = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.horarioDAO = new HorarioDAO();
        
        // --- LÓGICA DE FILTRO REMOVIDA ---
        // A FilteredList foi removida.
        
        horariosTableView.setItems(masterData);
        carregarHorarios(); // Carregamento inicial
    }

    /**
     * MÉTODO CENTRAL REESCRITO
     */
    private void carregarHorarios() {
        try {
            int totalHorarios = horarioDAO.countHorariosFiltrados(termoBuscaAtual);
            totalPaginas = (int) Math.ceil((double) totalHorarios / limitePorPagina);
            if (totalPaginas == 0) totalPaginas = 1;
            
            List<Horario> horarios = horarioDAO.getHorariosPaginadoEFiltrado(
                termoBuscaAtual, paginaAtual, limitePorPagina
            );
            
            masterData.setAll(horarios);
            atualizarControlesPaginacao();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Não foi possível carregar os horários.");
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
        carregarHorarios();
    }

    /**
     * NOVO: Chamado pelo botão "Anterior".
     */
    @FXML
    private void onPaginaAnterior() {
        if (paginaAtual > 1) {
            paginaAtual--;
            carregarHorarios();
        }
    }

    /**
     * NOVO: Chamado pelo botão "Próxima".
     */
    @FXML
    private void onPaginaProxima() {
        if (paginaAtual < totalPaginas) {
            paginaAtual++;
            carregarHorarios();
        }
    }

    // --- MÉTODOS DE AÇÃO (onEditar, onExcluir, onFechar) ---
    // (A lógica interna deles não muda, mas adicionamos o initOwner)

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
            CadastroHorarioController controller = loader.getController();
            controller.setHorarioParaEdicao(horarioSelecionado);
            
            Stage stage = new Stage();
            stage.setTitle("Editar Horário");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // CORREÇÃO: Define o "dono" para corrigir o Tiling Window Manager
            stage.initOwner(horariosTableView.getScene().getWindow());
            
            stage.showAndWait();

            carregarHorarios(); // Atualiza a página atual
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
                
                // Recarrega a página (verifica se a página ficou vazia)
                if (masterData.size() == 1 && paginaAtual > 1) {
                    paginaAtual--;
                }
                carregarHorarios();
                
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