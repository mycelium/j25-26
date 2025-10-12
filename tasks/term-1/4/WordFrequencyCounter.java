import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class WordFrequencyCounter {

    public Map<String, Integer> countWords(Path filePath) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Выберите метод обработки файла:");
        System.out.println("[1] - загрузка всего файла в память;");
        System.out.println("[2] - потоковая обработка файла.");
        System.out.println("Ваш выбор: ");

        try {
            int input = scanner.nextInt();
            return switch (input) {
                case 1 -> countWordsInMemory(filePath);
                case 2 -> countWordsInStream(filePath);
                default -> {
                    System.err.println("Ошибка ввода: введите 1 или 2.");
                    yield Collections.emptyMap();
                }
            };
        } catch (InputMismatchException e) {
            System.err.println("Ошибка ввода: введите 1 или 2.");
            return Collections.emptyMap();
        }
    }

    public Map<String, Integer> countWordsInMemory(Path filePath) {
        System.out.println("Выбран метод загрузки всего файла в память.");

        Map<String, Integer> frequencies = new HashMap<>();

        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);

            String[] words = content.toLowerCase()
                    .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", " ")
                    .split("\\s+");

            for (String word : words) {
                if (!word.isEmpty()) {
                    frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                }
            }
            return frequencies;
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Map<String, Integer> countWordsInStream(Path filePath) {
        System.out.println("Выбран метод потоковой обработки.");

        Map<String, Integer> frequencies = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
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
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            return Collections.emptyMap();
        }

        return frequencies;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        if (frequencies == null || frequencies.isEmpty()) {
            System.out.println("Нет данных для вывода.");
            return;
        }

        List<Map.Entry<String, Integer>> sortedEntries = frequencies.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .toList();

        System.out.println("\nРезультаты подсчета частоты слов в файле:");

        int limit = Math.min(20, sortedEntries.size());

        for(int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = sortedEntries.get(i);
            System.out.printf("%-20s : %d%n", entry.getKey(), entry.getValue());
        }

        if(sortedEntries.size() > 20) {
            System.out.println("... и еще " + (sortedEntries.size()-20) + " слов.");
        }

        int totalCount = frequencies.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("Общее количество слов: " + totalCount);
    }

    public static void main(String[] args) {
        System.out.println("\n=== Подсчет частоты слов в текстовом файле ===");

        WordFrequencyCounter counter = new WordFrequencyCounter();
        Scanner scanner = new Scanner(System.in);

        boolean continueRunning = true;

        while (continueRunning) {
            try {
                System.out.print("Введите путь к файлу (например: C:\\Users\\Имя\\document.txt): ");
                String filePathStr = scanner.nextLine().trim();

                if (filePathStr.isEmpty()) {
                    System.out.println("Пожалуйста, введите путь к файлу!");
                    continue;
                }

                if (filePathStr.startsWith("\"") && filePathStr.endsWith("\"")) {
                    filePathStr = filePathStr.substring(1, filePathStr.length() - 1);
                }

                Path filePath = Paths.get(filePathStr);

                if (!Files.exists(filePath)) {
                    System.err.println("Ошибка: Файл не найден - " + filePath.toAbsolutePath());
                } else if (!Files.isRegularFile(filePath)) {
                    System.err.println("Ошибка: Указанный путь ведет к директории, а не к файлу");
                } else {
                    Map<String, Integer> frequencies = counter.countWords(filePath);
                    counter.printFrequencies(frequencies);
                }

                System.out.println("\n----------------------------------------");
                System.out.println("[1] - Проанализировать другой файл");
                System.out.println("[2] - Выйти из программы");
                System.out.print("Выберите действие: ");

                String choice = scanner.nextLine().trim();
                if (choice.equals("2")) {
                    continueRunning = false;
                    System.out.println("Выход из программы...");
                }

            } catch (InvalidPathException e) {
                System.err.println("Ошибка: Неверный путь к файлу - " + e.getMessage());
            }
        }

        scanner.close();
    }
}