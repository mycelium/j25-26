import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WordFrequencyCounter {

    public Map<String, Integer> countWordsInMemory(Path filePath) throws IOException {
        Map<String, Integer> frequencies = new HashMap<>();

        String content = Files.readString(filePath);
        String[] words = content.toLowerCase()
                .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", " ")
                .split("\\s+");

        for (String word : words) {
            if (!word.isEmpty()) {
                frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
            }
        }

        return frequencies;
    }


    public Map<String, Integer> countWordsStreaming(Path filePath) throws IOException {
        Map<String, Integer> frequencies = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase()
                        .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", " ")
                        .split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                    }
                }
            }
        }
        return frequencies;
    }

    public Map<String, Integer> countWords(Path filePath) {
        try {
            long fileSize = Files.size(filePath);
            long maxMemory = Runtime.getRuntime().maxMemory();
            System.out.println("Доступная оперативная память: " + (maxMemory / (1024 * 1024 )) + " MB");
            System.out.println("Размер файла: " + (fileSize/(1024 * 1024 )) + "MB");
            if (fileSize < maxMemory * 0.5) {
                System.out.println("Используется метод: загрузка всего файла в память");
                return countWordsInMemory(filePath);
            } else {
                System.out.println("Используется метод: потоковая обработка");
                return countWordsStreaming(filePath);
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            return Collections.emptyMap();
        }
    }



    public void printFrequencies(Map<String, Integer> frequencies, int topN) {
        System.out.println("\nВсе слова и их частоты:");
        frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("%-20s: %d%n", entry.getKey(), entry.getValue()));

    }



    public static void create5GBFile() throws IOException {
        Path largeFile = Paths.get("5gbfile.txt");
        String sampleText = "Это пример текста для создания большого файла ";
        long targetSize = 5L * 1024 * 1024 * 1024;

        try (BufferedWriter writer = Files.newBufferedWriter(largeFile,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            long currentSize = 0;
            int iteration = 0;

            while (currentSize < targetSize) {
                writer.write(sampleText);
                currentSize += sampleText.getBytes().length;
                iteration++;

                if (iteration % 100 == 0) {
                    writer.write("\n");
                    currentSize += System.lineSeparator().getBytes().length;
                }
            }
        }
        long actualSize = Files.size(largeFile);
        System.out.println("Создан файл размером: " +
                (actualSize / (1024.0 * 1024 * 1024)) + " GB");
    }



    
    public static void main(String[] args) {


        try {
        System.out.println("Начало создание файла 5GB");// т.к. у меня доступной оперативной памяти 4ГБ было принято решение создавать файл 5ГБ для потоковой обработки
        create5GBFile();
        System.out.println("Файл успешно создан");
        } catch (IOException e) {
            System.err.println("Ошибка при создании файла: " + e.getMessage());
        }

        WordFrequencyCounter counter = new WordFrequencyCounter();

        System.out.println("=== Анализ короткого текста ===");
        Path pathShort = Path.of("short.txt");
        Map<String, Integer> shortResult = counter.countWords(pathShort);
        counter.printFrequencies(shortResult,10);

        System.out.println("\n=== Анализ длинного текста ===");
        Path pathLong = Path.of("5gbfile.txt");
        Map<String, Integer> longResult = counter.countWords(pathLong);
        counter.printFrequencies(longResult,10);
    }
}


