package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Curso;
import com.example.repository.CursoDAO;
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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GestaoCursosController implements Initializable {

    // --- FXML da Tabela e Pesquisa ---
    @FXML private TableView<Curso> cursosTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<Curso, Void> colAcoes;

    // --- NOVOS FXML DE PAGINAÇÃO ---
    @FXML private Button btnPaginaAnterior;
    @FXML private Button btnPaginaProxima;
    @FXML private Label lblStatusPaginacao;

    // --- DAOs e Listas ---
    private CursoDAO cursoDAO;
    private final ObservableList<Curso> masterData = FXCollections.observableArrayList();

    // --- NOVAS VARIÁVEIS DE ESTADO DA PAGINAÇÃO ---
    private int paginaAtual = 1;
    private final int limitePorPagina = 15; // Define quantos por página
    private String termoBuscaAtual = "";
    private int totalPaginas = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.cursoDAO = new CursoDAO();
        configurarColunaAcoes();

        // --- LÓGICA DE FILTRO REMOVIDA ---
        
        cursosTableView.setItems(masterData);
        carregarCursos(); // Carregamento inicial
    }

    /**
     * MÉTODO CENTRAL REESCRITO
     */
    private void carregarCursos() {
        try {
            int totalCursos = cursoDAO.countCursosFiltrados(termoBuscaAtual);
            totalPaginas = (int) Math.ceil((double) totalCursos / limitePorPagina);
            if (totalPaginas == 0) totalPaginas = 1;

            List<Curso> cursos = cursoDAO.getCursosPaginadoEFiltrado(
                termoBuscaAtual, paginaAtual, limitePorPagina
            );
            
            masterData.setAll(cursos);
            atualizarControlesPaginacao();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Não foi possível carregar os cursos.");
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
        carregarCursos();
    }

    /**
     * NOVO: Chamado pelo botão "Anterior".
     */
    @FXML
    private void onPaginaAnterior() {
        if (paginaAtual > 1) {
            paginaAtual--;
            carregarCursos();
        }
    }

    /**
     * NOVO: Chamado pelo botão "Próxima".
     */
    @FXML
    private void onPaginaProxima() {
        if (paginaAtual < totalPaginas) {
            paginaAtual++;
            carregarCursos();
        }
    }

    // --- MÉTODOS DE AÇÃO (Topo) ---
    
    @FXML
    private void onCadastrarNovoCurso() {
        SceneNavigator.openNewWindow("CadastroCurso", "Cadastro de Novo Curso");
        
        // Após cadastrar, limpa a busca e volta para a página 1
        termoBuscaAtual = "";
        searchField.clear();
        paginaAtual = 1;
        carregarCursos(); 
    }
    
    // --- LÓGICA DE AÇÕES NA TABELA (COM TOOLTIPS) ---

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<Curso, Void>() {
            
            private final Button btnGerirGrade = new Button("", new FontAwesomeIconView(FontAwesomeIcon.LIST_UL));
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnGerirGrade, btnEditar, btnExcluir);

            {
                btnGerirGrade.getStyleClass().add("salvar-button");
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));

                Tooltip.install(btnGerirGrade, new Tooltip("Gerir grade curricular"));
                Tooltip.install(btnEditar, new Tooltip("Editar dados do curso"));
                Tooltip.install(btnExcluir, new Tooltip("Excluir curso"));
                
                btnGerirGrade.setOnAction(event -> {
                    Curso cursoSelecionado = getTableView().getItems().get(getIndex());
                    handleGerirGrade(cursoSelecionado);
                });
                
                btnEditar.setOnAction(event -> {
                    Curso cursoSelecionado = getTableView().getItems().get(getIndex());
                    handleEditar(cursoSelecionado);
                });
                
                btnExcluir.setOnAction(event -> {
                    Curso cursoSelecionado = getTableView().getItems().get(getIndex());
                    handleExcluir(cursoSelecionado);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : painelBotoes);
            }
        });
    }

    /**
     * Este é o método que abre o popup da Grade, corrigido.
     */
    private void handleGerirGrade(Curso cursoSelecionado) {
        if (cursoSelecionado == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/GestaoGrade.fxml"));
            Parent root = loader.load();
            GestaoGradeController controller = loader.getController();
            
            // Aqui usamos o método setCurso() que corrigimos
            controller.setCurso(cursoSelecionado); 

            Stage stage = new Stage();
            stage.setTitle("Gerir Grade de: " + cursoSelecionado.getNomeCurso());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(cursosTableView.getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de gestão de grade.");
        }
    }


    private void handleEditar(Curso cursoSelecionado) {
        if (cursoSelecionado == null) return; 

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroCurso.fxml"));
            Parent root = loader.load();
            CadastroCursoController controller = loader.getController();
            controller.setCursoParaEdicao(cursoSelecionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Curso");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(cursosTableView.getScene().getWindow()); 
            stage.showAndWait();

            carregarCursos(); // Recarrega a página atual
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(Curso cursoSelecionado) {
        if (cursoSelecionado == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir o curso '" + cursoSelecionado.getNomeCurso() + "'?");
        alert.setContentText("Atenção: Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                cursoDAO.delete(cursoSelecionado.getId());
                
                if (masterData.size() == 1 && paginaAtual > 1) {
                    paginaAtual--;
                }
                carregarCursos();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir o curso. Verifique se ele não está associado a turmas ou grades.");
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