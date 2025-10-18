import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.management.GarbageCollectorMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WordFrequencyCounter {

    public static Map<String, Integer> countWordsString(String line) {
        if (line == null || line.isEmpty()) {
            return new HashMap<>();
        }
        
        String[] words = line.toLowerCase()
                .replaceAll("[^a-zA-Zа-яА-Я\\s]", " ")
                .split("\\s+"); 
        
        Map<String, Integer> dict = new HashMap<>();
        
        for (String word : words) {
            word = word.trim();
            if (!word.isEmpty() && word.chars().anyMatch(Character::isLetter)) {
                dict.put(word, dict.getOrDefault(word, 0) + 1);
            }
        }
        
        return dict;
    }
    
    public static Map<String, Integer> countWords(Path filePath) throws Exception {
        
        if (Files.size(filePath) > Runtime.getRuntime().maxMemory()) {
            Map<String, Integer> dict = new HashMap<>();
            StringBuilder leftover = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(filePath)))) {
                char[] buffer = new char[4096];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    String chunk = leftover.append(buffer, 0, read).toString();

                    String[] parts = chunk.split("\\s+");

                    leftover.setLength(0);
                    if (!Character.isWhitespace(chunk.charAt(chunk.length() - 1))) {
                        leftover.append(parts[parts.length - 1]);
                        parts = Arrays.copyOf(parts, parts.length - 1);
                    }

                    for (String word : parts) {
                        word = word.toLowerCase().replaceAll("[^a-zа-я]", "").trim();
                        if (!word.isEmpty()) {
                            dict.merge(word, 1, Integer::sum);
                        }
                    }
                }

                if (leftover.length() > 0) {
                    String word = leftover.toString().toLowerCase().replaceAll("[^a-zа-я]", "").trim();
                    if (!word.isEmpty()) {
                        dict.merge(word, 1, Integer::sum);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return dict;

        }else{
            try {
                String content = Files.readString(filePath);
                return countWordsString(content);
            } catch (IOException e) {
                    e.printStackTrace();
                    return Collections.emptyMap();
            }
        }
    }

    public static void printFrequencies(Map<String, Integer> frequencies) {
        frequencies.forEach((key, value) -> System.out.printf("%s - %d\n", key, value));
    }

    public static void main(String[] args) 
    {   
        try {
            printFrequencies(countWords(Paths.get("./big.txt")));
        }
        catch (Exception e) {
            System.err.printf("Error while executing: %s", e.getMessage());
        }
    }
}
