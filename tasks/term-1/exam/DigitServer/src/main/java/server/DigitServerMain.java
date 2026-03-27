package server;

import org.example.ModelManager;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DigitServerMain {
    private static final int PORT = 8080;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("Инициализация сервера и загрузка модели...");
        
        MultiLayerNetwork model = null;
        try {
            
            model = ModelManager.getModel();
        } catch (Exception e) {
            System.err.println("Критическая ошибка при загрузке модели: " + e.getMessage());
            return; 
        }

        System.out.println("Модель загружена. Запуск сервера на порту " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Подключился клиент: " + clientSocket.getInetAddress());

                
                threadPool.execute(new ClientHandler(clientSocket, model));
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}