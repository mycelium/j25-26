import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WordFrequencyCounter {
    // --- Option 1: read the entire file into memory ---
    public Map<String, Integer> countWordsFull(Path filePath) {
        Map<String, Integer> wordCount = new HashMap<>();
        try {
            String content = Files.readString(filePath);
            String[] words = content.toLowerCase().split("[^a-zA-Z0-9]+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return wordCount;
    }

    // --- Option 2: stream processing (line by line) ---
    public Map<String, Integer> countWordsStream(Path filePath) {
        Map<String, Integer> wordCount = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase().split("[^a-zA-Z0-9]+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error during streaming read: " + e.getMessage());
        }
        return wordCount;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(entry ->
                        System.out.printf("%-20s %d%n", entry.getKey(), entry.getValue()));
    }

    public static void main(String[] args) {
        Path filePath = Path.of("input.txt"); 
        WordFrequencyCounter counter = new WordFrequencyCounter();

        System.out.println("=== Option 1: Read entire file ===");
        long start1 = System.nanoTime();
        Map<String, Integer> full = counter.countWordsFull(filePath);
        long end1 = System.nanoTime();
        System.out.printf("Time: %.3f seconds%n", (end1 - start1) / 1_000_000_000.0);
        counter.printFrequencies(full);

        System.out.println("\n=== Option 2: Stream processing ===");
        long start2 = System.nanoTime();
        Map<String, Integer> streamed = counter.countWordsStream(filePath);
        long end2 = System.nanoTime();
        System.out.printf("Time: %.3f seconds%n", (end2 - start2) / 1_000_000_000.0);
        counter.printFrequencies(streamed);
    }
}
