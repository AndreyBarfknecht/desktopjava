package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Disciplina; // O modelo
import com.example.repository.DisciplinaDAO; // O DAO
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
 * ARQUIVO NOVO: src/main/java/com/example/controller/GestaoDisciplinasController.java
 * Este controller gerencia a tela GestaoDisciplinas.fxml, unificando
 * consulta, cadastro, edição e exclusão.
 */
public class GestaoDisciplinasController implements Initializable {

    // --- FXML da Tabela e Pesquisa ---
    @FXML private TableView<Disciplina> disciplinasTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<Disciplina, Void> colAcoes;

    // --- DAOs e Listas ---
    private DisciplinaDAO disciplinaDAO;
    private ObservableList<Disciplina> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.disciplinaDAO = new DisciplinaDAO();
        
        configurarColunaAcoes();
        carregarDisciplinas(); // Carrega os dados

        // Lógica de filtro (baseada em ConsultaDisciplinaController)
        FilteredList<Disciplina> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(disciplina -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (disciplina.getNomeDisciplina().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(disciplina.getCargaHoraria()).contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        disciplinasTableView.setItems(filteredData);
    }
    
    private void carregarDisciplinas() {
        masterData.clear();
        List<Disciplina> disciplinasList = disciplinaDAO.getAll();
        masterData.addAll(disciplinasList);
    }

    // --- MÉTODO PARA O BOTÃO DE AÇÃO (Topo) ---
    
    @FXML
    private void onCadastrarNovaDisciplina() {
        // Abre o popup de cadastro que já existe
        SceneNavigator.openNewWindow("CadastroDisciplina", "Cadastro de Nova Disciplina");
        carregarDisciplinas(); // Atualiza a tabela
    }

    // --- LÓGICA DE AÇÕES NA TABELA (Editar/Excluir) ---

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<Disciplina, Void>() {
            
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));
                
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
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(painelBotoes);
                }
            }
        });
    }

    // --- LÓGICA MOVIDA DE "ConsultaDisciplinaController" ---

    private void handleEditar(Disciplina disciplinaSelecionada) {
        if (disciplinaSelecionada == null) return; 

        try {
            // Lógica de ConsultaDisciplinaController.onEditar()
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroDisciplina.fxml"));
            Parent root = loader.load();

            CadastroDisciplinaController controller = loader.getController();
            controller.setDisciplinaParaEdicao(disciplinaSelecionada); //

            Stage stage = new Stage();
            stage.setTitle("Editar Disciplina");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait(); 

            carregarDisciplinas(); // Atualiza a tabela
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(Disciplina disciplinaSelecionada) {
        if (disciplinaSelecionada == null) return; 

        // Lógica de ConsultaDisciplinaController.onExcluir()
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir a disciplina '" + disciplinaSelecionada.getNomeDisciplina() + "'?");
        alert.setContentText("Atenção: Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                disciplinaDAO.delete(disciplinaSelecionada.getId());
                masterData.remove(disciplinaSelecionada); // Atualiza a UI
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir a disciplina. Verifique se ela não está associada a grades curriculares.");
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