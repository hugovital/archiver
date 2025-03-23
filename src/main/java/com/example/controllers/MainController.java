package com.example.controllers;

import com.example.models.SearchModel;
import com.example.views.MainView;
import javafx.scene.control.Alert;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {
    private SearchModel model;
    private MainView view;
    private static final Pattern URL_PATTERN = Pattern.compile("\\b(https?://\\S+)\\b");

    // Add constants for tooltips to avoid string concatenation
    private static final String TOOLTIP_FILE = "Abrir o arquivo";
    private static final String TOOLTIP_FOLDER = "Abrir a pasta do arquivo";
    private static final String TOOLTIP_URL = "Abrir o link no navegador";

    public MainController(SearchModel model) {
        this.model = model;
    }

    public void setView(MainView view) {
        this.view = view;
    }

    public void handleSearch() {
        // Clear previous results
        view.clearResults();
        
        String searchTerm = view.getSearchText().trim();
        
        // Validate search term
        if (searchTerm == null || searchTerm.isEmpty()) {
            showAlert("Error", "Search term is empty", "Please enter a search term.");
            return;
        }

        // Perform search
        model.setSearchTerm(searchTerm);
        List<String> results = model.searchRecords();
        
        if (results.isEmpty()) {
            showAlert("Search Results", "No matches found", 
                     "No records found matching the search term: " + searchTerm);
        } else {
            // Display results
            results.forEach(result -> view.addResultItem(result));
        }
    }

    private void showAlert(String title, String header, String content) {
        // Print to console first
        System.err.println("\nShowing alert:");
        System.err.println("Title: " + title);
        System.err.println("Header: " + header);
        System.err.println("Content: " + content + "\n");
        
        // Show alert dialog
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void handleAdd() {
        // Implement add functionality
        System.out.println("Add button clicked");
    }

    public void handleClean() {
        view.cleanAllFields();
    }

    public void handleItemSelection(String selectedText) {
        try {
            String[] parts = selectedText.split("\\|");
            StringBuilder result = new StringBuilder();
            boolean foundAnyFile = false;
            
            for (String part : parts) {
                String cleaned = part.trim();
                
                if (!cleaned.isEmpty()) {
                    try {
                        Path filePath = Paths.get(cleaned);
                        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                            String fileName = filePath.toString();
                            // File link with tooltip - optimized string concatenation
                            result.append("File: <a href='javascript:void(0)' ")
                                  .append("onclick='javaApp.openFile(\"").append(escapeJavaScript(fileName)).append("\")' ")
                                  .append("style='color: green; text-decoration: underline;' ")
                                  .append("title='").append(TOOLTIP_FILE).append("'>")
                                  .append(escapeHtml(fileName))
                                  .append("</a> ");

                            // Folder icon with tooltip - optimized
                            result.append("<a href='javascript:void(0)' ")
                                  .append("onclick='javaApp.openFolder(\"").append(escapeJavaScript(filePath.getParent().toString())).append("\")' ")
                                  .append("style='text-decoration: none; margin-left: 5px;' ")
                                  .append("title='").append(TOOLTIP_FOLDER).append("'>")
                                  .append("<img src='data:image/png;base64,")
                                  .append(MainView.getFolderIconBase64())
                                  .append("' style='width: 16px; height: 16px; vertical-align: middle;' alt='pasta'/>")
                                  .append("</a>");

                            // If it's a txt file, append its content
                            if (fileName.toLowerCase().endsWith(".txt")) {
                                String content = Files.readString(Path.of(filePath.toString()), StandardCharsets.UTF_8);                                result.append("\nContent:\n");
                                result.append(processTextForUrls(content));
                            } else {
                                result.append("\n(Not a text file)");
                            }
                            result.append("\n\n");
                            foundAnyFile = true;
                        } else {
                            // Not a file, check for URLs in the text
                            result.append(processTextForUrls(cleaned)).append("\n");
                        }
                    } catch (Exception e) {
                        // If path is invalid, just process the text for URLs
                        result.append(processTextForUrls(cleaned)).append("\n");
                    }
                }
            }

            if (!foundAnyFile) {
                result.append("No files found. Original selection:\n")
                      .append(processTextForUrls(selectedText));
            }

            view.setFoundItemsText(result.toString());
            
        } catch (Exception e) {
            System.err.println("Error in handleItemSelection");
            System.err.println("Selected text: " + selectedText);
            e.printStackTrace();
            showAlert("Error Processing Selection", 
                     "An error occurred while processing the selected item",
                     e.getMessage());
        }
    }

    private String processTextForUrls(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        Matcher matcher = URL_PATTERN.matcher(text);
        int lastEnd = 0;
        
        while (matcher.find()) {
            // Add text before URL
            result.append(escapeHtml(text.substring(lastEnd, matcher.start())));
            
            // Add URL as clickable link with tooltip - optimized
            String url = matcher.group();
            result.append("<a href='javascript:void(0)' ")
                  .append("onclick='javaApp.openUrl(\"").append(escapeJavaScript(url)).append("\")' ")
                  .append("style='color: blue; text-decoration: underline;' ")
                  .append("title='").append(TOOLTIP_URL).append("'>")
                  .append(escapeHtml(url))
                  .append("</a>");
            
            lastEnd = matcher.end();
        }
        
        // Add remaining text
        if (lastEnd < text.length()) {
            result.append(escapeHtml(text.substring(lastEnd)));
        }
        
        return result.toString();
    }

    private String escapeJavaScript(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("'", "\\'");
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("\n", "<br>");
    }

} 