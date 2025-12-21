package ru.reviewanalyzer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;

public class DatasetFormatter {
    public List<Review> datasetToReviews(String filePath) {
        List<Review> reviews = new ArrayList<>();

        Path projectPath = Paths.get("").toAbsolutePath();
        Path fullPath = projectPath.resolve(filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(fullPath.toFile()))) {
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


