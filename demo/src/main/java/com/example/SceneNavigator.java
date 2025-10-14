package com.example;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SceneNavigator {

    private static Stage mainStage;

    public static void setStage(Stage stage) {
        mainStage = stage;
    }

    /**
     * Troca a cena na janela principal. Usado para navegação principal (ex: Login -> Menu).
     */
    public static void switchTo(String fxmlName) {
        try {
            String path = "/com/example/view/" + fxmlName + ".fxml";
            Parent root = FXMLLoader.load(SceneNavigator.class.getResource(path));
            mainStage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Erro ao carregar o FXML: " + fxmlName);
            e.printStackTrace();
        }
    }

    /**
     * NOVO MÉTODO: Abre uma nova janela (Stage) na frente da janela principal.
     * Perfeito para formulários de cadastro ou edição.
     * @param fxmlName O nome do FXML da nova janela.
     * @param title O título da nova janela.
     */
    public static void openNewWindow(String fxmlName, String title) {
        try {
            String path = "/com/example/view/" + fxmlName + ".fxml";
            Parent root = FXMLLoader.load(SceneNavigator.class.getResource(path));

            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setScene(new Scene(root));

            // Bloqueia a interação com a janela principal enquanto esta estiver aberta
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(mainStage);
            newStage.setResizable(false);

            // Mostra a nova janela e espera ela ser fechada
            newStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erro ao abrir nova janela: " + fxmlName);
            e.printStackTrace();
        }
    }
}