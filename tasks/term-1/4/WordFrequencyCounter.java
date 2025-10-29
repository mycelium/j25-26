import java.util.*;
import java.nio.file.*;
import java.io.*;

public class WordFrequencyCounter {

    // токенизация слов
    private String[] tokenizeText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", " ")
                .split("\\s+");
    }

    // подсчет частоты слов
    private void countWordsInArray(String[] words, Map<String, Integer> frequencies) {
        for (String word : words) {
            if (!word.isEmpty()) {
                frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
            }
        }
    }
    
    // загрузка всего файла в память
    private Map<String, Integer> countWordsInMemory(Path filePath) throws IOException {
        Map<String, Integer> frequencies = new HashMap<>();
        String content = new String(Files.readAllBytes(filePath));
        String[] words = tokenizeText(content);
        countWordsInArray(words, frequencies);
        return frequencies;
    }

    // потоковая обработка для больших файлов
    private Map<String, Integer> countWordsStreaming(Path filePath) throws IOException {
        Map<String, Integer> frequencies = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = tokenizeText(line);
                countWordsInArray(words, frequencies);
            }
        }

        return frequencies;
    }

    public Map<String, Integer> countWords(Path filePath) {
        try {
            // выбираем метод в зависимости от размера файла
            long fileSize = Files.size(filePath);
            if (fileSize < 1024 * 1024) { // если файл меньше 1MB
                System.out.println("Используется метод: загрузка всего файла в память");
                return countWordsInMemory(filePath);
            } else {
                System.out.println("Используется метод: потоковая обработка");
                return countWordsStreaming(filePath);
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        System.out.println("\nВсе слова (отсортированные по частоте):");
        System.out.println("======================================");

        frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry ->
                        System.out.printf("%-20s : %d%n", entry.getKey(), entry.getValue())
                );
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();

        System.out.println("=== Анализ короткого текста ===");
        Path pathShort = Path.of("tasks/term-1/4/short_text.txt");
        Map<String, Integer> shortResult = counter.countWords(pathShort);
        counter.printFrequencies(shortResult);

        System.out.println("\n=== Анализ длинного текста ===");
        Path pathLong = Path.of("tasks/term-1/4/long_text.txt");
        Map<String, Integer> longResult = counter.countWords(pathLong);
        counter.printFrequencies(longResult);
    }
}
