import java.util.*;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;

public class WordFrequencyCounter {
    private static final long changeModeSize = 512 * 1024;
    private static final int blockSize = 512 * 1024;

    private Map<String, Integer> countWordsSmall (Path filePath) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        try {
            String fileString = Files.readString(filePath);
            StringBuilder currentWord = new StringBuilder();
            
            for (int i = 0; i < fileString.length(); i++) {
                char symbol = fileString.charAt(i);

                if (Character.isLetterOrDigit(symbol)) {
                    currentWord.append(symbol);
                }
                else {
                    if (currentWord.length() > 0){
                        String newWord = currentWord.toString().toLowerCase();
                        frequencyMap.put(newWord, frequencyMap.getOrDefault(newWord, 0) + 1);
                        currentWord.setLength(0);
                    }
                }
            }

            if (currentWord.length() > 0) {
                String newWord = currentWord.toString().toLowerCase();
                frequencyMap.put(newWord, frequencyMap.getOrDefault(newWord, 0) + 1);
                currentWord.setLength(0);
            }

        }
        catch (IOException e) {
            System.err.println("Something went wrong: " + e.getMessage());
        }

        return frequencyMap;
    }

    private Map<String, Integer> countWordsBig (Path filePath) {
        Map <String, Integer> frequencyMap = new HashMap<>();
        StringBuilder incompleteWord = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(filePath), blockSize)) {
            char[] buffer = new char[blockSize];
            int symbolCode;

            while ((symbolCode = reader.read(buffer)) != -1) {    
                String currentBlock = incompleteWord.toString() + new String(buffer, 0, symbolCode);    
                incompleteWord.setLength(0);

                StringBuilder currentWord = new StringBuilder();

                for (int i = 0; i < currentBlock.length(); i++) {
                    char c = currentBlock.charAt(i);
                    if (Character.isLetterOrDigit(c)) {
                        currentWord.append(c);
                    }
                    else {
                        if (currentWord.length() > 0) {
                            String newWord = currentWord.toString().toLowerCase();
                            frequencyMap.put(newWord, frequencyMap.getOrDefault(newWord, 0) + 1);
                            currentWord.setLength(0);
                        }
                    }
                }

                if (currentWord.length() > 0) {
                    incompleteWord.append(currentWord);
                }
                
            }

            if (incompleteWord.length() > 0) {
                String newWord = incompleteWord.toString().toLowerCase();
                frequencyMap.put(newWord, frequencyMap.getOrDefault(newWord, 0) + 1);
            }

        } 
        catch (IOException e) {
            System.err.println("Something went wrong: " + e.getMessage());
        }


        return frequencyMap;
    }

    public Map<String, Integer> countWords(Path filePath) {
        try {
        if (Files.size(filePath) > changeModeSize) {
            return countWordsBig(filePath);
        }
        else {
            return countWordsSmall(filePath);
        }
        }
        catch (IOException e) {
            System.err.println("Something went wrong: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        System.out.println("------------- Word Frequencies -------------");
        frequencies.forEach((word, count) -> System.out.println("[" + word + " : " + count + "]"));
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        WordFrequencyCounter counter = new WordFrequencyCounter();
        Map<String, Integer> smallFileCounter = counter.countWords(Path.of("term-1/4/smallFile.txt"));

        counter.printFrequencies(smallFileCounter);
        
        Map<String, Integer> bigFileCounter = counter.countWords(Path.of("term-1/4/bigFile.txt"));

        counter.printFrequencies(bigFileCounter);

    }
}
