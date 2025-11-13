package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Curso; // O modelo
import com.example.repository.CursoDAO; // O DAO
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

public class GestaoCursosController implements Initializable {

    // --- FXML da Tabela e Pesquisa ---
    @FXML private TableView<Curso> cursosTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<Curso, Void> colAcoes;

    // --- DAOs e Listas ---
    private CursoDAO cursoDAO;
    private ObservableList<Curso> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.cursoDAO = new CursoDAO();
        
        configurarColunaAcoes();
        carregarCursos(); // Carrega os dados

        // Lógica de filtro (copiada de ConsultaCursosController)
        FilteredList<Curso> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(curso -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (curso.getNomeCurso().toLowerCase().contains(lowerCaseFilter)) return true;
                if (curso.getNivel().toLowerCase().contains(lowerCaseFilter)) return true; //
                return false;
            });
        });
        cursosTableView.setItems(filteredData);
    }
    
    private void carregarCursos() {
        masterData.clear();
        List<Curso> cursosList = cursoDAO.getAll(); //
        masterData.addAll(cursosList);
    }

    // --- MÉTODOS PARA OS BLOCOS DE AÇÃO (Topo) ---
    
    @FXML
    private void onCadastrarNovoCurso() {
        // Abre o popup de cadastro que já existe
        SceneNavigator.openNewWindow("CadastroCurso", "Cadastro de Novo Curso"); //
        carregarCursos(); // Atualiza a tabela
    }

    @FXML
    private void onGerenciarGrade() {
        // Abre o popup de associação que já existe
        SceneNavigator.openNewWindow("GestaoGrade", "Gerenciar Grade Curricular"); //
    }
    
    // --- LÓGICA DE AÇÕES NA TABELA (Decisão 3) ---

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<Curso, Void>() {
            
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));
                
                btnEditar.setOnAction(event -> {
                    Curso curso = getTableView().getItems().get(getIndex());
                    handleEditar(curso);
                });
                
                btnExcluir.setOnAction(event -> {
                    Curso curso = getTableView().getItems().get(getIndex());
                    handleExcluir(curso);
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

    // --- LÓGICA MOVIDA DE "ConsultaCursosController" ---

    private void handleEditar(Curso cursoSelecionado) {
        if (cursoSelecionado == null) return; 

        try {
            // Lógica exata de ConsultaCursosController.onEditar()
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroCurso.fxml"));
            Parent root = loader.load();

            CadastroCursoController controller = loader.getController();
            controller.setCursoParaEdicao(cursoSelecionado); //

            Stage stage = new Stage();
            stage.setTitle("Editar Curso");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(cursosTableView.getScene().getWindow());
            stage.showAndWait(); 

            carregarCursos(); // Atualiza a tabela
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(Curso cursoSelecionado) {
        if (cursoSelecionado == null) return; 

        // Lógica de ConsultaCursosController.onExcluir()
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir o curso '" + cursoSelecionado.getNomeCurso() + "'?");
        alert.setContentText("Atenção: Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                cursoDAO.delete(cursoSelecionado.getId()); //
                masterData.remove(cursoSelecionado); // Atualiza a UI
            } catch (SQLException e) {
                // Se o curso tiver turmas ou grades (Foreign Key), este alerta será mostrado
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir o curso. Verifique se ele não está associado a turmas ou grades curriculares.");
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