import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.util.stream.*;
import java.util.regex.*;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords(Path filePath) {
        return countWordsInMemory(filePath);
    }
    
    //1: загрузка всего содержимого файла в память
    public Map<String, Integer> countWordsInMemory(Path filePath) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        try {
            String content = Files.readString(filePath);
            String[] words = content.toLowerCase()
                                  .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", " ")
                                  .split("\\s+");
            
            processWordsArray(words, frequencyMap);
            
        } catch (IOException e) {
            System.err.println("ошибка чтения файла " + e.getMessage());
        }
        
        return frequencyMap;
    }
    
    //2: потоковая обработка 
    public Map<String, Integer> countWordsStreaming(Path filePath) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            Pattern pattern = Pattern.compile("[a-zA-Zа-яА-Я0-9]+");
            
            while ((line = reader.readLine()) != null) {
                processLine(line.toLowerCase(), pattern, frequencyMap);
            }
            
        } catch (IOException e) {
            System.err.println("ошибка чтения файла " + e.getMessage());
        }
        
        return frequencyMap;
    }

    //общая обработка массива
    private void processWordsArray(String[] words, Map<String, Integer> frequencyMap) {
        for (String word : words) {
            if (!word.isEmpty()) {
                incrementWordCount(word, frequencyMap);
            }
        }
    }
    
    //общая обработка строки
    private void processLine(String line, Pattern pattern, Map<String, Integer> frequencyMap) {
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String word = matcher.group();
            incrementWordCount(word, frequencyMap);
        }
    }
    
    //общее увеличение счетчика
    private void incrementWordCount(String word, Map<String, Integer> frequencyMap) {
        frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        printFrequencies(frequencies, Integer.MAX_VALUE);
    }
    
    //ограничение количества результатов
    public void printFrequencies(Map<String, Integer> frequencies, int limit) {
        if (limit == Integer.MAX_VALUE) {
            frequencies.entrySet()
                      .stream()
                      .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                      .forEach(entry -> 
                          System.out.printf("%-20s : %d%n", entry.getKey(), entry.getValue()));
        } else {
            System.out.println("\nТоп-" + limit + " самых частых слов:");
            frequencies.entrySet()
                      .stream()
                      .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                      .limit(limit)
                      .forEach(entry -> 
                          System.out.printf("%-20s : %d%n", entry.getKey(), entry.getValue()));
        }
    }
    
    //общее измерение времени выполнения
    private Map<String, Integer> countWordsWithTiming(Path filePath, boolean useStreaming) {
        long startTime = System.currentTimeMillis();
        Map<String, Integer> frequencies = useStreaming ? 
            countWordsStreaming(filePath) : countWordsInMemory(filePath);
        long endTime = System.currentTimeMillis();
        System.out.println("Время выполнения: " + (endTime - startTime) + " мс");
        return frequencies;
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();
        
        if (args.length == 0) {
            System.out.println("пожалуйста укажите файл для обработки!");
            return;
        }
        
        Path filePath = Paths.get(args[0]);
        
        if (!Files.exists(filePath)) {
            System.err.println("такого файла нет: " + filePath);
            return;
        }
        
        try {
            long fileSize = Files.size(filePath);
            System.out.println("Размер файла: " + fileSize + " байт");
            
            Map<String, Integer> frequencies;
            
            //метод выбирается в зависиомсти от размера файла (если меньше 20 МБ то первый метод)
            if (fileSize < 20 * 1024 * 1024) { 
                System.out.println("Файл небольшого размера -> используется метод с загрузкой в память");
                frequencies = counter.countWordsWithTiming(filePath, false);
            } else {
                System.out.println("Большой файл -> используется потоковый метод");
                frequencies = counter.countWordsWithTiming(filePath, true);
            }
            
            System.out.println("\nОбщее количество уникальных слов: " + frequencies.size());
            counter.printFrequencies(frequencies, 20); // 20 наиболее часто встречающихся слов
            
        } catch (IOException e) {
            System.err.println("ошибка при работе с файлом: " + e.getMessage());
        }
    }
}
