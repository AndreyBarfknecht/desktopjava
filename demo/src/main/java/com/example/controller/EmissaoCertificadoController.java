package com.example.controller;

import com.example.model.Aluno;
import com.example.model.Matricula;
import com.example.repository.GradeCurricularDAO;
import com.example.repository.MatriculaDAO;
import com.example.service.GeradorPdfService; // O nosso serviço

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.UUID; // Para o código de verificação
import java.util.stream.Collectors;

public class EmissaoCertificadoController {

    @FXML private Label lblNomeAluno;
    @FXML private TableView<Matricula> matriculasConcluidasView;
    @FXML private Button btnGerarPdf;
    @FXML private Label lblStatus;

    private Aluno alunoSelecionado;
    private MatriculaDAO matriculaDAO;
    private GradeCurricularDAO gradeCurricularDAO;
    private GeradorPdfService pdfService;

    // Lista para a tabela
    private ObservableList<Matricula> matriculasConcluidas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.matriculaDAO = new MatriculaDAO();
        this.gradeCurricularDAO = new GradeCurricularDAO();
        this.pdfService = new GeradorPdfService();

        matriculasConcluidasView.setItems(matriculasConcluidas);

        // Ativa o botão apenas se um curso for selecionado
        matriculasConcluidasView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                btnGerarPdf.setDisable(newSelection == null);
            }
        );
    }

    /**
     * Método chamado pelo GestaoAlunosController para passar o Aluno.
     */
    public void initData(Aluno aluno) {
        this.alunoSelecionado = aluno;
        lblNomeAluno.setText(aluno.getNomeCompleto() + " (CPF: " + aluno.getCpf() + ")");
        
        carregarMatriculasConcluidas();
    }

    /**
     * Filtra a lista de matrículas do aluno para mostrar apenas as "Concluídas".
     */
    private void carregarMatriculasConcluidas() {
        // 1. Busca TODAS as matrículas (agora com os dados completos do curso)
        List<Matricula> todasMatriculas = matriculaDAO.getMatriculasPorAluno(alunoSelecionado.getId());

        // 2. Filtra a lista em Java
        List<Matricula> concluidas = todasMatriculas.stream()
                .filter(m -> "Concluído".equalsIgnoreCase(m.getStatus())) // Regra de negócio!
                .collect(Collectors.toList());

        matriculasConcluidas.setAll(concluidas);

        if (concluidas.isEmpty()) {
            lblStatus.setText("Este aluno não possui cursos com status 'Concluído'.");
            matriculasConcluidasView.setPlaceholder(new Label("Nenhum curso concluído encontrado."));
        }
    }

    @FXML
    private void onGerarPdf() {
        Matricula matriculaSelecionada = matriculasConcluidasView.getSelectionModel().getSelectedItem();
        if (matriculaSelecionada == null) {
            return; // Botão deveria estar desativado, mas é uma segurança
        }

        try {
            // 1. Buscar a Carga Horária Total
            int idCurso = matriculaSelecionada.getTurma().getCurso().getId();
            int cargaHorariaTotal = gradeCurricularDAO.getCargaHorariaTotalByCursoId(idCurso);

            // 2. Gerar o Código de Verificação Único
            String codigoVerificacao = UUID.randomUUID().toString();

            // 3. Chamar o Serviço
            pdfService.gerarCertificado(matriculaSelecionada, cargaHorariaTotal, codigoVerificacao);

            // 4. (Opcional) Salvar o código de verificação na BD (não implementado aqui)
            
            lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);
            lblStatus.setText("Certificado gerado e salvo com sucesso!");

        } catch (IOException e) {
            e.printStackTrace();
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            lblStatus.setText("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) btnGerarPdf.getScene().getWindow();
        stage.close();
    }
}