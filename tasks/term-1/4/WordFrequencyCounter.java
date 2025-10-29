import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class WordFrequencyCounter {

    // минималотный размер большого файла(5 МБ)
    private static final long AUTO_SWITCH_THRESHOLD = 5 * 1024 * 1024;

    // режим 1: загрузка всего содержимого файла
    public Map<String, Integer> countWordsInMemory(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        String[] words = content.toLowerCase()
                .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", " ")
                .split("\\s+");

        Map<String, Integer> frequencies = new HashMap<>();
        for (String word : words) {
            if (!word.trim().isEmpty()) {
                frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
            }
        }
        return frequencies;
    }

    // режим 2: потоковая обработка для больших файлов
    public Map<String, Integer> countWordsStreaming(Path filePath) throws IOException {
        Map<String, Integer> frequencies = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase()
                        .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", " ")
                        .split("\\s+");

                for (String word : words) {
                    if (!word.trim().isEmpty()) {
                        frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                    }
                }
            }
        }
        return frequencies;
    }

    public Map<String, Integer> countWordsAuto(Path filePath) throws IOException {
        long fileSize = Files.size(filePath);
        if (fileSize < AUTO_SWITCH_THRESHOLD) {
            return countWordsInMemory(filePath);
        } else {
            return countWordsStreaming(filePath);
        }
    }

    // печать топ-N самых частых слов
    public void printTopFrequencies(Map<String, Integer> frequencies, int topN) {
        List<Map.Entry<String, Integer>> topEntries = frequencies.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toList());

        System.out.println("Топ-" + topN + " самых частых слов:");
        for (Map.Entry<String, Integer> entry : topEntries) {
            System.out.printf("%s: %d%n", entry.getKey(), entry.getValue());
        }
    }

    public void printStats(Map<String, Integer> frequencies, long executionTime, long fileSize) {
        int totalWords = frequencies.values().stream().mapToInt(Integer::intValue).sum();

        System.out.println("Размер файла: " + fileSize + " байт");
        System.out.println("Всего слов: " + totalWords);
        System.out.println("Уникальных слов: " + frequencies.size());
        System.out.println("Время выполнения: " + executionTime + " мс");
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();

        try {
            // неправильно - нет аргументов
            //java WordFrequencyCounter

            // правильно - только путь к файлу
            //java WordFrequencyCounter text.txt

            // очень правильно - путь к файлу и количество слов для топа
            //java WordFrequencyCounter text.txt 15


            if (args.length == 0) { // проверка количества аргументов командной строки
                System.out.println("Использование: java WordFrequencyCounter <путь_к_файлу> [topN]");
                return;
            }

            Path path = Paths.get(args[0]);
            if (!Files.exists(path)) {
                System.out.println("Файл не найден: " + args[0]);
                return;
            }

            int topN = 10;
            if (args.length > 1) {
                topN = Integer.parseInt(args[1]);
            }

            long startTime = System.currentTimeMillis();
            Map<String, Integer> frequencies = counter.countWordsAuto(path);
            long endTime = System.currentTimeMillis();
            long fileSize = Files.size(path);

            counter.printStats(frequencies, endTime - startTime, fileSize);
            System.out.println();
            counter.printTopFrequencies(frequencies, topN);

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}