module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.jensd.fx.glyphs.fontawesome;
    requires java.sql;
    
    // LINHAS OBRIGATÓRIAS PARA O PDF:
    requires org.apache.pdfbox;
    requires java.desktop; 

    // ABRE OS PACOTES:
    opens com.example.controller to javafx.fxml;
    opens com.example.view to javafx.fxml;
    opens com.example.model to javafx.fxml;

    // EXPORTA OS PACOTES:
    exports com.example;
    exports com.example.model;
    exports com.example.util;       
    exports com.example.repository; 
    exports com.example.controller;
    
    // LINHA OBRIGATÓRIA PARA O PDF:
    exports com.example.service; 
}