import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WordFrequencyCounter {

    public static Map<String, Integer> countWords(Path filePath) {
        Map<String, Integer> wordCounts = new HashMap<>();
        StringBuilder unprocessedTail = new StringBuilder();

        try (Reader inputReader = Files.newBufferedReader(filePath)) {
            char[] readBuffer = new char[4096];
            int charsRead;

            while ((charsRead = inputReader.read(readBuffer)) != -1) {
                String currentChunk = unprocessedTail.append(readBuffer, 0, charsRead).toString();
                String[] potentialWords = currentChunk.split("\\s+");

                unprocessedTail.setLength(0);

                if (!currentChunk.isEmpty() && !Character.isWhitespace(currentChunk.charAt(currentChunk.length() - 1))) {
                    unprocessedTail.append(potentialWords[potentialWords.length - 1]);
                    potentialWords = Arrays.copyOf(potentialWords, potentialWords.length - 1);
                }

                for (String rawWord : potentialWords) {
                    String normalized = normalizeWord(rawWord);
                    if (!normalized.isEmpty()) {
                        wordCounts.merge(normalized, 1, Integer::sum);
                    }
                }
            }

            if (unprocessedTail.length() > 0) {
                String finalWord = normalizeWord(unprocessedTail.toString());
                if (!finalWord.isEmpty()) {
                    wordCounts.merge(finalWord, 1, Integer::sum);
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to read file: " + e.getMessage());
        }

        return wordCounts;
    }

    private static String normalizeWord(String word) {
        return word.toLowerCase()
                   .replaceAll("[^a-zа-яё]", "")
                   .trim();
    }

    public static void printFrequencies(Map<String, Integer> frequencies) {
        frequencies.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> System.out.println(entry.getKey() + " - " + entry.getValue()));
    }

    public static void main(String[] args) {
        Path targetFile = Paths.get("tasks\\term-1\\4\\test.txt");
        Map<String, Integer> result = countWords(targetFile);
        printFrequencies(result);
    }
}