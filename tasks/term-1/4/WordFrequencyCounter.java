import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class WordFrequencyCounter {
    private static final long MAX_SIZE = 1024*1024;
    private static final long BATCH_SIZE = 1024*1024;

    public static Map<String, Integer> countWords(Path filePath) {
        try {
            if (Files.size(filePath) > MAX_SIZE) { return countWordsLong(filePath); }
            else { return countWordsShort(filePath); }
        }
        catch (IOException e) {
            System.out.println("Error when trying to read a file: " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println(
                "Unexpected exception when handling file size: "
                + e.getMessage()
            );
        }
        return null;
    }

    public static Map<String, Integer> countWordsShort(Path filePath) {
        Map<String, Integer> dict = new HashMap<String, Integer>();

        try {
            String file = Files.readString(filePath);
            for (String word : getWordsString(file)) {
                if (dict.containsKey(word)){
                    dict.put(word, dict.get(word)+1);
                }
                else { dict.put(word, 1); }
            }
        }
        catch (IOException e){
            System.out.println("Error when handling IO: " + e.getMessage());
        }

        return dict;
    }

    public static Map<String, Integer> countWordsLong(Path filePath) {
        Map<String, Integer> dict = new HashMap<String, Integer>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))){
            String line;
            int count = 0;
            while ((line = br.readLine()) != null){
                for (String word : getWordsString(line)){
                    if (dict.containsKey(word)){
                        dict.put(word, dict.get(word)+1);
                    }
                    else { dict.put(word, 1); }
                }
            }
        }
        catch (FileNotFoundException e){
            System.out.println("Couldnt find file: " + e.getMessage());
        }
        catch (IOException e){
            System.out.println("Error when handling IO: " + e.getMessage());
        }
        catch (Exception e){
            System.out.println(
                "Unexpected exception when handling BufferReader: "
                + e.getMessage()
            );
        }

        return dict;
    }

    public static String[] getWordsString(String str){
        return Arrays.stream(
                str.toLowerCase().split(" "))
                    .filter(
                            string ->
                                    !string.isEmpty() &&
                                    string.chars().allMatch(Character::isLetter)
                    ).toArray(String[]::new);
    }

    public static void printFrequencies(Map<String, Integer> frequencies) {

        System.out.println(" ----======= Word frequencies =======----");
        frequencies.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .forEach(
                (element) ->
                    System.out.println(
                        String.format(
                            "%-30s : %d",
                            element.getKey(),
                            element.getValue()
                        )
                    )
            );
    }

    public static void main(String[] args) {
        Path path = Path.of("tasks/term-1/4/short_text.txt");

        printFrequencies(countWords(path));

        path = Path.of("tasks/term-1/4/long_text.txt");

        printFrequencies(countWords(path));
    }
}
