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

    public void backupRecordsFile() {
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
                now.getNano() / 1_000_000 // Convert nanos to millis
            );
            
            // Create backup filename
            String originalFileName = sourceFile.getName();
            String fileNameWithoutExt = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
            String backupFileName = String.format("%s_backup_%s.back", fileNameWithoutExt, timestamp);
            
            // Create backup file path
            File backupFile = new File(backupFolder, backupFileName);
            
            // Copy the file using Java NIO for better performance
            java.nio.file.Files.copy(
                sourceFile.toPath(),
                backupFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            
        } catch (Exception e) {
            System.err.println("Error creating backup: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 