package com.example.models;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public List<String> searchRecords() {
        List<String> results = new ArrayList<>();
        
        try {
            InputStream is = getClass().getResourceAsStream(DATABASE_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(searchTerm.toLowerCase())) {
                    // Format the line nicely - now handling any number of fields
                    String[] parts = line.split(DELIMITER);
                    StringBuilder formattedResult = new StringBuilder();
                    
                    // Add each field with its label if available
                    for (int i = 0; i < parts.length; i++) {
                        String value = parts[i].trim();
                        
                        switch (i) {
                            case 0:
                                formattedResult.append("ID: ").append(value);
                                break;
                            case 1:
                                formattedResult.append(" | Name: ").append(value);
                                break;
                            case 2:
                                formattedResult.append(" | Position: ").append(value);
                                break;
                            case 3:
                                formattedResult.append(" | Location: ").append(value);
                                break;
                            case 4:
                                formattedResult.append(" | Salary: $").append(value);
                                break;
                            default:
                                formattedResult.append(" | Field").append(i + 1).append(": ").append(value);
                        }
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