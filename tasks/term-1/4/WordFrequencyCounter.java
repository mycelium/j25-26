import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;
import java.util.*;
import java.nio.file.*;

public class WordFrequencyCounter {

    // Подсчет слов с загрузкой всего файла
    public Map<String, Integer> countWords(Path filePath) {
        Map<String, Integer> wordCount = new HashMap<>();
        try {
            String content = Files.readString(filePath);
            String[] words = content.toLowerCase().split("[\\s]+"); // \\s - любой пробельный символ
            
            for (String word : words) {
                if (word.isEmpty()) continue;
                
                if (wordCount.containsKey(word)) {
                    wordCount.put(word, wordCount.get(word) + 1);
                } 
                else {
                    wordCount.put(word, 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
        return wordCount;
    }

    // Потоковое чтение (по байтам)
    public Map<String, Integer> countWordsStream(Path filePath) {
        Map<String, Integer> wordCount = new HashMap<>();
        char[] buffer = new char[8]; // размер буфера 8 байт
        StringBuilder leftover = new StringBuilder();

        try (Reader reader = Files.newBufferedReader(filePath)) {
            int charsRead;
            
            while ((charsRead = reader.read(buffer)) != -1) {
                // Добавляем текущий блок к остаткам с прошлого чтения
                leftover.append(buffer, 0, charsRead);

                String[] words = leftover.toString().toLowerCase().split("[\\s]+");

                // Если последнее слово обрезано
                leftover.setLength(0);
                if (!Character.isWhitespace(buffer[charsRead - 1])) { // isWhitespace определяет любой пробельный символ
                    // Если конец блока не пробел — последнее слово может обрезано
                    if (words.length > 0) {
                        leftover.append(words[words.length - 1]);
                    }
                    // Обрабатываем все, кроме последнего
                    for (int i = 0; i < words.length - 1; i++) {
                        String word = words[i];
                        if (!word.isEmpty()) {
                        	if (wordCount.containsKey(word)) {
                                wordCount.put(word, wordCount.get(word) + 1);
                            } 
                        	else {
                                wordCount.put(word, 1);
                            }
                        }
                    }
                } 
                else {
                    // Если блок кончился на пробеле (последнее слово не обрезано)
                    for (String word : words) {
                        if (!word.isEmpty()) {
                        	if (wordCount.containsKey(word)) {
                                wordCount.put(word, wordCount.get(word) + 1);
                            } 
                        	else {
                                wordCount.put(word, 1);
                            }
                        }
                    }
                }
            }

            // После чтения остался хвост — добавляем последнее слово, если есть
            if (leftover.length() > 0) {
                String word = leftover.toString().toLowerCase().trim();
                if (!word.isEmpty()) {
                	if (wordCount.containsKey(word)) {
                        wordCount.put(word, wordCount.get(word) + 1);
                    } 
                	else {
                        wordCount.put(word, 1);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }

        return wordCount;
    }


    public void printFrequencies(Map<String, Integer> frequencies) {
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
        	System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();

        Path filePath = Paths.get("C:\\Users\\olegl\\all\\polytech\\параллельное программирование\\j25-26\\term-1\\4\\Lorem.txt");

        System.out.println("Подсчет с загрузкой всего файла:");
        Map<String, Integer> allAtOnce = counter.countWords(filePath);
        counter.printFrequencies(allAtOnce);

        System.out.println("Потоковая обработка файла:");
        Map<String, Integer> streamed = counter.countWordsStream(filePath);
        counter.printFrequencies(streamed);
    }
}
