package counter;

import java.util.*;
import java.nio.file.*;
import java.io.*;

public class WordFrequencyCounter {

    //  Загрузка всего файла 
    public Map<String, Integer> countWordsInMemory(Path filePath) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        try {
            String content = Files.readString(filePath);
            String[] words = content.toLowerCase()
                                  .replaceAll("[^a-zA-Zа-яА-ЯёЁ\\s-]", " ")
                                  .split("\\s+");
            
            for (String word : words) {
                if (!word.isEmpty() && word.length() > 1) {
                    frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }
        
        return frequencyMap;
    }
    
    // Потоковая обработка если файл будет побольше
    public Map<String, Integer> countWordsStreaming(Path filePath) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase()
                                   .replaceAll("[^a-zA-Zа-яА-ЯёЁ\\s-]", " ")
                                   .split("\\s+");
                
                for (String word : words) {
                    if (!word.isEmpty() && word.length() > 1) {
                        frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }
        
        return frequencyMap;
    }

    // это метод, который автоматически выбирает способ обработки
    public Map<String, Integer> countWords(Path filePath) {
        try {
            long fileSize = Files.size(filePath);
            
            if (fileSize < 10 * 1024 * 1024) { // До 10 МБ
                System.out.println("Используется метод с загрузкой в память");
                return countWordsInMemory(filePath);
            } else {
                System.out.println("Файл большой, используется потоковый метод");
                return countWordsStreaming(filePath);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при проверке размера файла: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // Вывод количества слов
    public void printFrequencies(Map<String, Integer> frequencies) {
        if (frequencies.isEmpty()) {
            System.out.println("Нет данных для вывода");
            return;
        }
        
        // Создаю список и сортирую его
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(frequencies.entrySet());
        sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        System.out.println("\nТоп-20 самых частых слов:");
        System.out.println("------------------------");
        
        int limit = Math.min(20, sortedList.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = sortedList.get(i);
            System.out.printf("%-20s : %d%n", entry.getKey(), entry.getValue());
        }
        
        // Общая статистика
        int totalWords = 0;
        for (int count : frequencies.values()) {
            totalWords += count;
        }
        int uniqueWords = frequencies.size();
        
        System.out.println("\nСтатистика:");
        System.out.println("Всего слов: " + totalWords);
        System.out.println("Уникальных слов: " + uniqueWords);
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Подсчет частоты слов в текстовом файле ===");
        
        while (true) {
            System.out.print("\nВведите путь к файлу (или 'exit' для выхода): ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            
            if (input.isEmpty()) {
                System.out.println("Путь не может быть пустым!");
                continue;
            }
            
            Path filePath = Paths.get(input);
            
            if (!Files.exists(filePath)) {
                System.err.println("Файл не найден: " + filePath.toAbsolutePath());
                continue;
            }
            
            if (!Files.isRegularFile(filePath)) {
                System.err.println("Это не файл: " + filePath.toAbsolutePath());
                continue;
            }
            
            try {
                long startTime = System.currentTimeMillis();
                Map<String, Integer> frequencies = counter.countWords(filePath);
                long endTime = System.currentTimeMillis();
                
                System.out.printf("Обработка заняла: %.2f секунд%n", (endTime - startTime) / 1000.0);
                
                counter.printFrequencies(frequencies);
                
            } catch (Exception e) {
                System.err.println("Ошибка при обработке файла: " + e.getMessage());
            }
        }
        
        scanner.close();
        System.out.println("Программа завершена.");
    }
}
