import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;

public class WordFrequencyCounter {
    public Map<String, Integer> countWords(Path filePath) throws IOException {
        if (Files.size(filePath) > (1024*1024))
        {
            return countWordsLongFile(filePath);
        }
        else {
            return countWordsShortFile(filePath);
        }
    }
    public Map<String, Integer> countWordsLongFile(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();
        try (BufferedReader bReader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = bReader.readLine()) != null) {
                String[] sWords = line.split("[\\s\\p{P}\\p{S}]+");
                for (String word : sWords) {
                    if (word.trim().isEmpty()) {
                        continue;
                    }
                    String lcWord = word.toLowerCase().trim();
                    frequencies.put(lcWord, frequencies.getOrDefault(lcWord, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            return new HashMap<>();
        }
        return frequencies;
    }

    public Map<String, Integer> countWordsShortFile(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] sWords = line.split("[\\s\\p{P}\\p{S}]+");
                for (String word : sWords) {
                    if (word.trim().isEmpty()) {
                        continue;
                    }
                    String lcWord = word.toLowerCase().trim();
                    frequencies.put(lcWord, frequencies.getOrDefault(lcWord, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            return new HashMap<>();
        }
        return frequencies;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
    if (frequencies.isEmpty()) {
        System.out.println("Нет данных.");
        return;
    }
    frequencies.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue()));
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();
        try {
            System.out.println("Подсчет слов в коротком тексте:");
            Map<String, Integer> result = counter.countWords(Path.of("short.txt"));
            counter.printFrequencies(result);
            System.out.println("Подсчет слов в длинном тексте:");
            result = counter.countWords(Path.of("long.txt"));
            counter.printFrequencies(result);
        }
        catch (IOException e)
        {
            System.err.println("Ошибка при чтении файла " + e.getMessage());
        }
    }
}
