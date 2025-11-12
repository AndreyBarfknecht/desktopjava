package com.example.controller;

import com.example.SceneNavigator;
import com.example.repository.ProfessorDAO;
import com.example.repository.TurmaDAO;
import com.example.repository.AlunoDAO;
import com.example.repository.CursoDAO;

// --- NOVAS IMPORTAÇÕES ---
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition; // Para o menu retrátil
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label; // Para os cartões
import javafx.scene.control.ScrollPane; // Para o menu retrátil
import javafx.scene.layout.BorderPane; // O painel principal
import javafx.util.Duration; // Para o menu retrátil
// --- FIM DAS NOVAS IMPORTAÇÕES ---

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;


public class TelaInicialController implements Initializable {

    // --- @FXML NOVOS (Decisão 1 e 2) ---
    @FXML private BorderPane mainBorderPane;
    @FXML private Label lblTotalAlunos;
    @FXML private Label lblTotalCursos;
    @FXML private Label lblTotalTurmas;
    @FXML private Label lblTotalProfessores;
    
    // --- @FXML PARA O MENU RETRÁTIL ---
    @FXML private ScrollPane sidebarScrollPane;
    private PauseTransition collapseTimer;

    // --- @FXML ANTIGOS MANTIDOS (Menu "Acadêmico" e "Sistema") ---
    @FXML private Accordion sidebarAccordion;
    @FXML private Button gestaoGradeButton;
    @FXML private Button associarDisciplinaButton;
    @FXML private Button cadastrarUsuarioButton;
    @FXML private Button sairButton;
    
    // (Os botões de cadastro/consulta que estavam no menu foram removidos,
    // mas os botões de "Ações Rápidas" no centro ainda funcionam)

    // --- DAOs ---
    private ProfessorDAO professorDAO;
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO;
    private CursoDAO cursoDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa DAOs
        this.professorDAO = new ProfessorDAO();
        this.turmaDAO = new TurmaDAO();
        this.alunoDAO = new AlunoDAO();
        this.cursoDAO = new CursoDAO();
        
        // NOVO: Carrega os dados dos cartões de resumo (Decisão 2)
        carregarResumosDashboard();
        
        // REMOVIDO: A lógica de carregar os gráficos foi substituída
        // carregarGraficoAlunosPorCurso();
        // carregarGraficoTurmasPorTurno();

        // Garante que o primeiro painel esteja expandido inicialmente
        Platform.runLater(() -> {
            if (sidebarAccordion != null && !sidebarAccordion.getPanes().isEmpty()) {
                sidebarAccordion.setExpandedPane(sidebarAccordion.getPanes().get(0));
            }
        });
        
