package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Professor;
import com.example.repository.ProfessorDAO;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GestaoProfessoresController implements Initializable {

    // --- FXML da Tabela e Pesquisa ---
    @FXML private TableView<Professor> professoresTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<Professor, Void> colAcoes;

    // --- NOVOS FXML DE PAGINAÇÃO ---
    @FXML private Button btnPaginaAnterior;
    @FXML private Button btnPaginaProxima;
    @FXML private Label lblStatusPaginacao;

    // --- DAOs e Listas ---
    private ProfessorDAO professorDAO;
    private final ObservableList<Professor> masterData = FXCollections.observableArrayList();

    // --- NOVAS VARIÁVEIS DE ESTADO DA PAGINAÇÃO ---
    private int paginaAtual = 1;
    private final int limitePorPagina = 15; // Define quantos por página
    private String termoBuscaAtual = "";
    private int totalPaginas = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.professorDAO = new ProfessorDAO();
        configurarColunaAcoes();

        // --- LÓGICA DE FILTRO REMOVIDA ---
        
        professoresTableView.setItems(masterData);
        carregarProfessores(); // Carregamento inicial
    }

    /**
     * MÉTODO CENTRAL REESCRITO
     */
    private void carregarProfessores() {
        try {
            int totalProfessores = professorDAO.countProfessoresFiltrados(termoBuscaAtual);
            totalPaginas = (int) Math.ceil((double) totalProfessores / limitePorPagina);
            if (totalPaginas == 0) totalPaginas = 1;

            List<Professor> professores = professorDAO.getProfessoresPaginadoEFiltrado(
                termoBuscaAtual, paginaAtual, limitePorPagina
            );
            
            masterData.setAll(professores);
            atualizarControlesPaginacao();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Não foi possível carregar os professores.");
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
        paginaAtual = 1; // Sempre volta para a página 1 ao buscar
        carregarProfessores();
    }

    /**
     * NOVO: Chamado pelo botão "Anterior".
     */
    @FXML
    private void onPaginaAnterior() {
        if (paginaAtual > 1) {
            paginaAtual--;
            carregarProfessores();
        }
    }

    /**
     * NOVO: Chamado pelo botão "Próxima".
     */
    @FXML
    private void onPaginaProxima() {
        if (paginaAtual < totalPaginas) {
            paginaAtual++;
            carregarProfessores();
        }
    }

    // --- MÉTODOS DE AÇÃO (Topo) ---
    
    @FXML
    private void onCadastrarNovoProfessor() {
        SceneNavigator.openNewWindow("CadastroProfessor", "Cadastro de Novo Professor");
        
        // Após cadastrar, limpa a busca e volta para a página 1
        termoBuscaAtual = "";
        searchField.clear();
        paginaAtual = 1;
        carregarProfessores(); 
    }

    // O método onAssociarDisciplinas() foi removido (agora está na linha)
    

    // --- LÓGICA DE AÇÕES NA TABELA (COM TOOLTIPS) ---

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<Professor, Void>() {
            
            private final Button btnAssociar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.BOOK));
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnAssociar, btnEditar, btnExcluir);

            {
                btnAssociar.getStyleClass().add("salvar-button");
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));

                Tooltip.install(btnAssociar, new Tooltip("Associar disciplinas a este professor"));
                Tooltip.install(btnEditar, new Tooltip("Editar dados do professor"));
                Tooltip.install(btnExcluir, new Tooltip("Excluir professor"));
                
                btnAssociar.setOnAction(event -> {
                    Professor professor = getTableView().getItems().get(getIndex());
                    handleAssociarDisciplinas(professor);
                });
                
                btnEditar.setOnAction(event -> {
                    Professor professor = getTableView().getItems().get(getIndex());
                    handleEditar(professor);
                });
                
                btnExcluir.setOnAction(event -> {
                    Professor professor = getTableView().getItems().get(getIndex());
                    handleExcluir(professor);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : painelBotoes);
            }
        });
    }

    private void handleAssociarDisciplinas(Professor professor) {
        if (professor == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Professor não encontrado.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/GestaoProfessorDisciplina.fxml"));
            Parent root = loader.load();
            GestaoProfessorDisciplinaController controller = loader.getController();
            
            controller.setProfessor(professor); // Método que já criámos

            Stage stage = new Stage();
            stage.setTitle("Associar Disciplinas: " + professor.getNomeCompleto());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(professoresTableView.getScene().getWindow());
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de associação de disciplinas.");
        }
    }

    private void handleEditar(Professor professor) {
        if (professor == null) return; 

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroProfessor.fxml"));
            Parent root = loader.load();
            CadastroProfessorController controller = loader.getController();
            controller.setProfessorParaEdicao(professor);

            Stage stage = new Stage();
            stage.setTitle("Editar Professor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(professoresTableView.getScene().getWindow()); 
            stage.showAndWait();

            carregarProfessores(); // Recarrega a página atual
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(Professor professor) {
        if (professor == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir o professor '" + professor.getNomeCompleto() + "'?");
        alert.setContentText("Atenção: Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                professorDAO.delete(professor.getId());
                
                // Recarrega a página (verifica se a página ficou vazia)
                if (masterData.size() == 1 && paginaAtual > 1) {
                    paginaAtual--;
                }
                carregarProfessores();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir o professor. Verifique se ele não está associado a horários ou disciplinas.");
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