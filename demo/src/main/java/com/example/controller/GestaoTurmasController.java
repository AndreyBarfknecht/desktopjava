package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Aluno; 
import com.example.model.Turma; 
import com.example.model.Horario;
import com.example.model.Nota; 
import com.example.repository.HorarioDAO;
import com.example.repository.AlunoDAO; 
import com.example.repository.TurmaDAO; 
import com.example.repository.NotaDAO; 
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException; 
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set; // Import necessário
import java.util.stream.Collectors; // Import necessário
import javafx.scene.control.TextInputDialog; 
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
import javafx.scene.control.ListView; // Import necessário
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
    @FXML private Button btnEditarHorario;
    @FXML private Button btnExcluirHorario;

    // --- FXML DA ABA AVALIAÇÕES (ATUALIZADO) ---
    @FXML private ListView<String> avaliacoesListView; // <-- Lista da Esquerda
    @FXML private TableView<Nota> notasTableView;       // <-- Tabela da Direita
    @FXML private Button btnEditarNota;
    @FXML private Button btnExcluirNota;

    // --- FXML de Paginação (Para a tabela de Turmas) ---
    @FXML private Button btnPaginaAnterior;
    @FXML private Button btnPaginaProxima;
    @FXML private Label lblStatusPaginacao;

    // --- DAOs ---
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO; 
    private HorarioDAO horarioDAO; 
    private NotaDAO notaDAO; 

    // --- Listas de Dados (ATUALIZADO) ---
    private final ObservableList<Turma> masterData = FXCollections.observableArrayList();
    private final ObservableList<Aluno> alunosMasterData = FXCollections.observableArrayList(); 
    private final ObservableList<Horario> horariosMasterData = FXCollections.observableArrayList(); 
    
    private final ObservableList<String> avaliacoesMasterData = FXCollections.observableArrayList(); 
    private final ObservableList<Nota> notasMasterData = FXCollections.observableArrayList();
    private FilteredList<Nota> notasFilteredData; 

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
        this.notaDAO = new NotaDAO(); 
        
        configurarColunaAcoes();
        
        turmasTableView.setItems(masterData);
        carregarTurmas(); 

        
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

        // --- LÓGICA DA ABA AVALIAÇÕES (ATUALIZADO) ---
        avaliacoesListView.setItems(avaliacoesMasterData);
        notasFilteredData = new FilteredList<>(notasMasterData, p -> false);
        notasTableView.setItems(notasFilteredData);

        // --- LISTENER MESTRE-DETALHE (TURMA -> ABAS) ---
        turmasTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, turmaSelecionada) -> {
                if (turmaSelecionada != null) {
                    carregarAlunosDaTurma(turmaSelecionada);
                    carregarHorariosDaTurma(turmaSelecionada); 
                    carregarAvaliacoesDaTurma(turmaSelecionada); 
                } else {
                    alunosMasterData.clear();
                    horariosMasterData.clear(); 
                    avaliacoesMasterData.clear();
                    notasMasterData.clear();
                }

                if (turmaSelecionada == null) {
                    btnEditarHorario.setDisable(true);
                    btnExcluirHorario.setDisable(true);
                    btnEditarNota.setDisable(true);
                    btnExcluirNota.setDisable(true);
                }
            }
        );

        // --- NOVO LISTENER (ListView de Avaliações -> TableView de Notas) ---
        avaliacoesListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldAvaliacao, nomeAvaliacao) -> {
                if (nomeAvaliacao != null) {
                    notasFilteredData.setPredicate(nota -> 
                        nota.getAvaliacao().equals(nomeAvaliacao)
                    );
                } else {
                    notasFilteredData.setPredicate(p -> false);
                }
            }
        );

        // --- LISTENER (HORÁRIOS -> BOTÕES) ---
        horariosTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldHorario, newHorario) -> {
                boolean selecionado = (newHorario != null);
                btnEditarHorario.setDisable(!selecionado);
                btnExcluirHorario.setDisable(!selecionado);
            }
        );

        // --- LISTENER (NOTAS -> BOTÕES) ---
        notasTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldNota, newNota) -> {
                boolean selecionado = (newNota != null);
                btnEditarNota.setDisable(!selecionado);
                btnExcluirNota.setDisable(!selecionado);
            }
        );
        
        btnEditarHorario.setDisable(true);
        btnExcluirHorario.setDisable(true);
        btnEditarNota.setDisable(true);
        btnExcluirNota.setDisable(true);
    }
    
    // --- Métodos de Paginação e Carga de Turmas ---
    
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
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar os horários da turma.");
        }
    }

    // --- MÉTODO NOVO (Para a aba Avaliações) ---
    private void carregarAvaliacoesDaTurma(Turma turma) {
        avaliacoesMasterData.clear();
        notasMasterData.clear();
        
        if (turma == null) {
            return;
        }
        
        try {
            // 1. Busca todas as notas (ERRO ESTAVA AQUI)
            List<Nota> todasNotas = notaDAO.getNotasByTurmaId(turma.getId());
            
            notasMasterData.setAll(todasNotas);
            
            // 2. Extrai os nomes únicos das avaliações
            Set<String> nomesAvaliacoes = todasNotas.stream()
                                                    .map(Nota::getAvaliacao)
                                                    .collect(Collectors.toSet());
            
            avaliacoesMasterData.addAll(nomesAvaliacoes);
            
            notasFilteredData.setPredicate(p -> false);

        } catch (Exception e) {
            e.printStackTrace(); // Bom para vermos o erro exato no log
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar as avaliações da turma.");
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
    
    // --- LÓGICA DE AÇÕES NA TABELA (Turmas) ---
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

    // --- LÓGICA DE AÇÕES NA TABELA DE ALUNOS ---
    private void configurarColunaAcoesAluno() {
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
    // --- CORRIGIDOS PARA INCLUIR 'Stage stage = new Stage();' ---

    private void handleDefinirHorario(Turma turma) {
        if (turma == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroHorario.fxml"));
            Parent root = loader.load();
            CadastroHorarioController controller = loader.getController();
            controller.setTurmaParaHorario(turma); 

            // --- CORREÇÃO (O Stage estava em falta) ---
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
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de cadastro de horários.");
        }
    }
    
    private void handleLancarNota(Aluno aluno) {
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

            // --- CORREÇÃO (O Stage estava em falta) ---
            Stage stage = new Stage();
            stage.setTitle("Lançar Nota para " + aluno.getNomeCompleto());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(turmasTableView.getScene().getWindow());
            stage.showAndWait();

            refreshNotas(); // Agora chama o refresh da aba de avaliações

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de registro de notas.");
        }
    }

    private void handleEditar(Turma turmaSelecionada) {
        if (turmaSelecionada == null) return; 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/CadastroTurma.fxml"));
            Parent root = loader.load();
            CadastroTurmaController controller = loader.getController();
            controller.setTurmaParaEdicao(turmaSelecionada); 

            // --- CORREÇÃO (O Stage estava em falta) ---
            Stage stage = new Stage();
            stage.setTitle("Editar Turma");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(turmasTableView.getScene().getWindow()); 
            stage.showAndWait(); 

            carregarTurmas(); 
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(Turma turmaSelecionada) {
        if (turmaSelecionada == null) return; 
        
        // --- CORREÇÃO (O Alert estava em falta) ---
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
                avaliacoesMasterData.clear(); // Limpa a nova lista
                notasMasterData.clear();      // Limpa a nova lista
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir a turma. Verifique se ela não está associada a matrículas ou horários.");
            }
        }
    }

    // --- MÉTODOS (Ações da Aba Horários) ---

    @FXML
    private void onEditarHorario() {
        // (Este método já estava correto no teu ficheiro original)
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

            Turma turmaSelecionada = turmasTableView.getSelectionModel().getSelectedItem();
            if (turmaSelecionada != null) {
                carregarHorariosDaTurma(turmaSelecionada);
            }
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de cadastro de horário.");
        }
    }

    @FXML
    private void onExcluirHorario() {
        // (Este método já estava correto no teu ficheiro original)
        Horario horarioSelecionado = horariosTableView.getSelectionModel().getSelectedItem();
        if (horarioSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione um horário para excluir.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja realmente excluir este horário?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText(null); 
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                horarioDAO.delete(horarioSelecionado.getId());
                
                Turma turmaSelecionada = turmasTableView.getSelectionModel().getSelectedItem();
                if (turmaSelecionada != null) {
                    carregarHorariosDaTurma(turmaSelecionada);
                }
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir o horário.");
            }
        }
    }
    
    
    // --- MÉTODOS (Ações da Aba Avaliações) ---

    @FXML
    private void onEditarNota() {
        Nota notaSelecionada = notasTableView.getSelectionModel().getSelectedItem();
        if (notaSelecionada == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma nota para editar.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(notaSelecionada.getValor()));
        dialog.setTitle("Editar Nota");
        // Diálogo melhorado para mostrar o aluno
        dialog.setHeaderText("Aluno: " + notaSelecionada.getNomeAluno());
        dialog.setContentText("Editando nota de '" + notaSelecionada.getAvaliacao() + 
                              "' (" + notaSelecionada.getNomeDisciplina() + "):");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                double novoValor = Double.parseDouble(result.get().replace(",", "."));
                
                notaSelecionada.setValor(novoValor);
                notaDAO.update(notaSelecionada);
                
                refreshNotas(); // Chama o método de refresh atualizado
                
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Formato", "O valor da nota é inválido. Use números (ex: 8.5).");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Não foi possível atualizar a nota.");
            }
        }
    }

    @FXML
    private void onExcluirNota() {
        Nota notaSelecionada = notasTableView.getSelectionModel().getSelectedItem();
        if (notaSelecionada == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Por favor, selecione uma nota para excluir.");
            return;
        }

        // Alerta de confirmação melhorado
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
            "Aluno: " + notaSelecionada.getNomeAluno() + "\n" +
            "Disciplina: " + notaSelecionada.getNomeDisciplina() + "\n" +
            "Avaliação: " + notaSelecionada.getAvaliacao() + " (" + notaSelecionada.getValor() + ")\n\n" +
            "Deseja realmente excluir esta nota?", 
            ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                notaDAO.delete(notaSelecionada.getIdNota());
                refreshNotas(); // Chama o método de refresh atualizadogetId
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir a nota.");
            }
        }
    }
    
    // --- MÉTODO REFRESH ATUALIZADO ---
    private void refreshNotas() {
        String avaliacaoSelecionada = avaliacoesListView.getSelectionModel().getSelectedItem();
        
        Turma turma = turmasTableView.getSelectionModel().getSelectedItem();
        if (turma != null) {
            carregarAvaliacoesDaTurma(turma);
            
            if (avaliacaoSelecionada != null) {
                avaliacoesListView.getSelectionModel().select(avaliacaoSelecionada);
            }
        }
    }
    
    // --- MÉTODO AUXILIAR ---
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}