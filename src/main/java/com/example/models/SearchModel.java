package com.example.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchModel {
    // Add model properties and methods here
    private String searchTerm;
    private static final String DELIMITER = ";"; // Changed from comma to semicolon

    // Add constructor to validate environment variable on initialization
    public SearchModel() {
        // Validate environment variable exists during initialization
        getRecordsFilePath();
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public boolean containsSearchTerm(String line){

        var words = searchTerm.toLowerCase().split(" ");

        var searchWords = new ArrayList<String>();
        var foundWords = new ArrayList<String>();

        for(var word : words){
            if ( word.trim().equals(""))
                continue;
            searchWords.add(word.toLowerCase());
        }

        for(var word : searchWords){
            if(line.toLowerCase().contains(word)){
                foundWords.add(word);
            }
        }

        if(foundWords.size() == searchWords.size()){
            return true;
        }

        return false;
    }

    public List<String> searchRecords() {
        List<String> results = new ArrayList<>();
        
        try {
            // Use File instead of ResourceStream since we're reading from filesystem path
            File file = new File(getRecordsFilePath());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            );
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (containsSearchTerm(line)){
                    // Split the line by delimiter and format
                    String[] parts = line.split(DELIMITER);
                    StringBuilder formattedResult = new StringBuilder();
                    
                    // Add each element with a separator
                    for (int i = 0; i < parts.length; i++) {
                        if (i > 0) {
                            formattedResult.append(" | ");
                        }
                        formattedResult.append(parts[i].trim());
                    }
                    
                    results.add(formattedResult.toString());
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return results;
    }

    private String getRecordsFilePath() {
        String filePath = System.getenv("RECORDS_FILEPATH");
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Error: RECORDS_FILEPATH environment variable is not set");
            System.exit(1);
            return null;
        }
        return filePath;
    }

    public void backupRecordsFile(String newContent) {
        try {
            // Get the source file
            File sourceFile = new File(getRecordsFilePath());
            
            // Create backup folder if it doesn't exist
            File backupFolder = new File(sourceFile.getParent(), "backup");
            if (!backupFolder.exists()) {
                if (!backupFolder.mkdir()) {
                    throw new RuntimeException("Failed to create backup directory");
                }
            }
            
            // Get current timestamp for filename
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            String timestamp = String.format("%d_%02d_%02d_%02d_%02d_%03d",
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                now.getSecond(),
                now.getNano() / 1_000_000, // Convert nanos to millis
                now.getNano() / 1_000_000  // Add this as the actual argument for %03d
            );
            
            // Create backup filename
            String originalFileName = sourceFile.getName();
            String fileNameWithoutExt = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
            String backupFileName = String.format("%s_backup_%s.back", fileNameWithoutExt, timestamp);
            
            // Create backup file path
            File backupFile = new File(backupFolder, backupFileName);
            
            // Create backup of existing file
            java.nio.file.Files.copy(
                sourceFile.toPath(),
                backupFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            
        } catch (Exception e) {
            System.err.println("Error creating backup: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to backup file: " + e.getMessage());
        }
    }

    public void addNewContent(String content, javafx.stage.Window parentWindow) {
        try {
            // First create backup
            backupRecordsFile(content);

            // Save content to new file and get the filename
            String savedFilePath = saveTheContent(content, parentWindow);
            if (savedFilePath != null) {
                // Format the content with the new file path
                String contentWithFile = content.trim() + System.lineSeparator() + savedFilePath;
                String formattedContent = formatContentForFile(contentWithFile);

                // Append the formatted content to the original file
                File sourceFile = new File(getRecordsFilePath());
                java.nio.file.Files.write(
                    sourceFile.toPath(),
                    (System.lineSeparator() + formattedContent).getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.APPEND
                );
            }
        } catch (Exception e) {
            System.err.println("Error adding new content: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add new content: " + e.getMessage());
        }
    }

    private String saveTheContent(String content, javafx.stage.Window parentWindow) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Content As");
        
        // Set initial directory to user's Documents folder
        String userHome = System.getProperty("user.home");
        File documentsFolder = new File(userHome + File.separator + "Documents");
        if (!documentsFolder.exists()) {
            documentsFolder = new File(userHome);
        }
        fileChooser.setInitialDirectory(documentsFolder);
        
        // Set file extension filter
        javafx.stage.FileChooser.ExtensionFilter extFilter = 
            new javafx.stage.FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        
        // Show save dialog
        File file = fileChooser.showSaveDialog(parentWindow);
        
        if (file != null) {
            try {
                // Ensure the file has .txt extension
                String filePath = file.getPath();
                if (!filePath.toLowerCase().endsWith(".txt")) {
                    filePath += ".txt";
                    file = new File(filePath);
                }
                
                // Write the original content to the file
                java.nio.file.Files.write(
                    file.toPath(),
                    content.getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
                );
                
                return file.getAbsolutePath();
            } catch (Exception e) {
                System.err.println("Error saving content to file: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to save content to file: " + e.getMessage());
            }
        }
        
        return null;
    }

    private String formatContentForFile(String content) {
        // Split content by newlines and remove empty lines
        String[] lines = content.split("\\R");
        StringBuilder formatted = new StringBuilder();
        
        for (String line : lines) {
            String trimmed = line
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim();
            if (!trimmed.isEmpty()) {
                // If line already contains semicolons, keep it as is
                // Otherwise, treat the whole line as one field
                if (!trimmed.contains(";")) {
                    trimmed = trimmed + ";";
                }
                formatted.append(trimmed);
            }
        }
        
        return formatted.toString();
    }
} 