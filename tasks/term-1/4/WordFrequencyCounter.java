import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.*;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords(Path filePath) {
        Map<String, Integer> freqMap = new HashMap<>();
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            String[] words = content.toLowerCase().split("[\\P{L}]+"); // \P{L} - всё, что не буква
            for (String word : words) {
                if (word.isEmpty()) continue;
                freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return freqMap;
    }

    public Map<String, Integer> countWordsStream(Path filePath) {
        Map<String, Integer> freqMap = new HashMap<>();
        try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {
            lines.forEach(line -> {
                String[] words = line.toLowerCase().split("[\\P{L}]+");
                for (String word : words) {
                    if (word.isEmpty()) continue;
                    freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return freqMap;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        frequencies.forEach((word, count) -> System.out.println(word + ": " + count));
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();

        long sizeThreshold = 1024 * 1024; // 1 МБ

        Path[] paths = {Path.of("shorttext.txt"), Path.of("longtext.txt")};

        for (Path path : paths) {
            try {
                long fileSize = Files.size(path);
                System.out.println("Processing file: " + path.getFileName() + ", size: " + fileSize + " bytes");

                Map<String, Integer> freq;

                if (fileSize > sizeThreshold) {
                    System.out.println("File is large, using streaming count:");
                    freq = counter.countWordsStream(path);
                } else {
                    System.out.println("File is small, using full file load count:");
                    freq = counter.countWords(path);
                }

                counter.printFrequencies(freq);
                System.out.println();
            } catch (IOException e) {
                System.err.println("Error accessing file: " + path.getFileName());
                e.printStackTrace();
            }
        }
    }


}
