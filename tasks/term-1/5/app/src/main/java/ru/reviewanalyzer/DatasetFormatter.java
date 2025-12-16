package ru.reviewanalyzer;

import java.util.*;
import java.io.*;

public class DatasetFormatter {
    public List<Review> datasetToReviews(String filePath) {
        List<Review> reviews = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null && reviews.size() != 1000) {
                line = cleanString(line);
                if (line.isEmpty()) continue;

                String[] parts = line.split(",(?=[^,]*$)");
                if (parts.length == 2) {
                    reviews.add(new Review(parts[0], parts[1]));
                }
            }
        } catch (IOException e) { System.err.println("Error: " + e.getMessage()); }


        return reviews;
    }

    private String cleanString(String text) {
        return text.replace("\"", "").replace("<br />", " ").trim();
    }

}


