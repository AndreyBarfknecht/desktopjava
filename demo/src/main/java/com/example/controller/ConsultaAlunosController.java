package com.example.controller;

import com.example.model.Aluno;
import com.example.service.AcademicService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class ConsultaAlunosController implements Initializable {

    @FXML private TableView<Aluno> alunosTableView;
    @FXML private Button fecharButton;

    private AcademicService service = AcademicService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // A configuração das colunas já foi feita no FXML com PropertyValueFactory.
        // Agora, só precisamos de fornecer os dados.
        
        alunosTableView.setItems(service.getAlunos());
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) alunosTableView.getScene().getWindow();
        stage.close();
    }
}