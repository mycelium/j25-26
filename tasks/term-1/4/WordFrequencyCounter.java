import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords(Path filePath) {
    try {
        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        Map<String, Integer> freq = new HashMap<>();
        countIntoMap(content, freq);
        return freq;
    } catch (IOException e) {
        System.err.println("Ошибка чтения: " + e.getMessage());
        return Collections.emptyMap();
    }
}

    public Map<String, Integer> countWordsStreaming(Path filePath) {
        Map<String, Integer> freq = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                countIntoMap(line, freq);
            }
        } 
        catch (IOException e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
            return Collections.emptyMap();
        }
        return freq;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        if (frequencies == null || frequencies.isEmpty()) {
            System.out.println("Нет данных для вывода.");
            return;
        }
        System.out.println("\nЧастоты слов:");
        System.out.println("========================================");

        List<Map.Entry<String,Integer>> items = new ArrayList<>(frequencies.entrySet());

        items.sort(
                Comparator.<Map.Entry<String,Integer>>comparingInt(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey)
        );

        for (Map.Entry<String,Integer> e : items) 
            System.out.printf("%-25s : %d%n", e.getKey(), e.getValue());

        int total = frequencies.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("----------------------------------------");
        System.out.println("Уникальных слов : " + frequencies.size());
        System.out.println("Всего слов      : " + total);
    }

    private static Pattern WORD_PATTERN = Pattern.compile("\\b[\\p{L}\\p{Nd}]+(?:['’\\-][\\p{L}\\p{Nd}]+)*\\b");
    private void countIntoMap(String chunk, Map<String, Integer> freq) {
        if (chunk == null || chunk.isEmpty()) 
            return;
        String lower = chunk.toLowerCase(Locale.ROOT);
        Matcher m = WORD_PATTERN.matcher(lower);
        while (m.find()) {
            freq.merge(m.group(), 1, Integer::sum);
        }
    }

    private static void printUsage() {
        System.out.println("Использование:");
        System.out.println("java WordFrequencyCounter [опции] <файл>");
        System.out.println("Опции:");
        System.out.println("  -m memory       - принудительно обработка в памяти");
        System.out.println("  -m stream       - принудительно потоковая обработка");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String method = "memory"; // <-- по умолчанию память
        Path file = null;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if ("-m".equals(a)) {
                if (i + 1 < args.length) {
                    method = args[++i].toLowerCase(Locale.ROOT);
                    if (!method.equals("memory") && !method.equals("stream")) {
                        System.err.println("Значение -m должно быть memory или stream.");
                        return;
                    }
                } else {
                    System.err.println("Ожидалось значение после -m (memory|stream).");
                    return;
                }
            } else {
                file = Paths.get(a);
            }
        }

        if (file == null) {
            System.err.println("Не указан путь к файлу.");
            printUsage();
            return;
        }
        if (!Files.isRegularFile(file)) {
            System.err.println("Файл не найден или это не обычный файл: " + file);
            return;
        }

        WordFrequencyCounter counter = new WordFrequencyCounter();
        Map<String, Integer> freq;

        try {
            if ("stream".equals(method)) {
                System.out.println("[Режим] потоковая обработка");
                freq = counter.countWordsStreaming(file);
            } else {
                System.out.println("[Режим] обработка в памяти");
                freq = counter.countWords(file);
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        counter.printFrequencies(freq);
    }
}
