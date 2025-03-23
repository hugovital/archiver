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
            return null; // This line will never be reached due to System.exit(1)
        }
        return filePath;
    }
} 