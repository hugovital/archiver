package com.example.utils;

import java.util.Base64;
import java.io.FileOutputStream;
import java.io.File;

public class IconGenerator {
    public static void main(String[] args) {
        // Professional folder icon in base64
        String base64Icon = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAAdgAAAHYBTnsmCAAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAADdSURBVDiNY2AYBfQHjMQqrK6u/s/AwMDAxMDAwMDLy8tEwACsmpkYGBgYWltbGXh5eZlwKXJxcWHA5QImPJphgBGnAQQ0/xflFsVuADGaYYAFn2ZcmjEMIFYzDDAjO5tUzSgGEKsZBvAGJBINgwdgGkCKZhQDSNEMAhUVFQPHAGI1wwDeaCSkGcUAQppBgImBCM0oLsBnCCMDA8N/BgYGBkackcnAwMXFBddVWVn5n4WFhYGBgYGBi4uLgYeHh4GTk5OBjY2NgZ2dnYGDg4OBk5OTgY2NjYGVlZWBmZmZYRACAKx2K7kZX69/AAAAAElFTkSuQmCC";

        try {
            // Create directories if they don't exist
            new File("src/main/resources/images").mkdirs();
            
            // Decode base64 to bytes
            byte[] iconBytes = Base64.getDecoder().decode(base64Icon);
            
            // Write to file
            try (FileOutputStream fos = new FileOutputStream("src/main/resources/images/folder-icon.png")) {
                fos.write(iconBytes);
            }
            
            System.out.println("Folder icon created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 