package com.example.controller;

import com.example.model.Aluno;
import com.example.repository.AlunoDAO;
import com.example.repository.ResponsavelDAO;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConsultaAlunosController implements Initializable {

    @FXML private TableView<Aluno> alunosTableView;
    @FXML private TextField searchField;

    private AlunoDAO alunoDAO;
    private ResponsavelDAO responsavelDAO;
    private ObservableList<Aluno> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.alunoDAO = new AlunoDAO();
        this.responsavelDAO = new ResponsavelDAO();
        carregarAlunos();

        FilteredList<Aluno> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(aluno -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (aluno.getNomeCompleto().toLowerCase().contains(lowerCaseFilter)) return true;
                if (aluno.getCpf() != null && aluno.getCpf().contains(lowerCaseFilter)) return true;
                if (aluno.getResponsavel().getNomeCompleto().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        alunosTableView.setItems(filteredData);
    }
    
    // --- MÉTODO ONEDITAR ADICIONADO ---
    @FXML
    private void onEditar() {
        Aluno alunoSelecionado = alunosTableView.getSelectionModel().getSelectedItem();
        if (alunoSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um aluno para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/tela_cadastro_aluno.fxml"));
            Parent root = loader.load();

            CadastroAlunoController controller = loader.getController();
            controller.setAlunoParaEdicao(alunoSelecionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Aluno");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            carregarAlunos(); // Atualiza a tabela após a edição
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODO ONEXCLUIR ADICIONADO ---
   @FXML
    private void onExcluir() {
        Aluno alunoSelecionado = alunosTableView.getSelectionModel().getSelectedItem();
        if (alunoSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um aluno para excluir.");
            return;
        }

        int responsavelId = alunoSelecionado.getResponsavel().getId();
        int numFilhos = alunoDAO.countAlunosByResponsavelId(responsavelId);

        if (numFilhos > 1) {
            // Caso 1: Responsável tem mais filhos, apaga só o aluno
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão");
            alert.setHeaderText("Este responsável tem outros alunos registados.");
            alert.setContentText("Deseja excluir apenas o aluno " + alunoSelecionado.getNomeCompleto() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                alunoDAO.delete(alunoSelecionado.getId());
                masterData.remove(alunoSelecionado); // Atualiza a UI
            }
        } else {
            // Caso 2: Este é o último filho, pergunta sobre o responsável
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão Completa");
            alert.setHeaderText("Este é o último aluno associado a " + alunoSelecionado.getResponsavel().getNomeCompleto() + ".");
            alert.setContentText("Deseja excluir o aluno E o seu responsável do sistema?");
            
            ButtonType btnExcluirAmbos = new ButtonType("Sim, excluir ambos");
            ButtonType btnExcluirApenasAluno = new ButtonType("Não, excluir apenas o aluno");
            alert.getButtonTypes().setAll(btnExcluirAmbos, btnExcluirApenasAluno, ButtonType.CANCEL);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == btnExcluirAmbos) {
                    // Apaga o aluno PRIMEIRO (por causa da chave estrangeira)
                    alunoDAO.delete(alunoSelecionado.getId());
                    // Depois apaga o responsável
                    responsavelDAO.delete(responsavelId);
                    masterData.remove(alunoSelecionado); // Atualiza a UI
                } else if (result.get() == btnExcluirApenasAluno) {
                    alunoDAO.delete(alunoSelecionado.getId());
                    masterData.remove(alunoSelecionado); // Atualiza a UI
                }
            }
        }
    }

    private void carregarAlunos() {
        masterData.clear();
        List<Aluno> alunosList = alunoDAO.getAll();
        masterData.addAll(alunosList);
    }
    
    @FXML
    private void onFechar() {
        Stage stage = (Stage) alunosTableView.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}