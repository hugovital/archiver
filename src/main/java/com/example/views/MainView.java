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
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.scene.layout.Region;
import javafx.scene.layout.ScrollPane;

public class MainView {
    private final MainController controller;
    private Scene scene;
    private TextField searchField;
    private TextArea lowerTextArea;
    private Button searchButton;
    private Button cleanButton;
    private Button addButton;
    private Button loadFileButton;
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
        Tooltip.install(searchBar, new Tooltip("searchBar (HBox)"));
        searchField = new TextField();
        searchField.setPrefWidth(300);
        searchButton = new Button("Search");
        cleanButton = new Button("Clean");
        
        // Add Enter key handler to search field
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                searchButton.fire(); // This triggers the search button's action
            }
        });
        
        searchBar.getChildren().addAll(searchField, searchButton, cleanButton);
        searchBar.setPadding(new Insets(10));
        searchBar.setAlignment(Pos.CENTER_LEFT);
        mainLayout.setTop(searchBar);

        // Left panel
        VBox leftPanel = new VBox(10);
        leftPanel.setPrefWidth(300);
        leftPanel.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        Tooltip.install(leftPanel, new Tooltip("leftPanel (VBox)"));
        
        resultsContainer = new VBox(5);
        resultsContainer.setPadding(new Insets(10));
        Tooltip.install(resultsContainer, new Tooltip("resultsContainer (VBox)"));
        leftPanel.getChildren().add(resultsContainer);

        // Wrap leftPanel in ScrollPane
        ScrollPane leftScrollPane = new ScrollPane(leftPanel);
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setPrefViewportWidth(300);
        leftScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainLayout.setLeft(leftScrollPane);

        // Center panel
        VBox centerPanel = new VBox(10);
        centerPanel.setPadding(new Insets(10));
        Tooltip.install(centerPanel, new Tooltip("centerPanel (VBox)"));
        
        // Initialize WebView with ScrollPane
        foundItemsView = new WebView();
        foundItemsView.setPrefHeight(200);
        Tooltip.install(foundItemsView, new Tooltip("foundItemsView (WebView)"));
        
        // Enable JavaScript and add bridge
        foundItemsView.getEngine().setJavaScriptEnabled(true);
        
        // Add JavaScript bridge for handling links
        foundItemsView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == State.SUCCEEDED) {
                JSObject window = (JSObject) foundItemsView.getEngine().executeScript("window");
                window.setMember("javaApp", new JavaApp());
            }
        });

        ScrollPane webViewScrollPane = new ScrollPane(foundItemsView);
        webViewScrollPane.setFitToWidth(true);
        webViewScrollPane.setFitToHeight(true);
        webViewScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(webViewScrollPane, Priority.ALWAYS);

        lowerTextArea = new TextArea();
        lowerTextArea.setPrefHeight(100);
        lowerTextArea.setWrapText(true);
        lowerTextArea.setPromptText("Add your text here...");
        lowerTextArea.setStyle("-fx-text-alignment: left; -fx-line-spacing: -0.4em; -fx-padding: 2;");
        
        addButton = new Button("ADD");
        loadFileButton = new Button("Load File");
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonBox.getChildren().addAll(loadFileButton, spacer, addButton);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));
        Tooltip.install(buttonBox, new Tooltip("buttonBox (HBox)"));

        centerPanel.getChildren().addAll(webViewScrollPane, lowerTextArea, buttonBox);
        mainLayout.setCenter(centerPanel);

        // Set prompts
        searchField.setPromptText("Enter search term...");

        // Event handlers
        searchButton.setOnAction(e -> controller.handleSearch());
        cleanButton.setOnAction(e -> controller.handleClean());
        addButton.setOnAction(e -> controller.handleAdd());
        loadFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File");
            
            // Set initial directory to user's Downloads folder
            String userHome = System.getProperty("user.home");
            File downloadsFolder = new File(userHome + "/Downloads");
            
            // If Downloads folder doesn't exist, fall back to user home
            if (!downloadsFolder.exists()) {
                downloadsFolder = new File(userHome);
            }
            
            fileChooser.setInitialDirectory(downloadsFolder);
            File selectedFile = fileChooser.showOpenDialog(scene.getWindow());
            
            if (selectedFile != null) {
                // Get current text and cursor position
                String currentText = lowerTextArea.getText();
                int caretPosition = lowerTextArea.getCaretPosition();
                
                // Insert file path at current position
                String filePath = selectedFile.getAbsolutePath();
                String newText;
                
                // If we're not at the start of a line, add a newline first
                if (caretPosition > 0 && !currentText.substring(caretPosition - 1, caretPosition).equals("\n")) {
                    newText = currentText.substring(0, caretPosition) + "\n" + filePath + 
                             currentText.substring(caretPosition);
                } else {
                    newText = currentText.substring(0, caretPosition) + filePath + 
                             currentText.substring(caretPosition);
                }
                
                lowerTextArea.setText(newText);
                // Position cursor after the inserted file path
                lowerTextArea.positionCaret(caretPosition + filePath.length() + 1);
            }
        });

        scene = new Scene(mainLayout);
        controller.setView(this);

        // Add tooltip for the main layout
        Tooltip.install(mainLayout, new Tooltip("mainLayout (BorderPane)"));
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
        html.append("img { display: inline-block; vertical-align: middle; cursor: pointer; }");
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
        html.append("<script>")
            .append("function openUrl(url) { javaApp.openUrl(url); }")
            .append("function openFile(path) { javaApp.openFile(path); }")
            .append("function openFolder(path) { javaApp.openFolder(path); }")
            .append("</script>");
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
        return lowerTextArea.getText();
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
        lowerTextArea.clear();
        clearResults();
    }

    public static String getFolderIconBase64() {
        return FOLDER_ICON_BASE64;
    }
} 