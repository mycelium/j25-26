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
                        LinkedHashMap::new  // сохраняет порядок сортировки
                ));
        return sortedMap;
    }
    public void printDictionary(Map<String, Integer> dict){
        Scanner in=new Scanner(System.in);
        System.out.print("Print all dictionary or a top frequently words?(type '1' or '2') ");
        int mapSize = dict.size();
        Map<String, Integer> sortedMap=sortMap(dict);
        dict=sortedMap;
        int choice=in.nextInt();
        if(choice==1){
            for (Map.Entry<String, Integer> entry : dict.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("Unique words: "+mapSize+"");
        } else if (choice==2) {
            int numWords;
            do {
                System.out.print("How many words do you want to print?(from 1 to " + mapSize + ") ");
                numWords = in.nextInt();
                if (numWords > mapSize) {
                    System.out.println("Number should be less than "+mapSize+"");
                }else if (numWords <= 0) {
                    System.out.println("Number should be more than 0");
                }else{
                    break;
                }
            } while (true);
            int count = 0;
            for (Map.Entry<String, Integer> entry : dict.entrySet()) {
                if (count >= numWords) {
                    break;
                }
                System.out.println(entry.getKey() + ": " + entry.getValue());
                count++;
            }
            System.out.println("Unique words: "+numWords+"");
        }
    }
    public static void main(String[] args){
        Scanner in=new Scanner(System.in);
        wordCounter counter=new wordCounter();
        Map<String, Integer> q;
        try {
            System.out.println("Enter filename(with .txt): ");
            String filename = in.nextLine();
            long fileSize = Files.size(Paths.get(filename));
            if (fileSize >= 1024 * 1024 * 1024) { //если размер файла превышает 1Гб
                q = counter.countFrequencyOfWordsStreaming(filename);
            } else {
                q = counter.countFrequencyOfWords(filename);
            }
            counter.printDictionary(q);
        }catch (IOException e) {
            System.err.println("Error to read file " + e.getMessage());
        }
    }
}