        // NOVO: Configura o menu retrátil
        configurarMenuRetratil();
    }
    
    // --- MÉTODOS DE NAVEGAÇÃO PRINCIPAL (NOVOS) ---

    /**
     * MÉTODO NOVO (Decisão 1)
     * Carrega um FXML na área central (no 'center') do BorderPane principal.
     * Esta é a nova forma de navegação do menu.
     *
     * @param fxmlName O nome do ficheiro FXML (sem a extensão .fxml)
     */
    private void loadView(String fxmlName) {
        try {
            String path = "/com/example/view/" + fxmlName + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            
            // Define a nova tela no centro do BorderPane
            mainBorderPane.setCenter(view); 
            
        } catch (IOException e) {
            System.err.println("Erro ao carregar FXML: " + fxmlName);
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar a tela: " + fxmlName + ".fxml");
        }
    }
    
    /**
     * MÉTODO NOVO (Decisão 2)
     * Carrega os dados para os cartões de resumo do dashboard.
     */
    private void carregarResumosDashboard() {
        // Verifica se os labels existem (caso o FXML ainda não tenha sido atualizado)
        if (lblTotalAlunos == null || lblTotalCursos == null || lblTotalTurmas == null) {
            return; 
        }
        
        try {
            // Usamos .size() por ser simples. Para bases de dados muito grandes,
            // um método .count() no DAO seria mais eficiente.
            lblTotalAlunos.setText(String.valueOf(alunoDAO.getAll().size()));
            lblTotalCursos.setText(String.valueOf(cursoDAO.getAll().size()));
            lblTotalTurmas.setText(String.valueOf(turmaDAO.getAll().size()));
            lblTotalProfessores.setText(String.valueOf(professorDAO.getAll().size()));
        } catch (Exception e) {
            e.printStackTrace();
            // Define um valor de erro se a base de dados falhar
            lblTotalAlunos.setText("!"); 
            lblTotalCursos.setText("!");
            lblTotalTurmas.setText("!");
        }
    }


    // --- HANDLERS DOS NOVOS BOTÕES DO MENU (Decisão 1) ---

    @FXML
    private void handleMenuDashboard() {
        // Para voltar ao Dashboard, precisamos recarregar o conteúdo central
        // da TelaInicial.
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/TelaInicial.fxml"));
            BorderPane root = loader.load(); // Carrega o FXML inteiro
            
            // Pega apenas o conteúdo central (o ScrollPane com os VBox)
            mainBorderPane.setCenter(root.getCenter()); 
            
            // Como o FXML foi recarregado, um *novo* controller foi criado.
            // Precisamos de injetar os DAOs e Labels neste novo controller
            // para que os cartões funcionem.
            TelaInicialController newController = loader.getController();
            newController.mainBorderPane = this.mainBorderPane; // Passa a referência do painel principal
            newController.alunoDAO = this.alunoDAO;
            newController.cursoDAO = this.cursoDAO;
            newController.turmaDAO = this.turmaDAO;
            newController.lblTotalAlunos = this.lblTotalAlunos;
            newController.lblTotalCursos = this.lblTotalCursos;
            newController.lblTotalTurmas = this.lblTotalTurmas;
            
            // Recarrega os dados nos labels
            carregarResumosDashboard(); 
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMenuAlunos() {
        // Carrega a nossa nova tela de gestão
        loadView("GestaoAlunos"); 
    }
    
    @FXML
    private void handleMenuProfessores() {
        // Esta tela ainda não existe, então mostramos um alerta.
        // Quando criar o "GestaoProfessores.fxml", basta trocar
        // o conteúdo deste método por: loadView("GestaoProfessores")
        loadView("GestaoProfessores");
    }
    
    @FXML
    private void handleMenuCursos() {
        // (Quando criar o GestaoCursos.fxml)
        // loadView("GestaoCursos"); 
        loadView("GestaoCurso");
    }
    
    @FXML
    private void handleMenuTurmas() {
        // (Quando criar o GestaoTurmas.fxml)
        // loadView("GestaoTurmas"); 
        loadView("GestaoTurmas");
    }

    @FXML
    private void handleMenuDisciplinas() {
        loadView("GestaoDisciplinas");
    }

    @FXML
    private void handleMenuPeriodosLetivos() {
        loadView("GestaoPeriodosLetivos");
    }


    // --- HANDLERS ANTIGOS MANTIDOS (Para "Ações Rápidas" e menu "Sistema") ---
    // Estes métodos continuam a abrir POPUPS como faziam antes.
    
    @FXML private void handleCadastroAlunoButton() { 
        SceneNavigator.openNewWindow("tela_cadastro_aluno", "Cadastro de Novo Aluno"); 
        carregarResumosDashboard(); // Atualiza o cartão de total de alunos
    }
    @FXML private void handleCadastroProfessorButton() { SceneNavigator.openNewWindow("CadastroProfessor", "Cadastro de Novo Professor"); }
    @FXML private void handleCadastroCursoButton() { 
        SceneNavigator.openNewWindow("CadastroCurso", "Cadastro de Novo Curso"); 
        carregarResumosDashboard(); // Atualiza o cartão de total de cursos
    }
    @FXML private void handleCadastroTurmaButton() { 
        SceneNavigator.openNewWindow("CadastroTurma", "Cadastro de Nova Turma"); 
        carregarResumosDashboard(); // Atualiza o cartão de total de turmas
    }
    
    // Handlers do menu "Acadêmico" e "Sistema" (Mantidos)
    @FXML private void handleGestaoGradeButton() { SceneNavigator.openNewWindow("GestaoGrade", "Gerenciar Grade Curricular"); }
    @FXML private void handleAssociarDisciplinaButton() { SceneNavigator.openNewWindow("GestaoProfessorDisciplina", "Associar Disciplinas ao Professor"); }
    @FXML private void handleCadastroUsuarioButton() { SceneNavigator.openNewWindow("RegisterUser", "Cadastro de Novo Usuário"); }
    @FXML private void handleSairButton() { Platform.exit(); }
    

    // --- LÓGICA DO MENU RETRÁTIL (NOVO, Bónus) ---
    // Esta lógica controla a expansão e colapso do menu lateral
    
    private void configurarMenuRetratil() {
        if (sidebarScrollPane == null) {
             System.err.println("Aviso: sidebarScrollPane é nulo. O menu retrátil não vai funcionar.");
             return;
        }

        // Define o estado inicial como encolhido
        sidebarScrollPane.setPrefWidth(70); // Largura encolhida
        if (!sidebarScrollPane.getStyleClass().contains("sidebar-collapsed")) {
             sidebarScrollPane.getStyleClass().add("sidebar-collapsed");
        }
        
        collapseTimer = new PauseTransition(Duration.millis(300));
        collapseTimer.setOnFinished(e -> {
            if (sidebarScrollPane != null && !sidebarScrollPane.isHover()) {
                sidebarScrollPane.setPrefWidth(70);
                sidebarScrollPane.getStyleClass().add("sidebar-collapsed");
            }
        });
    }

    @FXML
    private void handleMenuExpand() {
        if (collapseTimer == null) configurarMenuRetratil(); // Segurança
        collapseTimer.stop();
        if (sidebarScrollPane == null) return;
        
        sidebarScrollPane.setPrefWidth(260); // Largura expandida (pode ajustar)
        sidebarScrollPane.getStyleClass().remove("sidebar-collapsed");
    }

    @FXML
    private void handleMenuCollapse() {
        if (collapseTimer == null) configurarMenuRetratil(); // Segurança
        collapseTimer.playFromStart();
    }
    
    // --- MÉTODO UTILITÁRIO (Mantido) ---
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}