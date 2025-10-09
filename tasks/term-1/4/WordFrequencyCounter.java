import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class WordFrequencyCounter {
    // Порог для переключения между полной загрузкой и потоковой обработкой в КБ
    private static final long SIZE_THRESHOLD = 1024 * 1024;
    // Размер блока для потокового чтения в КБ
    private static final int BLOCK_SIZE = 1024 * 1024;
    public Map<String, Integer> countWords(Path filePath) throws IOException {
        long fileSize = Files.size(filePath);
        if (fileSize <= SIZE_THRESHOLD) {
            return countWordsFull(filePath);
        } else {
            return countWordsBlocks(filePath);
        }
    }

    private Map<String, Integer> countWordsFull(Path filePath) throws IOException {
        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        return countWordsInString(content);
    }

    private Map<String, Integer> countWordsInString(String text) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        StringBuilder currentWord = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (Character.isLetterOrDigit(c)) {
                currentWord.append(c);
            } else {
                if (currentWord.length() > 0) {
                    String word = currentWord.toString().toLowerCase();
                    frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
                    currentWord.setLength(0);
                }
            }
        }
        
        if (currentWord.length() > 0) {
            String word = currentWord.toString().toLowerCase();
            frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
        }
        
        return frequencyMap;
    }

    private Map<String, Integer> countWordsBlocks(Path filePath) throws IOException {
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        try (InputStream inputStream = Files.newInputStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), BLOCK_SIZE)) {
            
            char[] buffer = new char[BLOCK_SIZE];
            StringBuilder incompleteWord = new StringBuilder();
            int charsRead;
            
            while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                String currentBlock = incompleteWord + new String(buffer, 0, charsRead);
                
                int lastCompleteWordEnd = findLastWordBoundary(currentBlock);
                
                String completeWordsPart = currentBlock.substring(0, lastCompleteWordEnd);
                if (!completeWordsPart.isEmpty()) {
                    processChunk(completeWordsPart, frequencyMap);
                }

                incompleteWord = new StringBuilder(currentBlock.substring(lastCompleteWordEnd));
            }
            
            if (incompleteWord.length() > 0) {
                processChunk(incompleteWord.toString(), frequencyMap);
            }
        }
        
        return frequencyMap;
    }

    private int findLastWordBoundary(String text) {
        for (int i = text.length() - 1; i >= 0; i--) {
            if (!Character.isLetterOrDigit(text.charAt(i))) {
                return i + 1;
            }
        }
        return 0;
    }
    
    private void processChunk(String chunk, Map<String, Integer> frequencyMap) {
        StringBuilder currentWord = new StringBuilder();
        
        for (int i = 0; i < chunk.length(); i++) {
            char c = chunk.charAt(i);
            
            if (Character.isLetterOrDigit(c)) {
                currentWord.append(c);
            } else {
                if (currentWord.length() > 0) {
                    String word = currentWord.toString().toLowerCase();
                    frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
                    currentWord.setLength(0);
                }
            }
        }

        if (currentWord.length() > 0) {
            String word = currentWord.toString().toLowerCase();
            frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
        }
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        printFrequencies(frequencies, 0); 
    }

    public void printFrequencies(Map<String, Integer> frequencies, int minFrequency) {
        List<Map.Entry<String, Integer>> sortedEntries = frequencies.entrySet()
                .stream()
                .filter(entry -> entry.getValue() >= minFrequency)
                .sorted((a, b) -> {
                    int freqCompare = b.getValue().compareTo(a.getValue());
                    return freqCompare != 0 ? freqCompare : a.getKey().compareTo(b.getKey());
                })
                .collect(Collectors.toList());
        
        System.out.println("Word Frequencies:");
        System.out.println("=-=-=-=-=-=-=-=-=");
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            System.out.printf("%-20s : %d%n", entry.getKey(), entry.getValue());
        }
        System.out.println("=-=-=-=-=-=-=-=-=");
        System.out.println("Total unique words: " + frequencies.size());
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();
        try {
            Path filePath = Path.of(".\\tasks\\term-1\\4\\big_file.txt");
            Map<String, Integer> frequencies = counter.countWords(filePath);
            counter.printFrequencies(frequencies, 1000);
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
