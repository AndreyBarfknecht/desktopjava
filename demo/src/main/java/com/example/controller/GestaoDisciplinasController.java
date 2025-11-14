package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Disciplina;
import com.example.repository.DisciplinaDAO;
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

public class GestaoDisciplinasController implements Initializable {

    // --- FXML da Tabela e Pesquisa ---
    @FXML private TableView<Disciplina> disciplinasTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<Disciplina, Void> colAcoes;

    // --- NOVOS FXML DE PAGINAÇÃO ---
    @FXML private Button btnPaginaAnterior;
    @FXML private Button btnPaginaProxima;
    @FXML private Label lblStatusPaginacao;

    // --- DAOs e Listas ---
    private DisciplinaDAO disciplinaDAO;
    private final ObservableList<Disciplina> masterData = FXCollections.observableArrayList();

    // --- NOVAS VARIÁVEIS DE ESTADO DA PAGINAÇÃO ---
    private int paginaAtual = 1;
    private final int limitePorPagina = 15; 
    private String termoBuscaAtual = "";
    private int totalPaginas = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.disciplinaDAO = new DisciplinaDAO();
        configurarColunaAcoes();

        // --- LÓGICA DE FILTRO REMOVIDA ---
        
        disciplinasTableView.setItems(masterData);
        carregarDisciplinas(); // Carregamento inicial
    }

    /**
     * MÉTODO CENTRAL REESCRITO
     */
    private void carregarDisciplinas() {
        try {
            int totalDisciplinas = disciplinaDAO.countDisciplinasFiltradas(termoBuscaAtual);
            totalPaginas = (int) Math.ceil((double) totalDisciplinas / limitePorPagina);
            if (totalPaginas == 0) totalPaginas = 1;

            List<Disciplina> disciplinas = disciplinaDAO.getDisciplinasPaginadoEFiltrado(
                termoBuscaAtual, paginaAtual, limitePorPagina
            );
            
            masterData.setAll(disciplinas);
            atualizarControlesPaginacao();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Não foi possível carregar as disciplinas.");
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
        carregarDisciplinas();
    }

    /**
     * NOVO: Chamado pelo botão "Anterior".
     */
    @FXML
    private void onPaginaAnterior() {
        if (paginaAtual > 1) {
            paginaAtual--;
            carregarDisciplinas();
        }
    }

    /**
     * NOVO: Chamado pelo botão "Próxima".
     */
    @FXML
    private void onPaginaProxima() {
        if (paginaAtual < totalPaginas) {
            paginaAtual++;
            carregarDisciplinas();
        }
    }
    
    // --- MÉTODOS DE AÇÃO (Topo) ---
    
    @FXML
    private void onCadastrarNovaDisciplina() {
        SceneNavigator.openNewWindow("CadastroDisciplina", "Cadastro de Nova Disciplina");
        
        // Após cadastrar, limpa a busca e volta para a página 1
        termoBuscaAtual = "";
        searchField.clear();
        paginaAtual = 1;
        carregarDisciplinas(); 
    }
    
    // --- LÓGICA DE AÇÕES NA TABELA (COM TOOLTIPS) ---

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<Disciplina, Void>() {
            
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));
                
                Tooltip.install(btnEditar, new Tooltip("Editar disciplina"));
                Tooltip.install(btnExcluir, new Tooltip("Excluir disciplina"));
                
                btnEditar.setOnAction(event -> {
                    Disciplina disciplina = getTableView().getItems().get(getIndex());
                    handleEditar(disciplina);
                });
                
                btnExcluir.setOnAction(event -> {
                    Disciplina disciplina = getTableView().getItems().get(getIndex());
                    handleExcluir(disciplina);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : painelBotoes);
            }
        });
    }


    private void handleEditar(Disciplina disciplinaSelecionada) {
        if (disciplinaSelecionada == null) return; 

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroDisciplina.fxml"));
            Parent root = loader.load();
            CadastroDisciplinaController controller = loader.getController();
            controller.setDisciplinaParaEdicao(disciplinaSelecionada); 

            Stage stage = new Stage();
            stage.setTitle("Editar Disciplina");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(disciplinasTableView.getScene().getWindow()); // Correção Tiling
            stage.showAndWait(); 

            carregarDisciplinas(); // Recarrega a página atual
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(Disciplina disciplinaSelecionada) {
        if (disciplinaSelecionada == null) return; 

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir a disciplina '" + disciplinaSelecionada.getNomeDisciplina() + "'?");
        alert.setContentText("Atenção: Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                disciplinaDAO.delete(disciplinaSelecionada.getId());
                
                if (masterData.size() == 1 && paginaAtual > 1) {
                    paginaAtual--;
                }
                carregarDisciplinas();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir a disciplina. Verifique se ela não está associada a grades ou horários.");
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