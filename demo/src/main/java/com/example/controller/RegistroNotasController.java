package com.example.controller;

import com.example.model.Aluno;
import com.example.model.Nota;
import com.example.model.Turma;
import com.example.service.AcademicService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegistroNotasController implements Initializable {

    @FXML private ComboBox<Turma> turmaComboBox;
    @FXML private ListView<Aluno> alunosListView;
    @FXML private TextField disciplinaField;
    @FXML private TextField avaliacaoField;
    @FXML private TextField notaField;
    @FXML private Button salvarNotaButton;

    private AcademicService service = AcademicService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        turmaComboBox.setItems(service.getTurmas());

        // Quando uma turma é selecionada, carrega os alunos matriculados nela
        turmaComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, turmaSelecionada) -> {
                if (turmaSelecionada != null) {
                    alunosListView.setItems(turmaSelecionada.getAlunosMatriculados());
                }
            }
        );
    }

    @FXML
    private void salvarNota() {
        Aluno alunoSelecionado = alunosListView.getSelectionModel().getSelectedItem();
        
        if (alunoSelecionado == null) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Selecione um aluno para lançar a nota.");
            return;
        }

        try {
            String disciplina = disciplinaField.getText();
            String avaliacao = avaliacaoField.getText();
            double valor = Double.parseDouble(notaField.getText().replace(",", "."));

            if (disciplina.isBlank() || avaliacao.isBlank()) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Os campos 'Disciplina' e 'Avaliação' são obrigatórios.");
                return;
            }

            Nota novaNota = new Nota(disciplina, valor, avaliacao);
            alunoSelecionado.adicionarNota(novaNota);

            System.out.println("Nota Lançada -> Aluno: " + alunoSelecionado.getNomeCompleto() + " | Nota: " + novaNota);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Nota de " + valor + " lançada para " + alunoSelecionado.getNomeCompleto() + ".");
            
            // Limpa os campos após salvar
            disciplinaField.clear();
            avaliacaoField.clear();
            notaField.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "O valor da nota é inválido. Use números (ex: 8.5).");
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