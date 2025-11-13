package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Aluno; // NOVO
import com.example.model.Turma; 
import com.example.repository.AlunoDAO; // NOVO
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GestaoTurmasController implements Initializable {

    // --- FXML da Tabela Principal (Turmas) ---
    @FXML private TableView<Turma> turmasTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<Turma, Void> colAcoes;

    // --- NOVO: FXML da Tabela de Detalhe (Alunos) ---
    @FXML private TableView<Aluno> alunosTableView;
    @FXML private TextField alunoSearchField;
    @FXML private TableColumn<Aluno, Void> colAcoesAluno;

    // --- DAOs ---
    private TurmaDAO turmaDAO;
    private AlunoDAO alunoDAO; // NOVO

    // --- Listas de Dados ---
    private ObservableList<Turma> masterData = FXCollections.observableArrayList();
    private ObservableList<Aluno> alunosMasterData = FXCollections.observableArrayList(); // NOVO

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.turmaDAO = new TurmaDAO();
        this.alunoDAO = new AlunoDAO(); // NOVO
        
        configurarColunaAcoes();
        carregarTurmas(); 

        // --- Filtro da Tabela Principal (Turmas) ---
        FilteredList<Turma> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(turma -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (turma.getNome().toLowerCase().contains(lowerCaseFilter)) return true;
                if (turma.getCurso().getNomeCurso().toLowerCase().contains(lowerCaseFilter)) return true;
                if (turma.getTurno().toLowerCase().contains(lowerCaseFilter)) return true;
                if (turma.getPeriodoLetivo().getNome().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        turmasTableView.setItems(filteredData);

        // --- NOVO: Configuração da Tabela de Alunos ---
        
        // 1. Configurar o filtro (search) da tabela de alunos
        FilteredList<Aluno> alunosFilteredData = new FilteredList<>(alunosMasterData, p -> true);
        alunoSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            alunosFilteredData.setPredicate(aluno -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return aluno.getNomeCompleto().toLowerCase().contains(lowerCaseFilter);
            });
        });
        alunosTableView.setItems(alunosFilteredData);

        // 2. Configurar o listener da tabela de turmas (Mestre-Detalhe)
        turmasTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, turmaSelecionada) -> {
                if (turmaSelecionada != null) {
                    // Se uma turma for selecionada, carrega os alunos dela
                    carregarAlunosDaTurma(turmaSelecionada);
                } else {
                    // Se nenhuma turma for selecionada, limpa a tabela de alunos
                    alunosMasterData.clear();
                }
            }
        );
        
        // 3. Configurar a nova coluna de ações "Lançar Notas"
        configurarColunaAcoesAluno();
    }
    
    private void carregarTurmas() {
        masterData.clear();
        List<Turma> turmasList = turmaDAO.getAll(); 
        masterData.addAll(turmasList);
    }
    
    // --- NOVO: Método para carregar os alunos da turma selecionada ---
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


    // --- MÉTODOS PARA OS BLOCOS DE AÇÃO (Topo) ---
    
    @FXML
    private void onCadastrarNovaTurma() {
        SceneNavigator.openNewWindow("CadastroTurma", "Cadastro de Nova Turma"); 
        carregarTurmas(); // Atualiza a tabela
    }

    // @FXML
    // private void onMatricularAluno() {
    //     // Esta ação agora pode ser mais inteligente
    //     Turma turmaSelecionada = turmasTableView.getSelectionModel().getSelectedItem();
        
    //     SceneNavigator.openNewWindow("Matricula", "Matrícula de Alunos");
        
    //     // Se uma turma estava selecionada, atualiza a lista de alunos
    //     if (turmaSelecionada != null) {
    //         carregarAlunosDaTurma(turmaSelecionada);
    //     }
    // }

    @FXML
private void onConsultarHorarios() {
    // Abre o popup de consulta que já existe
    SceneNavigator.openNewWindow("ConsultaHorarios", "Consulta de Horários");
}
    
    @FXML
    private void onDefinirHorarios() {
        SceneNavigator.openNewWindow("CadastroHorario", "Definição de Horários"); 
    }
    
    // --- LÓGICA DE AÇÕES NA TABELA (Turmas) ---

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<Turma, Void>() {
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnEditar, btnExcluir);
            {
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));
                
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

    // --- NOVO: LÓGICA DE AÇÕES NA TABELA DE ALUNOS ---

    private void configurarColunaAcoesAluno() {
        colAcoesAluno.setCellFactory(param -> new TableCell<Aluno, Void>() {
            // Criamos apenas o botão "Lançar Notas"
            // LINHA CORRIGIDA
            private final Button btnLancarNota = new Button("", new FontAwesomeIconView(FontAwesomeIcon.EDIT)); // Ícone de nota (corrigido)
            private final HBox painelBotoes = new HBox(5, btnLancarNota);

            {
                btnLancarNota.getStyleClass().add("salvar-button"); // Estilo azul
                painelBotoes.setPadding(new Insets(5));
                
                btnLancarNota.setOnAction(event -> {
                    Aluno aluno = getTableView().getItems().get(getIndex());
                    handleLancarNota(aluno); // Chama a nova ação
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : painelBotoes);
            }
        });
    }
    
    // --- NOVO: Método para o botão "Lançar Notas" ---
    private void handleLancarNota(Aluno aluno) {
        if (aluno == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Aluno não encontrado.");
            return;
        }
        
        // Pega a turma que já está selecionada no painel esquerdo
        Turma turmaSelecionada = turmasTableView.getSelectionModel().getSelectedItem();
        if (turmaSelecionada == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Nenhuma turma selecionada.");
            return;
        }

        try {
            // 1. Carrega o FXML manualmente (como fazemos no handleEditar)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/RegistroNotas.fxml"));
            Parent root = loader.load();

            // 2. Pega o controlador da nova janela
            RegistroNotasController controller = loader.getController();
            
            // 3. CHAMA O NOVO MÉTODO: Injeta os dados da turma e do aluno
            controller.initData(turmaSelecionada, aluno);

            // 4. Cria e exibe a nova janela (popup)
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

    // --- LÓGICA MOVIDA DE "ConsultaTurmasController" ---

    private void handleEditar(Turma turmaSelecionada) {
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

            carregarTurmas(); // Atualiza a tabela
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(Turma turmaSelecionada) {
        if (turmaSelecionada == null) return; 

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir a turma '" + turmaSelecionada.getNome() + "'?");
        alert.setContentText("Atenção: Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                turmaDAO.delete(turmaSelecionada.getId()); 
                masterData.remove(turmaSelecionada); // Atualiza a UI
                alunosMasterData.clear(); // Limpa a tabela de alunos
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Exclusão", "Não foi possível excluir a turma. Verifique se ela não está associada a matrículas ou horários.");
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