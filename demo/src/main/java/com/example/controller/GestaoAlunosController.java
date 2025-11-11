package com.example.controller;

import com.example.SceneNavigator; // Usaremos para abrir os popups
import com.example.model.Aluno;
import com.example.model.Responsavel; // Precisamos para o onExcluir
import com.example.repository.AlunoDAO;
import com.example.repository.ResponsavelDAO;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView; // Para os ícones
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.control.TableCell; // Importante
import javafx.scene.control.TableColumn; // Importante
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox; // Para os botões na célula
import javafx.stage.Modality;
import javafx.stage.Stage;

// Este novo controller combina a lógica de ConsultaAlunosController
// com a navegação para as ações
public class GestaoAlunosController implements Initializable {

    // --- FXML da Tabela e Pesquisa (Igual ao ConsultaAlunosController) ---
    @FXML private TableView<Aluno> alunosTableView;
    @FXML private TextField searchField;

    // --- NOVA COLUNA DE AÇÕES (Decisão 3) ---
    @FXML private TableColumn<Aluno, Void> colAcoes;

    // --- DAOs e Listas (Igual ao ConsultaAlunosController) ---
    private AlunoDAO alunoDAO;
    private ResponsavelDAO responsavelDAO;
    private ObservableList<Aluno> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.alunoDAO = new AlunoDAO();
        this.responsavelDAO = new ResponsavelDAO();
        
        // Configura a coluna de ações (Passo 3 da sua decisão)
        configurarColunaAcoes();
        
        carregarAlunos(); // Carrega os dados

        // Lógica de filtro (copiada de ConsultaAlunosController)
        FilteredList<Aluno> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(aluno -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (aluno.getNomeCompleto().toLowerCase().contains(lowerCaseFilter)) return true;
                if (aluno.getCpf() != null && aluno.getCpf().contains(lowerCaseFilter)) return true;
                if (aluno.getResponsavel().getNomeCompleto().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        alunosTableView.setItems(filteredData);
    }
    
    private void carregarAlunos() {
        masterData.clear();
        List<Aluno> alunosList = alunoDAO.getAll();
        masterData.addAll(alunosList);
    }

    // --- NOVOS MÉTODOS PARA OS BLOCOS DE AÇÃO (Decisão 4) ---
    
    @FXML
    private void onCadastrarNovoAluno() {
        // Abre o popup de cadastro que já existe
        SceneNavigator.openNewWindow("tela_cadastro_aluno", "Cadastro de Novo Aluno");
        // Atualiza a tabela caso um novo aluno tenha sido salvo
        carregarAlunos(); 
    }

    @FXML
    private void onMatricularAluno() {
        // Abre o popup de matrícula que já existe
        SceneNavigator.openNewWindow("Matricula", "Matrícula de Alunos");
        // Não precisamos recarregar os alunos aqui, mas é uma boa prática
    }
    
    // --- LÓGICA DE AÇÕES NA TABELA (Decisão 3) ---

    private void configurarColunaAcoes() {
        // 1. Define uma "Cell Factory" (Fábrica de Células) para a coluna
        colAcoes.setCellFactory(param -> new TableCell<Aluno, Void>() {
            
            // Criamos os botões com ícones
            private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
            private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            private final HBox painelBotoes = new HBox(5, btnEditar, btnExcluir);

            {
                // Adiciona classes de estilo (do style.css)
                btnEditar.getStyleClass().add("salvar-button");
                btnExcluir.getStyleClass().add("cancel-button");
                painelBotoes.setPadding(new Insets(5));
                
                // 3. Define as ações dos botões
                btnEditar.setOnAction(event -> {
                    Aluno aluno = getTableView().getItems().get(getIndex());
                    handleEditar(aluno); // Chama a lógica de edição
                });
                
                btnExcluir.setOnAction(event -> {
                    Aluno aluno = getTableView().getItems().get(getIndex());
                    handleExcluir(aluno); // Chama a lógica de exclusão
                });
            }

            // 2. Este método é chamado para desenhar a célula
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null); // Se a linha estiver vazia, não mostra nada
                } else {
                    setGraphic(painelBotoes); // Mostra os botões
                }
            }
        });
    }

    // --- LÓGICA MOVIDA DE "ConsultaAlunosController" ---
    // (Note que mudei os nomes para "handleEditar" e "handleExcluir"
    // e eles agora recebem o Aluno como parâmetro)

    private void handleEditar(Aluno alunoSelecionado) {
        if (alunoSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Aluno não encontrado.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/tela_cadastro_aluno.fxml"));
            Parent root = loader.load();

            CadastroAlunoController controller = loader.getController();
            controller.setAlunoParaEdicao(alunoSelecionado); // Passa o aluno para a tela

            Stage stage = new Stage();
            stage.setTitle("Editar Aluno");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            carregarAlunos(); // Atualiza a tabela após a edição
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de edição.");
        }
    }

   private void handleExcluir(Aluno alunoSelecionado) {
        if (alunoSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválível", "Aluno não encontrado.");
            return;
        }

        // Lógica de exclusão (exatamente a mesma que estava em ConsultaAlunosController)
        int responsavelId = alunoSelecionado.getResponsavel().getId();
        int numFilhos = alunoDAO.countAlunosByResponsavelId(responsavelId);

        if (numFilhos > 1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão");
            alert.setHeaderText("Este responsável tem outros alunos registados.");
            alert.setContentText("Deseja excluir apenas o aluno " + alunoSelecionado.getNomeCompleto() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                alunoDAO.delete(alunoSelecionado.getId());
                masterData.remove(alunoSelecionado);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão Completa");
            alert.setHeaderText("Este é o último aluno associado a " + alunoSelecionado.getResponsavel().getNomeCompleto() + ".");
            alert.setContentText("Deseja excluir o aluno E o seu responsável do sistema?");
            
            ButtonType btnExcluirAmbos = new ButtonType("Sim, excluir ambos");
            ButtonType btnExcluirApenasAluno = new ButtonType("Não, excluir apenas o aluno");
            alert.getButtonTypes().setAll(btnExcluirAmbos, btnExcluirApenasAluno, ButtonType.CANCEL);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == btnExcluirAmbos) {
                    alunoDAO.delete(alunoSelecionado.getId());
                    responsavelDAO.delete(responsavelId);
                    masterData.remove(alunoSelecionado);
                } else if (result.get() == btnExcluirApenasAluno) {
                    alunoDAO.delete(alunoSelecionado.getId());
                    masterData.remove(alunoSelecionado);
                }
            }
        }
    }

    // Método utilitário (copiado de ConsultaAlunosController)
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}