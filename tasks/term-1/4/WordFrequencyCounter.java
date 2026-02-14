import java.util.*;
import java.nio.file.Path;
import java.io.*;

public class WordFrequencyCounter {

    public Map<String, Integer> countWordsInMemory(Path filePath) {
        Map<String, Integer> freq = new HashMap<>();
        StringBuilder res = new StringBuilder();
        try (BufferedReader file = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = file.readLine()) != null) {
                res.append(line).append(" ");
            }
            String text = res.toString();
            String[] wordsArr = text.split(" ");
            
            for (String word : wordsArr) {
                word = cleanWord(word);
                if (!word.isEmpty()) {
                    if (freq.containsKey(word)) {
                        freq.put(word, freq.get(word) + 1);
                    } else {
                        freq.put(word, 1);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("not found");
        }
        return freq;
    }

    public Map<String, Integer> countWordsStreaming(Path filePath) {
        Map<String, Integer> freq = new HashMap<>();
        StringBuilder buffer = new StringBuilder();
        char[] chunk = new char[1024];

        try (FileReader reader = new FileReader(filePath.toFile())) {
            int n;
            while ((n = reader.read(chunk)) != -1) {
                buffer.append(chunk, 0, n);

                int lastSpace = -1;
                for (int i = buffer.length() - 1; i >= 0; i--) {
                    if (buffer.charAt(i) == ' ') {
                        lastSpace = i;
                        break;
                    }
                }

                if (lastSpace != -1) {
                    String current = buffer.substring(0, lastSpace + 1);
                    buffer.delete(0, lastSpace + 1);

                    String[] words = current.split(" ");
                    for (String word : words) {
                        word = cleanWord(word.toLowerCase());
                        if (!word.isEmpty()) {
                            if (freq.containsKey(word)) {
                                freq.put(word, freq.get(word) + 1);
                            } else {
                                freq.put(word, 1);
                            }
                        }
                    }
                }
            }
            if (buffer.length() > 0) {
                String word = cleanWord(buffer.toString().toLowerCase());
                if (!word.isEmpty()) {
                    if (freq.containsKey(word)) {
                        freq.put(word, freq.get(word) + 1);
                    } else {
                        freq.put(word, 1);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("not found");
        }
        return freq;
    }

    private String cleanWord(String word) {
        StringBuilder clean = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c >= 'a' && c <= 'z') {
                clean.append(c);
            }
        }
        return clean.toString();
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        for (String word : frequencies.keySet()) {
            System.out.println(word + ": " + frequencies.get(word));
        }
    }
}
