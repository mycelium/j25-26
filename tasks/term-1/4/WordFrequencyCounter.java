import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class WordFrequencyCounter {

    
    //Вариант 1 — загрузка всего содержимого файла в память.
     
    public Map<String, Integer> countWords(Path filePath) {
        Map<String, Integer> wordCount = new HashMap<>();

        try {
            // читаем всё содержимое файла как одну строку
            String content = Files.readString(filePath);
            // разделяем по не-буквенным символам 
            String[] words = content.toLowerCase().split("[^a-zа-яё0-9]+");

            for (String word : words) {
                if (word.isEmpty()) continue;
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }

        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }

        return wordCount;
    }

    
    // Вариант 2 — потоковая (построчная) обработка.
     
    public Map<String, Integer> countWordsStream(Path filePath) {
        Map<String, Integer> wordCount = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase().split("[^a-zа-яё0-9]+");
                for (String word : words) {
                    if (word.isEmpty()) continue;
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }

        return wordCount;
    }

    //печать отсортированных результатов по убыванию частоты.
     
    public void printFrequencies(Map<String, Integer> frequencies) {
        frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(entry -> System.out.printf("%s: %d%n", entry.getKey(), entry.getValue()));
    }

    
    public static void main(String[] args) {
        Path filePath = Path.of("text.txt");
        WordFrequencyCounter counter = new WordFrequencyCounter();

        System.out.println("=== Подсчёт с загрузкой всего файла ===");
        long start1 = System.nanoTime();
        Map<String, Integer> result1 = counter.countWords(filePath);
        long end1 = System.nanoTime();
        System.out.printf("Время: %.3f с%n", (end1 - start1) / 1e9);
        System.out.println("Все слова и их частоты:");
        counter.printFrequencies(result1);


        System.out.println("\n=== Потоковая обработка ===");
        long start2 = System.nanoTime();
        Map<String, Integer> result2 = counter.countWordsStream(filePath);
        long end2 = System.nanoTime();
        System.out.printf("Время: %.3f с%n", (end2 - start2) / 1e9);
        System.out.println("Все слова и их частоты:");
        counter.printFrequencies(result2);
    }
}

