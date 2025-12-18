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
        String fileName = "test.txt";

        // 1. Ищем файл во всех возможных местах
        Path testFile = findFileInPossibleLocations(fileName);

        if (testFile == null) {
            System.err.println("CRITICAL ERROR: Could not find '" + fileName + "'");
            System.err.println("Searched in current dir: " + System.getProperty("user.dir"));
            System.err.println("Searched in 'app' subdirectory.");
            System.err.println("Searched in parent directories.");
            return;
        }

        // Результаты сохраняем рядом с найденным файлом
        Path resultFile = testFile.getParent().resolve("results.txt");

        System.out.println("--------------------------------------------------");
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        System.out.println("Input Found:       " + testFile.toAbsolutePath());
        System.out.println("Output Set To:     " + resultFile.toAbsolutePath());
        System.out.println("--------------------------------------------------");

        // --- Инициализация CoreNLP ---
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,sentiment");

        props.setProperty("quiet", "true");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        List<String> lines;
        try {
            lines = Files.readAllLines(testFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
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

                // Вывод в консоль для контроля
                System.out.println(String.format("[%d] %-10s %s...", id, sentiment,
                        review.length() > 50 ? review.substring(0, 50) : review));
                id++;
            }
            System.out.println("--------------------------------------------------");
            System.out.println("Done! Results saved to: " + resultFile.getFileName());
        } catch (IOException e) {
            System.err.println("Error writing result: " + e.getMessage());
        }
    }


    private static Path findFileInPossibleLocations(String fileName) {
        Path currentDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

        // Вариант 1: Файл прямо в текущей папке (стандартный случай)
        Path candidate = currentDir.resolve(fileName);
        if (Files.exists(candidate)) return candidate;

        // Вариант 2: Файл внутри папки 'app' (ваш случай сейчас)
        candidate = currentDir.resolve("app").resolve(fileName);
        if (Files.exists(candidate)) return candidate;

        // Вариант 3: Поднимаемся вверх (если запущено из вложенной папки)
        // Ищем в родительской папке и "родительской родителя"
        Path parent = currentDir.getParent();
        for (int i = 0; i < 3; i++) {
            if (parent == null) break;

            // Проверяем в самом родителе
            candidate = parent.resolve(fileName);
            if (Files.exists(candidate)) return candidate;

            // Проверяем в родителе/app/test.txt (на всякий случай)
            candidate = parent.resolve("app").resolve(fileName);
            if (Files.exists(candidate)) return candidate;

            parent = parent.getParent();
        }

        return null;
    }

    // --- Методы NLP (остаются без изменений) ---
    private static String predictSentiment(StanfordCoreNLP pipeline, String text) {
        if (text == null || text.trim().isEmpty()) return "neutral";
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences == null || sentences.isEmpty()) return "neutral";

        int sum = 0, count = 0;
        for (CoreMap sentence : sentences) {
            String sentClass = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            sum += mapSentimentToScore(sentClass);
            count++;
        }
        int avg = (count == 0) ? 2 : Math.round((float) sum / count);
        return mapScoreToLabel(avg);
    }

    private static int mapSentimentToScore(String sentiment) {
        if (sentiment == null) return 2;
        switch (sentiment.toLowerCase()) {
            case "very negative": case "verynegative": return 0;
            case "negative": return 1;
            case "neutral": return 2;
            case "positive": return 3;
            case "very positive": case "verypositive": return 4;
            default: return 2;
        }
    }

    private static String mapScoreToLabel(int score) {
        if (score <= 1) return "negative";
        if (score == 2) return "neutral";
        return "positive";
    }
}