package com.example.controller;

import com.example.model.Aluno;
import com.example.model.Disciplina;
import com.example.model.Nota;
import com.example.model.Turma;
import com.example.repository.AlunoDAO;
import com.example.repository.GradeCurricularDAO;
import com.example.repository.MatriculaDAO; 
import com.example.repository.NotaDAO;
import com.example.repository.TurmaDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label; // NOVO
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class RegistroNotasController implements Initializable {

    // --- CAMPOS FXML MODIFICADOS ---
    @FXML private ComboBox<Disciplina> disciplinaComboBox;
    @FXML private TextField avaliacaoField;
    @FXML private TextField notaField;
    @FXML private Button salvarNotaButton;
    
    // NOVO: Labels para exibir dados
    @FXML private Label lblNomeTurma;
    @FXML private Label lblNomeAluno;
    
    // REMOVIDO: Controles de seleção
    // @FXML private ComboBox<Turma> turmaComboBox;
    // @FXML private ListView<Aluno> alunosListView;

    // DAOs
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO;
    private NotaDAO notaDAO;
    private GradeCurricularDAO gradeCurricularDAO;
    private MatriculaDAO matriculaDAO;

    // NOVO: Variáveis para guardar os dados recebidos
    private Aluno alunoSelecionado;
    private Turma turmaSelecionada;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa os DAOs
        this.turmaDAO = new TurmaDAO();
        this.alunoDAO = new AlunoDAO();
        this.notaDAO = new NotaDAO();
        this.gradeCurricularDAO = new GradeCurricularDAO();
        this.matriculaDAO = new MatriculaDAO();

        // Configura o ComboBox de Disciplina (o resto foi removido)
        configurarControles();
        
        disciplinaComboBox.setDisable(true);
    }
    
    /**
     * NOVO MÉTODO: Chamado pelo GestaoTurmasController para injetar os dados.
     */
    public void initData(Turma turma, Aluno aluno) {
        this.turmaSelecionada = turma;
        this.alunoSelecionado = aluno;
        
        // 1. Define o texto dos Labels
        lblNomeTurma.setText(turma.getNome());
        lblNomeAluno.setText(aluno.getNomeCompleto());
        
        // 2. Carrega as disciplinas (lógica que estava no listener)
        carregarDisciplinasDaTurma(turma);
        disciplinaComboBox.setDisable(false);
    }
    
    private void configurarControles() {
        // REMOVIDO: Configuração de Turma e Aluno
        
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
        // --- LÓGICA DE VALIDAÇÃO SIMPLIFICADA ---
        // Não precisamos mais verificar turma e aluno, pois eles já vêm injetados
        Disciplina disciplinaSelecionada = disciplinaComboBox.getSelectionModel().getSelectedItem();

        if (disciplinaSelecionada == null || avaliacaoField.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Selecione uma disciplina e preencha a avaliação.");
            return;
        }

        try {
            double valor = Double.parseDouble(notaField.getText().replace(",", "."));

            // --- LÓGICA PRINCIPAL (Quase igual, mas usa as variáveis da classe) ---

            // 1. Buscar o ID da Matrícula
            Integer idMatricula = matriculaDAO.getMatriculaId(
                alunoSelecionado.getId(),
                turmaSelecionada.getId()
            );

            // 2. Verificar
            if (idMatricula == null) {
                showAlert(Alert.AlertType.ERROR, "Erro de Matrícula",
                    "O aluno '" + alunoSelecionado.getNomeCompleto() +
                    "' não parece estar matriculado na turma '" + turmaSelecionada.getNome() + "'.");
                return;
            }

            // 3. Criar o objeto Nota
            Nota novaNota = new Nota(
                idMatricula, 
                disciplinaSelecionada.getId(), 
                valor,
                avaliacaoField.getText()
            );
            
            // 4. Salvar
            notaDAO.save(novaNota);

            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Nota de " + valor + " lançada para " + alunoSelecionado.getNomeCompleto() + ".");
            
            // Limpa os campos após salvar
            disciplinaComboBox.getSelectionModel().clearSelection();
            avaliacaoField.clear();
            notaField.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "O valor da nota é inválido. Use números (ex: 8.5).");
        } catch (SQLException e) {
             showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar a nota.");
             e.printStackTrace();
        }
    }
    
    @FXML
    private void onFechar() {
        // O botão "salvarNotaButton" é um controle válido para fechar a janela
        Stage stage = (Stage) salvarNotaButton.getScene().getWindow();
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