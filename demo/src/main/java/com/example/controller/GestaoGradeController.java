package com.example.controller;

import com.example.model.Curso;
import com.example.model.Disciplina;
import com.example.repository.DisciplinaDAO;
import com.example.repository.GradeCurricularDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException; // Importar SQLException
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors; // Importar Stream Collectors

public class GestaoGradeController implements Initializable {

    // --- FXML Fields (baseado no teu FXML) ---
    @FXML private Label lblNomeCurso;
    @FXML private ListView<Disciplina> associadasListView;
    @FXML private Button removerButton;
    @FXML private ComboBox<Disciplina> disponiveisComboBox;
    @FXML private Button adicionarButton;
    @FXML private Button fecharButton;

    // --- DAOs ---
    private DisciplinaDAO disciplinaDAO;
    private GradeCurricularDAO gradeCurricularDAO;

    // --- Controlo de Estado ---
    private Curso cursoSelecionado;
    private final ObservableList<Disciplina> associadasList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.disciplinaDAO = new DisciplinaDAO();
        this.gradeCurricularDAO = new GradeCurricularDAO();

        configurarControlesVisuais();

        associadasListView.setItems(associadasList);
        
        // Os controlos começam desativados (como no teu FXML)
        // e serão ativados pelo método setCurso()
    }

    /**
     * NOVO: Este é o método de entrada para esta tela.
     * Deve ser chamado pelo GestaoCursosController.
     */
    public void setCurso(Curso curso) {
        this.cursoSelecionado = curso;
        lblNomeCurso.setText(curso.getNomeCurso());

        // Ativa os controlos
        associadasListView.setDisable(false);
        removerButton.setDisable(false);
        disponiveisComboBox.setDisable(false);
        adicionarButton.setDisable(false);
        
        // Carrega os dados iniciais
        loadAssociadas();
        
        // Configura o autocompletar AGORA
        setupAutocompleteDisponiveis();
    }

    private void configurarControlesVisuais() {
        // Configura a ListView para mostrar o nome da disciplina
        associadasListView.setCellFactory(lv -> new ListCell<Disciplina>() {
            @Override
            protected void updateItem(Disciplina item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getNomeDisciplina());
            }
        });
    }

    /**
     * Carrega a lista de disciplinas JÁ associadas a este curso.
     */
    private void loadAssociadas() {
        if (cursoSelecionado == null) return;
        List<Disciplina> disciplinas = gradeCurricularDAO.getDisciplinasByCurso(cursoSelecionado.getId());
        associadasList.setAll(disciplinas);
    }
    
    /**
     * NOVO: Configura o autocompletar para o ComboBox de Disciplinas Disponíveis.
     */
    private void setupAutocompleteDisponiveis() {
        disponiveisComboBox.setEditable(true);

        // Ensina o ComboBox a converter String <-> Disciplina
        disponiveisComboBox.setConverter(new StringConverter<Disciplina>() {
            @Override
            public String toString(Disciplina object) {
                return object == null ? null : object.getNomeDisciplina();
            }
            @Override
            public Disciplina fromString(String string) {
                // Permite que o texto digitado seja usado
                return disponiveisComboBox.getItems().stream()
                         .filter(item -> item.getNomeDisciplina().equals(string))
                         .findFirst().orElse(null);
            }
        });

        // O "Ouvinte" que faz a busca
        disponiveisComboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                disponiveisComboBox.setItems(FXCollections.observableArrayList());
                return;
            }

            Disciplina selecionada = disponiveisComboBox.getSelectionModel().getSelectedItem();
            if (selecionada != null && selecionada.toString().equals(newText)) {
                return;
            }

            // 1. Busca no Banco de Dados (usando o método que já criámos)
            List<Disciplina> sugestoes = disciplinaDAO.searchByName(newText);
            
            // 2. LÓGICA CRÍTICA: Remove as disciplinas que já estão na lista "Associadas"
            List<Disciplina> sugestoesFiltradas = sugestoes.stream()
                    .filter(sugestao -> associadasList.stream()
                            .noneMatch(associada -> associada.getId() == sugestao.getId()))
                    .collect(Collectors.toList());

            disponiveisComboBox.setItems(FXCollections.observableArrayList(sugestoesFiltradas));
            disponiveisComboBox.show();
        });
    }


    @FXML
    private void onAdicionar() {
        Disciplina disciplinaParaAdicionar = disponiveisComboBox.getSelectionModel().getSelectedItem();
        
        if (disciplinaParaAdicionar == null || cursoSelecionado == null) {
            if (!disponiveisComboBox.getEditor().getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma disciplina válida da lista.");
            }
            return;
        }

        try {
            // Ação imediata no banco de dados (como o teu FXML sugere)
            gradeCurricularDAO.addDisciplina(cursoSelecionado.getId(), disciplinaParaAdicionar.getId());
            
            // Atualiza a lista de associadas
            loadAssociadas();
            
            // Limpa o ComboBox
            disponiveisComboBox.getSelectionModel().clearSelection();
            disponiveisComboBox.getEditor().clear();
            disponiveisComboBox.setItems(FXCollections.observableArrayList());

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível adicionar a disciplina à grade.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onRemover() {
        Disciplina disciplinaParaRemover = associadasListView.getSelectionModel().getSelectedItem();
        
        if (disciplinaParaRemover == null || cursoSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma disciplina da lista 'Associadas' para remover.");
            return;
        }

        try {
            // Ação imediata no banco de dados
            gradeCurricularDAO.removerDisciplina(cursoSelecionado.getId(), disciplinaParaRemover.getId());
            
            // Atualiza a lista
            loadAssociadas();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível remover a disciplina da grade.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) fecharButton.getScene().getWindow();
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