<<<<<<< HEAD
package test;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class WordFrequencyCounte {

    // ✅ Lecture complète du fichier
    public Map<String, Integer> countWordsFull(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();

        try {
            String content = Files.readString(filePath);
            String[] words = content.toLowerCase().split("\\W+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de lecture : " + e.getMessage());
        }
        return frequencies;
    }

    // ✅ Lecture en flux (pour très gros fichiers)
    public Map<String, Integer> countWordsStream(Path filePath) {
        Map<String, Integer> frequencies = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase().split("\\W+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de lecture : " + e.getMessage());
        }
        return frequencies;
    }

    // ✅ Affichage des résultats triés
    public void printFrequencies(Map<String, Integer> frequencies) {
        frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> System.out.printf("%-15s : %d%n", entry.getKey(), entry.getValue()));
    }

    // ✅ Méthode principale sans argument
    public static void main(String[] args) {
        WordFrequencyCounte counter = new WordFrequencyCounte();

        // 👉 Définis ici ton fichier texte
        // Exemple : sous Windows
        Path filePath = Paths.get("C:\\Users\\pamel\\eclipse-workspace\\tester\\filePath.txt");

        // Exemple : sous Linux / Mac
        // Path filePath = Paths.get("/home/ton_nom/Documents/texte.txt");

        System.out.println("=== Lecture complète ===");
        Map<String, Integer> fullMap = counter.countWordsFull(filePath);
        counter.printFrequencies(fullMap);

        System.out.println("\n=== Lecture en flux ===");
        Map<String, Integer> streamMap = counter.countWordsStream(filePath);
        counter.printFrequencies(streamMap);
=======
import java.util.*;
import java.nio.file.Path;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords(Path filePath) {
        // read file, tokenize words, update map
        return null;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        // print word counts
    }

    public static void main(String[] args) {
        // run word frequency counter
>>>>>>> 063d39b (First term tasks)
    }
}
