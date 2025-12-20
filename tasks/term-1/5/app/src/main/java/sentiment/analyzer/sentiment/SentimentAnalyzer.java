package sentiment.analyzer.sentiment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalyzer {

    private final StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        props.setProperty("tokenize.language", "en");
        this.pipeline = new StanfordCoreNLP(props);
    }

    
    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences =
                annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }

        int positive = 0;
        int negative = 0;
        int neutral = 0;

        for (CoreMap sentence : sentences) {
            String sentiment =
                    sentence.get(SentimentCoreAnnotations.SentimentClass.class);

            String simplified = simplifySentiment(sentiment);

            switch (simplified) {
                case "positive" -> positive++;
                case "negative" -> negative++;
                default -> neutral++;
            }
        }

        if (positive > negative && positive > neutral) {
            return "positive";
        } else if (negative > positive && negative > neutral) {
            return "negative";
        } else {
            return "neutral";
        }
    }

    /**
     * Приведение 5 классов Stanford CoreNLP
     * к 3 итоговым классам
     */
    private String simplifySentiment(String sentiment) {
        if (sentiment == null) return "neutral";

        String lower = sentiment.toLowerCase();
        if (lower.contains("positive")) {
            return "positive";
        } else if (lower.contains("negative")) {
            return "negative";
        } else {
            return "neutral";
        }
    }

    public void analyzeIMDBDataset(String filePath, int maxReviews) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            System.out.println("====================================================================");
            System.out.println("STANFORD CORENLP SENTIMENT ANALYSIS - IMDB DATASET");
            System.out.println("====================================================================");
            System.out.println("Dataset: " + new File(filePath).getName());
            System.out.println("Total records in file: " + (lines.size() - 1));
            System.out.println("Analyzing: " + Math.min(maxReviews, lines.size() - 1) + " reviews");
            System.out.println("Categories: positive, negative, neutral");
            System.out.println("====================================================================\n");

            int positiveCount = 0;
            int neutralCount = 0;
            int negativeCount = 0;
            int matches = 0;
            int totalAnalyzed = 0;

            for (int i = 1; i < lines.size() && totalAnalyzed < maxReviews; i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                int lastComma = line.lastIndexOf(",");
                if (lastComma == -1) continue;

                String review = line.substring(0, lastComma)
                        .replace("\"", "")
                        .trim();

                String actualSentiment = line.substring(lastComma + 1)
                        .replace("\"", "")
                        .trim();

                if (review.isEmpty()) continue;

                String predicted = analyzeSentiment(review);

                String simplifiedActual = simplifySentiment(actualSentiment);
                boolean match = simplifiedActual.equals(predicted);

                if (match) matches++;

                System.out.printf("REVIEW %d:%n", totalAnalyzed + 1);
                System.out.printf("  Text: %.100s%s%n",
                        review, review.length() > 100 ? "..." : "");
                System.out.printf("  Actual: %-8s | Predicted: %-8s %s%n",
                        simplifiedActual, predicted,
                        match ? "✓ MATCH" : "✗ DIFFERENT");
                System.out.println();

                switch (predicted) {
                    case "positive" -> positiveCount++;
                    case "negative" -> negativeCount++;
                    default -> neutralCount++;
                }

                totalAnalyzed++;
            }

            System.out.println("====================================================================");
            System.out.println("ANALYSIS SUMMARY");
            System.out.println("====================================================================");
            System.out.printf("Positive: %2d reviews%n", positiveCount);
            System.out.printf("Neutral:  %2d reviews%n", neutralCount);
            System.out.printf("Negative: %2d reviews%n", negativeCount);
            System.out.println("----------------------------------------");
            System.out.printf("Total Analyzed: %d reviews%n", totalAnalyzed);
            System.out.printf("Accuracy: %.1f%% (%d/%d)%n",
                    (float) matches / totalAnalyzed * 100,
                    matches, totalAnalyzed);
            System.out.println("====================================================================");

        } catch (IOException e) {
            System.err.println("Error reading dataset: " + e.getMessage());
            System.err.println("Current directory: " + System.getProperty("user.dir"));
        }
    }

    public static void main(String[] args) {
        System.out.println("Initializing Stanford CoreNLP...");
        SentimentAnalyzer analyzer = new SentimentAnalyzer();

        String filePath;
        int maxReviews = 100;

        if (args.length > 0) {
            filePath = args[0];
            if (args.length > 1) {
                try {
                    maxReviews = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number for max reviews, using default: 100");
                }
            }
        } else {
            filePath = "app/imdb_small.csv";
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Path projectRoot = Paths.get(System.getProperty("user.dir")).getParent();
            path = projectRoot.resolve(filePath);

            if (!Files.exists(path)) {
                System.err.println("Error: Dataset file not found: " + filePath);
                System.err.println("Searched in: " + projectRoot);
                return;
            }
            filePath = path.toString();
        }

        System.out.println("Using dataset: " + filePath);
        analyzer.analyzeIMDBDataset(filePath, maxReviews);
    }
}
