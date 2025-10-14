module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    // Requisito para a biblioteca de ícones que adicionámos
    requires de.jensd.fx.glyphs.fontawesome;

    // Abre os pacotes para que o JavaFX possa acedê-los (via reflection)
    opens com.example.controller to javafx.fxml;
    opens com.example.view to javafx.fxml;
    
    // --- LINHA ADICIONADA ---
    // Permite que o JavaFX aceda aos seus modelos (útil para ComboBox)
    opens com.example.model to javafx.fxml;

    // Exporta os pacotes para que as suas classes sejam visíveis em todo o projeto
    exports com.example;
    
    // --- LINHAS ADICIONADAS ---
    exports com.example.model;
    exports com.example.service;
}