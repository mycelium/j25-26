
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WordFrequencyCounter {

   
    public Map<String, Integer> countWordsFull(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();

        try {
            String content = Files.readString(filePath);
            String[] words = content.toLowerCase().split("\\W+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return frequencies;
    }

    
    public Map<String, Integer> countWordsStream(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(filePath)))) {
            char[] buffer = new char[8192]; // 8 KB chunks
            StringBuilder wordBuilder = new StringBuilder();
            int charsRead;

            while ((charsRead = reader.read(buffer)) != -1) {
                for (int i = 0; i < charsRead; i++) {
                    char c = Character.toLowerCase(buffer[i]);
                    if (Character.isLetterOrDigit(c)) {
                        wordBuilder.append(c);
                    } else {
                        if (wordBuilder.length() > 0) {
                            String word = wordBuilder.toString();
                            frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                            wordBuilder.setLength(0); // reset
                        }
                    }
                }
            }

            
            if (wordBuilder.length() > 0) {
                String word = wordBuilder.toString();
                frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return frequencies;
    }

    
    public void printFrequencies(Map<String, Integer> frequencies, int topN) {
        if (frequencies.isEmpty()) {
            System.out.println("No words found.");
            return;
        }

        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(frequencies.entrySet());
        sortedList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        System.out.println("\nTop " + topN + " most frequent words:");
        for (int i = 0; i < Math.min(topN, sortedList.size()); i++) {
            Map.Entry<String, Integer> entry = sortedList.get(i);
            System.out.printf("%-15s : %d%n", entry.getKey(), entry.getValue());
        }

        System.out.println("\nTotal unique words: " + frequencies.size());
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();

        if (args.length == 0) {
            System.out.println("Please provide the path to the file as an argument.");
            return;
        }

        Path filePath = Paths.get(args[0]);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            System.err.println("File not found or is not a regular file: " + filePath);
            return;
        }

        try {
            long fileSize = Files.size(filePath);
            System.out.printf("File size: %.2f MB%n", fileSize / (1024.0 * 1024.0));

            Map<String, Integer> frequencies;

          
            if (fileSize < 100 * 1024 * 1024) { // <100 MB
                System.out.println("File is small → using full file reading");
                frequencies = counter.countWordsFull(filePath);
            } else {
                System.out.println("File is large → using buffered stream reading");
                frequencies = counter.countWordsStream(filePath);
            }

            counter.printFrequencies(frequencies, 20);

        } catch (IOException e) {
            System.err.println("Error checking file size: " + e.getMessage());
        }
    }
}
