//import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords(Path filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (var reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) { //try автоматически закрывает файл
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(' ');
            }
        }
        String text = sb.toString();
        String[] tokens = text.split("[^\\p{L}']+"); // разделяем по не-буквам
        Map<String, Integer> freq = new HashMap<>();
        for (String t : tokens) {
            if (t.isEmpty()) continue;
            String w = t.toLowerCase();
            freq.put(w, freq.getOrDefault(w, 0) + 1);
        }
        return freq;
    }

    public Map<String, Integer> countWordsStream(Path filePath) throws IOException {
        Map<String, Integer> freq = new HashMap<>();
        try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {
            lines.forEach(line -> { //только одна строка из потока в памяти
                String[] tokens = line.split("[^\\p{L}']+");
                for (String t : tokens) {
                    if (t.isEmpty()) continue;
                    String w = t.toLowerCase();
                    freq.put(w, freq.getOrDefault(w, 0) + 1);
                }
            });
        }
        return freq;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        for (Map.Entry<String, Integer> e : frequencies.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }

    public static void main(String[] args) {
        System.out.print("Введите путь к файлу: "); // например: C:\Users\sergey\IdeaProjects\j25-26\tasks\term-1\4\text.txt

        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            String filePath = console.readLine().trim();
            Path p = Paths.get(filePath);
            System.out.print(p);
            if (!Files.exists(p)) {
                System.err.println("Файл не найден: " + p.toAbsolutePath());
                return;
            }
            if (!Files.isRegularFile(p)) {
                System.err.println("Указанный путь не является файлом: " + p.toAbsolutePath());
                return;
            }

            WordFrequencyCounter wfc = new WordFrequencyCounter();

            System.out.println("\n--- Full-load variant ---");
            try {
                Map<String, Integer> full = wfc.countWords(p);
                wfc.printFrequencies(full);
            } catch (OutOfMemoryError oome) {
                System.err.println("Недостаточно памяти для полной загрузки файла: " + oome.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при чтении: " + ex.getMessage());
            }

            System.out.println("\n--- Stream variant ---");
            try {
                Map<String, Integer> stream = wfc.countWordsStream(p);
                wfc.printFrequencies(stream);
            } catch (Exception ex) {
                System.err.println("Ошибка в потоковом режиме: " + ex.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения пути из консоли: " + e.getMessage());
        }
    }
}