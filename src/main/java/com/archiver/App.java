package com.archiver;

import com.archiver.controllers.MainController;
import com.archiver.models.SearchModel;
import com.archiver.views.MainView;

import javafx.application.Application;
import javafx.stage.Stage;

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
    	System.out.println( "Running in: "  + SearchModel.getOperatingSystem() );    	
        launch(args);
    }
} 