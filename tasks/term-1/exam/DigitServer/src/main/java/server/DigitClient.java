package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class DigitClient {
    public static void main(String[] args) {
        
        String imagePath = "/home/andrey/java/j25-26/tasks/term-1/8/app/7.png"; 

        System.out.println("Подключение к серверу...");

        try (Socket socket = new Socket("localhost", 8080);
             
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Успешно подключено! Читаем файл картинки...");
            
            
            byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
            
            
            ImageRequest request = new ImageRequest(imageBytes);
            out.writeObject(request);
            out.flush();
            System.out.println("Запрос с картинкой (размер " + imageBytes.length + " байт) отправлен на сервер.");

            
            DigitResponse response = (DigitResponse) in.readObject();
            
            
            System.out.println("=====================================");
            System.out.println("ОТВЕТ СЕРВЕРА: Распознана цифра " + response.getRecognizedDigit());
            System.out.println("=====================================");

        } catch (Exception e) {
            System.err.println("Ошибка на клиенте: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
