package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.PeriodoLetivo; // O modelo
import com.example.repository.PeriodoLetivoDAO; // O DAO
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException; // Importar para o try-catch
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * ARQUIVO NOVO: src/main/java/com/example/controller/GestaoPeriodosLetivosController.java
 * Este controller gerencia a tela GestaoPeriodosLetivos.fxml.
 */
public class GestaoPeriodosLetivosController implements Initializable {

    // --- FXML da Tabela e Pesquisa ---
    @FXML private TableView<PeriodoLetivo> periodosTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<PeriodoLetivo, Void> colAcoes;

    // --- DAOs e Listas ---
    private PeriodoLetivoDAO periodoLetivoDAO;
    private ObservableList<PeriodoLetivo> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.periodoLetivoDAO = new PeriodoLetivoDAO();
        
        configurarColunaAcoes();
        carregarPeriodos(); 

        // Lógica de filtro (baseada em ConsultaPeriodosLetivosController)
        FilteredList<PeriodoLetivo> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(periodo -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (periodo.getNome().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return periodo.getStatus().toLowerCase().contains(lowerCaseFilter);
            });
        });
        periodosTableView.setItems(filteredData);
    }
    
    private void carregarPeriodos() {
        masterData.clear();
        List<PeriodoLetivo> periodosList = periodoLetivoDAO.getAll();
        masterData.addAll(periodosList);
    }

    // --- MÉTODO PARA O BOTÃO DE AÇÃO (Topo) ---
    
    @FXML
    private void onCadastrarNovoPeriodo() {
        // Abre o popup de cadastro que já existe
        SceneNavigator.openNewWindow("CadastroPeriodoLetivo", "Cadastro de Novo Período Letivo");
        carregarPeriodos(); // Atualiza a tabela
    }

    // --- LÓGICA DE AÇÕES NA TABELA (Editar/Excluir) ---

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<PeriodoLetivo, Void>() {
            
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));
                
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
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(painelBotoes);
                }
            }
        });
    }

    // --- LÓGICA MOVIDA DE "ConsultaPeriodosLetivosController" ---

    private void handleEditar(PeriodoLetivo periodoSelecionado) {
        if (periodoSelecionado == null) return; 

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroPeriodoLetivo.fxml"));
            Parent root = loader.load();

            CadastroPeriodoLetivoController controller = loader.getController();
            controller.setPeriodoParaEdicao(periodoSelecionado); //

            Stage stage = new Stage();
            stage.setTitle("Editar Período Letivo");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait(); 

            carregarPeriodos(); // Atualiza a tabela
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(PeriodoLetivo periodoSelecionado) {
        if (periodoSelecionado == null) return; 

        // Lógica de ConsultaPeriodosLetivosController.onExcluir()
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja realmente excluir o período '" + periodoSelecionado.getNome() + "'?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText(null); // Limpa o cabeçalho para um visual mais limpo
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                periodoLetivoDAO.delete(periodoSelecionado.getId());
                masterData.remove(periodoSelecionado);
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