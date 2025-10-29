import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords(Path filePath) {
        // read file, tokenize words, update map
        try {
            long fileSize = Files.size(filePath);
            boolean useStreaming = fileSize > 55000; // порог для теста

            System.out.println("Размер файла: " + fileSize + " байт");
            System.out.println("Используется метод: " + (useStreaming ? "потоковый" : "загрузка в память"));
            
            if (useStreaming) {
                return countWordsStreaming(filePath);
            } else {
                return countWordsInMemory(filePath);
            }
            
        } catch (IOException e) {
            System.out.println("Ошибка определения размера файла: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Integer> countWordsInMemory(Path filePath) {
        try {
            String content = new String(Files.readAllBytes(filePath));
            return processText(content);
            
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла: " + e.getMessage(), e);
        }
    }

    private Map<String, Integer> countWordsStreaming(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                Map<String, Integer> lineFrequencies = processText(line);
                mergeFrequencies(frequencies, lineFrequencies);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла: " + e.getMessage(), e);
        }
        
        return frequencies;
    }

    private Map<String, Integer> processText(String text) {
        String[] words = text.toLowerCase()
                          .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", " ")
                          .split("\\s+");
        
        Map<String, Integer> frequencies = new HashMap<>();
        for (String word : words) {
            if (!word.isEmpty()) {
                frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
            }
        }
        return frequencies;
    }

    private void mergeFrequencies(Map<String, Integer> target, Map<String, Integer> source) {
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            target.put(entry.getKey(), target.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        // print word counts
        frequencies.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(20)
            .forEach(entry -> 
                System.out.println(entry.getKey() + ": " + entry.getValue())
            );
        
        int totalWords = frequencies.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("\nВсего слов: " + totalWords);
        System.out.println("Уникальных слов: " + frequencies.size());
        frequencies.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .ifPresent(entry -> 
                System.out.println("Самое частое: '" + entry.getKey() + "' (" + entry.getValue() + " раз)")
            );
    }

    public static void main(String[] args) {
        // run word frequency counter
        WordFrequencyCounter counter = new WordFrequencyCounter();
        
        String[] testFiles = {"test_short.txt", "test_long.txt"};
    
        for (String filename : testFiles) {
            Path filePath = Paths.get(filename);
        
            if (!Files.exists(filePath)) {
                System.out.println("Файл " + filename + " не найден, пропускаем");
                continue;
            }
        
            System.out.println("\n=== Обработка файла: " + filename + " ===");
        
            try {
                long startTime = System.currentTimeMillis();

                Map<String, Integer> frequencies = counter.countWords(filePath);
            
                long endTime = System.currentTimeMillis();
            
                counter.printFrequencies(frequencies);
                System.out.println("\nВремя: " + (endTime - startTime) + " мс");
            
            } catch (Exception e) {
                System.out.println("Ошибка при обработке файла " + filename + ": " + e.getMessage());
            }
        }
    }
}
