package sentiment.analysis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ReviewProcessor {

    public List<Review> loadReviewsFromCsv(String filePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Ресурс не найден в classpath: " + filePath);
            }

            List<Review> reviews = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;

                reader.readLine();

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String cleanLine = line;
                    if (cleanLine.startsWith("\"") && cleanLine.endsWith("\"")) {
                        cleanLine = cleanLine.substring(1, cleanLine.length() - 1);
                    }

                    int lastComma = cleanLine.lastIndexOf(',');
                    if (lastComma == -1) {
                        System.err.println("Skipping invalid line: " + line);
                        continue;
                    }

                    String text = cleanLine.substring(0, lastComma).trim();
                    if (text.startsWith("\"") && text.endsWith("\"")) {
                        text = text.substring(1, text.length() - 1);
                    }

                    String actualSentiment = cleanLine.substring(lastComma + 1).trim();
                    if (actualSentiment.startsWith("\"") && actualSentiment.endsWith("\"")) {
                        actualSentiment = actualSentiment.substring(1, actualSentiment.length() - 1);
                    }

                    reviews.add(new Review(text, actualSentiment));
                }
            }
            return reviews;
        }
    }
}
