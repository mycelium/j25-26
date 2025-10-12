import java.util.*;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;

public class WordFrequencyCounter {
    private static final long changeModeSize = 2 * 1024 * 1024;
    private static final int blockSize = 1024 * 1024;

    private Map<String, Integer> countWordsSmall (Path filePath) {
        Map<String, Integer> frequencyMap = new hashMap<>();
        
        try {
            String fileString = Files.readString(filePath);
            StringBuilder currentWord;
            
            for (int i = 0; i < fileString.length; i++) {
                char symbol = fileString.charAt(i);

                if (Character.isLetterOrDigit(symbol)) {
                    currentWord.append(symbol);
                }
                else {
                    String newWord = currentWord.toString();
                    frequencyMap.put(newWord, frequencyMap.getOrDefault(newWord, 0) + 1);
                    currentWord.setLength(0);
                }
            }
        }
        catch (IOException e) {
            System.err.println("Something went wrong: " + e.getMessage());
        }

        return frequencyMap;
    }

    public Map<String, Integer> countWords(Path filePath) {
        // read file, tokenize words, update map
        return null;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        // print word counts
    }

    public static void main(String[] args) {
        // run word frequency counter
    }
}
