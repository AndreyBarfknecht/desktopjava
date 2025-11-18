package com.example.controller;

import com.example.SceneNavigator;
import com.example.model.Aluno;
import com.example.model.Responsavel; 
import com.example.repository.AlunoDAO;
import com.example.repository.ResponsavelDAO;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView; 
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// REMOVIDO: FilteredList não é mais usado
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label; // NOVO: Para o status da paginação
import javafx.scene.control.TableCell; 
import javafx.scene.control.TableColumn; 
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip; // NOVO: Para as dicas
import javafx.scene.layout.HBox; 
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.example.service.GeradorPdfService; // Precisamos disto
import java.util.UUID; // Para o código de verificação

public class GestaoAlunosController implements Initializable {

    // --- FXML da Tabela e Pesquisa ---
    @FXML private TableView<Aluno> alunosTableView;
    @FXML private TextField searchField;
    @FXML private TableColumn<Aluno, Void> colAcoes;

    // --- NOVOS FXML DE PAGINAÇÃO ---
    @FXML private Button btnPaginaAnterior;
    @FXML private Button btnPaginaProxima;
    @FXML private Label lblStatusPaginacao;

    // --- DAOs e Listas ---
    private AlunoDAO alunoDAO;
    private ResponsavelDAO responsavelDAO;
    // masterData agora guarda apenas a PÁGINA ATUAL
    private ObservableList<Aluno> masterData = FXCollections.observableArrayList();

    // --- NOVAS VARIÁVEIS DE ESTADO DA PAGINAÇÃO ---
    private int paginaAtual = 1;
    private final int limitePorPagina = 15; // Define quantos alunos por página
    private String termoBuscaAtual = "";
    private int totalPaginas = 1;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.alunoDAO = new AlunoDAO();
        this.responsavelDAO = new ResponsavelDAO();
        
        configurarColunaAcoes();
        
        // --- LÓGICA DE FILTRO REMOVIDA ---
        // A FilteredList foi removida.
        
        // A tabela agora é preenchida diretamente pela masterData (que conterá apenas 15 itens)
        alunosTableView.setItems(masterData);

