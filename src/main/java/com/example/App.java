package com.example;

import javafx.application.Application;
import javafx.stage.Stage;
import com.example.views.MainView;
import com.example.controllers.MainController;
import com.example.models.SearchModel;
import javafx.scene.Scene;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize MVC components
            SearchModel model = new SearchModel();
            MainController controller = new MainController(model);
            MainView mainView = new MainView(controller);
            
            // Set up the primary stage
            primaryStage.setScene(mainView.getScene());
            primaryStage.setTitle("Search Application");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 