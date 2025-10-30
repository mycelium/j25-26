import java.util.*;
import java.nio.file.*;
import java.io.*;

public class WordFrequencyCounter {

    private static final int DEFAULT_WORDS_LIMIT = 20;

    public Map<String, Integer> countWords(Path filePath) {
        Map<String, Integer> wordCount = new HashMap<>();

        try {
            String content = Files.readString(filePath);
            processText(content, wordCount);
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }

        return wordCount;
    }

    public Map<String, Integer> countWordsStream(Path filePath) {
        Map<String, Integer> wordCount = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                processText(line, wordCount);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при потоковом чтении: " + e.getMessage());
        }

        return wordCount;
    }

    private void processText(String text, Map<String, Integer> wordCount) {
        String[] words = text.toLowerCase().split("[^а-яА-Яa-zA-Z0-9]+");

        for (String word : words) {
            if (!word.isEmpty()) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        printFrequencies(frequencies, DEFAULT_WORDS_LIMIT);
    }

    public void printFrequencies(Map<String, Integer> frequencies, int wordsLimit) {
        List<Map.Entry<String, Integer>> sortedEntries = frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(wordsLimit)
                .toList();

        int maxLength = sortedEntries.stream()
                .mapToInt(entry -> entry.getKey().length())
                .max()
                .orElse(0);

        sortedEntries.forEach(entry ->
            System.out.printf("%-" + (maxLength + 5) + "s : %d%n",
                            entry.getKey(), entry.getValue()));
    }

    public static void loadFile(Path filePath) {
        if (!Files.exists(filePath)) {
            System.err.println("Файл " + filePath.toAbsolutePath() + " не найден: ");
            System.err.println("Текущая директория: " + Path.of("").toAbsolutePath());
            return;
        }

        try {
            long fileSize = Files.size(filePath);
            System.out.printf("Загрузка файла %s размером: %d байт!\n", filePath.getFileName(), fileSize);

            WordFrequencyCounter counter = new WordFrequencyCounter();
            Map<String, Integer> frequencies;

            long startTime;
            long endTime;
            if (fileSize < Math.pow(2, 20)) {
                System.out.println("Метод загрузки файла: полная загрузка в память");
                startTime = System.currentTimeMillis();
                frequencies = counter.countWords(filePath);
                endTime = System.currentTimeMillis();
            } else {
                System.out.println("Метод загрузки файла: потоковая обработка");
                startTime = System.currentTimeMillis();
                frequencies = counter.countWordsStream(filePath);
                endTime = System.currentTimeMillis();
            }

            System.out.println("\n=== Результат работы подсчета ===");
            System.out.println("Уникальных слов: " + frequencies.size() +
                    " (выведено " + Math.min(DEFAULT_WORDS_LIMIT, frequencies.size()) + " самых часто-встречаемых слов)");
            counter.printFrequencies(frequencies);

            System.out.printf("\nВремя выполнения: %d мс%n\n", (endTime - startTime));

        } catch (IOException e) {
            System.err.println("Ошибка при работе с файлом: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Path firstFilePath = Path.of(".\\tasks\\term-1\\4\\small_input.txt");
        Path secondFilePath = Path.of(".\\tasks\\term-1\\4\\big_input.txt");

        loadFile(firstFilePath);
        loadFile(secondFilePath);
    }
}