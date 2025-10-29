package com.example.controller;

import com.example.SceneNavigator;
import com.example.repository.ProfessorDAO;
import com.example.repository.TurmaDAO;
import com.example.repository.AlunoDAO;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

public class TelaInicialController implements Initializable {

    // --- Botões ---
    @FXML private Button cadastrarAlunoButton;
    @FXML private Button sairButton;
    @FXML private Button cadastrarUsuarioButton;
    @FXML private Button cadastrarProfessorButton;
    @FXML private Button cadastrarTurmaButton;
    @FXML private Button registroNotasButton;
    @FXML private Button consultarAlunosButton;
    @FXML private Button cadastrarPeriodoLetivoButton;
    @FXML private Button cadastrarHorarioButton;
    @FXML private Button matriculaButton;
    @FXML private Button consultarProfessoresButton;
    @FXML private Button cadastrarCursoButton;
    @FXML private Button cadastrarDisciplinaButton;

    // --- Widgets ---
    @FXML private VBox professoresWidget;
    @FXML private Label professoresCountLabel;
    @FXML private VBox cursosWidget;
    @FXML private Label cursosCountLabel;
    @FXML private VBox turmasWidget;
    @FXML private Label turmasCountLabel;
    @FXML private VBox alunosWidget;
    @FXML private Label alunosCountLabel;

    // --- Sidebar ---
    @FXML private VBox sidebarContainer;
    @FXML private Accordion sidebarAccordion;
    @FXML private Button toggleSidebarButton;
    @FXML private FontAwesomeIconView toggleIcon;
    @FXML private TitledPane cadastrosPane;
    @FXML private TitledPane academicoPane;
    @FXML private TitledPane consultasPane;
    @FXML private TitledPane sistemaPane;

    // --- DAOs ---
    private ProfessorDAO professorDAO;
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO;
    // private CursoDAO cursoDAO; // Descomente se/quando implementar CursoDAO

    // --- Controle Sidebar ---
    private boolean isSidebarExpanded = true;
    private final double expandedWidth = 220.0;
    private final double collapsedWidth = 65.0;

    // Mapa para guardar apenas o texto COMPLETO (originalmente da tooltip ou do botão)
    private final Map<Button, String> buttonFullTextMap = new HashMap<>();
    // Mapa para guardar o texto original dos TitledPanes
    private final Map<TitledPane, String> paneTitleMap = new HashMap<>();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.professorDAO = new ProfessorDAO();
        this.turmaDAO = new TurmaDAO();
        this.alunoDAO = new AlunoDAO();
        // this.cursoDAO = new CursoDAO(); // Descomente se/quando implementar CursoDAO

        atualizarContagemProfessores();
        atualizarContagemCursos(); // Implemente a lógica real aqui se necessário
        atualizarContagemTurmas();
        atualizarContagemAlunos();

        sidebarContainer.setPrefWidth(expandedWidth);

