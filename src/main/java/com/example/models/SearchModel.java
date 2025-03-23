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

    public boolean addNewContent(String content, javafx.stage.Window parentWindow) {
        try {
            // First create backup
            backupRecordsFile(content);

            // Get destination for new file first
            String savedFilePath = saveTheContent(content, parentWindow);
            if (savedFilePath != null) {
                File destinationFile = new File(savedFilePath);
                File destinationFolder = destinationFile.getParentFile();
                
                // Process content lines and copy files
                String[] lines = content.split("\\R");
                StringBuilder processedContent = new StringBuilder();
                
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty()) {
                        try {
                            File potentialFile = new File(trimmedLine);
                            if (potentialFile.exists() && potentialFile.isFile()) {
                                // Copy file to destination folder
                                String newFileName = potentialFile.getName();
                                File copiedFile = new File(destinationFolder, newFileName);
                                
                                // Handle file name conflicts
                                int counter = 1;
                                while (copiedFile.exists()) {
                                    String nameWithoutExt = newFileName.substring(0, newFileName.lastIndexOf('.'));
                                    String extension = newFileName.substring(newFileName.lastIndexOf('.'));
                                    copiedFile = new File(destinationFolder, nameWithoutExt + "_" + counter + extension);
                                    counter++;
                                }
                                
                                // Copy the file
                                java.nio.file.Files.copy(
                                    potentialFile.toPath(),
                                    copiedFile.toPath(),
                                    java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
                                );
                                
                                // Add the new file path to content
                                processedContent.append(copiedFile.getAbsolutePath()).append(System.lineSeparator());
                            } else {
                                // Not a file, keep original line
                                processedContent.append(trimmedLine).append(System.lineSeparator());
                            }
                        } catch (Exception e) {
                            // If there's any error processing the line, keep original
                            processedContent.append(trimmedLine).append(System.lineSeparator());
                        }
                    }
                }
                
                // Add the new file path to the processed content
                processedContent.append(savedFilePath);
                
                // Write the processed content to the new file
                java.nio.file.Files.write(
                    destinationFile.toPath(),
                    processedContent.toString().getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
                );
                
                // Format and append to the records file
                String formattedContent = formatContentForFile(processedContent.toString());
                File sourceFile = new File(getRecordsFilePath());
                java.nio.file.Files.write(
                    sourceFile.toPath(),
                    (System.lineSeparator() + formattedContent).getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.APPEND
                );
                
                return true; // Return true if save was successful
            }
            
            return false; // Return false if user cancelled
            
        } catch (Exception e) {
            System.err.println("Error adding new content: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add new content: " + e.getMessage());
        }
    }

    private String saveTheContent(String content, javafx.stage.Window parentWindow) {

        // Validate directory exists
        String initialDirectory = "C:\\Users\\Hugo\\Desktop\\Banco Itaú SA";
        File saveDir = new File(initialDirectory);
        if (!saveDir.exists() || !saveDir.isDirectory()) {
            throw new RuntimeException("Directory specified in INITIAL_SAVE_DIRECTORY does not exist: " + initialDirectory);
        }
        
        // Generate suggested filename from content
        String suggestedName = generateSuggestedFilename(content);
        
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Content As");
        
        // Set initial directory from environment variable
        fileChooser.setInitialDirectory(saveDir);
        
        // Set initial filename
        fileChooser.setInitialFileName(suggestedName);
        
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

    private String generateSuggestedFilename(String content) {
        java.util.Set<String> uniqueWords = new java.util.LinkedHashSet<>(); // LinkedHashSet to maintain order
        String[] lines = content.split("\\R");
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            // Skip if line is a file path
            if (new File(trimmedLine).exists()) {
                continue;
            }
            
            // Split line into words and add to set
            String[] words = trimmedLine.split("\\s+");
            for (String word : words) {
                // Clean the word and add if valid
                String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                if (!cleanWord.isEmpty()) {
                    uniqueWords.add(cleanWord);
                    // Break if we have 10 words
                    if (uniqueWords.size() >= 10) {
                        break;
                    }
                }
            }
            // Break if we have 10 words
            if (uniqueWords.size() >= 10) {
                break;
            }
        }
        
        // Join words with underscore
        StringBuilder filename = new StringBuilder();
        int count = 0;
        for (String word : uniqueWords) {
            if (count > 0) {
                filename.append("_");
            }
            filename.append(word);
            count++;
            if (count >= 10) {
                break;
            }
        }
        
        // If no words were found, use a default name
        if (filename.length() == 0) {
            filename.append("new_content");
        }
        
        // Add extension
        filename.append(".txt");
        
        return filename.toString();
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