package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiThreadedClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8081;
    private static final int THREAD_COUNT = 4;

    public static void main(String[] args) {
        String[] reviews = {
            "This product is amazing! I love it.",
            "Absolutely terrible experience. Never buying again.",
            "It is okay, nothing special but does the job.",
            "The best thing I have ever bought.",
            "Bad quality and slow shipping.",
            "I am neutral about this."
        };

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (String review : reviews) {
            executor.submit(() -> sendRequest(review));
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private static void sendRequest(String reviewText) {
        try (Socket socket = new Socket(HOST, PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Request request = new Request(reviewText);
            out.writeObject(request);
            out.flush();

            Response response = (Response) in.readObject();

            if (response.isSuccess()) {
                System.out.printf("[Thread %d] Result: %s | Text: %s%n", 
                        Thread.currentThread().getId(), 
                        response.getSentiment(),
                        reviewText.length() > 30 ? reviewText.substring(0, 30) + "..." : reviewText);
            } else {
                System.err.println("Server error: " + response.getSentiment());
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}