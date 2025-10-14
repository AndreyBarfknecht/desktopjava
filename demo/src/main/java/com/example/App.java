package com.example;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.setStage(stage);
        
        // ALTERADO: A aplicação agora começa na tela de Login
        SceneNavigator.switchTo("Login"); 
        
        stage.setTitle("Sistema Acadêmico");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}