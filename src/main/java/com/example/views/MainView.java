package com.example.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;  // This includes TextArea
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import com.example.controllers.MainController;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.control.ScrollPane;
import java.awt.Desktop;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;

public class MainView {
    private final MainController controller;
    private Scene scene;
    private TextField searchField;
    private TextField lowerTextField;
    private Button searchButton;
    private Button cleanButton;
    private Button addButton;
    private VBox resultsContainer;
    private Label selectedLabel;
    private static final String NORMAL_STYLE = "-fx-padding: 5; -fx-background-color: #f0f0f0; -fx-background-radius: 5;";
    private static final String SELECTED_STYLE = "-fx-padding: 5; -fx-background-color: #0096ff; -fx-background-radius: 5; -fx-text-fill: white;";
    private TextArea foundItemsArea;
    private static final Pattern URL_PATTERN = Pattern.compile(
        "\\b(https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");

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
        
        // Initialize TextArea
        foundItemsArea = new TextArea();
        foundItemsArea.setWrapText(true);
        foundItemsArea.setEditable(false);
        foundItemsArea.setPrefRowCount(10);
        foundItemsArea.setPromptText("Found items will be displayed here");
        VBox.setVgrow(foundItemsArea, Priority.ALWAYS);

        // Create context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> foundItemsArea.copy());
        MenuItem selectAllItem = new MenuItem("Select All");
        selectAllItem.setOnAction(e -> foundItemsArea.selectAll());
        contextMenu.getItems().addAll(copyItem, selectAllItem);
        
        foundItemsArea.setContextMenu(contextMenu);

        // Add click handler for URLs
        foundItemsArea.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                String selectedText = foundItemsArea.getSelectedText();
                if (selectedText == null || selectedText.isEmpty()) {
                    String text = foundItemsArea.getText();
                    int caretPosition = foundItemsArea.getCaretPosition();
                    String url = findUrlAtPosition(text, caretPosition);
                    if (url != null) {
                        try {
                            Desktop.getDesktop().browse(new URI(url));
                        } catch (Exception ex) {
                            showErrorAlert("Error opening URL", 
                                         "Could not open the URL in browser", 
                                         ex.getMessage());
                        }
                    }
                }
            }
        });

        lowerTextField = new TextField();
        lowerTextField.setPrefHeight(100);
        
        addButton = new Button("ADD");
        HBox addButtonBox = new HBox();
        addButtonBox.setAlignment(Pos.CENTER_RIGHT);
        addButtonBox.getChildren().add(addButton);
        addButtonBox.setPadding(new Insets(5, 0, 0, 0));

        centerPanel.getChildren().addAll(foundItemsArea, lowerTextField, addButtonBox);
        mainLayout.setCenter(centerPanel);

        // Set prompts
        searchField.setPromptText("Enter search term...");
        lowerTextField.setPromptText("Lower text field");

        // Event handlers
        searchButton.setOnAction(e -> controller.handleSearch());
        cleanButton.setOnAction(e -> controller.handleClean());
        addButton.setOnAction(e -> controller.handleAdd());

        scene = new Scene(mainLayout);
        controller.setView(this);
    }

    private String findUrlAtPosition(String text, int position) {
        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            if (position >= matcher.start() && position <= matcher.end()) {
                return matcher.group();
            }
        }
        return null;
    }

    public void setFoundItemsText(String content) {
        if (content == null || content.trim().isEmpty()) {
            foundItemsArea.clear();
            return;
        }
        foundItemsArea.setText(content);
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public String getLowerText() {
        return lowerTextField.getText();
    }

    public void clearResults() {
        resultsContainer.getChildren().clear();
        selectedLabel = null;
    }

    public void addResultItem(String result) {
        Label resultLabel = new Label(result);
        resultLabel.setWrapText(true);
        resultLabel.setStyle(NORMAL_STYLE);
        resultLabel.setMaxWidth(280);
        
        resultLabel.setOnMouseClicked(e -> handleLabelSelection(resultLabel));
        
        resultsContainer.getChildren().add(resultLabel);
    }

    private void handleLabelSelection(Label clickedLabel) {
        if (selectedLabel != null) {
            selectedLabel.setStyle(NORMAL_STYLE);
        }
        
        if (selectedLabel == clickedLabel) {
            selectedLabel = null;
            clearFoundItemsText();
        } else {
            clickedLabel.setStyle(SELECTED_STYLE);
            selectedLabel = clickedLabel;
            controller.handleItemSelection(clickedLabel.getText());
        }
    }

    private void clearFoundItemsText() {
        foundItemsArea.clear();
    }

    public void cleanAllFields() {
        searchField.clear();
        foundItemsArea.clear();
        lowerTextField.clear();
        clearResults();
    }
} 