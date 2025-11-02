import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WordFrequencyCounter {

    private static final String TEST_FILE_NAME = "sample_text1.txt";

  
    public void createTestFile() {
        Path filePath = Paths.get(TEST_FILE_NAME);
        String content = "«На свете существует множество ума, но все они, как правило, сводятся к одному и тому же.\r\n"
        		+ "            Каждый хочет понять, что есть правда. Но что такое правда?\r\n"
        		+ "            Разные люди могут ответить на этот вопрос по-разному.\r\n"
        		+ "            Я, например, не праздный наблюдатель. Я стремлюсь понять, что происходит около меня,\r\n"
        		+ "            что движет людьми и какие побудительные мотивы делают их счастливыми или несчастными». 4545 ";
                
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("Ошибка при создании файла: " + e.getMessage());
        }
    }

   
    public Map<String, Integer> countWords(Path filePath) {
        Map<String, Integer> wordCounts = new HashMap<>();

        try {
            long fileSize = Files.size(filePath);
            final long MEMORY_LIMIT = 10 * 1024 * 1024; 

            if (fileSize > MEMORY_LIMIT) {
                System.out.println("Файл слишком большой, используем потоковую обработку.");
                countWordsStream(filePath, wordCounts);
            } else {
                System.out.println("Файл подходит для полной загрузки, подсчитываем слова.");
                String content = Files.readString(filePath);
                String[] words = content.toLowerCase().split("[^\\p{L}\\p{N}]+");

                for (String word : words) {
                    if (!word.isEmpty()) {
                        wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }

        return wordCounts;
    }
    
    
    private void countWordsStream(Path filePath, Map<String, Integer> wordCounts) {
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.toLowerCase().split("[^\\p{L}\\p{N}]+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        frequencies.entrySet()
                  .stream()
                  .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                  .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();

        counter.createTestFile();

        
        System.out.println("Частота слов:");
        Map<String, Integer> frequencies = counter.countWords(Paths.get(TEST_FILE_NAME));
        counter.printFrequencies(frequencies);
    }
}
