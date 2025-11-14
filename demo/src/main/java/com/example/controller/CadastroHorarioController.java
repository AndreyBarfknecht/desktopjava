package com.example.controller;

import com.example.model.Horario; 
import com.example.model.Professor;
import com.example.model.Turma;
import com.example.model.Disciplina; 
import com.example.repository.ProfessorDAO;      
import com.example.repository.TurmaDAO;         
import com.example.repository.HorarioDAO;       
import com.example.repository.TurmaProfessorDAO; 
import com.example.repository.DisciplinaDAO; 

import java.net.URL;
import java.sql.SQLException; 
import java.time.LocalDate; // Importar LocalDate
import java.util.List; // Importar List
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter; // NOVO: Importar StringConverter

public class CadastroHorarioController implements Initializable {

    @FXML private ComboBox<Disciplina> disciplinaComboBox;
    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private ComboBox<Professor> professorComboBox;
    @FXML private ComboBox<String> diaSemanaComboBox;
    @FXML private TextField horaInicioField;
    @FXML private TextField horaFimField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    private DisciplinaDAO disciplinaDAO;
    private TurmaDAO turmaDAO;
    private ProfessorDAO professorDAO;
    private TurmaProfessorDAO turmaProfessorDAO;
    private HorarioDAO horarioDAO;

    private Horario horarioParaEditar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.disciplinaDAO = new DisciplinaDAO();
        this.turmaDAO = new TurmaDAO();
        this.professorDAO = new ProfessorDAO();
        this.turmaProfessorDAO = new TurmaProfessorDAO();
        this.horarioDAO = new HorarioDAO();

        // --- MUDANÇA PRINCIPAL AQUI ---
        // NÃO carregamos mais "getAll()" para disciplina e professor
        
        // A Turma é pré-carregada (pois são poucas) ou definida pelo setTurmaParaHorario
        // Esta linha pode ser mantida ou removida, já que setTurmaParaHorario é o ideal
        turmaComboBox.setItems(FXCollections.observableArrayList(turmaDAO.getAll()));
        
        // Adicionamos os listeners de autocompletar
        setupAutocompleteDisciplina(disciplinaComboBox);
        setupAutocompleteProfessor(professorComboBox);
        
        // Configura o ComboBox de Turma (para mostrar o nome)
        turmaComboBox.setConverter(new StringConverter<Turma>() {
            @Override public String toString(Turma t) { return t == null ? "" : t.getNome(); }
            @Override public Turma fromString(String s) { return null; }
        });

