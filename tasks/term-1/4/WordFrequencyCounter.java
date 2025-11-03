import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WordFrequencyCounter {

    
public Map<String, Integer> countWords(Path filePath) {
    System.out.println("Start countWords");
    try {
        long fileSize = Files.size(filePath);
        long maxSize = 20 * 1024 * 1024;
        
        if (fileSize > maxSize) {
            System.out.println("File size " + (fileSize/1024 )/1024+ " MB exceeds 20 MB limit. Cannot process in memory.");
            System.out.println("Switch to countWordsStreaming");
            return countWordsStreaming(filePath);
        } else {
            return countWordsInMemory(filePath);
        }
    } catch (IOException e) {
        System.err.println("Error checking file size: " + e.getMessage());
        return Collections.emptyMap();
    }
}

private Map<String, Integer> countWordsInMemory(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();
        try {
            String content = Files.readString(filePath, java.nio.charset.StandardCharsets.UTF_8);
            processLine(content, frequencies);
        } catch (IOException e) {
            System.err.println("IOException detected " + e.getMessage());
        }
        return frequencies;
    }
    
   
private Map<String, Integer> countWordsStreaming(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line, frequencies);
            }
        } catch (IOException e) {
            System.err.println("IOException detected " + e.getMessage());
        }
        return frequencies;
    }
    
    private void processLine(String line, Map<String, Integer> frequencies) {
    String[] words = line.split("[^\\p{L}-]+");
    for (String word : words) {
        if (!word.isEmpty() && word.matches(".*\\p{L}.*")) {
           
            if (word.startsWith("-")) {
                word = word.replaceAll("^-+", "");
            }
            if (!word.isEmpty()) {
                frequencies.merge(word.toLowerCase(), 1, Integer::sum);
            }
        }
    }
}

public void printFrequencies(Map<String, Integer> frequencies) {
    if (frequencies.isEmpty()) {
        System.out.println("No words");
        return;
    }
    
    PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    
    Map<Integer, List<String>> frequencyMap = frequencies.entrySet()
            .stream()
            .collect(Collectors.groupingBy(
                Map.Entry::getValue,
                Collectors.mapping(Map.Entry::getKey, Collectors.toList())
            ));
    
    frequencyMap.entrySet()
            .stream()
            .sorted(Map.Entry.<Integer, List<String>>comparingByKey().reversed())
            .forEach(entry -> {
                int frequency = entry.getKey();
                List<String> words = entry.getValue();
                Collections.sort(words);
                String wordsString = String.join(", ", words);
                out.println("Frequencie = " + frequency +"; Words: "+ " [" + wordsString + "]");
            });
    }
    


      public static void main(String[] args) {
     
        WordFrequencyCounter counter = new WordFrequencyCounter();
    
          Path filePath = Path.of(".\\tasks\\term-1\\4\\smallTest.txt"); // проверка для небольшого текста = 29 Кб
            //Path filePath = Path.of(".\\tasks\\term-1\\4\\largeTest.txt"); // проверка для большого текста = 44318 Кб
            if (!Files.exists(filePath)) {
            System.out.println("File not exists: " + filePath);
            return; 
        }
            Map<String, Integer> frequencies = counter.countWords(filePath);
            counter.printFrequencies(frequencies);
    }
   
}