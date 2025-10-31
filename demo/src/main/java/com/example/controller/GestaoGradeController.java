package com.example.controller;

import com.example.model.Curso;
import com.example.model.Disciplina;
import com.example.repository.CursoDAO;
import com.example.repository.DisciplinaDAO;
import com.example.repository.GradeCurricularDAO;
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

public class GestaoGradeController implements Initializable {

    @FXML private ComboBox<Curso> cursoComboBox;
    @FXML private ListView<Disciplina> associadasListView;
    @FXML private Button removerButton;
    @FXML private ComboBox<Disciplina> disponiveisComboBox;
    @FXML private Button adicionarButton;
    @FXML private Button fecharButton;

    private CursoDAO cursoDAO;
    private DisciplinaDAO disciplinaDAO;
    private GradeCurricularDAO gradeCurricularDAO;

    // Listas Observáveis para atualizar a UI automaticamente
    private ObservableList<Curso> cursosList = FXCollections.observableArrayList();
    private ObservableList<Disciplina> disciplinasAssociadasList = FXCollections.observableArrayList();
    private ObservableList<Disciplina> disciplinasDisponiveisList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa os DAOs
        cursoDAO = new CursoDAO();
        disciplinaDAO = new DisciplinaDAO();
        gradeCurricularDAO = new GradeCurricularDAO();

        // Configura como os nomes serão exibidos nos ComboBoxes e ListView
        configurarControlesVisuais();

        // Carrega os cursos no ComboBox principal
        carregarCursos();

        // Associa as listas observáveis aos controles da UI
        associadasListView.setItems(disciplinasAssociadasList);
        disponiveisComboBox.setItems(disciplinasDisponiveisList);

        // Adiciona listeners para habilitar/desabilitar botões
        adicionarListeners();
    }

    private void configurarControlesVisuais() {
        // Converte Curso para String no ComboBox
        cursoComboBox.setConverter(new StringConverter<Curso>() {
            @Override
            public String toString(Curso curso) {
                return curso == null ? null : curso.getNomeCurso();
            }
            @Override
            public Curso fromString(String string) { return null; } // Não necessário para seleção
        });

        // Converte Disciplina para String no ComboBox de disponíveis
        disponiveisComboBox.setConverter(new StringConverter<Disciplina>() {
            @Override
            public String toString(Disciplina disciplina) {
                return disciplina == null ? null : disciplina.getNomeDisciplina();
            }
            @Override
            public Disciplina fromString(String string) { return null; }
        });

        // Configura como a Disciplina é exibida na ListView
        associadasListView.setCellFactory(param -> new ListCell<Disciplina>() {
            @Override
            protected void updateItem(Disciplina item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomeDisciplina());
            }
        });
    }

    private void carregarCursos() {
        cursosList.setAll(cursoDAO.getAll());
        cursoComboBox.setItems(cursosList);
    }

    private void adicionarListeners() {
        // Listener para quando um CURSO é selecionado
        cursoComboBox.valueProperty().addListener((obs, oldCurso, newCurso) -> {
            if (newCurso != null) {
                // Carrega as disciplinas associadas e disponíveis para o curso selecionado
                carregarDisciplinasDoCurso(newCurso);
                carregarDisciplinasDisponiveis(newCurso);
                // Habilita controles dependentes
                associadasListView.setDisable(false);
                disponiveisComboBox.setDisable(false);
            } else {
                // Limpa e desabilita se nenhum curso estiver selecionado
                disciplinasAssociadasList.clear();
                disciplinasDisponiveisList.clear();
                associadasListView.setDisable(true);
                disponiveisComboBox.setDisable(true);
                removerButton.setDisable(true);
                adicionarButton.setDisable(true);
            }
        });

        // Listener para habilitar/desabilitar botão REMOVER
        associadasListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            removerButton.setDisable(newSelection == null); // Habilita se algo for selecionado
        });

        // Listener para habilitar/desabilitar botão ADICIONAR
        disponiveisComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            adicionarButton.setDisable(newSelection == null); // Habilita se algo for selecionado
        });
    }

    private void carregarDisciplinasDoCurso(Curso curso) {
        if (curso == null) return;
        List<Disciplina> disciplinas = gradeCurricularDAO.getDisciplinasByCurso(curso.getId());
        disciplinasAssociadasList.setAll(disciplinas);
    }

    private void carregarDisciplinasDisponiveis(Curso curso) {
        if (curso == null) return;
        List<Disciplina> todasDisciplinas = disciplinaDAO.getAll();
        List<Disciplina> associadas = disciplinasAssociadasList; // Pega a lista já carregada

        // Filtra: mantém apenas as disciplinas que NÃO estão na lista de associadas
        List<Disciplina> disponiveis = todasDisciplinas.stream()
                .filter(disciplina -> associadas.stream().noneMatch(assoc -> assoc.getId() == disciplina.getId()))
                .collect(Collectors.toList());

        disciplinasDisponiveisList.setAll(disponiveis);
    }

    @FXML
    private void onAdicionar() {
        Curso cursoSelecionado = cursoComboBox.getSelectionModel().getSelectedItem();
        Disciplina disciplinaParaAdicionar = disponiveisComboBox.getSelectionModel().getSelectedItem();

        if (cursoSelecionado == null || disciplinaParaAdicionar == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Selecione um curso e uma disciplina disponível para adicionar.");
            return;
        }

        try {
            gradeCurricularDAO.adicionarDisciplinaNaGrade(cursoSelecionado.getId(), disciplinaParaAdicionar.getId());
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Disciplina '" + disciplinaParaAdicionar.getNomeDisciplina() + "' adicionada à grade do curso '" + cursoSelecionado.getNomeCurso() + "'.");

            // Atualiza as listas após adicionar
            carregarDisciplinasDoCurso(cursoSelecionado);
            carregarDisciplinasDisponiveis(cursoSelecionado);
            disponiveisComboBox.getSelectionModel().clearSelection(); // Limpa seleção

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao adicionar a disciplina à grade.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onRemover() {
        Curso cursoSelecionado = cursoComboBox.getSelectionModel().getSelectedItem();
        Disciplina disciplinaParaRemover = associadasListView.getSelectionModel().getSelectedItem();

        if (cursoSelecionado == null || disciplinaParaRemover == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Selecione um curso e uma disciplina da lista para remover.");
            return;
        }

        // Alerta de confirmação
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Remoção");
        alert.setHeaderText("Remover Disciplina da Grade");
        alert.setContentText("Tem certeza que deseja remover a disciplina '" + disciplinaParaRemover.getNomeDisciplina() + "' da grade do curso '" + cursoSelecionado.getNomeCurso() + "'?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gradeCurricularDAO.removerDisciplinaDaGrade(cursoSelecionado.getId(), disciplinaParaRemover.getId());
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Disciplina removida com sucesso.");

                // Atualiza as listas após remover
                carregarDisciplinasDoCurso(cursoSelecionado);
                carregarDisciplinasDisponiveis(cursoSelecionado);
                associadasListView.getSelectionModel().clearSelection(); // Limpa seleção

            } catch (Exception e) { // Captura genérica pois removerDisciplinaDaGrade não lança SQLException (pode mudar se quiser)
                showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro ao remover a disciplina.");
                e.printStackTrace();
            }
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