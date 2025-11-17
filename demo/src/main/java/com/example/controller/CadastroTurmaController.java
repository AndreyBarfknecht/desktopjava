package com.example.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import com.example.model.Curso;
import com.example.model.PeriodoLetivo;
import com.example.model.Turma; 
import com.example.repository.CursoDAO;
import com.example.repository.PeriodoLetivoDAO;
import com.example.repository.TurmaDAO; 

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter; // Importar o StringConverter
import javafx.scene.control.Label; // <-- IMPORTA O LABEL

public class CadastroTurmaController implements Initializable {

    @FXML private TextField nomeTurmaField;
    @FXML private ComboBox<Curso> cursoComboBox;
    @FXML private ComboBox<PeriodoLetivo> periodoLetivoComboBox;
    @FXML private ComboBox<String> turnoComboBox;
    @FXML private TextField salaField;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;
    @FXML private Label lblTituloJanela;
    private TurmaDAO turmaDAO; 
    private CursoDAO cursoDAO;
    private PeriodoLetivoDAO periodoLetivoDAO;

    private Turma turmaParaEditar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.turmaDAO = new TurmaDAO();
        this.cursoDAO = new CursoDAO();
        this.periodoLetivoDAO = new PeriodoLetivoDAO();

        turnoComboBox.getItems().addAll("Manhã", "Tarde", "Noite");

        // Configura os ComboBoxes para terem o autocompletar
        setupAutocompleteCurso(cursoComboBox);
        setupAutocompletePeriodo(periodoLetivoComboBox);
    }
    
    /**
     * NOVO: Configura o autocompletar para o ComboBox de Cursos.
     */
    private void setupAutocompleteCurso(ComboBox<Curso> comboBox) {
        comboBox.setEditable(true); 

        // Define como o objeto Curso deve ser mostrado como texto
        comboBox.setConverter(new StringConverter<Curso>() {
            @Override
            public String toString(Curso object) {
                return object == null ? "" : object.getNomeCurso();
            }
            @Override
            public Curso fromString(String string) {
                // Isto é crucial: se o texto for uma string, tentamos encontrar
                // o objeto correspondente na lista de itens.
                return comboBox.getItems().stream()
                         .filter(item -> item.getNomeCurso().equals(string))
                         .findFirst().orElse(null);
            }
        });

        comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                comboBox.setItems(FXCollections.observableArrayList()); // Limpa
                return;
            }

            // Evita buscar no banco se o usuário apenas selecionou um item
            Curso selecionado = comboBox.getSelectionModel().getSelectedItem();
            if (selecionado != null && selecionado.toString().equals(newText)) {
                return;
            }

            // Busca no DAO (com limite)
            List<Curso> sugestoes = cursoDAO.searchByName(newText);
            
            // Guarda o item selecionado (se houver)
            Curso itemSelecionado = comboBox.getSelectionModel().getSelectedItem();
            
            comboBox.setItems(FXCollections.observableArrayList(sugestoes));
            
            // Restaura a seleção se ainda for válida
            if (itemSelecionado != null && sugestoes.contains(itemSelecionado)) {
                comboBox.getSelectionModel().select(itemSelecionado);
            }
            
            comboBox.show(); // Mostra o dropdown com os resultados
        });
    }

    /**
     * NOVO: Configura o autocompletar para o ComboBox de Períodos Letivos.
     */
    private void setupAutocompletePeriodo(ComboBox<PeriodoLetivo> comboBox) {
        comboBox.setEditable(true); 

        // Define como o objeto PeriodoLetivo deve ser mostrado como texto
        comboBox.setConverter(new StringConverter<PeriodoLetivo>() {
            @Override
            public String toString(PeriodoLetivo object) {
                return object == null ? "" : object.getNome();
            }
            @Override
            public PeriodoLetivo fromString(String string) {
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
            
            PeriodoLetivo selecionado = comboBox.getSelectionModel().getSelectedItem();
            if (selecionado != null && selecionado.toString().equals(newText)) {
                return;
            }

            List<PeriodoLetivo> sugestoes = periodoLetivoDAO.searchByName(newText);
            
            PeriodoLetivo itemSelecionado = comboBox.getSelectionModel().getSelectedItem();
            comboBox.setItems(FXCollections.observableArrayList(sugestoes));
            if (itemSelecionado != null && sugestoes.contains(itemSelecionado)) {
                comboBox.getSelectionModel().select(itemSelecionado);
            }
            
            comboBox.show();
        });
    }


    public void setTurmaParaEdicao(Turma turma) {
        this.turmaParaEditar = turma;
        lblTituloJanela.setText("Editar Turma");
        nomeTurmaField.setText(turma.getNome());
        salaField.setText(turma.getSala());
        turnoComboBox.setValue(turma.getTurno());
        salvarButton.setText("Atualizar");

        // CORREÇÃO: Adiciona e seleciona o item
        if (turma.getCurso() != null) {
            // Adiciona o item à lista (mesmo que vazia) e depois seleciona
            cursoComboBox.getItems().setAll(turma.getCurso());
            cursoComboBox.getSelectionModel().select(turma.getCurso());
        }

        if (turma.getPeriodoLetivo() != null) {
            periodoLetivoComboBox.getItems().setAll(turma.getPeriodoLetivo());
            periodoLetivoComboBox.getSelectionModel().select(turma.getPeriodoLetivo());
        }
    }

    @FXML
    private void onSalvar() {
        
        // --- CORREÇÃO DEFINITIVA ---
        // Pegamos o *item selecionado* (o objeto) e não o *valor* (que pode ser String)
        Curso cursoSelecionado = cursoComboBox.getSelectionModel().getSelectedItem();
        PeriodoLetivo periodoSelecionado = periodoLetivoComboBox.getSelectionModel().getSelectedItem();
        String turnoSelecionado = turnoComboBox.getValue();

        // Validação (usando as novas variáveis)
        if (nomeTurmaField.getText().trim().isEmpty() || 
            cursoSelecionado == null ||
            periodoSelecionado == null ||
            turnoSelecionado == null ||
            salaField.getText().trim().isEmpty()) {
            
            // Feedback melhor se o utilizador digitou mas não selecionou
            if (cursoSelecionado == null && !cursoComboBox.getEditor().getText().isEmpty()) {
                 showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Curso inválido. Por favor, selecione um curso da lista.");
                 return;
            }
            if (periodoSelecionado == null && !periodoLetivoComboBox.getEditor().getText().isEmpty()) {
                 showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Período Letivo inválido. Por favor, selecione um período da lista.");
                 return;
            }
            
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Todos os campos são obrigatórios.");
            return;
        }

        try {
            if (turmaParaEditar == null) { 
                Turma novaTurma = new Turma(
                    nomeTurmaField.getText(),
                    cursoSelecionado,
                    periodoSelecionado,
                    turnoSelecionado,
                    salaField.getText()
                );
                turmaDAO.save(novaTurma);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Turma salva com sucesso!");
            
            } else {
                turmaParaEditar.setNome(nomeTurmaField.getText());
                turmaParaEditar.setCurso(cursoSelecionado);
                turmaParaEditar.setPeriodoLetivo(periodoSelecionado);
                turmaParaEditar.setTurno(turnoSelecionado);
                turmaParaEditar.setSala(salaField.getText());
                
                turmaDAO.update(turmaParaEditar);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Turma atualizada com sucesso!");
            }
            fecharJanela();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Ocorreu um erro ao salvar a turma.");
            e.printStackTrace();
        }
    }
    
    // O método isTurmaDataValid() foi removido.
    
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