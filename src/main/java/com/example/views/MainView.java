package com.example.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.example.controllers.MainController;

public class MainView {
    private final MainController controller;
    private Scene scene;
    private TextField searchField;
    private TextArea foundItemsTextArea;
    private TextField lowerTextField;
    private Button searchButton;
    private Button cleanButton;
    private Button addButton;
    private VBox resultsContainer;
    private Label selectedLabel;
    private static final String NORMAL_STYLE = "-fx-padding: 5; -fx-background-color: #f0f0f0; -fx-background-radius: 5;";
    private static final String SELECTED_STYLE = "-fx-padding: 5; -fx-background-color: #0096ff; -fx-background-radius: 5; -fx-text-fill: white;";

    public MainView(MainController controller) {
        this.controller = controller;
        
        BorderPane mainLayout = new BorderPane();
        
        // Top search bar
        HBox searchBar = new HBox(10);
        searchField = new TextField();
        searchField.setPrefWidth(300);
        searchButton = new Button("Search");
        cleanButton = new Button("Clean");
        searchBar.getChildren().addAll(searchField, searchButton, cleanButton);
        searchBar.setPadding(new Insets(10));
        searchBar.setAlignment(Pos.CENTER_LEFT);
        mainLayout.setTop(searchBar);

        // Left panel
        VBox leftPanel = new VBox(10);
        leftPanel.setPrefWidth(300);
        leftPanel.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        
        resultsContainer = new VBox(5);
        resultsContainer.setPadding(new Insets(10));
        leftPanel.getChildren().add(resultsContainer);
        mainLayout.setLeft(leftPanel);

        // Center panel
        VBox centerPanel = new VBox(10);
        centerPanel.setPadding(new Insets(10));
        
        foundItemsTextArea = new TextArea();
        foundItemsTextArea.setWrapText(true);
        foundItemsTextArea.setEditable(false);
        foundItemsTextArea.setPrefRowCount(20);
        VBox.setVgrow(foundItemsTextArea, Priority.ALWAYS);
        
        lowerTextField = new TextField();
        lowerTextField.setPrefHeight(100);
        
        addButton = new Button("ADD");
        HBox addButtonBox = new HBox();
        addButtonBox.setAlignment(Pos.CENTER_RIGHT);
        addButtonBox.getChildren().add(addButton);
        addButtonBox.setPadding(new Insets(5, 0, 0, 0));

        centerPanel.getChildren().addAll(foundItemsTextArea, lowerTextField, addButtonBox);
        mainLayout.setCenter(centerPanel);

        // Set prompts
        searchField.setPromptText("Enter search term...");
        foundItemsTextArea.setPromptText("Found items will be displayed here");
        lowerTextField.setPromptText("Lower text field");

        // Event handlers
        searchButton.setOnAction(e -> controller.handleSearch());
        cleanButton.setOnAction(e -> controller.handleClean());
        addButton.setOnAction(e -> controller.handleAdd());

        scene = new Scene(mainLayout);
        controller.setView(this);
    }

    public Scene getScene() {
        return scene;
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public String getFoundItemsText() {
        return foundItemsTextArea.getText();
    }

    public String getLowerText() {
        return lowerTextField.getText();
    }

    public void clearResults() {
        resultsContainer.getChildren().clear();
        selectedLabel = null; // Reset selection when clearing results
    }

    public void addResultItem(String result) {
        Label resultLabel = new Label(result);
        resultLabel.setWrapText(true);
        resultLabel.setStyle(NORMAL_STYLE);
        resultLabel.setMaxWidth(280);
        
        // Add click handler for selection
        resultLabel.setOnMouseClicked(e -> handleLabelSelection(resultLabel));
        
        resultsContainer.getChildren().add(resultLabel);
    }

    private void handleLabelSelection(Label clickedLabel) {
        // If there was a previously selected label, reset its style
        if (selectedLabel != null) {
            selectedLabel.setStyle(NORMAL_STYLE);
        }
        
        // If clicking the same label, deselect it
        if (selectedLabel == clickedLabel) {
            selectedLabel = null;
            foundItemsTextArea.clear();
        } else {
            // Select the new label and process its content
            clickedLabel.setStyle(SELECTED_STYLE);
            selectedLabel = clickedLabel;
            controller.handleItemSelection(clickedLabel.getText());
        }
    }

    public void setFoundItemsText(String text) {
        foundItemsTextArea.setText(text);
    }

    public void cleanAllFields() {
        searchField.clear();
        foundItemsTextArea.clear();
        lowerTextField.clear();
        clearResults();
    }
} 