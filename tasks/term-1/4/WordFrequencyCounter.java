import java.util.*;
import java.nio.file.*;
import java.io.*;

public class WordFrequencyCounter {
    private static final long MEMORY_THRESHOLD = 1024 * 1024;

    public Map<String, Integer> countWords(Path filePath) {
        try {
            long fileSize = Files.size(filePath);
            if (fileSize < MEMORY_THRESHOLD) {
                return countWordsInMemory(filePath);
            } else {
                return countWordsStreaming(filePath);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при определении размера файла: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Map<String, Integer> countWordsInMemory(Path filePath) {
        Map<String, Integer> frequencyMap = new HashMap<>();

        try {
            String content = Files.readString(filePath);
            processTextContent(content, frequencyMap);
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }

        return frequencyMap;
    }

    public Map<String, Integer> countWordsStreaming(Path filePath) {
        Map<String, Integer> frequencyMap = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                processTextContent(line, frequencyMap);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при потоковом чтении: " + e.getMessage());
        }

        return frequencyMap;
    }

    private void processTextContent(String text, Map<String, Integer> frequencyMap) {
        String[] words = text.toLowerCase().split("[^a-zA-Zа-яА-Я0-9']+");

        for (String word : words) {
            if (!word.isEmpty() && word.chars().anyMatch(Character::isLetter)) {
                frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
            }
        }
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        printFrequencies(frequencies, Integer.MAX_VALUE);
    }

    public void printFrequencies(Map<String, Integer> frequencies, int limit) {
        System.out.println("================ СТАТИСТИКА СЛОВ ================");

        if (frequencies.isEmpty()) {
            System.out.println("Нет данных для отображения");
            return;
        }

        List<Map.Entry<String, Integer>> sortedEntries = frequencies.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .toList();

        int maxWordLength = sortedEntries.stream()
                .mapToInt(entry -> entry.getKey().length())
                .max()
                .orElse(20);

        System.out.printf("%-" + (maxWordLength + 2) + "s | %s%n", "СЛОВО", "ЧАСТОТА");
        System.out.println("-".repeat(maxWordLength + 3) + "|---------");

        for (Map.Entry<String, Integer> entry : sortedEntries) {
            System.out.printf("%-" + (maxWordLength + 2) + "s | %d%n",
                    entry.getKey(), entry.getValue());
        }

        System.out.println("Всего уникальных слов: " + frequencies.size());
        System.out.println("==================================================");
    }

    // Тестирование обоих методов
    public static void testWithFile(Path filePath) {
        WordFrequencyCounter counter = new WordFrequencyCounter();

        try {
            long fileSize = Files.size(filePath);
            System.out.println("Файл: " + filePath.getFileName());
            System.out.println("Размер: " + fileSize + " байт");
            System.out.println("Метод: " + (fileSize < MEMORY_THRESHOLD ? "ПОЛНАЯ ЗАГРУЗКА" : "ПОТОКОВАЯ ОБРАБОТКА"));
            System.out.println();

            long startTime = System.currentTimeMillis();
            Map<String, Integer> frequencies = counter.countWords(filePath);
            long endTime = System.currentTimeMillis();

            counter.printFrequencies(frequencies, 15);
            System.out.printf("Время выполнения: %d мс%n%n", (endTime - startTime));

        } catch (IOException e) {
            System.err.println("Ошибка при тестировании файла: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();

        Path[] testFiles = {
                Path.of("./tasks/term-1/4/small_text.txt"),
                Path.of("./tasks/term-1/4/big_text.txt")
        };

        for (Path filePath : testFiles) {
            if (Files.exists(filePath)) {
                testWithFile(filePath);
            } else {
                System.out.println("Файл не найден: " + filePath.toAbsolutePath());

                if (filePath.toString().equals("small_text.txt")) {
                    System.out.println("Демонстрация на тестовом тексте:\n");
                    String testText = "one two three four five six seven eight nine ten! " +
                            "one two three.";

                    Map<String, Integer> demoFreq = new HashMap<>();
                    counter.processTextContent(testText, demoFreq);
                    counter.printFrequencies(demoFreq);
                }
            }
        }
    }
}