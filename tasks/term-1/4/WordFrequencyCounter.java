import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class wordCounter {
    public Map<String, Integer> countFrequencyOfWords(String filename) {
        System.out.println("Used function: loading the entire file.");
        Map<String, Integer> wordsMap = new HashMap<String, Integer>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            String content = String.join("\n", lines);
            String[] words = content.split("\\s+");
            for (String word : words) {
                String cleanedWord = word.replaceAll("[^a-zA-Zа-яА-Я]", "").toLowerCase();

                if (!cleanedWord.isEmpty()) {
                    wordsMap.put(cleanedWord, wordsMap.getOrDefault(cleanedWord, 0) + 1);
                }
            }

        } catch (IOException e) {
            System.err.println("Error to read file " + e.getMessage());
        }
        return wordsMap;
    }
    public Map<String, Integer> countFrequencyOfWordsStreaming(String filename) {
        System.out.println("Used function: streaming the file");
        Map<String, Integer> wordsMap = new HashMap<String, Integer>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filename))) {
            String line;
            Pattern pattern = Pattern.compile("[a-zA-Zа-яА-Я]+");

            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line.toLowerCase());

                while (matcher.find()) {
                    String word = matcher.group();
                    wordsMap.put(word, wordsMap.getOrDefault(word, 0) + 1);
                }
            }

        } catch (IOException e) {
            System.err.println("Error to read file " + e.getMessage());
        }

        return wordsMap;
    }
    public Map<String, Integer> sortMap(Map<String, Integer> map){
        Map<String, Integer> sortedMap = map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        return sortedMap;
    }
    public void printDictionary(Map<String, Integer> dict){

        int mapSize = dict.size();
        Map<String, Integer> sortedMap=sortMap(dict);
        dict=sortedMap;
        System.out.println("All dictionary: ");
        for (Map.Entry<String, Integer> entry : dict.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Unique words: "+mapSize+"");
        System.out.println("");

        int numWords=10;
        System.out.println("Top "+numWords+" frequent words: ");
        int count = 0;
        for (Map.Entry<String, Integer> entry : dict.entrySet()) {
            if (count >= numWords) {
                break;
            }
            System.out.println(entry.getKey() + ": " + entry.getValue());
            count++;
        }
        System.out.println("Unique words: "+numWords+"");
        System.out.println("");

    }
    public static void main(String[] args){
        wordCounter counter=new wordCounter();
        Map<String, Integer> test1;
        Map<String, Integer> test2;
        String f1="allFile.txt";
        String f2="streamingFile.txt";
        try {
            long fileSize = Files.size(Paths.get(f1));
            if (fileSize >= 1024) { //если размер файла превышает 1Мб
                test1 = counter.countFrequencyOfWordsStreaming(f1);
            } else {
                test1 = counter.countFrequencyOfWords(f1);
            }
            counter.printDictionary(test1);

            long fileSize1 = Files.size(Paths.get(f2));
            if (fileSize >= 1024) { //если размер файла превышает 1Мб
                test2 = counter.countFrequencyOfWordsStreaming(f2);
            } else {
                test2 = counter.countFrequencyOfWords(f2);
            }
            counter.printDictionary(test2);
        }catch (IOException e) {
            System.err.println("Error to read file " + e.getMessage());
        }
    }
}

