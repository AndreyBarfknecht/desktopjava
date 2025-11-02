package com.example.controller;

import com.example.model.Professor; // ALTERADO
import com.example.model.Disciplina;
import com.example.repository.ProfessorDAO; // ALTERADO
import com.example.repository.DisciplinaDAO;
import com.example.repository.ProfessorDisciplinaDAO; // ALTERADO

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller para gerir a associação N-N entre Professores e Disciplinas.
 * Baseado no GestaoGradeController.
 */
public class GestaoProfessorDisciplinaController implements Initializable {

    // --- FXML ---
    @FXML private ComboBox<Professor> professorComboBox; // ALTERADO
    @FXML private ListView<Disciplina> associadasListView;
    @FXML private Button removerButton;
    @FXML private ComboBox<Disciplina> disponiveisComboBox;
    @FXML private Button adicionarButton;
    @FXML private Button fecharButton;

    // --- DAOs ---
    private ProfessorDAO professorDAO; // ALTERADO
    private DisciplinaDAO disciplinaDAO;
    private ProfessorDisciplinaDAO professorDisciplinaDAO; // ALTERADO

    // --- Listas Observáveis ---
    private ObservableList<Professor> professoresList = FXCollections.observableArrayList(); // ALTERADO
    private ObservableList<Disciplina> disciplinasAssociadasList = FXCollections.observableArrayList();
    private ObservableList<Disciplina> disciplinasDisponiveisList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa os DAOs
        professorDAO = new ProfessorDAO(); // ALTERADO
        disciplinaDAO = new DisciplinaDAO();
        professorDisciplinaDAO = new ProfessorDisciplinaDAO(); // ALTERADO

        configurarControlesVisuais();
        carregarProfessores(); // ALTERADO

        associadasListView.setItems(disciplinasAssociadasList);
        disponiveisComboBox.setItems(disciplinasDisponiveisList);

        adicionarListeners();
    }

    private void configurarControlesVisuais() {
        // Converte Professor para String no ComboBox
        professorComboBox.setConverter(new StringConverter<Professor>() { // ALTERADO
            @Override
            public String toString(Professor professor) { // ALTERADO
                return professor == null ? null : professor.getNomeCompleto(); // ALTERADO
            }
            @Override
            public Professor fromString(String string) { return null; } // ALTERADO
        });

        // (O resto deste método é idêntico ao GestaoGradeController)
        disponiveisComboBox.setConverter(new StringConverter<Disciplina>() {
            @Override
            public String toString(Disciplina disciplina) {
                return disciplina == null ? null : disciplina.getNomeDisciplina();
            }
            @Override
            public Disciplina fromString(String string) { return null; }
        });

        associadasListView.setCellFactory(param -> new ListCell<Disciplina>() {
            @Override
            protected void updateItem(Disciplina item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomeDisciplina());
            }
        });
    }

    // ALTERADO: Carrega professores em vez de cursos
    private void carregarProfessores() {
        professoresList.setAll(professorDAO.getAll());
        professorComboBox.setItems(professoresList);
    }

    private void adicionarListeners() {
        // Listener para quando um PROFESSOR é selecionado
        professorComboBox.valueProperty().addListener((obs, oldProfessor, newProfessor) -> { // ALTERADO
            if (newProfessor != null) {
                carregarDisciplinasDoProfessor(newProfessor); // ALTERADO
                carregarDisciplinasDisponiveis(newProfessor); // ALTERADO
                
                associadasListView.setDisable(false);
                disponiveisComboBox.setDisable(false);
            } else {
                disciplinasAssociadasList.clear();
                disciplinasDisponiveisList.clear();
                associadasListView.setDisable(true);
                disponiveisComboBox.setDisable(true);
                removerButton.setDisable(true);
                adicionarButton.setDisable(true);
            }
        });

        // (Listeners dos botões permanecem iguais)
        associadasListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            removerButton.setDisable(newSelection == null);
        });

        disponiveisComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            adicionarButton.setDisable(newSelection == null);
        });
    }

    // ALTERADO: Busca disciplinas do professor
    private void carregarDisciplinasDoProfessor(Professor professor) {
        if (professor == null) return;
        List<Disciplina> disciplinas = professorDisciplinaDAO.getDisciplinasDoProfessor(professor.getId());
        disciplinasAssociadasList.setAll(disciplinas);
    }

    // ALTERADO: Lógica de filtro (apenas o parâmetro)
    private void carregarDisciplinasDisponiveis(Professor professor) {
        if (professor == null) return;
        List<Disciplina> todasDisciplinas = disciplinaDAO.getAll();
        List<Disciplina> associadas = disciplinasAssociadasList; 

        List<Disciplina> disponiveis = todasDisciplinas.stream()
                .filter(disciplina -> associadas.stream().noneMatch(assoc -> assoc.getId() == disciplina.getId()))
                .collect(Collectors.toList());

        disciplinasDisponiveisList.setAll(disponiveis);
    }

    @FXML
    private void onAdicionar() {
        Professor professorSelecionado = professorComboBox.getSelectionModel().getSelectedItem(); // ALTERADO
        Disciplina disciplinaParaAdicionar = disponiveisComboBox.getSelectionModel().getSelectedItem();

        if (professorSelecionado == null || disciplinaParaAdicionar == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Selecione um professor e uma disciplina disponível.");
            return;
        }

        try {
            // ALTERADO: Chama o DAO correto
            professorDisciplinaDAO.adicionarAssociacao(professorSelecionado.getId(), disciplinaParaAdicionar.getId());
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Disciplina '" + disciplinaParaAdicionar.getNomeDisciplina() + "' associada ao professor '" + professorSelecionado.getNomeCompleto() + "'.");

            carregarDisciplinasDoProfessor(professorSelecionado); // ALTERADO
            carregarDisciplinasDisponiveis(professorSelecionado); // ALTERADO
            disponiveisComboBox.getSelectionModel().clearSelection();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao associar a disciplina.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onRemover() {
        Professor professorSelecionado = professorComboBox.getSelectionModel().getSelectedItem(); // ALTERADO
        Disciplina disciplinaParaRemover = associadasListView.getSelectionModel().getSelectedItem();

        if (professorSelecionado == null || disciplinaParaRemover == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Selecione um professor e uma disciplina da lista.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Remoção");
        alert.setHeaderText("Remover Associação");
        alert.setContentText("Tem certeza que deseja remover a disciplina '" + disciplinaParaRemover.getNomeDisciplina() + "' do professor '" + professorSelecionado.getNomeCompleto() + "'?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // ALTERADO: Chama o DAO correto
                professorDisciplinaDAO.removerAssociacao(professorSelecionado.getId(), disciplinaParaRemover.getId());
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Associação removida com sucesso.");

                carregarDisciplinasDoProfessor(professorSelecionado); // ALTERADO
                carregarDisciplinasDisponiveis(professorSelecionado); // ALTERADO
                associadasListView.getSelectionModel().clearSelection();

            } catch (Exception e) { 
                showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro ao remover a associação.");
                e.printStackTrace();
            }
        }
    }

    // --- Métodos onFechar e showAlert (idênticos) ---

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