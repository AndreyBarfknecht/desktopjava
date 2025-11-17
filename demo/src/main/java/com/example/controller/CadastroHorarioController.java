package com.example.controller;

import com.example.model.Disciplina;
import com.example.model.Professor;
import com.example.model.Turma;
import com.example.model.Horario;
import com.example.repository.DisciplinaDAO;
import com.example.repository.ProfessorDAO; // Mantido para o setHorario (embora o DAO novo seja melhor)
import com.example.repository.HorarioDAO;
import com.example.repository.ProfessorDisciplinaDAO; // --- DAO NOVO ---
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections; // --- IMPORT NOVO ---
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter; // Mantido para os conversores

public class CadastroHorarioController implements Initializable {

    @FXML private ComboBox<Disciplina> disciplinaComboBox;
    @FXML private ComboBox<Professor> professorComboBox;
    @FXML private ComboBox<String> diaSemanaComboBox;
    @FXML private TextField horaInicioField;
    @FXML private TextField horaFimField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;
    @FXML private Label lblNomeTurma;
    @FXML private Label lblTituloJanela;

    private DisciplinaDAO disciplinaDAO;
    private ProfessorDAO professorDAO; // Mantido por segurança
    private HorarioDAO horarioDAO;
    private ProfessorDisciplinaDAO professorDisciplinaDAO; // --- DAO ADICIONADO ---
    
    private Horario horarioParaEditar;
    private Turma turmaSelecionada;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.disciplinaDAO = new DisciplinaDAO();
        this.professorDAO = new ProfessorDAO();
        this.horarioDAO = new HorarioDAO();
        this.professorDisciplinaDAO = new ProfessorDisciplinaDAO(); // --- INICIALIZA O DAO ---
        
        // Removemos os setups de autocomplete
        
        // Configura a aparência dos ComboBoxes
        configurarConversores();
        
        // Configura a lógica em cascata
        configurarDependenciaProfessor();

        diaSemanaComboBox.getItems().addAll(
            "Segunda-feira", "Terça-feira", "Quarta-feira", 
            "Quinta-feira", "Sexta-feira", "Sábado"
        );
    }
    
    /**
     * Configura os StringConverters para que os ComboBoxes saibam
     * como exibir os nomes dos objetos Disciplina e Professor.
     */
    private void configurarConversores() {
        disciplinaComboBox.setConverter(new StringConverter<Disciplina>() {
            @Override
            public String toString(Disciplina object) {
                return object == null ? "Selecione uma disciplina..." : object.getNomeDisciplina();
            }
            @Override
            public Disciplina fromString(String string) { return null; } // Não usado em não-editável
        });
        
        professorComboBox.setConverter(new StringConverter<Professor>() {
            @Override
            public String toString(Professor object) {
                return object == null ? "Selecione um professor..." : object.getNomeCompleto();
            }
            @Override
            public Professor fromString(String string) { return null; } // Não usado em não-editável
        });
        
        // Define o texto inicial
        disciplinaComboBox.setPromptText("Selecione uma disciplina...");
        professorComboBox.setPromptText("Selecione um professor...");
    }
    
    /**
     * NOVO: Configura o listener que filtra os professores.
     * Quando uma disciplina é escolhida, este método é ativado
     * e carrega os professores correspondentes.
     */
    private void configurarDependenciaProfessor() {
        disciplinaComboBox.valueProperty().addListener((obs, oldDisciplina, newDisciplina) -> {
            if (newDisciplina != null) {
                // Se uma nova disciplina for selecionada, carrega os professores dela
                carregarProfessoresDaDisciplina(newDisciplina.getId());
            } else {
                // Se a disciplina for limpa, limpa os professores
                professorComboBox.getItems().clear();
            }
        });
    }

    /**
     * NOVO: Carrega a lista de disciplinas do curso.
     * Usa o método que criamos anteriormente.
     */
    private void carregarDisciplinasDoCurso(int cursoId) {
        // Usamos "" para buscar todas as disciplinas (LIKE '%%') daquele curso
        List<Disciplina> disciplinasDoCurso = disciplinaDAO.searchByNameAndCursoId("", cursoId);
        disciplinaComboBox.setItems(FXCollections.observableArrayList(disciplinasDoCurso));
    }
    
    /**
     * NOVO: Carrega a lista de professores filtrada pela disciplina.
     * Usa o DAO de associação 'professor_disciplina'.
     */
    private void carregarProfessoresDaDisciplina(int disciplinaId) {
        List<Professor> professoresDaDisciplina = professorDisciplinaDAO.getProfessoresByDisciplinaId(disciplinaId);
        professorComboBox.setItems(FXCollections.observableArrayList(professoresDaDisciplina));
    }


    /**
     * Define o formulário para o modo de EDIÇÃO.
     * MÉTODO MODIFICADO
     */
    public void setHorarioParaEdicao(Horario horario) {
        this.horarioParaEditar = horario;
        lblTituloJanela.setText("Editar Horário"); 
        this.turmaSelecionada = horario.getTurma();
        lblNomeTurma.setText(horario.getNomeTurma());

        horaInicioField.setText(horario.getHoraInicio());
        horaFimField.setText(horario.getHoraFim());
        diaSemanaComboBox.setValue(horario.getDiaSemana());
        salvarButton.setText("Atualizar");

        // --- LÓGICA DE CARGA MODIFICADA ---
        
        // 1. Carrega todas as disciplinas do curso
        carregarDisciplinasDoCurso(horario.getTurma().getCurso().getId());
        // 2. Seleciona a disciplina salva
        disciplinaComboBox.setValue(horario.getDisciplina());

        // 3. Carrega os professores daquela disciplina
        carregarProfessoresDaDisciplina(horario.getDisciplina().getId());
        // 4. Seleciona o professor salvo
        professorComboBox.setValue(horario.getProfessor());
    }

    /**
     * Define o formulário para o modo de CRIAÇÃO.
     * MÉTODO MODIFICADO
     */
    public void setTurmaParaHorario(Turma turma) {
        this.turmaSelecionada = turma;
        if (lblNomeTurma != null) {
            lblNomeTurma.setText(turma.getNome());
        }
        if (lblTituloJanela != null) {
            lblTituloJanela.setText("Cadastro de Horário");
        }
        
        // --- LÓGICA DE CARGA ADICIONADA ---
        // 1. Carrega as disciplinas do curso da turma
        carregarDisciplinasDoCurso(turma.getCurso().getId());
        // 2. Limpa a seleção de professor (caso a janela esteja sendo reutilizada)
        professorComboBox.getItems().clear();
    }

    @FXML
    private void onSalvar() {
        // ... (Este método continua 100% igual)
        Disciplina disciplina = disciplinaComboBox.getSelectionModel().getSelectedItem();
        Turma turma = this.turmaSelecionada; 
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

    private boolean isDataValid(Disciplina d, Turma t, Professor p, String dia, String inicio, String fim) {
        // --- VALIDAÇÃO MODIFICADA (REMOVEMOS A CHECAGEM DE TEXTO DO EDITOR) ---
        if (d == null || t == null || p == null || dia == null || inicio.trim().isEmpty() || fim.trim().isEmpty()) {
            
            // Removemos as checagens de "getEditor()" pois não são mais editáveis
            
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
    
    // --- MÉTODOS 'setupAutocompleteDisciplina' E 'setupAutocompleteProfessor' REMOVIDOS ---
    
}