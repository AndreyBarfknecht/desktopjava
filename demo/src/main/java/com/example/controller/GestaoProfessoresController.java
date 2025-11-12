package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Professor; // O modelo
import com.example.repository.ProfessorDAO; // O DAO
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
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

public class GestaoProfessoresController implements Initializable {

    // --- FXML da Tabela e Pesquisa ---
    @FXML private TableView<Professor> professoresTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<Professor, Void> colAcoes;

    // --- DAOs e Listas ---
    private ProfessorDAO professorDAO;
    private ObservableList<Professor> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.professorDAO = new ProfessorDAO();
        
        configurarColunaAcoes();
        carregarProfessores(); // Carrega os dados

        // Lógica de filtro (copiada de ConsultaProfessoresController)
        FilteredList<Professor> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(professor -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (professor.getNomeCompleto().toLowerCase().contains(lowerCaseFilter)) return true;
                if (professor.getCpf() != null && professor.getCpf().contains(lowerCaseFilter)) return true;
                // (Podemos adicionar filtro por email ou telefone se quisermos)
                return false;
            });
        });
        professoresTableView.setItems(filteredData);
    }
    
    private void carregarProfessores() {
        masterData.clear();
        List<Professor> professoresList = professorDAO.getAll(); //
        masterData.addAll(professoresList);
    }

    // --- MÉTODOS PARA OS BLOCOS DE AÇÃO (Topo) ---
    
    @FXML
    private void onCadastrarNovoProfessor() {
        // Abre o popup de cadastro que já existe
        SceneNavigator.openNewWindow("CadastroProfessor", "Cadastro de Novo Professor"); //
        carregarProfessores(); // Atualiza a tabela
    }

    @FXML
    private void onAssociarDisciplina() {
        // Abre o popup de associação que já existe
        SceneNavigator.openNewWindow("GestaoProfessorDisciplina", "Associar Disciplinas ao Professor"); //
        // Não precisa recarregar professores aqui
    }
    
    // --- LÓGICA DE AÇÕES NA TABELA (Decisão 3) ---

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<Professor, Void>() {
            
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));
                
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
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(painelBotoes);
                }
            }
        });
    }

    // --- LÓGICA MOVIDA DE "ConsultaProfessoresController" ---

    private void handleEditar(Professor professorSelecionado) {
        if (professorSelecionado == null) return; // Segurança

        try {
            // Lógica exata de ConsultaProfessoresController.onEditar()
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroProfessor.fxml")); //
            Parent root = loader.load();

            CadastroProfessorController controller = loader.getController(); //
            controller.setProfessorParaEdicao(professorSelecionado); //

            Stage stage = new Stage();
            stage.setTitle("Editar Professor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait(); // Espera a janela de edição fechar

            carregarProfessores(); // Atualiza a tabela
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(Professor professorSelecionado) {
        if (professorSelecionado == null) return; // Segurança

        // Lógica de ConsultaProfessoresController.onExcluir()
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Exclusão");
        alert.setHeaderText("Tem a certeza que deseja excluir o professor?");
        alert.setContentText(professorSelecionado.getNomeCompleto());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            professorDAO.delete(professorSelecionado.getId()); //
            masterData.remove(professorSelecionado); // Atualiza a UI
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