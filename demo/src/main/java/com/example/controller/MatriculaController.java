package com.example.controller;

import com.example.model.Aluno;
import com.example.model.Matricula;
import com.example.model.Turma;
// REMOVIDO: import com.example.repository.AlunoDAO;
import com.example.repository.MatriculaDAO;
import com.example.repository.TurmaDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors; // NOVO

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label; // NOVO
import javafx.scene.control.ListView; // NOVO
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class MatriculaController implements Initializable {

    // REMOVIDO: @FXML private ComboBox<Aluno> alunoComboBox;
    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private Button matricularButton;
    @FXML private Button cancelarButton;

    // NOVO: Ligação para os novos campos do FXML
    @FXML private Label lblNomeAluno;
    @FXML private ListView<String> turmasAtuaisListView;

    // REMOVIDO: private AlunoDAO alunoDAO;
    private TurmaDAO turmaDAO;
    private MatriculaDAO matriculaDAO;

    // NOVO: Variável para guardar o aluno que foi passado
    private Aluno alunoSelecionado;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // REMOVIDO: this.alunoDAO = new AlunoDAO();
        this.turmaDAO = new TurmaDAO();
        this.matriculaDAO = new MatriculaDAO();

        // REMOVIDO: Carregamento do alunoComboBox
        // setupAutocompleteAluno(alunoComboBox);
        
        // Carregamento do turmaComboBox (continua igual)
        setupAutocompleteTurma(turmaComboBox);
    }

    /**
     * NOVO: Método para injetar o aluno vindo da GestãoAlunos.
     * Este é o método chamado pelo "handleMatricular" do GestaoAlunosController
     */
    public void setAlunoParaMatricular(Aluno aluno) { // O nome mudou para corresponder ao GestaoAlunosController
        this.alunoSelecionado = aluno;
        if (lblNomeAluno != null) {
            lblNomeAluno.setText(aluno.getNomeCompleto() + " (ID: " + aluno.getId() + ")");
        }
        
        // NOVO: Carrega as turmas em que o aluno já está
        carregarTurmasAtuais(aluno.getId());
    }

    /**
     * NOVO: Método para carregar a lista de turmas atuais do aluno.
     */
    private void carregarTurmasAtuais(int alunoId) {
        if (turmasAtuaisListView == null) return;
        
        try {
            // Usamos o método que já existe no MatriculaDAO
            List<Matricula> matriculas = matriculaDAO.getMatriculasPorAluno(alunoId);
            
            // Extraímos apenas os nomes das turmas
            List<String> nomesTurmas = matriculas.stream()
                .map(matricula -> matricula.getTurma().getNome())
                .collect(Collectors.toList());

            if (nomesTurmas.isEmpty()) {
                turmasAtuaisListView.setItems(FXCollections.observableArrayList("Aluno não matriculado em turmas."));
            } else {
                turmasAtuaisListView.setItems(FXCollections.observableArrayList(nomesTurmas));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar as turmas atuais do aluno.");
            turmasAtuaisListView.setItems(FXCollections.observableArrayList("Erro ao carregar turmas."));
        }
    }


    @FXML
    private void onMatricular() {
        // ATUALIZADO: Pega o aluno da variável da classe
        Aluno aluno = this.alunoSelecionado;
        Turma turma = turmaComboBox.getSelectionModel().getSelectedItem();

        if (!isDataValid(aluno, turma)) {
            return;
        }

        try {
            Matricula novaMatricula = new Matricula(aluno, turma, null, null);
            matriculaDAO.save(novaMatricula);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Aluno matriculado na turma com sucesso!");
            
            // ATUALIZADO: Atualiza a lista de turmas na própria janela
            carregarTurmasAtuais(aluno.getId());
            
            // Opcional: fechar a janela automaticamente
            // fecharJanela(); 

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Código de erro para "Duplicate entry"
                showAlert(Alert.AlertType.ERROR, "Erro de Matrícula", "Este aluno já está matriculado nesta turma.");
            } else {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao realizar a matrícula.");
            }
        }
    }

    // Método de validação atualizado
    private boolean isDataValid(Aluno aluno, Turma turma) {
        if (aluno == null || turma == null) {
            // Validação de seleção manual (autocompletar)
            if (turma == null && !turmaComboBox.getEditor().getText().isEmpty()) {
                 showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Turma inválida. Por favor, selecione uma da lista.");
                 return false;
            }
            
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Todos os campos são obrigatórios.");
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

    // REMOVIDO: Método setupAutocompleteAluno (não é mais necessário)

    // Método setupAutocompleteTurma (permanece igual)
    private void setupAutocompleteTurma(ComboBox<Turma> comboBox) {
        comboBox.setEditable(true);
        
        comboBox.setConverter(new StringConverter<Turma>() {
            @Override
            public String toString(Turma object) {
                return object == null ? "" : object.getNome();
            }
            @Override
            public Turma fromString(String string) {
                return comboBox.getItems().stream()
                         .filter(item -> item.getNome().equals(string))
                         .findFirst().orElse(null);
            }
        });

        comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                comboBox.setItems(FXCollections.observableArrayList());
                return;
            }

            Turma selecionada = comboBox.getSelectionModel().getSelectedItem();
            if (selecionada != null && selecionada.toString().equals(newText)) {
                return;
            }

            List<Turma> sugestoes = turmaDAO.searchByName(newText);
            Turma itemSelecionado = comboBox.getSelectionModel().getSelectedItem(); 
            comboBox.setItems(FXCollections.observableArrayList(sugestoes)); 
            
            if (itemSelecionado != null && sugestoes.contains(itemSelecionado)) {
                comboBox.getSelectionModel().select(itemSelecionado); 
            }
            comboBox.show();
        });
    }
}