        diaSemanaComboBox.getItems().addAll(
            "Segunda-feira", "Terça-feira", "Quarta-feira", 
            "Quinta-feira", "Sexta-feira", "Sábado"
        );
    }
    
    /**
     * NOVO: Configura o autocompletar para o ComboBox de Disciplinas.
     */
    private void setupAutocompleteDisciplina(ComboBox<Disciplina> comboBox) {
        comboBox.setEditable(true);
        
        // Ensina o ComboBox a converter String <-> Disciplina
        comboBox.setConverter(new StringConverter<Disciplina>() {
            @Override
            public String toString(Disciplina object) {
                return object == null ? "" : object.getNomeDisciplina();
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

            List<Disciplina> sugestoes = disciplinaDAO.searchByName(newText);
            Disciplina itemSelecionado = comboBox.getSelectionModel().getSelectedItem(); // Salva seleção
            comboBox.setItems(FXCollections.observableArrayList(sugestoes)); // Atualiza lista
            
            if (itemSelecionado != null && sugestoes.contains(itemSelecionado)) {
                comboBox.getSelectionModel().select(itemSelecionado); // Restaura seleção
            }
            comboBox.show();
        });
    }

    /**
     * NOVO: Configura o autocompletar para o ComboBox de Professores.
     */
    private void setupAutocompleteProfessor(ComboBox<Professor> comboBox) {
        comboBox.setEditable(true);

        // Ensina o ComboBox a converter String <-> Professor
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
            Professor itemSelecionado = comboBox.getSelectionModel().getSelectedItem(); // Salva seleção
            comboBox.setItems(FXCollections.observableArrayList(sugestoes)); // Atualiza lista
            
            if (itemSelecionado != null && sugestoes.contains(itemSelecionado)) {
                comboBox.getSelectionModel().select(itemSelecionado); // Restaura seleção
            }
            comboBox.show(); 
        });
    }

    // Método para modo de edição (Atualizado)
    public void setHorarioParaEdicao(Horario horario) {
        this.horarioParaEditar = horario;

        horaInicioField.setText(horario.getHoraInicio());
        horaFimField.setText(horario.getHoraFim());
        diaSemanaComboBox.setValue(horario.getDiaSemana());
        salvarButton.setText("Atualizar");

        // Adiciona e seleciona os itens nos ComboBoxes agora vazios
        disciplinaComboBox.getItems().setAll(horario.getDisciplina());
        disciplinaComboBox.getSelectionModel().select(horario.getDisciplina());

        turmaComboBox.setValue(horario.getTurma()); // Este já estava carregado ou é pré-selecionado

        professorComboBox.getItems().setAll(horario.getProfessor());
        professorComboBox.getSelectionModel().select(horario.getProfessor());
    }

    // Método para pré-selecionar a turma (vinda da Gestão de Turmas)
    public void setTurmaParaHorario(Turma turma) {
        if (turmaComboBox != null) {
            // Não precisa carregar "getAll", apenas define o valor
            turmaComboBox.getItems().setAll(turma);
            turmaComboBox.setValue(turma);
            turmaComboBox.setDisable(true);
        }
    }

    @FXML
    private void onSalvar() {
        // --- CORREÇÃO: Usar getSelectionModel().getSelectedItem() ---
        Disciplina disciplina = disciplinaComboBox.getSelectionModel().getSelectedItem();
        Turma turma = turmaComboBox.getSelectionModel().getSelectedItem();
        Professor professor = professorComboBox.getSelectionModel().getSelectedItem();
        String dia = diaSemanaComboBox.getValue();
        String inicio = horaInicioField.getText();
        String fim = horaFimField.getText();

        if (!isDataValid(disciplina, turma, professor, dia, inicio, fim)) {
            return;
        }
        
        try {
            if (horarioParaEditar == null) {
                Horario novoHorario = new Horario(disciplina, turma, professor, dia, inicio, fim);
                horarioDAO.save(novoHorario);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Horário salvo com sucesso!");
            } else {
                horarioParaEditar.setDisciplina(disciplina); 
                horarioParaEditar.setTurma(turma);
                horarioParaEditar.setProfessor(professor);
                horarioParaEditar.setDiaSemana(dia);
                horarioParaEditar.setHoraInicio(inicio);
                horarioParaEditar.setHoraFim(fim);
                horarioDAO.update(horarioParaEditar);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Horário atualizado com sucesso!");
            }
            fecharJanela();
        } catch (SQLException e) {
             System.err.println("Erro ao salvar horário: " + e.getMessage());
            if (e.getErrorCode() == 1062) { 
                showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Conflito de horário detectado. Verifique os dados.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar o horário.");
            }
            e.printStackTrace();
        }
    }

    // Método de validação atualizado
    private boolean isDataValid(Disciplina d, Turma t, Professor p, String dia, String inicio, String fim) {
        if (d == null || t == null || p == null || dia == null || inicio.trim().isEmpty() || fim.trim().isEmpty()) {
            
            // Feedback melhor se o utilizador digitou mas não selecionou
            if (d == null && !disciplinaComboBox.getEditor().getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Disciplina inválida. Por favor, selecione uma da lista.");
                return false;
            }
             if (p == null && !professorComboBox.getEditor().getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Professor inválido. Por favor, selecione um da lista.");
                return false;
            }

            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Todos os campos são obrigatórios.");
            return false;
        }
        
        String timeRegex = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        if (!inicio.matches(timeRegex) || !fim.matches(timeRegex)) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O formato da hora deve ser HH:MM (ex: 08:30).");
            return false;
        }
        return true;
    }

    @FXML
    private void onCancelar() {
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
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