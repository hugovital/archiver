package Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class CsvGenerator {
    private static final String[] RANDOM_WORDS = {
        "apple", "banana", "orange", "grape", "mango",
        "car", "bike", "train", "plane", "boat",
        "book", "pen", "pencil", "paper", "desk"
    };

    public static void main(String[] args) {
        String csvFile = "src/main/resources/records.csv";
        Random random = new Random();

        try (FileWriter writer = new FileWriter(csvFile, true)) {
            for (int i = 0; i < 100; i++) {
                // Generate a random line with "hugo" and 3 random words
                StringBuilder line = new StringBuilder();
                line.append("hugo,");
                
                // Add 3 random words
                for (int j = 0; j < 3; j++) {
                    line.append(RANDOM_WORDS[random.nextInt(RANDOM_WORDS.length)]);
                    if (j < 2) {
                        line.append(",");
                    }
                }
                
                // Add newline
                line.append("\n");
                
                // Write the line to the file
                writer.write(line.toString());
            }
            System.out.println("Successfully added 100 new lines to records.csv");
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }
} 