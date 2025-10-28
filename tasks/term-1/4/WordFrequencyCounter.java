import java.util.*;
import java.nio.file.Path;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords(Path filePath) {
        // read file, tokenize words, update map
        return null;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        // print word counts
        if (frequencies.isEmpty()) {
            System.out.println("Нет данных.");
            return;
        }
        System.out.println("Количество слов:");
        frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue()));
    }

    public static void main(String[] args) {
        // run word frequency counter
    }
}
