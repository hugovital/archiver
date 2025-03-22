package com.example.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import com.example.controllers.MainController;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.input.MouseButton;
import javafx.concurrent.Worker.State;
import netscape.javascript.JSObject;
import java.awt.Desktop;
import java.net.URI;
import java.io.File;
import java.util.Base64;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

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
    private WebView foundItemsView;
    private static final Pattern URL_PATTERN = Pattern.compile(
        "\\b(https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");
    
    // Default base64 encoded small folder icon (16x16 pixels)
    private static final String DEFAULT_FOLDER_ICON = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAAbwAAAG8B8aLcQwAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAB5SURBVDiNY2AYBYMPMBKrMCsr638GBgYGXl5eZiIMwKqZiYGBgaG5uZmBl5eXCZciFxcXBlwuYMKjGQYYcRqAR/N/UVFR7AYQoxkGWPBpxqUZwwBiNcMAM7KzSdWMYgCxmmEAb0Ai0TB4AKYBpGhGMYAUzSBQUVExAAYYc+4EGH/GAAAAAElFTkSuQmCC";
    
    private static final String FOLDER_ICON_BASE64;
    
    static {
        String iconBase64;
        try {
            System.out.println("Attempting to load folder icon...");
            InputStream is = MainView.class.getResourceAsStream("/images/folder-icon.png");
            if (is != null) {
                try {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[1024];
                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    iconBase64 = Base64.getEncoder().encodeToString(buffer.toByteArray());
                    is.close();
                    System.out.println("Successfully loaded folder icon from resources");
                } catch (Exception e) {
                    System.err.println("Error reading folder icon file: " + e.getMessage());
                    System.err.println("Stack trace:");
                    e.printStackTrace();
                    iconBase64 = DEFAULT_FOLDER_ICON;
                }
            } else {
                System.err.println("Folder icon not found in resources at: /images/folder-icon.png");
                System.err.println("Expected path: src/main/resources/images/folder-icon.png");
                System.err.println("Using default folder icon instead");
                iconBase64 = DEFAULT_FOLDER_ICON;
            }
        } catch (Exception e) {
            System.err.println("Error loading folder icon: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            iconBase64 = DEFAULT_FOLDER_ICON;
        }
        FOLDER_ICON_BASE64 = iconBase64;
    }

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
        
        // Initialize WebView
        foundItemsView = new WebView();
        foundItemsView.setPrefHeight(200);
        VBox.setVgrow(foundItemsView, Priority.ALWAYS);
        
        // Enable context menu for copy/paste
        foundItemsView.setContextMenuEnabled(true);
        
        // Add JavaScript bridge for handling links
        foundItemsView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == State.SUCCEEDED) {
                JSObject window = (JSObject) foundItemsView.getEngine().executeScript("window");
                window.setMember("javaApp", new JavaApp());
            }
        });

        // Set initial content
        setFoundItemsText("");

        lowerTextField = new TextField();
        lowerTextField.setPrefHeight(100);
        
        addButton = new Button("ADD");
        HBox addButtonBox = new HBox();
        addButtonBox.setAlignment(Pos.CENTER_RIGHT);
        addButtonBox.getChildren().add(addButton);
        addButtonBox.setPadding(new Insets(5, 0, 0, 0));

        centerPanel.getChildren().addAll(foundItemsView, lowerTextField, addButtonBox);
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

    public void setFoundItemsText(String content) {
        if (content == null || content.trim().isEmpty()) {
            String emptyHtml = createHtmlContent("Found items will be displayed here");
            foundItemsView.getEngine().loadContent(emptyHtml);
            return;
        }

        // Convert content to HTML with styled links
        String htmlContent = createHtmlContent(content);
        foundItemsView.getEngine().loadContent(htmlContent);
    }

    private String createHtmlContent(String content) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 10px; font-size: 13px; }");
        html.append("a { cursor: pointer; }");
        html.append("a[title] { text-decoration: none; }");
        html.append(".text-content { white-space: pre-wrap; }");
        html.append("::selection { background: lightblue; }");
        html.append("img { display: inline-block; }");
        html.append("[title] { position: relative; }");
        html.append("[title]:hover::after { ")
            .append("content: attr(title); ")
            .append("position: absolute; ")
            .append("top: 100%; ")
            .append("left: 50%; ")
            .append("transform: translateX(-50%); ")
            .append("background: #333; ")
            .append("color: white; ")
            .append("padding: 4px 8px; ")
            .append("border-radius: 4px; ")
            .append("font-size: 12px; ")
            .append("white-space: nowrap; ")
            .append("z-index: 1000; ")
            .append("margin-top: 5px; ")
            .append("}");
        html.append("</style>");
        html.append("</head><body><div class='text-content'>");

        html.append(content);

        html.append("</div></body></html>");
        return html.toString();
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("\n", "<br>");
    }

    private String escapeJavaScript(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("'", "\\'");
    }

    // JavaScript interface class
    public class JavaApp {
        public void openUrl(String url) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("Error opening URL", 
                             "Could not open the URL in browser", 
                             e.getMessage());
            }
        }

        public void openFile(String filePath) {
            try {
                File file = new File(filePath);
                Desktop.getDesktop().open(file);
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("Error opening file", 
                             "Could not open the file", 
                             e.getMessage());
            }
        }

        public void openFolder(String folderPath) {
            try {
                File folder = new File(folderPath);
                Desktop.getDesktop().open(folder);
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("Error opening folder", 
                             "Could not open the folder", 
                             e.getMessage());
            }
        }
    }

    public void showErrorAlert(String title, String header, String content) {
        // Print to console first
        System.err.println("Error occurred: " + title);
        System.err.println("Header: " + header);
        System.err.println("Content: " + content);
        
        // Show alert dialog
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFoundItemsText() {
        setFoundItemsText("");
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

    public void cleanAllFields() {
        searchField.clear();
        clearFoundItemsText();
        lowerTextField.clear();
        clearResults();
    }

    public static String getFolderIconBase64() {
        return FOLDER_ICON_BASE64;
    }
} 