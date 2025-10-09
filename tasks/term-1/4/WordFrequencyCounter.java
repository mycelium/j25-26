import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WordFrequencyCounter {
    private static final long SWITCHING_SIZE = 1024 * 1024;

    public static Map<String, Integer> countWords(Path filePath) {
        // read file, tokenize words, update map
        Map<String, Integer> numWord = new HashMap<String, Integer>();
        try {
            long fileSize = Files.size(filePath);

            if (fileSize < SWITCHING_SIZE) {
                String content = Files.readString(filePath);
                String[] words = content.split("\\s+");
                for (String word : words) {
                    word = word.toLowerCase();
                    word = word.replaceAll("[^а-яa-z]", "");
                    if (!word.isEmpty()) {
                        numWord.put(word, numWord.getOrDefault(word, 0) + 1);
                    }
                }
            } else {
                try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] words = line.split("\\s+");
                        for (String word : words) {
                            word = word.toLowerCase();
                            word = word.replaceAll("[^а-яa-z]", "");
                            if (!word.isEmpty()) {
                                numWord.put(word, numWord.getOrDefault(word, 0) + 1);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numWord;
    }

    public static void printFrequencies(Map<String, Integer> frequencies) {
        // print word counts
        for (var i : frequencies.entrySet()) {
            System.out.printf("%s %s \n", i.getKey(), i.getValue());
        }
    }

    public static void main(String[] args) {
        // run word frequency counter
        Path path = Path.of("C:/University/5_Sem/Java/j25-26/tasks/term-1/4/test_short.txt");
        printFrequencies(countWords(path));
    }
}
