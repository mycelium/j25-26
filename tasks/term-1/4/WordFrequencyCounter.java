import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WordFrequencyCounter {

    public static int maxSize = 10000; //in bytes

    public static Map<String, Integer> countWordsString(String line)
    {
        String[] words = Arrays.stream(line.split(" "))
                    .filter(str ->  str != null && 
                                    !str.isEmpty() && 
                                    str.chars().allMatch(Character::isLetter))
                    .toArray(String[]::new);

        var dict = new HashMap<String, Integer>();

        for (String word : words) 
        {   
            word = word.toLowerCase();
            
            if (dict.containsKey(word)) {
                dict.replace(word, dict.get(word)+1);
            }    
            else{
                dict.put(word, 1);
            }
        }

        return dict;
    }
    
    public static Map<String, Integer> countWords(Path filePath) throws Exception {
        // read file, tokenize words, update map

        var file = filePath.toFile();
        
        if (!file.exists()) {
            throw new Exception("File does not exists");
        }
        
        if (file.isDirectory()) {
            throw new Exception("It's a directory, not a file");
        }
        
        if (!file.canRead()) {
            throw new Exception("Can't read file");
        }

        if (Files.size(filePath) < maxSize) 
        {
            return countWordsString(Files.readString(Paths.get(filePath.toString())));
        }

        Map<String, Integer> result = new HashMap<>();            
            
        var reader = new BufferedReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null) 
        {
            countWordsString(line).forEach(
                (key, value) -> {
                    result.merge(key, value, Integer::sum);
                }
            );
        }

        reader.close();

        return result;
    }

    public static void printFrequencies(Map<String, Integer> frequencies) {
        frequencies.forEach((key, value) -> System.out.printf("%s - %d\n", key, value));
    }

    public static void main(String[] args) 
    {
        try {
            printFrequencies(countWords(Paths.get(args[0])));
        }
        catch (IndexOutOfBoundsException e){
            System.err.println("Для запуска программы передайте одним аргументом путь до файла");
        }
        catch (Exception e) {
            System.err.printf("Error while executing: %s", e.getMessage());
        }
    }
}
