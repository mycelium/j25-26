import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.nio.file.*;

public class WordFrequencyCounter {

    // Максимальный размер файла для способа с помощью загрузки всего содержимого разом
    private static final long maxSize = 5_242_880; // 5 мб

    // Размер блоков, используемых для последовательной
    // загрузки файла в символах
    private static final int buffrSize = 64;

    public static Map<String, Integer> countWords(Path filePath) throws IOException {

       if (Files.size(filePath) < maxSize) return countWordsForSmallFiles(filePath);
       return countWordsForBigFiles(filePath);
    }

    public static Map<String, Integer> countWordsForSmallFiles(Path filePath) throws IOException {

        Map<String, Integer> wordsFreqs = new HashMap<>();
        String               fileText   = Files.readString(filePath);

        for (String word: fileText.toLowerCase()
                          .replaceAll("[^а-яa-z'\\s]", " ") // 1. Очищаем строку
                                                            // от незначащих символов;
                          .replaceAll("\\s+", " ")          // 2. Заменяем все \\s+ на
                                                            // единичные пробелы;
                          .stripLeading()                   // 3. Удаляем пробел в начале строки;
                          .split(" ")                       // 4. Разделяем строку по пробелам;
                           ) {
            wordsFreqs.put(word, wordsFreqs.getOrDefault(word, 0) + 1);
        }
        return wordsFreqs;
    }

    public static Map<String, Integer> countWordsForBigFiles(Path filePath) throws IOException {

        Map<String, Integer> wordsFreqs     = new HashMap<>();
        BufferedReader       buffrRr         = new BufferedReader(new FileReader(filePath.toFile()));

        // Массив, используемый для последовательного чтения
        // файла блоками.
        char[] buffr = new char[buffrSize];
        // Количество прочитанных символов за итерацию
        int symbolsRead;
        // Слово, которое могло быть прервано из-за
        // того что чтение файла происходит по блокам
        String unfinishedWord = "";

        while ((symbolsRead = buffrRr.read(buffr)) != -1) {

            String line    = unfinishedWord + new String(buffr, 0, symbolsRead);
            unfinishedWord = ""; // С каждой итерацией очищаем прерванное слово,
                                 // потому что оно и так добавляется к исходному тексту

            // Сохраняем очищенную строку, чтобы определить
            // является ли последнее слово прерванным
            line = line.toLowerCase()
                        .replaceAll("[^а-яa-z'\\s]", " ")
                        .replaceAll("\\s+", " ")
                        .stripLeading();
            String[] wordsArr = line.split(" ");

            // Определяем есть ли прерванное слово
            if (wordsArr.length != 0)
                if(!line.endsWith(" "))
                    unfinishedWord = wordsArr[wordsArr.length - 1];

            // Если есть прерванное слово, не добавляем его в словарь
            int wordsAmt = wordsArr.length - (unfinishedWord.isEmpty() ? 0 : 1);
            for(int i = 0; i < wordsAmt; i++)
                wordsFreqs.put(wordsArr[i], wordsFreqs.getOrDefault(wordsArr[i], 0) + 1);
        }
        return wordsFreqs;
    }

    public static void printFrequencies(Map<String, Integer> frequencies) {

        int    sumFreq = 0;
        String longestWord = "";
        for (String word : frequencies.keySet()){
            if (word.length() > longestWord.length())
                longestWord = word;
        }
        for (Map.Entry<String, Integer> ent : frequencies.entrySet()) {
            sumFreq += ent.getValue();
            System.out.println(ent.getKey() + 
                               " ".repeat(longestWord.length() - ent.getKey().length())
                               + " | " + ent.getValue());
        }
        System.out.printf("Total number of words : %d\nNumber of unique words: %d\n",
                           sumFreq,frequencies.entrySet().size());
    }

    public static void main(String[] args) {

        try {
            System.out.println("  Small file  \n---------------");
            printFrequencies(countWords(Path.of("smallText.txt")));
            System.out.println("  Big file  \n---------------");
            printFrequencies(countWords(Path.of("bigText.txt")));
        } catch(IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
