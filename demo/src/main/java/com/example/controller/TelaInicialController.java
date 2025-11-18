package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Aluno;
import com.example.model.Horario;
import com.example.repository.AlunoDAO;
import com.example.repository.CursoDAO;
import com.example.repository.HorarioDAO;
import com.example.repository.ProfessorDAO;
import com.example.repository.TurmaDAO;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

public class TelaInicialController implements Initializable {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label lblTotalAlunos;
    @FXML private Label lblTotalCursos;
    @FXML private Label lblTotalTurmas;
    @FXML private Label lblTotalProfessores;
    
    // --- NOVOS FXML (GRÁFICOS E TABELAS) ---
    @FXML private TableView<Horario> tabelaAulasHoje;
    @FXML private TableColumn<Horario, String> colHoraInicio;
    @FXML private TableColumn<Horario, String> colTurmaHoje;
    @FXML private TableColumn<Horario, String> colDisciplinaHoje;
    @FXML private TableColumn<Horario, String> colProfHoje;

    @FXML private TableView<Aluno> tabelaUltimosAlunos;
    @FXML private TableColumn<Aluno, String> colNomeAlunoRecente;
    @FXML private TableColumn<Aluno, String> colDataNascRecente;
    @FXML private TableColumn<Aluno, String> colRespRecente;

    @FXML private BarChart<String, Number> graficoTurnos;
    @FXML private BarChart<Number, String> graficoCursos; // Eixo X (Número), Eixo Y (Texto) 

    // --- MENU RETRÁTIL ---
    @FXML private ScrollPane sidebarScrollPane;
    @FXML private Accordion sidebarAccordion;
    private PauseTransition collapseTimer;

    // --- DAOs ---
    private ProfessorDAO professorDAO;
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO;
    private CursoDAO cursoDAO;
    private HorarioDAO horarioDAO; // Novo DAO necessário

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.professorDAO = new ProfessorDAO();
        this.turmaDAO = new TurmaDAO();
        this.alunoDAO = new AlunoDAO();
        this.cursoDAO = new CursoDAO();
        this.horarioDAO = new HorarioDAO();
        
        carregarResumosDashboard();
        
        // --- CARREGAR NOVOS WIDGETS ---
        if (tabelaAulasHoje != null) carregarAulasDeHoje();
        if (tabelaUltimosAlunos != null) carregarUltimosRegistros();
        if (graficoCursos != null) carregarGraficoCursos();
        if (graficoTurnos != null) carregarGraficoTurnos();

        Platform.runLater(() -> {
            if (sidebarAccordion != null && !sidebarAccordion.getPanes().isEmpty()) {
                sidebarAccordion.setExpandedPane(sidebarAccordion.getPanes().get(0));
            }
        });
        
