package com.example.controller;

import com.example.model.Professor;
import com.example.repository.ProfessorDAO;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList; // Importação Adicionada
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField; // Importação Adicionada
import javafx.stage.Stage;
import com.example.SceneNavigator;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class ConsultaProfessoresController implements Initializable {

    @FXML private TableView<Professor> professoresTableView;
    @FXML private TextField searchField; // NOVO: Ligação com o campo de busca

    private ProfessorDAO professorDAO;
    private ObservableList<Professor> masterData = FXCollections.observableArrayList(); // Lista com todos os professores

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.professorDAO = new ProfessorDAO();
        carregarProfessores(); // Carrega os dados da base de dados

        // 1. Envolve a lista principal (masterData) numa FilteredList.
        FilteredList<Professor> filteredData = new FilteredList<>(masterData, p -> true);

        // 2. Adiciona um "ouvinte" (listener) ao campo de busca.
        //    Este código será executado sempre que o texto no campo mudar.
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(professor -> {
                // Se o campo de busca estiver vazio, mostra todos os professores.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Verifica se o nome do professor (em minúsculas) contém o texto do filtro.
                if (professor.getNomeCompleto().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Encontrou no nome
                } 
                // Verifica se o CPF do professor contém o texto do filtro.
                else if (professor.getCpf().contains(lowerCaseFilter)) {
                    return true; // Encontrou no CPF
                }
                
                return false; // Não encontrou, esconde este registo.
            });
        });
        
        // 3. Define os itens da tabela como sendo a lista filtrada.
        professoresTableView.setItems(filteredData);
    }

    
    @FXML
    private void onExcluir() {
        Professor professorSelecionado = professoresTableView.getSelectionModel().getSelectedItem();
        
        if (professorSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione um professor na tabela para excluir.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Exclusão");
        alert.setHeaderText("Tem a certeza que deseja excluir o professor?");
        alert.setContentText(professorSelecionado.getNomeCompleto());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            professorDAO.delete(professorSelecionado.getId());
            masterData.remove(professorSelecionado); // Remove da lista na UI para atualização instantânea
        }
    }

    // --- MÉTODO EDITAR CORRIGIDO ---
    @FXML
    private void onEditar() {
        Professor professorSelecionado = professoresTableView.getSelectionModel().getSelectedItem();

        if (professorSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione um professor na tabela para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroProfessor.fxml"));
            Parent root = loader.load();

            CadastroProfessorController controller = loader.getController();
            controller.setProfessorParaEdicao(professorSelecionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Professor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Mantém o modal para a janela principal, mas não para a de consulta
            
            // --- ALTERAÇÃO PRINCIPAL AQUI ---
            // Em vez de esperar, apenas mostramos a nova janela.
            stage.show();

            // --- MELHORIA DE USABILIDADE ---
            // Fecha a janela de consulta automaticamente após abrir a de edição.
            onFechar();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro ao Abrir", "Não foi possível abrir a janela de edição de professor.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void carregarProfessores() {
        // Limpa a lista antes de carregar para evitar duplicados
        masterData.clear();
        // Pede ao DAO a lista de professores da base de dados.
        List<Professor> professoresList = professorDAO.getAll();
        // Adiciona todos os professores à lista principal.
        masterData.addAll(professoresList);
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) professoresTableView.getScene().getWindow();
        stage.close();
    }
}