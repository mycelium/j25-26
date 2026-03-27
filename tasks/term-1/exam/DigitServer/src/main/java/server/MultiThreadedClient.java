package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MultiThreadedClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final int THREAD_COUNT = 5; 

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Использование: java server.MultiThreadedClient <путь_к_изображению1> [<путь_к_изображению2> ...]");
            System.exit(1);
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (String imagePath : args) {
            executor.submit(() -> processImage(imagePath));
        }

        executor.shutdown();

    }

    private static void processImage(String imagePath) {
        Path path = Paths.get(imagePath);

        if (!Files.exists(path) || Files.isDirectory(path)) {
            System.err.println("[" + Thread.currentThread().getName() + "] " +
                    "Ошибка: файл не существует: " + imagePath);
            return;
        }

        System.out.println("[" + Thread.currentThread().getName() + "] " +
                "Подключение к серверу для файла: " + path.getFileName());

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    "Успешное подключение. Чтение файла " + path.getFileName());

            byte[] imageBytes = Files.readAllBytes(path);
            ImageRequest request = new ImageRequest(imageBytes);
            out.writeObject(request);
            out.flush();

            DigitResponse response = (DigitResponse) in.readObject();

            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    "=====================================");
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    "ОТВЕТ СЕРВЕРА: Распознана цифра " + response.getRecognizedDigit() + " для файла " + path.getFileName());
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    "=====================================");

        } catch (Exception e) {
            System.err.println("[" + Thread.currentThread().getName() + "] " +
                    "Ошибка на клиенте при обработке " + imagePath + ": " + e.getMessage());
            e.printStackTrace(); 
        }
    }
}