        configurarMenuRetratil();
    }
    
    private void carregarAulasDeHoje() {
        // Configura colunas
        colHoraInicio.setCellValueFactory(new PropertyValueFactory<>("horaInicio"));
        colTurmaHoje.setCellValueFactory(new PropertyValueFactory<>("nomeTurma"));
        colDisciplinaHoje.setCellValueFactory(new PropertyValueFactory<>("nomeDisciplina"));
        colProfHoje.setCellValueFactory(new PropertyValueFactory<>("nomeProfessor"));

        // Obtém o dia da semana atual em Português (Ex: "Segunda-feira")
        DayOfWeek diaSemana = LocalDate.now().getDayOfWeek();
        String diaNome = diaSemana.getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        // Ajusta capitalização (primeira letra maiúscula)
        diaNome = diaNome.substring(0, 1).toUpperCase() + diaNome.substring(1).toLowerCase();
        
        // Se for sábado ou domingo, talvez queira mostrar "Segunda-feira" para teste,
        // ou simplesmente mostrar vazio.
        
        try {
            List<Horario> aulas = horarioDAO.getHorariosDoDia(diaNome);
            tabelaAulasHoje.setItems(FXCollections.observableArrayList(aulas));
            tabelaAulasHoje.setPlaceholder(new Label("Sem aulas agendadas para hoje (" + diaNome + ")."));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarUltimosRegistros() {
        colNomeAlunoRecente.setCellValueFactory(new PropertyValueFactory<>("nomeCompleto"));
        colDataNascRecente.setCellValueFactory(new PropertyValueFactory<>("dataNascimento"));
        colRespRecente.setCellValueFactory(new PropertyValueFactory<>("responsavel")); // toString do Responsavel

        try {
            List<Aluno> recentes = alunoDAO.getUltimosCadastrados(5);
            tabelaUltimosAlunos.setItems(FXCollections.observableArrayList(recentes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarGraficoCursos() {
    try {
        Map<String, Double> dados = cursoDAO.getMediaNotasPorCurso();
        
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Média Geral");

        dados.forEach((curso, media) -> {
            if (media > 0) { // Mostra apenas cursos com notas lançadas
                XYChart.Data<Number, String> data = new XYChart.Data<>(media, curso);
                series.getData().add(data);
            }
        });

        graficoCursos.getData().clear();
        graficoCursos.getData().add(series);

        // --- MÁGICA DO ESTILO DINÂMICO ---
        Platform.runLater(() -> {
            for (XYChart.Series<Number, String> serie : graficoCursos.getData()) {
                for (XYChart.Data<Number, String> data : serie.getData()) {
                    double media = data.getXValue().doubleValue();
                    javafx.scene.Node node = data.getNode();
                    
                    // Remove todos os estilos existentes para evitar conflitos
                    node.getStyleClass().removeAll("bar-critico", "bar-atencao", "bar-sucesso");

                    // Aplica o novo estilo com base na média
                    if (media < 6.0) {
                        node.getStyleClass().add("bar-critico"); // Vermelho
                    } else if (media < 8.0) {
                        node.getStyleClass().add("bar-atencao"); // Amarelo
                    } else {
                        node.getStyleClass().add("bar-sucesso"); // Verde
                    }
                }
            }
        });
        // --- FIM DO ESTILO DINÂMICO ---

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void carregarGraficoTurnos() {
        try {
            Map<String, Integer> dados = turmaDAO.getContagemAlunosPorTurno();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Alunos");

            dados.forEach((turno, total) -> {
                series.getData().add(new XYChart.Data<>(turno, total));
            });

            graficoTurnos.getData().clear();
            graficoTurnos.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarResumosDashboard() {
        if (lblTotalAlunos == null) return;
        try {
            lblTotalAlunos.setText(String.valueOf(alunoDAO.getAll().size()));
            lblTotalCursos.setText(String.valueOf(cursoDAO.getAll().size()));
            lblTotalTurmas.setText(String.valueOf(turmaDAO.getAll().size()));
            lblTotalProfessores.setText(String.valueOf(professorDAO.getAll().size()));
        } catch (Exception e) {
            lblTotalAlunos.setText("-");
        }
    }

    private void loadView(String fxmlName) {
        try {
            String path = "/com/example/view/" + fxmlName + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Navegação", "Não foi possível carregar: " + fxmlName);
        }
    }

    // --- HANDLERS DO MENU ---

    @FXML private void handleMenuDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/TelaInicial.fxml"));
            BorderPane root = loader.load();
            mainBorderPane.setCenter(root.getCenter());
            
            TelaInicialController newController = loader.getController();
            // Injeta as dependências necessárias para o novo controller funcionar
            newController.mainBorderPane = this.mainBorderPane;
            newController.alunoDAO = this.alunoDAO;
            newController.cursoDAO = this.cursoDAO;
            newController.turmaDAO = this.turmaDAO;
            newController.professorDAO = this.professorDAO;
            newController.horarioDAO = this.horarioDAO;
            
            // Recarrega os widgets no novo controller
            newController.carregarResumosDashboard();
            newController.carregarAulasDeHoje();
            newController.carregarUltimosRegistros();
            newController.carregarGraficoCursos();
            newController.carregarGraficoTurnos();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleMenuAlunos() { loadView("GestaoAlunos"); }
    @FXML private void handleMenuProfessores() { loadView("GestaoProfessores"); }
    @FXML private void handleMenuCursos() { loadView("GestaoCurso"); } // Atenção ao nome do FXML (GestaoCurso vs GestaoCursos)
    @FXML private void handleMenuTurmas() { loadView("GestaoTurmas"); }
    @FXML private void handleMenuDisciplinas() { loadView("GestaoDisciplinas"); }
    @FXML private void handleMenuPeriodosLetivos() { loadView("GestaoPeriodosLetivos"); }

    // Handlers Ações Rápidas (Popups)
    @FXML private void handleCadastroAlunoButton() { 
        SceneNavigator.openNewWindow("tela_cadastro_aluno", "Cadastro de Novo Aluno"); 
        carregarResumosDashboard(); 
        carregarUltimosRegistros(); // Atualiza a lista também
    }
    @FXML private void handleCadastroProfessorButton() { SceneNavigator.openNewWindow("CadastroProfessor", "Cadastro de Novo Professor"); }
    @FXML private void handleCadastroCursoButton() { SceneNavigator.openNewWindow("CadastroCurso", "Cadastro de Novo Curso"); }
    @FXML private void handleCadastroTurmaButton() { SceneNavigator.openNewWindow("CadastroTurma", "Cadastro de Nova Turma"); }
    
    // Handlers Académico/Sistema
    @FXML private void handleGestaoGradeButton() { SceneNavigator.openNewWindow("GestaoGrade", "Gerenciar Grade"); }
    @FXML private void handleAssociarDisciplinaButton() { SceneNavigator.openNewWindow("GestaoProfessorDisciplina", "Associar Disciplinas"); }
    @FXML private void handleCadastroUsuarioButton() { SceneNavigator.openNewWindow("RegisterUser", "Novo Usuário"); }
    @FXML private void handleSairButton() { Platform.exit(); }

    // --- MENU RETRÁTIL ---
    private void configurarMenuRetratil() {
        if (sidebarScrollPane == null) return;
        sidebarScrollPane.setPrefWidth(70);
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

    @FXML private void handleMenuExpand() {
        if (collapseTimer == null) configurarMenuRetratil();
        collapseTimer.stop();
        if (sidebarScrollPane == null) return;
        sidebarScrollPane.setPrefWidth(260);
        sidebarScrollPane.getStyleClass().remove("sidebar-collapsed");
    }

    @FXML private void handleMenuCollapse() {
        if (collapseTimer == null) configurarMenuRetratil();
        collapseTimer.playFromStart();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}