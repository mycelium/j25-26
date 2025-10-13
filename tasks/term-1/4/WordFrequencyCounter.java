import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordFrequencyCounter {

    private static final long SIZE_THRESHOLD = 10 * 1024 * 1024; //порог размера файла 10 MB

    //полная загрузка файла
    public static Map<String, Long> countWordsFullLoad(String filePath) throws IOException {
        Path path = Paths.get(filePath); //преобразуем строку пути в объект Path
        String content = Files.readString(path, StandardCharsets.UTF_8); //читаем весь файл как строку в кодировке UTF-8
        return extractWords(content)
                .collect(Collectors.groupingBy(word -> word, Collectors.counting()));
    }

    //для потокового подсчета слов
    public static Map<String, Long> countWordsStreaming(String filePath) throws IOException {
        Map<String, Long> wordCount = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) { //автоматически закрывает BufferedReader
            String line;
            while ((line = reader.readLine()) != null) { //читаем файл построчно, пока не достигнем конца файла
                extractWords(line).forEach(word ->
                        wordCount.merge(word, 1L, Long::sum) //увеличиваем счетчик слова на 1
                );
            }
        }
        return wordCount;
    }

    //извлечение слов из текста
    private static Stream<String> extractWords(String text) {
        return Arrays.stream(text.toLowerCase().split("[^\\p{L}\\p{N}]+")) //приводим к нижнему регистру и разбиваем по не-буквенным и не-цифровым символам
                .filter(word -> !word.isEmpty());
    }

    public static void printResults(Map<String, Long> wordCount) {
        wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) //сортируем по убыванию частоты
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue())); //выводим каждую пару слово-частота
    }

    public static void main(String[] args) {
        String filePath;

        // Если аргумент командной строки не передан, запрашиваем путь у пользователя
        if (args.length != 1) {
            System.out.println("Аргумент командной строки не передан.");
            System.out.println("Введите путь к файлу для анализа:");

            Scanner scanner = new Scanner(System.in);
            filePath = scanner.nextLine().trim();
            scanner.close();

            if (filePath.isEmpty()) {
                System.err.println("Путь к файлу не может быть пустым.");
                return;
            }
        } else {
            filePath = args[0]; //получаем путь к файлу из первого аргумента
        }

        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            System.err.println("Файл не существует: " + filePath);
            return;
        }

        if (!Files.isReadable(path)) {
            System.err.println("Нет прав на чтение файла: " + filePath);
            return;
        }

        if (Files.isDirectory(path)) {
            System.err.println("Указанный путь является директорией, а не файлом: " + filePath);
            return;
        }

        try {
            long fileSize = Files.size(path); //получаем размер файла в байтах
            Map<String, Long> result;

            System.out.printf("Обработка файла: %s (%.2f MB)%n", //выводим информацию о файле (имя и размер в МБ)
                    path.getFileName(), fileSize / (1024.0 * 1024.0));

            //выбираем метод обработки в зависимости от размера файла
            if (fileSize < SIZE_THRESHOLD) {
                System.out.println("Используется метод полной загрузки");
                result = countWordsFullLoad(filePath);
            } else {
                System.out.println("Используется потоковый метод");
                result = countWordsStreaming(filePath);
            }

            System.out.printf("Найдено уникальных слов: %d%n", result.size());
            System.out.println("\nТоп-20 самых частых слов:");

            // Выводим только топ-20 результатов для удобства просмотра
            result.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(20)
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));

        } catch (IOException e) {
            System.err.println("Ошибка при обработке файла: " + e.getMessage());
        }
    }
}