package wordcounter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.*;

public class WordFrequencyCounter {

    // Метод 1: Загрузка всего файла в память с последующей обработкой
    public Map<String, Integer> countWords(Path filePath) {
        try {
            // Читаем весь файл как строку
            String content = Files.readString(filePath, java.nio.charset.StandardCharsets.UTF_8);
            return processContent(content);
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    // Метод 2: Потоковая обработка для больших файлов
    public Map<String, Integer> countWordsStreaming(Path filePath) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        try (BufferedReader reader = Files.newBufferedReader(filePath, java.nio.charset.StandardCharsets.UTF_8)) {
            String line;
            // Паттерн для разделения слов: не-буквы и не-цифры (кроме апострофов)
            Pattern wordPattern = Pattern.compile("[^\\p{L}0-9']+");
            
            while ((line = reader.readLine()) != null) {
                // Обрабатываем каждую строку отдельно
                String[] words = wordPattern.split(line.toLowerCase());
                
                for (String word : words) {
                    if (!word.isEmpty()) { // УБРАЛ: && word.length() > 1 - теперь считаем однобуквенные слова
                        frequencyMap.merge(word, 1, Integer::sum);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            return Collections.emptyMap();
        }
        
        return frequencyMap;
    }

    // Вспомогательный метод для обработки содержимого (первый подход)
    private Map<String, Integer> processContent(String content) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        // Разделяем по не-буквенным символам (включая русские буквы)
        String[] words = content.toLowerCase().split("[^\\p{L}0-9']+");
        
        for (String word : words) {
            if (!word.isEmpty()) { // УБРАЛ: && word.length() > 1 - теперь считаем однобуквенные слова
                frequencyMap.merge(word, 1, Integer::sum);
            }
        }
        
        return frequencyMap;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        if (frequencies.isEmpty()) {
            System.out.println("Слова не найдены или ошибка чтения файла.");
            return;
        }

        System.out.println("\nЧастота слов:");
        System.out.println("-----------------");
        
        // Сортируем по убыванию частоты, затем по алфавиту
        frequencies.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(entry -> 
                    System.out.printf("%-20s : %d%n", entry.getKey(), entry.getValue()));
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Счетчик частоты слов");
        System.out.println("====================");
        
        // Получаем путь к файлу от пользователя
        System.out.print("Введите путь к текстовому файлу: ");
        String filePath = scanner.nextLine();
        
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            System.err.println("Файл не существует или это не обычный файл.");
            return;
        }
        
        try {
            long fileSize = Files.size(path);
            final long MEMORY_THRESHOLD = 10 * 1024 * 1024; // Порог 10MB
            
            Map<String, Integer> frequencies;
            
            if (fileSize <= MEMORY_THRESHOLD) {
                System.out.println("Используется обработка в памяти (размер файла: " + 
                    String.format("%,d", fileSize) + " байт)");
                frequencies = counter.countWords(path);
            } else {
                System.out.println("Используется потоковая обработка (размер файла: " + 
                    String.format("%,d", fileSize) + " байт - превышает порог)");
                frequencies = counter.countWordsStreaming(path);
            }
            
            counter.printFrequencies(frequencies);
            
            // Показываем статистику
            System.out.println("\nСтатистика:");
            System.out.println("-----------");
            System.out.println("Уникальных слов: " + frequencies.size());
            int totalWords = frequencies.values().stream().mapToInt(Integer::intValue).sum();
            System.out.println("Всего слов: " + totalWords);
            
        } catch (IOException e) {
            System.err.println("Ошибка доступа к файлу: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}