package com.example;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainWindow extends Stage {
    
    public MainWindow() {
        Button btn = new Button("Click Me");
        btn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setHeaderText(null);
            alert.setContentText("Hello!");
            alert.showAndWait();
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);
        this.setScene(scene);
        this.setTitle("JavaFX Demo");
    }
} 