        // A busca agora é acionada pelo método onBuscar() (ligado ao Enter no FXML)
        // E o carregamento inicial é feito aqui:
        carregarAlunos(); 
    }
    
    /**
     * MÉTODO CENTRAL REESCRITO
     * Carrega os alunos do banco de dados de forma paginada e filtrada.
     */
    private void carregarAlunos() {
        try {
            // 1. Busca o total de alunos (filtrado) para calcular as páginas
            int totalAlunos = alunoDAO.countAlunosFiltrados(termoBuscaAtual);
            
            // 2. Calcula o total de páginas
            totalPaginas = (int) Math.ceil((double) totalAlunos / limitePorPagina);
            if (totalPaginas == 0) {
                totalPaginas = 1; // Garante que sempre temos pelo menos a página 1
            }
            
            // 3. Busca a lista de alunos da página atual
            List<Aluno> alunos = alunoDAO.getAlunosPaginadoEFiltrado(
                termoBuscaAtual, 
                paginaAtual, 
                limitePorPagina
            );
            
            // 4. Atualiza a tabela
            masterData.setAll(alunos);

            // 5. Atualiza os controlos de paginação
            atualizarControlesPaginacao();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro de Base de Dados", "Não foi possível carregar os alunos.");
        }
    }

    @FXML
    private void onConsultarMatriculas() {
        SceneNavigator.openNewWindow("ConsultaMatriculas", "Consulta de Matrículas");
    }
    
    /**
     * NOVO: Atualiza o texto da legenda e o estado dos botões de paginação.
     */
    private void atualizarControlesPaginacao() {
        lblStatusPaginacao.setText("Página " + paginaAtual + " de " + totalPaginas);
        btnPaginaAnterior.setDisable(paginaAtual == 1);
        btnPaginaProxima.setDisable(paginaAtual >= totalPaginas);
    }
    
    /**
     * NOVO: Chamado quando o utilizador pressiona Enter no campo de busca.
     */
    @FXML
    private void onBuscar() {
        termoBuscaAtual = searchField.getText();
        paginaAtual = 1; // Sempre volta para a página 1 ao buscar
        carregarAlunos();
    }

    /**
     * NOVO: Chamado pelo botão "Anterior".
     */
    @FXML
    private void onPaginaAnterior() {
        if (paginaAtual > 1) {
            paginaAtual--;
            carregarAlunos();
        }
    }

    /**
     * NOVO: Chamado pelo botão "Próxima".
     */
    @FXML
    private void onPaginaProxima() {
        if (paginaAtual < totalPaginas) {
            paginaAtual++;
            carregarAlunos();
        }
    }

    // --- MÉTODOS DE AÇÃO (Topo) ---
    
    @FXML
    private void onCadastrarNovoAluno() {
        SceneNavigator.openNewWindow("tela_cadastro_aluno", "Cadastro de Novo Aluno");
        
        // Após cadastrar, limpa a busca e volta para a página 1
        termoBuscaAtual = "";
        searchField.clear();
        paginaAtual = 1;
        carregarAlunos(); 
    }

    // O método onMatricularAluno() foi removido (agora está na linha)

    
    // --- LÓGICA DE AÇÕES NA TABELA (COM TOOLTIPS) ---

    private void configurarColunaAcoes() {
    colAcoes.setCellFactory(param -> new TableCell<Aluno, Void>() {

        // 1. Adiciona o novo botão
        private final Button btnCertificado = new Button("", new FontAwesomeIconView(FontAwesomeIcon.FILE_PDF_ALT));
        private final Button btnMatricular = new Button("", new FontAwesomeIconView(FontAwesomeIcon.ADDRESS_BOOK));
        private final Button btnEditar = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
        private final Button btnExcluir = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));

        // 2. Adiciona-o ao painel (na primeira posição)
        private final HBox painelBotoes = new HBox(5, btnCertificado, btnMatricular, btnEditar, btnExcluir);

        {
            // 3. Define o estilo e a dica (Tooltip)
            btnCertificado.getStyleClass().add("salvar-button");
            Tooltip.install(btnCertificado, new Tooltip("Emitir certificados para este aluno"));

            btnMatricular.getStyleClass().add("salvar-button");
            btnEditar.getStyleClass().add("salvar-button");
            btnExcluir.getStyleClass().add("cancel-button");
            painelBotoes.setPadding(new Insets(5));

            Tooltip.install(btnMatricular, new Tooltip("Matricular este aluno"));
            Tooltip.install(btnEditar, new Tooltip("Editar dados do aluno"));
            Tooltip.install(btnExcluir, new Tooltip("Excluir aluno e responsável"));

            // 4. Define a ação do novo botão
            btnCertificado.setOnAction(event -> {
                Aluno aluno = getTableView().getItems().get(getIndex());
                handleGerarCertificado(aluno); // Chama o novo método handler
            });

            // Ações dos botões (sem alteração)
            btnMatricular.setOnAction(event -> {
                Aluno aluno = getTableView().getItems().get(getIndex());
                handleMatricular(aluno);
            });

            btnEditar.setOnAction(event -> {
                Aluno aluno = getTableView().getItems().get(getIndex());
                handleEditar(aluno); 
            });

            btnExcluir.setOnAction(event -> {
                Aluno aluno = getTableView().getItems().get(getIndex());
                handleExcluir(aluno); 
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : painelBotoes);
        }
    });
}

    /**
     * NOVO: Ação para o botão de matricular na linha.
     */
    private void handleMatricular(Aluno aluno) {
        if (aluno == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Aluno não encontrado.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/Matricula.fxml"));
            Parent root = loader.load();
            MatriculaController controller = loader.getController();
            
            // Injeta o aluno no controlador do popup
            controller.setAlunoParaMatricular(aluno);

            Stage stage = new Stage();
            stage.setTitle("Matricular Aluno: " + aluno.getNomeCompleto());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            // Define o "dono" para corrigir o problema do Tiling Window Manager
            stage.initOwner(alunosTableView.getScene().getWindow()); 
            stage.showAndWait();
            
            // Não precisa recarregar, pois a matrícula não afeta esta lista.
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de matrícula.");
        }
    }

    private void handleGerarCertificado(Aluno aluno) {
    if (aluno == null) {
        showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Aluno não encontrado.");
        return;
    }

    try {
        // 1. Carrega o novo FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/EmissaoCertificado.fxml"));
        Parent root = loader.load();

        // 2. Obtém o controller do popup
        EmissaoCertificadoController controller = loader.getController();

        // 3. Injeta o aluno no controlador do popup
        controller.initData(aluno);

        Stage stage = new Stage();
        stage.setTitle("Emitir Certificado para: " + aluno.getNomeCompleto());
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(alunosTableView.getScene().getWindow()); 
        stage.showAndWait();

    } catch (IOException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de emissão de certificados.");
    }
}


    private void handleEditar(Aluno alunoSelecionado) {
        if (alunoSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Inválida", "Aluno não encontrado.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/tela_cadastro_aluno.fxml"));
            Parent root = loader.load();
            CadastroAlunoController controller = loader.getController();
            controller.setAlunoParaEdicao(alunoSelecionado); 

            Stage stage = new Stage();
            stage.setTitle("Editar Aluno");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            // Define o "dono" para corrigir o problema do Tiling Window Manager
            stage.initOwner(alunosTableView.getScene().getWindow());
            stage.showAndWait();

            // Atualiza a página atual após a edição
            carregarAlunos(); 
            
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
                } else if (result.get() == btnExcluirApenasAluno) {
                    alunoDAO.delete(alunoSelecionado.getId());
                }
            }
        }
        
        // Após a exclusão, recarrega a página
        // (Verifica se a página ficou vazia e volta uma)
        if (masterData.size() == 1 && paginaAtual > 1) {
            paginaAtual--;
        }
        carregarAlunos();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}