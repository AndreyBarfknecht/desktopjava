package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.PeriodoLetivo;
import com.example.repository.PeriodoLetivoDAO;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// REMOVIDO: FilteredList
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label; // NOVO
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip; // NOVO
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GestaoPeriodosLetivosController implements Initializable {

    // --- FXML da Tabela e Pesquisa ---
    @FXML private TableView<PeriodoLetivo> periodosTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<PeriodoLetivo, Void> colAcoes;

    // --- NOVOS FXML DE PAGINAÇÃO ---
    @FXML private Button btnPaginaAnterior;
    @FXML private Button btnPaginaProxima;
    @FXML private Label lblStatusPaginacao;

    // --- DAOs e Listas ---
    private PeriodoLetivoDAO periodoLetivoDAO;
    private final ObservableList<PeriodoLetivo> masterData = FXCollections.observableArrayList();

    // --- NOVAS VARIÁVEIS DE ESTADO DA PAGINAÇÃO ---
    private int paginaAtual = 1;
    private final int limitePorPagina = 15; 
    private String termoBuscaAtual = "";
    private int totalPaginas = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.periodoLetivoDAO = new PeriodoLetivoDAO();
        configurarColunaAcoes();

        // --- LÓGICA DE FILTRO REMOVIDA ---
        
        periodosTableView.setItems(masterData);
        carregarPeriodos(); // Carregamento inicial
    }

    /**
     * MÉTODO CENTRAL REESCRITO
     */
    private void carregarPeriodos() {
        try {
            int totalPeriodos = periodoLetivoDAO.countPeriodosLetivosFiltrados(termoBuscaAtual);
            totalPaginas = (int) Math.ceil((double) totalPeriodos / limitePorPagina);
            if (totalPaginas == 0) totalPaginas = 1;

            List<PeriodoLetivo> periodos = periodoLetivoDAO.getPeriodosLetivosPaginadoEFiltrado(
                termoBuscaAtual, paginaAtual, limitePorPagina
            );
            
            masterData.setAll(periodos);
            atualizarControlesPaginacao();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Não foi possível carregar os períodos letivos.");
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
        paginaAtual = 1; 
        carregarPeriodos();
    }

    /**
     * NOVO: Chamado pelo botão "Anterior".
     */
    @FXML
    private void onPaginaAnterior() {
        if (paginaAtual > 1) {
            paginaAtual--;
            carregarPeriodos();
        }
    }

    /**
     * NOVO: Chamado pelo botão "Próxima".
     */
    @FXML
    private void onPaginaProxima() {
        if (paginaAtual < totalPaginas) {
            paginaAtual++;
            carregarPeriodos();
        }
    }
    
    // --- MÉTODOS DE AÇÃO (Topo) ---
    
    @FXML
    private void onCadastrarNovoPeriodo() {
        SceneNavigator.openNewWindow("CadastroPeriodoLetivo", "Cadastro de Novo Período Letivo");
        
        // Após cadastrar, limpa a busca e volta para a página 1
        termoBuscaAtual = "";
        searchField.clear();
        paginaAtual = 1;
        carregarPeriodos(); 
    }
    
    // --- LÓGICA DE AÇÕES NA TABELA (COM TOOLTIPS) ---

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<PeriodoLetivo, Void>() {
            
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));
                
                Tooltip.install(btnEditar, new Tooltip("Editar período letivo"));
                Tooltip.install(btnExcluir, new Tooltip("Excluir período letivo"));
                
                btnEditar.setOnAction(event -> {
                    PeriodoLetivo periodo = getTableView().getItems().get(getIndex());
                    handleEditar(periodo);
                });
                
                btnExcluir.setOnAction(event -> {
                    PeriodoLetivo periodo = getTableView().getItems().get(getIndex());
                    handleExcluir(periodo);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : painelBotoes);
            }
        });
    }


    private void handleEditar(PeriodoLetivo periodoSelecionado) {
        if (periodoSelecionado == null) return; 

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroPeriodoLetivo.fxml"));
            Parent root = loader.load();
            CadastroPeriodoLetivoController controller = loader.getController();
            controller.setPeriodoParaEdicao(periodoSelecionado); 

            Stage stage = new Stage();
            stage.setTitle("Editar Período Letivo");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(periodosTableView.getScene().getWindow()); // Correção Tiling
            stage.showAndWait(); 

            carregarPeriodos(); // Recarrega a página atual
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(PeriodoLetivo periodoSelecionado) {
        if (periodoSelecionado == null) return; 

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja realmente excluir o período '" + periodoSelecionado.getNome() + "'?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                periodoLetivoDAO.delete(periodoSelecionado.getId());
                
                if (masterData.size() == 1 && paginaAtual > 1) {
                    paginaAtual--;
                }
                carregarPeriodos();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir o período. Verifique se ele não está associado a turmas.");
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}