import java.util.*;
import java.nio.file.Path;
import java.io.*;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords1(Path filePath) {
        Map<String, Integer> freq = new Map<>();
        StringBuilder res = new StringBuilder();
        try {
            BufferedReader file = new BufferedReader(new FileReader(filePath.toFile()));
            String line;
            while ((line = file.readLine()) != null) {
                res.append(line).append(" ");
            }
            file.close();
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

    public Map<String, Integer> countWords2(Path filePath) {
        Map<String, Integer> freq = new HashMap<>();
        try {
            BufferedReader r = new BufferedReader(new FileReader(filePath.toFile()));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.toLowerCase();
                String[] words = line.split(" ");
                for (String word : words) {
                    word = cleanWord(word);
                    if (!word.isEmpty()) {
                        if (freq.containsKey(word)) {
                            freq.put(word, freq.get(word) + 1);
                        } else {
                            freq.put(word, 1);
                        }
                    }
                }
            }
            r.close();
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
    }// ! / ...

    public void printFrequencies(Map<String, Integer> frequencies) {
        for (String word : frequencies.keySet()) {
            System.out.println(word + ": " + frequencies.get(word));
        }
    }

}
