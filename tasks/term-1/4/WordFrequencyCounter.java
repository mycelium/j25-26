import java.util.*;
import java.nio.file.*;
import java.io.*;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords(Path filePath) throws IOException{
        // read file, tokenize words, update map
        if (!Files.exists(filePath)){
            throw new FileNotFoundException("File foesn't exist: " + filePath);
        }
        if (!Files.isRegularFile(filePath)){
            throw new IllegalArgumentException("This filepath isn't a file: " + filePath);
        }
        if (Files.size(filePath) == 0){
            throw new IOException ("This file is empty!" );
        }
        if (!Files.isReadable(filePath)){
            throw new IOException ("There are not privileges on reading this file: " + filePath);
        }
        try {
            return countWordsTotal(filePath);
        }  catch (OutOfMemoryError e) { 
            System.err.println("Warning: switch to stream processing");
            return countWordsStreaming(filePath);
        }
    }

    private Map<String,Integer> countWordsTotal(Path filePath) throws IOException{
        Map<String, Integer> frequencies = new HashMap<>();
        System.out.print("Total processing.. \n");
        String content = Files.readString(filePath, java.nio.charset.StandardCharsets.UTF_8);
        String[] words = content.toLowerCase().replaceAll("[^a-zA-Zа-яА-ЯёЁ\\s]", " ")
                                .split("\\s+");
        for (String word : words) {
            frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
        }                      
        return frequencies;
    }

    private Map<String, Integer> countWordsStreaming(Path filePath) throws IOException {
        Map<String, Integer> frequencies = new HashMap<>();
        System.out.print("Stream processing.. \n");
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase().replaceAll("[^a-zA-Zа-яА-ЯёЁ\\s]", " ")
                                     .split("\\s+");
                for (String word : words) {
                    frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                }
            }
        }
        
        return frequencies;
    }


    public void printFrequencies(Map<String, Integer> frequencies) {
        // print word counts
        frequencies.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> System.out.printf("%-20s: %d%n", entry.getKey(), entry.getValue()));
    }

    public static void main(String[] args) {       
        // run word frequency counter
        Path path;
        try (Scanner in = new Scanner(System.in)) {
            System.out.print("Enter the path to file: ");
            path = Paths.get(in.nextLine().trim());
        } 

        WordFrequencyCounter counter = new WordFrequencyCounter();
        try {
            Map<String, Integer> freq = counter.countWords(path);
            counter.printFrequencies(freq);
        } catch (Exception e) {
            System.err.println("Error on input or file processing: " + e.getMessage());
        }
    }
}

