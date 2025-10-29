import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WordFrequencyCounter {
    
    public static Map<String, Integer> countWords(Path filePath) throws Exception 
    {
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
    }

    public static void printFrequencies(Map<String, Integer> frequencies) {
        frequencies.forEach((key, value) -> System.out.printf("%s - %d\n", key, value));
    }

    public static void main(String[] args) 
    {   
        try {
            printFrequencies(countWords(Paths.get("./my.txt")));
        }
        catch (Exception e) {
            System.err.printf("Error while executing: %s", e.getMessage());
        }
    }
}
