import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class WordFrequencyCounter {

    private static final long MEMORY_THRESHOLD = 1024 * 1024;

    public Map<String, Integer> loadAndCount(Path path) throws IOException {
        String fullText = Files.readString(path);
        return extractWordFrequencies(fullText);
    }

    public Map<String, Integer> streamAndCount(Path path) throws IOException {
        Map<String, Integer> stats = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            br.lines().forEach(line -> processLine(line, stats));
        }
        return stats;
    }

    private Map<String, Integer> extractWordFrequencies(String content) {
        Map<String, Integer> result = new HashMap<>();
        processLine(content, result);
        return result;
    }

    private void processLine(String line, Map<String, Integer> accumulator) {
        String[] tokens = line.split("[^\\p{L}]+");

        for (String token : tokens) {
            if (!token.isEmpty()) {
                String normalized = token.toLowerCase(Locale.ROOT);
                accumulator.merge(normalized, 1, Integer::sum);
            }
        }
    }

    public void displayResults(Map<String, Integer> wordStats) {
        if (wordStats.isEmpty()) {
            System.out.println("No words found in the file.");
            return;
        }


        List<Map.Entry<String, Integer>> sorted = wordStats.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Integer>comparingByValue().reversed()
                                .thenComparing(Map.Entry.comparingByKey())
                )
                .collect(Collectors.toList());

        System.out.println("\nWord frequencies:");
        System.out.println("------------------");
        for (Map.Entry<String, Integer> entry : sorted) {
            System.out.printf("%-20s : %d%n", entry.getKey(), entry.getValue());
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please provide a file path as argument.");
            System.err.println("Example: java WordFrequencyCounter input.txt");
            return;
        }

        Path inputFile = Paths.get(args[0]);
        if (!Files.exists(inputFile)) {
            System.err.println("File not found: " + inputFile.toAbsolutePath());
            return;
        }

        WordFrequencyCounter analyzer = new WordFrequencyCounter();

        try {
            long fileSize = Files.size(inputFile);
            Map<String, Integer> frequencies;

            if (fileSize > MEMORY_THRESHOLD) {
                System.out.println("Large file detected (" + fileSize + " bytes). Using streaming mode...");
                frequencies = analyzer.streamAndCount(inputFile);
            } else {
                System.out.println("Small file (" + fileSize + " bytes). Loading fully into memory...");
                frequencies = analyzer.loadAndCount(inputFile);
            }

            analyzer.displayResults(frequencies);

        } catch (IOException ex) {
            System.err.println("Failed to read file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}