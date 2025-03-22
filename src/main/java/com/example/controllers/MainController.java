package com.example.controllers;

import com.example.models.SearchModel;
import com.example.views.MainView;
import javafx.scene.control.Alert;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class MainController {
    private SearchModel model;
    private MainView view;
    private static final int MAX_DEPTH = 4; // Maximum directory depth to search
    private static final int MAX_FILES = 10; // Maximum number of files to find

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
            // Parse the selected text to get potential file paths
            String[] parts = selectedText.split("\\|");
            StringBuilder result = new StringBuilder();
            boolean foundAnyFile = false;
            
            for (String part : parts) {
                String cleaned = part.trim();
                
                if (!cleaned.isEmpty()) {
                    try {
                        Path filePath = Paths.get(cleaned);
                        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                            String content = Files.readString(filePath);
                            result.append("File: ").append(filePath).append("\n");
                            result.append("Content:\n").append(content).append("\n\n");
                            foundAnyFile = true;
                        }
                    } catch (Exception e) {
                        // Print stack trace to console
                        System.err.println("Error processing file path: " + cleaned);
                        e.printStackTrace();
                        
                        // Show alert for this specific file error
                        showAlert("File Error", 
                                "Error processing file: " + cleaned,
                                e.getMessage());
                        continue;
                    }
                }
            }

            // If no files found, display the original selected text
            if (!foundAnyFile) {
                result.append("No files found. Original selection:\n").append(selectedText);
            }

            // Update the UI
            view.setFoundItemsText(result.toString());
            
        } catch (Exception e) {
            // Print stack trace to console
            System.err.println("Error in handleItemSelection");
            System.err.println("Selected text: " + selectedText);
            e.printStackTrace();
            
            // Show alert for general error
            showAlert("Error Processing Selection", 
                     "An error occurred while processing the selected item",
                     e.getMessage());
        }
    }

    private List<Path> searchFiles(String fileName) {
        List<Path> results = new ArrayList<>();
        AtomicInteger fileCount = new AtomicInteger(0);
        
        try {
            Path searchPath = Paths.get(System.getProperty("user.dir"));
            
            Files.walkFileTree(searchPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (fileCount.get() >= MAX_FILES) {
                        return FileVisitResult.TERMINATE;
                    }
                    
                    if (file.getFileName().toString().toLowerCase()
                            .contains(fileName.toLowerCase())) {
                        results.add(file);
                        fileCount.incrementAndGet();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    // Skip hidden directories and limit depth
                    if (dir.toFile().isHidden() || 
                        dir.getNameCount() - searchPath.getNameCount() > MAX_DEPTH) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return results;
    }
} 