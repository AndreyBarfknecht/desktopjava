package com.example.controller;

import com.example.model.Aluno;
import com.example.model.Disciplina;
import com.example.model.Nota;
import com.example.model.Turma;
import com.example.repository.AlunoDAO;
import com.example.repository.GradeCurricularDAO;
import com.example.repository.MatriculaDAO; // <-- IMPORTAR O NOVO DAO
import com.example.repository.NotaDAO;
import com.example.repository.TurmaDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

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

public class RegistroNotasController implements Initializable {

    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private ListView<Aluno> alunosListView;
    @FXML private ComboBox<Disciplina> disciplinaComboBox;
    @FXML private TextField avaliacaoField;
    @FXML private TextField notaField;
    @FXML private Button salvarNotaButton;

    // DAOs
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO;
    private NotaDAO notaDAO;
    private GradeCurricularDAO gradeCurricularDAO;
    private MatriculaDAO matriculaDAO; // <-- ADICIONAR VARIÁVEL PARA O DAO

    private final ObservableList<Aluno> alunosDaTurma = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa os DAOs
        this.turmaDAO = new TurmaDAO();
        this.alunoDAO = new AlunoDAO();
        this.notaDAO = new NotaDAO();
        this.gradeCurricularDAO = new GradeCurricularDAO();
        this.matriculaDAO = new MatriculaDAO(); // <-- INICIALIZAR O DAO

        configurarControles();
        
        turmaComboBox.setItems(FXCollections.observableArrayList(turmaDAO.getAll()));
        alunosListView.setItems(alunosDaTurma);

        disciplinaComboBox.setDisable(true);

        // Listener da Turma
        turmaComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, turmaSelecionada) -> {
                if (turmaSelecionada != null) {
                    alunosDaTurma.setAll(alunoDAO.getAlunosByTurmaId(turmaSelecionada.getId()));
                    carregarDisciplinasDaTurma(turmaSelecionada);
                    disciplinaComboBox.setDisable(false);
                } else {
                    alunosDaTurma.clear();
                    disciplinaComboBox.getItems().clear();
                    disciplinaComboBox.setDisable(true);
                }
            }
        );
    }
    
    private void configurarControles() {
        // Configura ComboBox de Turma
        turmaComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Turma turma) {
                return turma == null ? "" : turma.getNome();
            }
            @Override
            public Turma fromString(String string) { return null; }
        });

        // Configura ListView de Aluno
        alunosListView.setCellFactory(param -> new ListCell<Aluno>() {
            @Override
            protected void updateItem(Aluno aluno, boolean empty) {
                super.updateItem(aluno, empty);
                setText(empty || aluno == null ? "" : aluno.getNomeCompleto());
            }
        });
        
        // Configura ComboBox de Disciplina
        disciplinaComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Disciplina disciplina) {
                return disciplina == null ? "" : disciplina.getNomeDisciplina();
            }
            @Override
            public Disciplina fromString(String string) { return null; }
        });
    }

    /**
     * Busca as disciplinas associadas ao curso da turma selecionada.
     */
    private void carregarDisciplinasDaTurma(Turma turma) {
        if (turma == null || turma.getCurso() == null) {
            disciplinaComboBox.getItems().clear();
            return;
        }
        
        List<Disciplina> disciplinas = gradeCurricularDAO.getDisciplinasByCurso(turma.getCurso().getId());
        disciplinaComboBox.setItems(FXCollections.observableArrayList(disciplinas));
    }


    @FXML
    private void salvarNota() {
        // --- PEGAR TODOS OS DADOS NECESSÁRIOS ---
        Aluno alunoSelecionado = alunosListView.getSelectionModel().getSelectedItem();
        Disciplina disciplinaSelecionada = disciplinaComboBox.getSelectionModel().getSelectedItem();
        Turma turmaSelecionada = turmaComboBox.getSelectionModel().getSelectedItem(); // <-- PRECISAMOS DA TURMA

        // --- VALIDAÇÕES INICIAIS ---
        if (alunoSelecionado == null || turmaSelecionada == null) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Selecione uma turma e um aluno para lançar a nota.");
            return;
        }

        try {
            String avaliacao = avaliacaoField.getText();
            double valor = Double.parseDouble(notaField.getText().replace(",", "."));

            if (disciplinaSelecionada == null || avaliacao.isBlank()) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Selecione um aluno, uma disciplina e preencha a avaliação.");
                return;
            }

            // --- LÓGICA PRINCIPAL DA MUDANÇA ---

            // 1. Buscar o ID da Matrícula usando o Aluno ID e a Turma ID
            Integer idMatricula = matriculaDAO.getMatriculaId(
                alunoSelecionado.getId(),
                turmaSelecionada.getId()
            );

            // 2. Verificar se o aluno está realmente matriculado nessa turma
            if (idMatricula == null) {
                showAlert(Alert.AlertType.ERROR, "Erro de Matrícula",
                    "O aluno '" + alunoSelecionado.getNomeCompleto() +
                    "' não parece estar matriculado na turma '" + turmaSelecionada.getNome() + "'.");
                return;
            }

            // 3. Criar o objeto Nota com os novos IDs (usando o construtor atualizado)
            Nota novaNota = new Nota(
                idMatricula, // <-- ID da Matrícula (int)
                disciplinaSelecionada.getId(), // <-- ID da Disciplina (int)
                valor,
                avaliacao
            );
            
            // 4. Salvar (o NotaDAO já está atualizado)
            notaDAO.save(novaNota);

            System.out.println("Nota Lançada -> Matrícula: " + idMatricula + " | Nota: " + novaNota);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Nota de " + valor + " lançada para " + alunoSelecionado.getNomeCompleto() + ".");
            
            // Limpa os campos após salvar
            disciplinaComboBox.getSelectionModel().clearSelection();
            avaliacaoField.clear();
            notaField.clear();
            alunosListView.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "O valor da nota é inválido. Use números (ex: 8.5).");
        } catch (SQLException e) {
             showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar a nota.");
             e.printStackTrace();
        }
    }
    
    @FXML
    private void onFechar() {
        Stage stage = (Stage) turmaComboBox.getScene().getWindow();
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