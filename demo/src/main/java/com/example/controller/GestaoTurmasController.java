package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Turma; // O modelo
import com.example.repository.TurmaDAO; // O DAO
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

public class GestaoTurmasController implements Initializable {

    // --- FXML da Tabela e Pesquisa ---
    @FXML private TableView<Turma> turmasTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<Turma, Void> colAcoes;

    // --- DAOs e Listas ---
    private TurmaDAO turmaDAO;
    private ObservableList<Turma> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.turmaDAO = new TurmaDAO();
        
        configurarColunaAcoes();
        carregarTurmas(); // Carrega os dados

        // Lógica de filtro (copiada de ConsultaTurmasController)
        FilteredList<Turma> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(turma -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                // Lógica de filtro do ConsultaTurmasController
                if (turma.getNome().toLowerCase().contains(lowerCaseFilter)) return true;
                if (turma.getCurso().getNomeCurso().toLowerCase().contains(lowerCaseFilter)) return true;
                if (turma.getTurno().toLowerCase().contains(lowerCaseFilter)) return true;
                if (turma.getPeriodoLetivo().getNome().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        turmasTableView.setItems(filteredData);
    }
    
    private void carregarTurmas() {
        masterData.clear();
        List<Turma> turmasList = turmaDAO.getAll(); //
        masterData.addAll(turmasList);
    }

    // --- MÉTODOS PARA OS BLOCOS DE AÇÃO (Topo) ---
    
    @FXML
    private void onCadastrarNovaTurma() {
        SceneNavigator.openNewWindow("CadastroTurma", "Cadastro de Nova Turma"); //
        carregarTurmas(); // Atualiza a tabela
    }

    @FXML
    private void onMatricularAluno() {
        SceneNavigator.openNewWindow("Matricula", "Matrícula de Alunos"); //
    }
    
    @FXML
    private void onDefinirHorarios() {
        SceneNavigator.openNewWindow("CadastroHorario", "Definição de Horários"); //
    }
    
    // --- LÓGICA DE AÇÕES NA TABELA (Decisão 3) ---

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<Turma, Void>() {
            
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));
                
                btnEditar.setOnAction(event -> {
                    Turma turma = getTableView().getItems().get(getIndex());
                    handleEditar(turma);
                });
                
                btnExcluir.setOnAction(event -> {
                    Turma turma = getTableView().getItems().get(getIndex());
                    handleExcluir(turma);
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

    // --- LÓGICA MOVIDA DE "ConsultaTurmasController" ---

    private void handleEditar(Turma turmaSelecionada) {
        if (turmaSelecionada == null) return; 

        try {
            // Lógica exata de ConsultaTurmasController.onEditar()
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroTurma.fxml"));
            Parent root = loader.load();

            CadastroTurmaController controller = loader.getController();
            controller.setTurmaParaEdicao(turmaSelecionada); //

            Stage stage = new Stage();
            stage.setTitle("Editar Turma");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait(); 

            carregarTurmas(); // Atualiza a tabela
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(Turma turmaSelecionada) {
        if (turmaSelecionada == null) return; 

        // Lógica de ConsultaTurmasController.onExcluir()
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir a turma '" + turmaSelecionada.getNome() + "'?");
        alert.setContentText("Atenção: Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                turmaDAO.delete(turmaSelecionada.getId()); //
                masterData.remove(turmaSelecionada); // Atualiza a UI
            } catch (SQLException e) {
                // Se a turma tiver alunos (Foreign Key), este alerta será mostrado
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir a turma. Verifique se ela não está associada a matrículas ou horários.");
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