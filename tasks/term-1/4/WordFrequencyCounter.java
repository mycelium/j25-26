import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords(Path filePath) {
        // read file, tokenize words, update map
        Map<String,Integer> hashCountWords = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                String[] words = line.split("\\W+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        String lower = word.toLowerCase();
                        hashCountWords.put(lower, hashCountWords.getOrDefault(lower, 0) + 1);
                    }
                }
            }
        } catch (Exception e){
            System.out.println("Error reading file"+e.getMessage());
        }

        return hashCountWords;
    }

    public Map<String, Integer> countWordsStream(Path filePath) {
        Map<String,Integer> hashCountWords = new HashMap<>();
        try (BufferedReader reader  = Files.newBufferedReader(filePath)){
            String line;
            while ((line = reader.readLine()) != null){
                String[] words = line.split("\\W+");
                for (String word : words) {
                    if(!word.isEmpty()){
                        String lower = word.toLowerCase();
                        hashCountWords.put(lower, hashCountWords.getOrDefault(lower, 0) + 1);
                    }
                }
            }
        } catch(Exception e){
            System.out.println("Error reading file"+e.getMessage());
        }
        return hashCountWords;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        // print word counts
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    public static void generateFile(Path filePath, int wordsCount) throws IOException {
        if (Files.exists(filePath)) {
            return;
        }
        String[] sampleWords = {"aaa", "bbbb", "cc", "ddddd", "oooo"};
        Random random = new Random();

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (int i = 0; i < wordsCount; i++) {
                writer.write(sampleWords[random.nextInt(sampleWords.length)] + " ");
                if (i % 15 == 0) writer.newLine();
            }
        }
    }


    public static void main(String[] args) throws IOException {
        // run word frequency counter
        WordFrequencyCounter wordFrequencyCounter = new WordFrequencyCounter();

        Path pathMini = Path.of("mini.txt");
        Path pathBig = Path.of("big.txt");

        generateFile(pathMini, 100);
        generateFile(pathBig, 1000000);
        Map<String, Integer> frequencies = wordFrequencyCounter.countWords(pathMini);
        wordFrequencyCounter.printFrequencies(frequencies);
        System.out.println("\nПотоковая обработка файла:");
        Map<String, Integer> frequenciesStream = wordFrequencyCounter.countWordsStream(pathBig);
        wordFrequencyCounter.printFrequencies(frequenciesStream);
    }
}
