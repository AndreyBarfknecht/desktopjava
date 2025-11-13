package com.example.controller;

import com.example.model.Aluno;
import com.example.model.Matricula;
import com.example.model.Turma;
import com.example.repository.AlunoDAO;
import com.example.repository.MatriculaDAO;
import com.example.repository.TurmaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class MatriculaController implements Initializable {

    @FXML private ComboBox<Aluno> alunoComboBox;
    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private DatePicker dataMatriculaPicker;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;
    
    // NOVO: Campo FXML para o Status
    @FXML private ComboBox<String> statusComboBox;

    private AlunoDAO alunoDAO;
    private TurmaDAO turmaDAO;
    private MatriculaDAO matriculaDAO;

    // NOVO: Variável para o modo de edição
    private Matricula matriculaParaEditar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.alunoDAO = new AlunoDAO();
        this.turmaDAO = new TurmaDAO();
        this.matriculaDAO = new MatriculaDAO();

        configurarComboBoxes();
        carregarDados();

        // NOVO: Preenche o ComboBox de Status
        statusComboBox.getItems().addAll("Ativo", "Trancado", "Concluído", "Cancelado");
        dataMatriculaPicker.setValue(LocalDate.now());
    }
    
    /**
     * Usado pelo GestaoAlunosController para pré-selecionar o aluno.
     */
    public void setAlunoParaMatricular(Aluno aluno) {
        if (alunoComboBox != null) {
            alunoComboBox.setValue(aluno);
            alunoComboBox.setDisable(true); // Bloqueia a troca
            statusComboBox.setValue("Ativo"); // Define status padrão
        }
    }

    /**
     * NOVO: Usado pelo ConsultaMatriculasController para o modo de edição.
     */
    public void setMatriculaParaEdicao(Matricula matricula) {
        this.matriculaParaEditar = matricula;

        // Preenche todos os campos
        alunoComboBox.setValue(matricula.getAluno());
        turmaComboBox.setValue(matricula.getTurma());
        dataMatriculaPicker.setValue(matricula.getDataMatricula());
        statusComboBox.setValue(matricula.getStatus());

        // Bloqueia campos que não devem ser editados
        alunoComboBox.setDisable(true);
        turmaComboBox.setDisable(true);
        dataMatriculaPicker.setDisable(true);
        
        // Permite apenas a alteração do Status
        statusComboBox.setDisable(false);
        salvarButton.setText("Atualizar Status");
    }

    private void configurarComboBoxes() {
        // (Este método permanece igual)
        alunoComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Aluno aluno) { return aluno == null ? "" : aluno.getNomeCompleto(); }
            @Override public Aluno fromString(String string) { return null; }
        });
        turmaComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Turma turma) { return turma == null ? "" : turma.getNome(); }
            @Override public Turma fromString(String string) { return null; }
        });
    }

    private void carregarDados() {
        alunoComboBox.setItems(FXCollections.observableArrayList(alunoDAO.getAll()));
        turmaComboBox.setItems(FXCollections.observableArrayList(turmaDAO.getAll()));
    }

    @FXML
    private void onSalvar() {
        try {
            // --- MODO DE EDIÇÃO ---
            if (matriculaParaEditar != null) {
                String novoStatus = statusComboBox.getValue();
                if (novoStatus == null) {
                    showAlert(Alert.AlertType.WARNING, "Dados Incompletos", "Por favor, selecione um status.");
                    return;
                }
                // Usamos o método específico do DAO para atualizar SÓ o status
                matriculaDAO.updateStatus(matriculaParaEditar.getId(), novoStatus);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Status da matrícula atualizado!");
            
            // --- MODO DE CRIAÇÃO ---
            } else {
                Aluno aluno = alunoComboBox.getSelectionModel().getSelectedItem();
                Turma turma = turmaComboBox.getSelectionModel().getSelectedItem();
                LocalDate data = dataMatriculaPicker.getValue();
                String status = statusComboBox.getValue(); // Usa o novo ComboBox

                if (aluno == null || turma == null || data == null || status == null) {
                    showAlert(Alert.AlertType.WARNING, "Dados Incompletos", "Todos os campos são obrigatórios.");
                    return;
                }

                Matricula novaMatricula = new Matricula(aluno, turma, data, status);
                matriculaDAO.save(novaMatricula);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Aluno matriculado com sucesso!");
            }
            
            fecharJanela(); // Fecha a janela após salvar ou atualizar
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { 
                showAlert(Alert.AlertType.ERROR, "Erro", "Este aluno já está matriculado nesta turma.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Ocorreu um erro ao salvar a matrícula.");
                e.printStackTrace();
            }
        }
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