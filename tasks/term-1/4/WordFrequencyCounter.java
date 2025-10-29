import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.nio.file.Path;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords(Path filePath) {
        try {
            String content = new String(Files.readAllBytes(filePath));
            Map<String, Integer> frequencies = new HashMap<>();
            processText(content, frequencies);
            return frequencies;

        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            return new HashMap<>();
        }
    }


    private void processText(String text, Map<String, Integer> frequencies) {
        String[] words = text.toLowerCase()
                .replaceAll("[^a-zA-Zа-яА-Я\\s]", " ")  // удалил 0-9 если не нужно числа
                .split("\\s+");

        for (String word : words) {
            if (!word.isEmpty()) {
                if (frequencies.containsKey(word)) {
                    frequencies.put(word, frequencies.get(word) + 1);
                } else {
                    frequencies.put(word, 1);
                }            }
        }
    }

    public Map<String, Integer> countWordsStreaming(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                processText(line, frequencies);
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }

        return frequencies;
    }



    public void printFrequencies(Map<String, Integer> frequencies) {
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();

        Path filePath = Paths.get("D:/Another/Java projects/j25-26/tasks/term-1/4/book.txt");

        if (!Files.exists(filePath)) {
            System.out.println("Файл не найден: " + filePath);
            return;
        }

        System.out.println("Загрузка всего файла:");
        Map<String, Integer> result1 = counter.countWords(filePath);
        counter.printFrequencies(result1);

        System.out.println("\nПотоковая обработка:");
        Map<String, Integer> result2 = counter.countWordsStreaming(filePath);
        counter.printFrequencies(result2);
    }
}
