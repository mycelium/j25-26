package org.example;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.example.Analizer;

public class Main {
    public static void main(String[] args){
        Map<String, String> reviewsMap = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get("IMDB Dataset.csv"))) {
            String line;
            Pattern pattern = Pattern.compile("^\"(.*)\",(positive|negative|neutral)$");
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    String rawReview = matcher.group(1);
                    String cleanReview = rawReview.replaceAll("<br\\s*/?>", " ")
                            .replaceAll("<[^>]+>", " ")
                            .replaceAll("\\s+", " ")
                            .trim();
                    String sentiment = matcher.group(2);
                    reviewsMap.put(cleanReview, sentiment);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        Analizer analyzer = new Analizer();
        List<Map.Entry<String, String>> randomReviews = new ArrayList<>(reviewsMap.entrySet());
        Collections.shuffle(randomReviews);

        int count = Math.min(10, randomReviews.size());
        int countCorrect=0;
        for (int i = 0; i < count; i++) {
            Map.Entry<String, String> entry = randomReviews.get(i);
            String review = entry.getKey();
            String actualSentiment = entry.getValue();
            String predictedSentiment = analyzer.analyzeSentiment(review);
            if(actualSentiment.equals(predictedSentiment)){
                countCorrect+=1;
            }
            System.out.println("Actual: " + actualSentiment + ", Predicted: " + predictedSentiment);
            System.out.println("Review: " + (review.length() > 100 ? review.substring(0, 100) + "..." : review));
            System.out.println("----------------------------------------------------");

        }
        System.out.println("Total analyzed: "+count+". Correct analyzed: "+countCorrect+".");
    }
}