        // Espera a UI estar pronta para configurar textos e estado inicial
        Platform.runLater(() -> {
            storeOriginalTexts(); // Guarda textos ANTES de qualquer modificação
            updateSidebarItemsDisplay(isSidebarExpanded); // Aplica estado inicial
            // Garante que o primeiro painel esteja expandido inicialmente (se houver painéis)
            if (sidebarAccordion != null && !sidebarAccordion.getPanes().isEmpty()) {
                sidebarAccordion.setExpandedPane(sidebarAccordion.getPanes().getFirst());
            }
        });
    }

    // Guarda os textos originais (títulos dos painéis e textos completos dos botões)
    private void storeOriginalTexts() {
        // Guarda títulos dos TitledPanes
        if (cadastrosPane != null) paneTitleMap.put(cadastrosPane, cadastrosPane.getText());
        if (academicoPane != null) paneTitleMap.put(academicoPane, academicoPane.getText());
        if (consultasPane != null) paneTitleMap.put(consultasPane, consultasPane.getText());
        if (sistemaPane != null) paneTitleMap.put(sistemaPane, sistemaPane.getText());

        // Guarda texto completo dos botões (prioriza tooltip, senão usa texto do botão)
        if (sidebarAccordion != null) {
            sidebarAccordion.getPanes().forEach(pane -> {
                Node content = pane.getContent();
                if (content instanceof VBox) {
                    ((VBox) content).getChildren().forEach(node -> {
                        if (node instanceof Button) {
                            Button button = (Button) node;
                            Tooltip tooltip = button.getTooltip();
                            // Guarda o texto da tooltip como texto completo, se existir e não for vazio
                            if (tooltip != null && tooltip.getText() != null && !tooltip.getText().isEmpty()) {
                                buttonFullTextMap.put(button, tooltip.getText());
                            } else {
                                // Fallback: usa o texto do botão definido no FXML se não houver tooltip válida
                                buttonFullTextMap.put(button, button.getText()); // Guarda o texto original do FXML
                            }
                        }
                    });
                }
            });
        }
    }


    @FXML
    private void handleToggleSidebar() {
        isSidebarExpanded = !isSidebarExpanded; // Inverte o estado

        // Cria a animação de largura
        Timeline timeline = new Timeline();
        KeyValue kvWidth = new KeyValue(sidebarContainer.prefWidthProperty(), isSidebarExpanded ? expandedWidth : collapsedWidth);
        KeyFrame kfWidth = new KeyFrame(Duration.millis(250), kvWidth); // Duração da animação
        timeline.getKeyFrames().add(kfWidth);

        // Muda o ícone do botão de toggle
        FontAwesomeIcon iconName = isSidebarExpanded ? FontAwesomeIcon.BARS : FontAwesomeIcon.ARROW_RIGHT;
        if (toggleIcon != null) toggleIcon.setGlyphName(iconName.name());

        // Atualiza a exibição dos itens (texto, tooltips) ANTES da animação começar
        updateSidebarItemsDisplay(isSidebarExpanded);

        // Inicia a animação
        timeline.play();
    }

    // Atualiza como os TitledPanes e Botões são exibidos (com ou sem texto, tooltip)
    private void updateSidebarItemsDisplay(boolean expanded) {
        if (sidebarAccordion == null) return; // Segurança

        // Itera sobre cada TitledPane (Cadastros, Acadêmico, etc.)
        for (TitledPane pane : sidebarAccordion.getPanes()) {
            if (pane == null) continue; // Segurança

            // Define o texto do título do painel (ex: "Cadastros") ou vazio se recolhido
            pane.setText(expanded ? paneTitleMap.getOrDefault(pane, "") : "");
            pane.setAnimated(expanded); // Animação ao abrir/fechar só funciona se expandido
            pane.setCollapsible(expanded); // Só pode colapsar/expandir TitledPane se sidebar estiver expandida

            // Processa os botões dentro de cada TitledPane
            Node content = pane.getContent();
            if (content instanceof VBox) {
                VBox contentBox = (VBox) content;
                contentBox.getChildren().forEach(node -> {
                    if (node instanceof Button) {
                        Button button = (Button) node;
                        Tooltip tooltip = button.getTooltip();
                        // Pega o texto completo guardado (original da tooltip ou do botão)
                        String fullText = buttonFullTextMap.getOrDefault(button, "");

                        if (expanded) {
                            // --- ESTADO EXPANDIDO ---
                            button.setContentDisplay(ContentDisplay.LEFT); // Ícone à esquerda do texto
                            // Define o texto CURTO que estava originalmente no FXML (ex: "Aluno")
                            button.setText(getButtonShortTextFromFXML(button));
                            button.getStyleClass().remove("sidebar-button-collapsed"); // Remove a classe que esconde o texto
                            // Desinstala a tooltip, pois o texto completo já é visível (ou quase)
                            if (tooltip != null) Tooltip.uninstall(button, tooltip);
                        } else {
                            // --- ESTADO RECOLHIDO ---
                            button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY); // Mostra SÓ o ícone
                            button.setText(""); // Remove qualquer texto visível
                            button.getStyleClass().add("sidebar-button-collapsed"); // Adiciona classe que formata para recolhido (e esconde texto via CSS)
                            // Instala ou atualiza a tooltip para mostrar o texto completo ao passar o mouse
                            if (tooltip != null) {
                                tooltip.setText(fullText); // Garante que a tooltip tem o texto correto
                                Tooltip.install(button, tooltip); // Reinstala/ativa
                            } else {
                                // Cria e instala uma nova tooltip se não existia
                                Tooltip newTooltip = new Tooltip(fullText);
                                button.setTooltip(newTooltip); // Associa ao botão
                                Tooltip.install(button, newTooltip); // Ativa
                            }
                        }
                    }
                });
            }
             // Garante que os TitledPanes estejam fechados se a sidebar estiver recolhida
            if (!expanded) {
                pane.setExpanded(false);
            }
        }
        // Se a sidebar foi expandida e NENHUM TitledPane está aberto, abre o primeiro
        if (expanded && sidebarAccordion.getExpandedPane() == null && !sidebarAccordion.getPanes().isEmpty()) {
            sidebarAccordion.setExpandedPane(sidebarAccordion.getPanes().getFirst());
        }
    }

    // Função auxiliar para obter o texto CURTO original do botão (definido no FXML)
    // Isso é necessário porque guardamos o texto LONGO (da tooltip) no mapa.
    private String getButtonShortTextFromFXML(Button button) {
         if (button == cadastrarAlunoButton) return "Aluno";
         if (button == cadastrarProfessorButton) return "Professor";
         if (button == cadastrarCursoButton) return "Curso";
         if (button == cadastrarDisciplinaButton) return "Disciplina";
         if (button == cadastrarTurmaButton) return "Turma";
         if (button == cadastrarPeriodoLetivoButton) return "Período Letivo";
         if (button == cadastrarHorarioButton) return "Horário";
         if (button == matriculaButton) return "Matrículas";
         if (button == registroNotasButton) return "Notas";
         if (button == consultarAlunosButton) return "Alunos";
         if (button == consultarProfessoresButton) return "Professores";
         if (button == cadastrarUsuarioButton) return "Usuário";
         if (button == sairButton) return "Sair";
         return ""; // Fallback caso o botão não seja reconhecido
    }


    // --- Métodos de Contagem ---
    // Atualiza o texto do widget de cursos (atualmente fixo)
    private void atualizarContagemCursos() {
        cursosCountLabel.setText("0"); // Mantenha como 0 ou implemente a lógica com CursoDAO
        // Exemplo com CursoDAO (descomente quando tiver):
        // try {
        //     cursosCountLabel.setText(String.valueOf(cursoDAO.getAll().size()));
        // } catch (Exception e) {
        //     cursosCountLabel.setText("!"); // Indica erro
        //     e.printStackTrace();
        // }
    }
    // Atualiza o texto do widget de turmas buscando do DAO
    private void atualizarContagemTurmas() {
        try {
            turmasCountLabel.setText(String.valueOf(turmaDAO.getAll().size()));
        } catch (Exception e) {
            turmasCountLabel.setText("!"); // Indica erro
            e.printStackTrace();
        }
    }
    // Atualiza o texto do widget de alunos buscando do DAO
    private void atualizarContagemAlunos() {
        try {
            alunosCountLabel.setText(String.valueOf(alunoDAO.getAll().size()));
        } catch (Exception e) {
            alunosCountLabel.setText("!"); // Indica erro
            e.printStackTrace();
        }
    }
    // Atualiza o texto do widget de professores buscando do DAO
    private void atualizarContagemProfessores() {
        try {
            professoresCountLabel.setText(String.valueOf(professorDAO.getAll().size()));
        } catch (Exception e) {
            professoresCountLabel.setText("!"); // Indica erro
            e.printStackTrace();
        }
    }

    // --- Handlers dos botões (ações ao clicar) ---
    @FXML private void handleCadastroAlunoButton() { SceneNavigator.openNewWindow("tela_cadastro_aluno", "Cadastro de Novo Aluno"); atualizarContagemAlunos();} // Atualiza contagem após fechar
    @FXML private void handleCadastroProfessorButton() { SceneNavigator.openNewWindow("CadastroProfessor", "Cadastro de Novo Professor"); atualizarContagemProfessores(); } // Atualiza contagem
    @FXML private void handleCadastroCursoButton() { SceneNavigator.openNewWindow("CadastroCurso", "Cadastro de Novo Curso"); atualizarContagemCursos(); } // Atualiza contagem
    @FXML private void handleCadastroDisciplinaButton() { SceneNavigator.openNewWindow("CadastroDisciplina", "Cadastro de Disciplina"); }
    @FXML private void handleCadastroTurmaButton() { SceneNavigator.openNewWindow("CadastroTurma", "Cadastro de Nova Turma"); atualizarContagemTurmas(); } // Atualiza contagem
    @FXML private void handleCadastroPeriodoLetivoButton() { SceneNavigator.openNewWindow("CadastroPeriodoLetivo", "Cadastro de Período Letivo"); }
    @FXML private void handleCadastroHorarioButton() { SceneNavigator.openNewWindow("CadastroHorario", "Cadastro de Horário"); }
    @FXML private void handleCadastroUsuarioButton() { SceneNavigator.openNewWindow("RegisterUser", "Cadastro de Novo Usuário"); }
    @FXML private void handleSairButton() { Platform.exit(); } // Fecha a aplicação
    @FXML private void handleMatriculaButton() { SceneNavigator.openNewWindow("Matricula", "Matrícula de Alunos"); atualizarContagemAlunos(); } // Atualiza contagem de alunos
    @FXML private void handleRegistroNotasButton() { SceneNavigator.openNewWindow("RegistroNotas", "Registro de Notas"); }
    @FXML private void handleConsultarAlunosButton() { SceneNavigator.openNewWindow("ConsultaAlunos", "Consulta de Alunos"); atualizarContagemAlunos(); } // Atualiza contagem (caso exclua/edite)
    @FXML private void handleConsultarProfessoresButton() { SceneNavigator.openNewWindow("ConsultaProfessores", "Consulta de Professores"); atualizarContagemProfessores(); } // Atualiza contagem

    // --- Handlers dos Widgets (ações ao clicar nos cartões) ---
    @FXML private void handleConsultarProfessoresWidget() { handleConsultarProfessoresButton(); } // Reutiliza a ação do botão
    @FXML private void handleConsultarAlunosWidget() { handleConsultarAlunosButton(); } // Reutiliza a ação do botão
}