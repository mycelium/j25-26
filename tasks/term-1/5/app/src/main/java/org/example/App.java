package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class App {
    public static void main(String[] args) {
        String targetPath = "tasks/term-1/5/test.txt";


        Path testFile = findFile(targetPath);

        if (testFile == null) {
            System.err.println("CRITICAL ERROR: Could not find " + targetPath);
            System.err.println("Current dir: " + System.getProperty("user.dir"));
            return;
        }

  
        Path resultFile = testFile.getParent().resolve("results.txt");

        System.out.println("Found Input:  " + testFile.toAbsolutePath());
        System.out.println("Set Output:   " + resultFile.toAbsolutePath());


        if (!Files.exists(testFile)) {
            System.err.println("Not found test.txt at directory!");
            return;
        }

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        List<String> lines;
        try {
            lines = Files.readAllLines(testFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("error reading file: " + e.getMessage());
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(resultFile, StandardCharsets.UTF_8)) {
            writer.write("id\tsentiment\treview");
            writer.newLine();

            int id = 1;
            for (String review : lines) {
                if (review == null) continue;
                review = review.trim();
                if (review.isEmpty()) continue;

                String sentiment = predictSentiment(pipeline, review);
                writer.write(id + "\t" + sentiment + "\t" + review);
                writer.newLine();

                System.out.println("[" + sentiment + "] " + review);
                id++;
            }

            System.out.println("end, results:: " + resultFile.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error to rite result: " + e.getMessage());
        }
    }

    private static Path findFile(String relativePath) {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();


        int steps = 0;
        while (current != null && steps < 10) {
            Path candidate = current.resolve(relativePath);
            if (Files.exists(candidate)) {
                return candidate;
            }

            if (relativePath.contains("tasks/term-1/")) {
                Path shorter = current.resolve("5/test.txt");
                if (Files.exists(shorter)) return shorter;
            }

            current = current.getParent();
            steps++;
        }
        return null;
    }
    private static String predictSentiment(StanfordCoreNLP pipeline, String text) {
        if (text == null || text.trim().isEmpty()) return "neutral";

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences == null || sentences.isEmpty()) return "neutral";

        int sum = 0, count = 0;
        for (CoreMap sentence : sentences) {
            String sentClass = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            int score = mapSentimentToScore(sentClass);
            sum += score;
            count++;
        }
        int avg = (count == 0) ? 2 : Math.round((float) sum / count);
        return mapScoreToLabel(avg);
    }

    private static int mapSentimentToScore(String sentiment) {
        if (sentiment == null) return 2;
        String s = sentiment.toLowerCase();
        switch (s) {
            case "very negative":
            case "verynegative":
                return 0;
            case "negative":
                return 1;
            case "neutral":
                return 2;
            case "positive":
                return 3;
            case "very positive":
            case "verypositive":
                return 4;
            default:
                return 2;
        }
    }

    private static String mapScoreToLabel(int score) {
        if (score <= 1) return "negative";
        if (score == 2) return "neutral";
        return "positive";
    }
}
