package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Aluno; 
import com.example.model.Turma; 
import com.example.model.Horario;
import com.example.repository.HorarioDAO;
import com.example.repository.AlunoDAO; 
import com.example.repository.TurmaDAO; 
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException; 
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label; 
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip; 
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GestaoTurmasController implements Initializable {

    // --- FXML da Tabela Principal (Turmas) ---
    @FXML private TableView<Turma> turmasTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<Turma, Void> colAcoes;

    // --- FXML da Tabela de Detalhe (Alunos) ---
    @FXML private TableView<Aluno> alunosTableView;
    @FXML private TextField alunoSearchField;
    @FXML private TableColumn<Aluno, Void> colAcoesAluno;

    // --- FXML da Tabela de Detalhe (Horários) ---
    @FXML private TableView<Horario> horariosTableView;

    // --- NOVOS BOTÕES FXML PARA A ABA HORÁRIOS ---
    @FXML private Button btnEditarHorario;
    @FXML private Button btnExcluirHorario;

    // --- FXML de Paginação (Para a tabela de Turmas) ---
    @FXML private Button btnPaginaAnterior;
    @FXML private Button btnPaginaProxima;
    @FXML private Label lblStatusPaginacao;

    // --- DAOs ---
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO; 
    private HorarioDAO horarioDAO; 

    // --- Listas de Dados ---
    private final ObservableList<Turma> masterData = FXCollections.observableArrayList();
    private final ObservableList<Aluno> alunosMasterData = FXCollections.observableArrayList(); 
    private final ObservableList<Horario> horariosMasterData = FXCollections.observableArrayList(); 

    // --- Variáveis de Paginação (Para Turmas) ---
    private int paginaAtual = 1;
    private final int limitePorPagina = 15;
    private String termoBuscaAtual = "";
    private int totalPaginas = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.turmaDAO = new TurmaDAO();
        this.alunoDAO = new AlunoDAO(); 
        this.horarioDAO = new HorarioDAO(); 
        
        configurarColunaAcoes();
        
        turmasTableView.setItems(masterData);
        carregarTurmas(); // Carregamento inicial paginado

        
        // --- LÓGICA DA ABA ALUNOS ---
        FilteredList<Aluno> alunosFilteredData = new FilteredList<>(alunosMasterData, p -> true);
        alunoSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            alunosFilteredData.setPredicate(aluno -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return aluno.getNomeCompleto().toLowerCase().contains(lowerCaseFilter);
            });
        });
        alunosTableView.setItems(alunosFilteredData);
        configurarColunaAcoesAluno();
        
        // --- LÓGICA DA ABA HORÁRIOS ---
        horariosTableView.setItems(horariosMasterData);

        // Listener Mestre-Detalhe (Carrega ambas as abas)
        turmasTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, turmaSelecionada) -> {
                if (turmaSelecionada != null) {
                    carregarAlunosDaTurma(turmaSelecionada);
                    carregarHorariosDaTurma(turmaSelecionada); 
                } else {
                    alunosMasterData.clear();
                    horariosMasterData.clear(); 
                }
            }
        );
    }
    
    // --- Métodos de Paginação e Carga de Turmas (Sem alterações) ---
    
    private void carregarTurmas() {
        try {
            int totalTurmas = turmaDAO.countTurmasFiltradas(termoBuscaAtual);
            totalPaginas = (int) Math.ceil((double) totalTurmas / limitePorPagina);
            if (totalPaginas == 0) totalPaginas = 1;

            List<Turma> turmas = turmaDAO.getTurmasPaginadoEFiltrado(
                termoBuscaAtual, paginaAtual, limitePorPagina
            );
            
            masterData.setAll(turmas);
            atualizarControlesPaginacao();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Não foi possível carregar as turmas.");
        }
    }

    private void atualizarControlesPaginacao() {
        lblStatusPaginacao.setText("Página " + paginaAtual + " de " + totalPaginas);
        btnPaginaAnterior.setDisable(paginaAtual == 1);
        btnPaginaProxima.setDisable(paginaAtual >= totalPaginas);
    }

    @FXML
    private void onBuscar() {
        termoBuscaAtual = searchField.getText();
        paginaAtual = 1; 
        carregarTurmas();
    }

    @FXML
    private void onPaginaAnterior() {
        if (paginaAtual > 1) {
            paginaAtual--;
            carregarTurmas();
        }
    }

    @FXML
    private void onPaginaProxima() {
        if (paginaAtual < totalPaginas) {
            paginaAtual++;
            carregarTurmas();
        }
    }
    
    // --- Carregamento dos Detalhes (Abas) ---

    private void carregarAlunosDaTurma(Turma turma) {
        if (turma == null) {
            alunosMasterData.clear();
            return;
        }
        try {
            List<Aluno> alunos = alunoDAO.getAlunosByTurmaId(turma.getId());
            alunosMasterData.setAll(alunos);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar os alunos da turma.");
        }
    }

    private void carregarHorariosDaTurma(Turma turma) {
        if (turma == null) {
            horariosMasterData.clear();
            return;
        }
        try {
            List<Horario> horarios = horarioDAO.getHorariosByTurmaId(turma.getId());
            horariosMasterData.setAll(horarios);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar os horários da turma.");
        }
    }


    // --- Métodos de Ação (Topo) ---
    
    @FXML
    private void onCadastrarNovaTurma() {
        SceneNavigator.openNewWindow("CadastroTurma", "Cadastro de Nova Turma"); 
        termoBuscaAtual = "";
        searchField.clear();
        paginaAtual = 1;
        carregarTurmas(); 
    }

    // --- MÉTODO REMOVIDO ---
    // @FXML private void onConsultarHorarios() { ... }
    
    
    // --- LÓGICA DE AÇÕES NA TABELA (Turmas) ---
    // (Esta parte não muda)
    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<Turma, Void>() {
            
            private final Button btnDefinirHorario = new Button("", new FontAwesomeIconView(FontAwesomeIcon.CALENDAR));
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnDefinirHorario, btnEditar, btnExcluir);

            {
                btnDefinirHorario.getStyleClass().add("salvar-button");
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));

                Tooltip.install(btnDefinirHorario, new Tooltip("Definir horários desta turma"));
                Tooltip.install(btnEditar, new Tooltip("Editar dados da turma"));
                Tooltip.install(btnExcluir, new Tooltip("Excluir turma"));

                btnDefinirHorario.setOnAction(event -> {
                    Turma turma = getTableView().getItems().get(getIndex());
                    handleDefinirHorario(turma);
                });
                
                btnEditar.setOnAction(event -> {
                    Turma turma = getTableView().getItems().get(getIndex());
                    handleEditar(turma);
                });
                
                btnExcluir.setOnAction(event -> {
                    Turma turma = getTableView().getItems().get(getIndex());
                    handleExcluir(turma);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : painelBotoes);
            }
        });
    }

    // --- LÓGICA DE AÇÕES NA TABELA DE ALUNOS (Não mexe) ---
    private void configurarColunaAcoesAluno() {
        // (Este método permanece 100% igual)
        colAcoesAluno.setCellFactory(param -> new TableCell<Aluno, Void>() {
            private final Button btnLancarNota = new Button("", new FontAwesomeIconView(FontAwesomeIcon.EDIT)); 
            private final HBox painelBotoes = new HBox(5, btnLancarNota);

            {
                btnLancarNota.getStyleClass().add("salvar-button"); 
                painelBotoes.setPadding(new Insets(5));
                Tooltip.install(btnLancarNota, new Tooltip("Lançar notas para este aluno"));
                
                btnLancarNota.setOnAction(event -> {
                    Aluno aluno = getTableView().getItems().get(getIndex());
                    handleLancarNota(aluno); 
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : painelBotoes);
            }
        });
    }
    
    // --- MÉTODOS DE AÇÃO (handle...) ---

    private void handleDefinirHorario(Turma turma) {
        // (Este método permanece 100% igual)
        if (turma == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroHorario.fxml"));
            Parent root = loader.load();
            CadastroHorarioController controller = loader.getController();
            controller.setTurmaParaHorario(turma); 

            Stage stage = new Stage();
            stage.setTitle("Definir Horários para: " + turma.getNome());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(turmasTableView.getScene().getWindow()); 
            stage.showAndWait();
            
            if (turma.equals(turmasTableView.getSelectionModel().getSelectedItem())) {
                carregarHorariosDaTurma(turma);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de cadastro de horários.");
        }
    }
    
    private void handleLancarNota(Aluno aluno) {
        // (Este método permanece 100% igual)
        if (aluno == null) return;
        Turma turmaSelecionada = turmasTableView.getSelectionModel().getSelectedItem();
        if (turmaSelecionada == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Nenhuma turma selecionada.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/RegistroNotas.fxml"));
            Parent root = loader.load();
            RegistroNotasController controller = loader.getController();
            controller.initData(turmaSelecionada, aluno);

            Stage stage = new Stage();
            stage.setTitle("Lançar Nota para " + aluno.getNomeCompleto());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(turmasTableView.getScene().getWindow());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de registro de notas.");
        }
    }

    private void handleEditar(Turma turmaSelecionada) {
        // (Este método permanece 100% igual)
        if (turmaSelecionada == null) return; 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroTurma.fxml"));
            Parent root = loader.load();
            CadastroTurmaController controller = loader.getController();
            controller.setTurmaParaEdicao(turmaSelecionada); 

            Stage stage = new Stage();
            stage.setTitle("Editar Turma");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(turmasTableView.getScene().getWindow()); 
            stage.showAndWait(); 

            carregarTurmas(); 
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(Turma turmaSelecionada) {
        // (Este método permanece 100% igual)
        if (turmaSelecionada == null) return; 
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir a turma '" + turmaSelecionada.getNome() + "'?");
        alert.setContentText("Atenção: Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                turmaDAO.delete(turmaSelecionada.getId()); 
                if (masterData.size() == 1 && paginaAtual > 1) {
                    paginaAtual--;
                }
                carregarTurmas();
                alunosMasterData.clear(); 
                horariosMasterData.clear(); 
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir a turma. Verifique se ela não está associada a matrículas ou horários.");
            }
        }
    }

    // --- MÉTODOS NOVOS (Ações da Aba Horários) ---
    // (A lógica foi copiada do ConsultaHorariosController)

    @FXML
    private void onEditarHorario() {
        Horario horarioSelecionado = horariosTableView.getSelectionModel().getSelectedItem();
        if (horarioSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um horário para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroHorario.fxml"));
            Parent root = loader.load();
            CadastroHorarioController controller = loader.getController();
            controller.setHorarioParaEdicao(horarioSelecionado);
            
            Stage stage = new Stage();
            stage.setTitle("Editar Horário");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(horariosTableView.getScene().getWindow()); 
            
            stage.showAndWait();

            // Recarrega os dados da aba de horários
            Turma turmaSelecionada = turmasTableView.getSelectionModel().getSelectedItem();
            if (turmaSelecionada != null) {
                carregarHorariosDaTurma(turmaSelecionada);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de cadastro de horário.");
        }
    }

    @FXML
    private void onExcluirHorario() {
        Horario horarioSelecionado = horariosTableView.getSelectionModel().getSelectedItem();
        if (horarioSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um horário para excluir.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja realmente excluir este horário?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText(null); // Limpa o cabeçalho
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                horarioDAO.delete(horarioSelecionado.getId());
                
                // Recarrega os dados da aba de horários
                Turma turmaSelecionada = turmasTableView.getSelectionModel().getSelectedItem();
                if (turmaSelecionada != null) {
                    carregarHorariosDaTurma(turmaSelecionada);
                }
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir o horário.");
            }
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}