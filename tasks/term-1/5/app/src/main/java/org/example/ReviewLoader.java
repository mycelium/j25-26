package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ReviewLoader {

    private static final Pattern CSV_QUOTE_PATTERN = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    public static List<MovieReview> fetchAllReviews(String resourceName) throws IOException {
        List<MovieReview> entries = new ArrayList<>();

        try (InputStream stream = ReviewLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + resourceName);
            }

            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                 var buffer = new java.io.BufferedReader(reader)) {

                String header = buffer.readLine();
                String line;
                while ((line = buffer.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String[] columns = CSV_QUOTE_PATTERN.split(line, -1);

                    String rawText = columns[0].replaceAll("^\"|\"$", "").trim();
                    String cleanedText = rawText.replace("<br />", " ");
                    String mood = columns[1].replaceAll("^\"|\"$", "").trim();

                    entries.add(new MovieReview(cleanedText, mood));
                }
            }
        }

        return entries;
    }

    public record MovieReview(String content, String mood) {}
}
