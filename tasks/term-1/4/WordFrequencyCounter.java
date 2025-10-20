import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WordFrequencyCounter {

    /**
     * Вариант 1: чтение всего файла в память и подсчёт слов.
     * Подходит для небольших файлов.
     */
    public Map<String, Integer> countWordsFull(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();
        try {
            String content = Files.readString(filePath);
            String[] words = content.toLowerCase().split("\\W+");

            for (String word : words) {
                if (word.isEmpty()) continue;
                frequencies.merge(word, 1, Integer::sum);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
        return frequencies;
    }

    /**
     * Вариант 2: потоковая обработка (буферное чтение по символам).
     * Подходит для файлов, которые не помещаются в память.
     */
    public Map<String, Integer> countWordsStreaming(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();
        StringBuilder wordBuffer = new StringBuilder();

        try (InputStream inputStream = Files.newInputStream(filePath);
             Reader reader = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(reader, 8192)) {

            int ch;
            while ((ch = br.read()) != -1) {
                char c = Character.toLowerCase((char) ch);
                if (Character.isLetterOrDigit(c)) {
                    wordBuffer.append(c);
                } else {
                    if (wordBuffer.length() > 0) {
                        String word = wordBuffer.toString();
                        frequencies.merge(word, 1, Integer::sum);
                        wordBuffer.setLength(0);
                    }
                }
            }

            // обрабатываем последнее слово, если есть
            if (wordBuffer.length() > 0) {
                String word = wordBuffer.toString();
                frequencies.merge(word, 1, Integer::sum);
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }

        return frequencies;
    }


    public void printFrequencies(Map<String, Integer> frequencies) {
        frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(entry ->
                        System.out.printf("%-15s : %d%n", entry.getKey(), entry.getValue()));
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Использование: java WordFrequencyCounter <путь_к_файлу> <режим>");
            System.out.println("Режимы:");
            System.out.println("  full   — загрузка всего файла");
            System.out.println("  stream — потоковая обработка (по символам)");
            return;
        }

        Path filePath = Paths.get(args[0]);
        String mode = args[1].toLowerCase();

        WordFrequencyCounter counter = new WordFrequencyCounter();
        Map<String, Integer> result = null;

        if (mode.equals("full")) {
            System.out.println("=== Подсчёт (режим: полная загрузка файла) ===");
            result = counter.countWordsFull(filePath);
        } else if (mode.equals("stream")) {
            System.out.println("=== Подсчёт (режим: потоковая обработка) ===");
            result = counter.countWordsStreaming(filePath);
        } else {
            System.out.println("Неизвестный режим: " + mode);
            return;
        }

        System.out.println("\n=== Результаты ===");
        counter.printFrequencies(result);
    }
}
