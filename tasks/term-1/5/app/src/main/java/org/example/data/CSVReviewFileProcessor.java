package org.example.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSVReviewFileProcessor {

    private static String[] parseCSVLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    public static List<Review> readReviews(String filePath) throws IOException {
        List<Review> reviews = new ArrayList<>();
        try (InputStream is = CSVReviewFileProcessor.class.getClassLoader().getResourceAsStream(filePath)){
            if (is == null) {
                throw new IOException("File not found in resources: " + filePath);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line = br.readLine();
                if (line == null) {
                    throw new IOException("CSV file is empty or missing header.");
                }
                while ((line = br.readLine()) != null) {
                    String[] parts = parseCSVLine(line);
                    if (parts.length >= 2) {
                        reviews.add(new Review(parts[0].trim(), parts[1].trim()));
                    }
                    else {
                        System.err.println("Skipping invalid line: " + line);
                    }
                }
            }
        }

        return reviews;
    }

}
