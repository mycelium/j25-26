import java.util.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordFrequencyCounter {
	private static final long SMALL_FILE_SIZE_LIMIT = 1024 * 1024;
    private static final Pattern WORD_DELIMITER = Pattern.compile("[^a-zA-Zа-яА-Я0-9]+");
    public Map<String, Integer> countWordsWholeFile(Path filePath) throws IOException {
        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        return processText(content);
    }
    
    public Map<String, Integer> countWordsStreaming(Path filePath) throws IOException {
        Map<String, Integer> wordFrequencies = new HashMap<>();
        try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {
            lines.forEach(line -> {
                String[] words = WORD_DELIMITER.split(line.toLowerCase(Locale.ROOT));
                for (String word : words) {
                    if (!word.isEmpty()) {
                        wordFrequencies.merge(word, 1, Integer::sum);
                    }
                }
            });
        }
        return wordFrequencies;
    }
    
    private Map<String, Integer> processText(String content) {
        String lowerCaseContent = content.toLowerCase(Locale.ROOT);
        String[] words = WORD_DELIMITER.split(lowerCaseContent);
        return Arrays.stream(words)
            .filter(word -> !word.isEmpty()) 
            .collect(Collectors.toMap(
                Function.identity(), 
                word -> 1,           
                Integer::sum,        
                HashMap::new         
            ));
    }
    
    public Map<String, Integer> countWordsAutoSelect(Path filePath) throws IOException {
        long fileSize = Files.size(filePath);
        if (fileSize <= SMALL_FILE_SIZE_LIMIT) {
            System.out.printf("файл маленький (%.2f КБ). использование метода 'чтение всего файла'.\n", (double) fileSize / 1024);
            return countWordsWholeFile(filePath);
        } else {
            System.out.printf("файл большой (%.2f МБ). использование метода 'потоковая обработка'.\n", (double) fileSize / (1024 * 1024));
            return countWordsStreaming(filePath);
        }
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        if (frequencies == null || frequencies.isEmpty()) {
            System.out.println("нет слов для подсчета.");
            return;
        }
        System.out.println("\n--- результат подсчета частоты слов ---");
        frequencies.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey()))
            .forEach(entry ->
                System.out.printf("%s: %d\n", entry.getKey(), entry.getValue())
            );
        System.out.println("---");
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();
        Path filePath = Path.of("test_text.txt"); 
        if (!Files.exists(filePath)) {
             System.err.println("ошибка! не удалось найти файл. ");
             System.err.println("создайте файл для тестирования.");
             return;
        }
        try {
            Map<String, Integer> finalFrequencies = counter.countWordsAutoSelect(filePath);
            counter.printFrequencies(finalFrequencies);  
        } catch (IOException e) {
            System.err.println("произошла ошибка при работе с файлом: " + e.getMessage());
        }
    }
}
