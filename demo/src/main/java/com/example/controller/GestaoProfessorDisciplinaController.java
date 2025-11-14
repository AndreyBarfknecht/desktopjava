package com.example.controller;

import com.example.model.Disciplina;
import com.example.model.Professor;
import com.example.repository.DisciplinaDAO;
import com.example.repository.ProfessorDAO;
import com.example.repository.ProfessorDisciplinaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException; // Importar SQLException
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GestaoProfessorDisciplinaController implements Initializable {

    // --- FXML Fields (baseado no teu FXML) ---
    @FXML private ComboBox<Professor> professorComboBox;
    @FXML private ListView<Disciplina> associadasListView;
    @FXML private Button removerButton;
    @FXML private ComboBox<Disciplina> disponiveisComboBox;
    @FXML private Button adicionarButton;
    @FXML private Button fecharButton;

    // --- DAOs ---
    private ProfessorDAO professorDAO;
    private DisciplinaDAO disciplinaDAO;
    private ProfessorDisciplinaDAO professorDisciplinaDAO;

    // --- Controlo de Estado ---
    private Professor professorSelecionado; // Variável de classe
    private final ObservableList<Disciplina> associadasList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.professorDAO = new ProfessorDAO();
        this.disciplinaDAO = new DisciplinaDAO();
        this.professorDisciplinaDAO = new ProfessorDisciplinaDAO();

        configurarControlesVisuais();

        associadasListView.setItems(associadasList);

        // Configura o autocompletar para o ComboBox de Professor
        setupAutocompleteProfessor(professorComboBox);
        
        // Configura o autocompletar para o ComboBox de Disciplinas (só será ativado após selecionar um prof)
        setupAutocompleteDisponiveis(disponiveisComboBox);
        
        // Ouve a seleção do professor
        professorComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                this.professorSelecionado = newVal; // Define a variável de classe
                // Ativa os outros controlos e carrega os dados
                disponiveisComboBox.setDisable(false);
                adicionarButton.setDisable(false);
                removerButton.setDisable(false);
                loadDisciplinasDoProfessor(newVal); // Passa o professor para o método
            } else {
                // Desativa tudo se nenhum professor for selecionado
                this.professorSelecionado = null;
                associadasList.clear();
                disponiveisComboBox.setDisable(true);
                adicionarButton.setDisable(true);
                removerButton.setDisable(true);
            }
        });
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
     * Carrega a lista de disciplinas JÁ associadas a este professor.
     * Este método agora recebe o professor como parâmetro.
     */
    private void loadDisciplinasDoProfessor(Professor professor) {
        if (professor == null) return;
        
        // --- ESTA É A LINHA 98 CORRIGIDA ---
        // Ela usa o 'professor' (parâmetro) e o 'getDisciplinasByProfessorId' (nome correto)
        List<Disciplina> disciplinas = professorDisciplinaDAO.getDisciplinasDoProfessor(professor.getId());
        associadasList.setAll(disciplinas);
    }
    
    /**
     * NOVO: Configura o autocompletar para o ComboBox de Professores.
     */
    private void setupAutocompleteProfessor(ComboBox<Professor> comboBox) {
        comboBox.setEditable(true);
        
        comboBox.setConverter(new StringConverter<Professor>() {
            @Override
            public String toString(Professor object) {
                return object == null ? "" : object.getNomeCompleto();
            }
            @Override
            public Professor fromString(String string) {
                return comboBox.getItems().stream()
                         .filter(item -> item.getNomeCompleto().equals(string))
                         .findFirst().orElse(null);
            }
        });

        comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                comboBox.setItems(FXCollections.observableArrayList());
                return;
            }
            Professor selecionado = comboBox.getSelectionModel().getSelectedItem();
            if (selecionado != null && selecionado.toString().equals(newText)) {
                return;
            }

            List<Professor> sugestoes = professorDAO.searchByName(newText);
            Professor itemSelecionado = comboBox.getSelectionModel().getSelectedItem();
            comboBox.setItems(FXCollections.observableArrayList(sugestoes));
            
            if (itemSelecionado != null && sugestoes.contains(itemSelecionado)) {
                comboBox.getSelectionModel().select(itemSelecionado);
            }
            comboBox.show();
        });
    }

    /**
     * NOVO: Configura o autocompletar para o ComboBox de Disciplinas Disponíveis.
     */
    private void setupAutocompleteDisponiveis(ComboBox<Disciplina> comboBox) {
        comboBox.setEditable(true);

        comboBox.setConverter(new StringConverter<Disciplina>() {
            @Override
            public String toString(Disciplina object) {
                return object == null ? null : object.getNomeDisciplina();
            }
            @Override
            public Disciplina fromString(String string) {
                return comboBox.getItems().stream()
                         .filter(item -> item.getNomeDisciplina().equals(string))
                         .findFirst().orElse(null);
            }
        });

        comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                comboBox.setItems(FXCollections.observableArrayList());
                return;
            }

            Disciplina selecionada = comboBox.getSelectionModel().getSelectedItem();
            if (selecionada != null && selecionada.toString().equals(newText)) {
                return;
            }

            // 1. Busca no Banco de Dados
            List<Disciplina> sugestoes = disciplinaDAO.searchByName(newText);
            
            // 2. Filtra as que já estão associadas
            List<Disciplina> sugestoesFiltradas = sugestoes.stream()
                    .filter(sugestao -> associadasList.stream()
                            .noneMatch(associada -> associada.getId() == sugestao.getId()))
                    .collect(Collectors.toList());

            comboBox.setItems(FXCollections.observableArrayList(sugestoesFiltradas));
            comboBox.show();
        });
    }

    public void setProfessor(Professor professor) {
    if (professor != null && professorComboBox != null) {
        // Adiciona o professor à lista (que estaria vazia) e seleciona-o
        professorComboBox.getItems().setAll(professor);
        professorComboBox.getSelectionModel().select(professor);
        
        // Desativa o ComboBox para que o utilizador não possa trocar
        professorComboBox.setDisable(true);
    }
}


    @FXML
    private void onAdicionar() {
        Disciplina disciplinaParaAdicionar = disponiveisComboBox.getSelectionModel().getSelectedItem();
        
        if (disciplinaParaAdicionar == null || professorSelecionado == null) {
            if (!disponiveisComboBox.getEditor().getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma disciplina válida da lista.");
            }
            return;
        }

        try {
            // Ação imediata no banco de dados
            professorDisciplinaDAO.addDisciplina(professorSelecionado.getId(), disciplinaParaAdicionar.getId());
            
            // Atualiza a lista de associadas
            loadDisciplinasDoProfessor(professorSelecionado); // Passa o professor
            
            // Limpa o ComboBox
            disponiveisComboBox.getSelectionModel().clearSelection();
            disponiveisComboBox.getEditor().clear();
            disponiveisComboBox.setItems(FXCollections.observableArrayList());

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível associar a disciplina ao professor.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onRemover() {
        Disciplina disciplinaParaRemover = associadasListView.getSelectionModel().getSelectedItem();
        
        if (disciplinaParaRemover == null || professorSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma disciplina da lista 'Associadas' para remover.");
            return;
        }

        try {
            // Ação imediata no banco de dados
            professorDisciplinaDAO.removerDisciplina(professorSelecionado.getId(), disciplinaParaRemover.getId());
            
            // Atualiza a lista
            loadDisciplinasDoProfessor(professorSelecionado); // Passa o professor
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível remover a disciplina.");
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