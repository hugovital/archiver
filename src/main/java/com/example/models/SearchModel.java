package com.example.models;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchModel {
    // Add model properties and methods here
    private String searchTerm;
    private static final String DATABASE_FILE = "/database/records.csv";
    private static final String DELIMITER = ";"; // Changed from comma to semicolon

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
            InputStream is = getClass().getResourceAsStream(DATABASE_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
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
} 