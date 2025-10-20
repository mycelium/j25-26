import java.util.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
public class WordFrequencyCounter {
    private static final Pattern WORD_PATTERN = Pattern.compile("[^a-zA-Zа-яА-Я0-9']+");
    
    public Map<String, Integer> countWords(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        String[] words = WORD_PATTERN.split(content.toLowerCase());
        
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
            }
        }
        
        return frequencyMap;
    }

    public Map<String, Integer> countWordsStreaming(Path filePath) throws IOException {
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        try (var lines = Files.lines(filePath)) {
            lines.forEach(line -> {
                String[] words = WORD_PATTERN.split(line.toLowerCase());
                for (String word : words) {
                    if (!word.isEmpty()) {
                        frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
                    }
                }
            });
        }
        
        return frequencyMap;
    }
    
    public void printFrequencies(Map<String, Integer> frequencies) {
        if (frequencies == null || frequencies.isEmpty()) {
            System.out.println("Нет данных для отображения");
            return;
        }
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("РЕЗУЛЬТАТ ПОДСЧЕТА ЧАСТОТЫ СЛОВ");
        System.out.println("=".repeat(50));
        
        frequencies.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> 
                System.out.printf("%-25s : %d%n", entry.getKey(), entry.getValue())
            );
        
        System.out.println("=".repeat(50));
        
        int totalWords = frequencies.values().stream().mapToInt(Integer::intValue).sum();
        System.out.printf("Общее количество слов: %d%n", totalWords);
        System.out.printf("Количество уникальных слов: %d%n", frequencies.size());
    }

    public static void createTestFile(String fileName) throws IOException {
        String[] testContents = {
            "Hello world! This is a simple test file for word frequency counting.\n" +
            "Hello again, this is only a test. Testing one two three.\n" +
            "Java is a great programming language. Java is used everywhere.\n" +
            "Programming in Java is fun and exciting. Let's test our word counter!\n" +
            "The quick brown fox jumps over the lazy dog. This sentence contains all letters.",
            
            "В чащах юга жил бы цитрус? Да, но фальшивый экземпляр!\n" +
            "Съешь же ещё этих мягких французских булок, да выпей чаю.\n" +
            "Программирование на Java требует внимания и практики.\n" +
            "Повторение мать учения. Слова слова слова тест тест проверка.",
            
            "Science and technology are evolving rapidly in the modern world.\n" +
            "Artificial intelligence and machine learning are transforming industries.\n" +
            "Data analysis requires careful processing and accurate algorithms.\n" +
            "The future of computing lies in quantum computers and advanced AI systems."
        };
        
        Random random = new Random();
        String content = testContents[random.nextInt(testContents.length)];
        
        Files.writeString(Paths.get(fileName), content);
        System.out.println("Создан тестовый файл: " + fileName);
    }

    public static void showMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("=".repeat(50));
        System.out.println("Выберите вариант:");
        System.out.println("1 - Указать путь к файлу");
        System.out.println("2 - Создать тестовый файл");
        System.out.println("0 - Выйти из программы");
        System.out.print("Ваш выбор (1/2/0): ");
    }

    public static void main(String[] args) {
        WordFrequencyCounter counter = new WordFrequencyCounter();
        Scanner scanner = new Scanner(System.in);
        
        boolean running = true;
        
        while (running) {
            showMenu();
            String choice = scanner.nextLine().trim();
            
            Path filePath = null;
            
            switch (choice) {
                case "1":
                    System.out.print("Введите путь к файлу: ");
                    String userPath = scanner.nextLine().trim();
                    filePath = Paths.get(userPath);
                    break;
                    
                case "2":
                    try {
                        String testFileName = "test_demo_" + System.currentTimeMillis() + ".txt";
                        createTestFile(testFileName);
                        filePath = Paths.get(testFileName);
                    } catch (IOException e) {
                        System.err.println("Ошибка создания тестового файла: " + e.getMessage());
                        continue;
                    }
                    break;
                    
                case "0":
                    System.out.println("Выход из программы...");
                    running = false;
                    continue;
                    
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
                    continue;
            }
            
            if (filePath != null) {
                try {
                    if (!Files.exists(filePath)) {
                        System.err.println("Файл не найден: " + filePath.toAbsolutePath());
                        System.out.println("\nСоветы:");
                        System.out.println("Убедитесь, что файл существует");
                        System.out.println("Используйте полный путь к файлу");
                        System.out.println("Или выберите вариант 2 для создания тестового файла");
                        continue;
                    }
                    
                    long fileSize = Files.size(filePath);
                    System.out.printf("Файл: %s%n", filePath.toAbsolutePath());
                    System.out.printf("Размер: %,d байт (%.2f MB)%n", fileSize, fileSize / (1024.0 * 1024.0));
                    
                    Map<String, Integer> frequencies;
                    
                    if (fileSize < 50 * 1024 * 1024) {
                        System.out.println("Используется: Загрузка всего содержимого файла");
                        frequencies = counter.countWords(filePath);
                    } else {
                        System.out.println("Используется: Потоковая обработка файла");
                        frequencies = counter.countWordsStreaming(filePath);
                    }
                    
                    counter.printFrequencies(frequencies);
                    
                    System.out.println("\nАнализ завершен! Возвращаемся в меню...");
                    
                } catch (IOException e) {
                    System.err.println("Ошибка ввода-вывода: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Неожиданная ошибка: " + e.getMessage());
                }
            }
        }
        
        scanner.close();
        System.out.println("Программа завершена.");
    }
}
