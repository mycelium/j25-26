import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;

public class WordFrequencyCounter {

    // весь файл в память
    public Map<String, Integer> countWords(Path filePath) throws IOException {
        // read file, tokenize words, update map
        String content = Files.readString(filePath);
        return processText(content);
        // return null;
    }

    // потоковая обработка файла
    public Map<String, Integer> countWordsStreaming(Path filePath) throws IOException {
        Map<String, Integer> frequencies = new HashMap<>(16, 0.75f);

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            // чтение построчно
            while ((line = reader.readLine()) != null) {
                updateFrequencies(frequencies, line);
            }
        }

        return frequencies;
    }

    // частоты слов
    private Map<String, Integer> processText(String text) {
        Map<String, Integer> frequencies = new HashMap<>(16, 0.75f);
        updateFrequencies(frequencies, text);
        return frequencies;
    }

    private void updateFrequencies(Map<String, Integer> frequencies, String text) {
        StringBuilder currentWord = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isLetter(c)) {
                currentWord.append(c);
            } else {
                if (currentWord.length() > 0) {
                    String word = currentWord.toString();
                    word = word.toLowerCase();
                    frequencies.merge(word, 1, Integer::sum);
                    currentWord.setLength(0);
                }
            }
        }

        // обработка последнего слова
        if (currentWord.length() > 0) {
            String word = currentWord.toString();
            word = word.toLowerCase();
            frequencies.merge(word, 1, Integer::sum);
        }
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        // print word counts
        frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("%s: %d%n", entry.getKey(), entry.getValue()));
    }

    public static void main(String[] args) {
        // run word frequency counter
        if (args.length != 1) {
            System.out.println("Usage: java WordFrequencyCounter <file_path>");
            return;
        }

        WordFrequencyCounter counter = new WordFrequencyCounter();
        Path filePath = Paths.get(args[0]);

        try {
            Map<String, Integer> frequencies;

            long fileSizeInBytes = Files.size(filePath);
            long thresholdInBytes = 10 * 1024 * 1024;

            if (fileSizeInBytes > thresholdInBytes) {
                System.out.println("File is large (" + fileSizeInBytes + " bytes). Using streaming processing.");
                frequencies = counter.countWordsStreaming(filePath);
            } else {
                System.out.println("File is small (" + fileSizeInBytes + " bytes). Loading into memory.");
                frequencies = counter.countWords(filePath);
            }

            counter.printFrequencies(frequencies);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
