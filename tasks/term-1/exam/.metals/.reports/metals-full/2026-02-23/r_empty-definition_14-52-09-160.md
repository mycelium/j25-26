error id: file:///C:/Users/79090/j25-26/tasks/term-1/exam/app/src/main/java/org/example/Client.java:local3
file:///C:/Users/79090/j25-26/tasks/term-1/exam/app/src/main/java/org/example/Client.java
empty definition using pc, found symbol in pc: 
found definition using semanticdb; symbol local3
empty definition using fallback
non-local guesses:

offset: 933
uri: file:///C:/Users/79090/j25-26/tasks/term-1/exam/app/src/main/java/org/example/Client.java
text:
```scala
package org.example;


import java.io.*;
import java.net.Socket;

public class Client {
    
    private static final String IP = "127.0.0.1";
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(IP, PORT)) {
            // Примеры отзывов для тестирования
            String[] reviews = {
                "This movie is just great! I got a lot of positive emotions.",
                "The movie is terrible, don't waste your time!",
                "Average guy, nothing special, but there was no horror either."
            };
            
            // Выбираем случайный отзыв для анализа
            String review = reviews[(int)(Math.random() * reviews.length)];
            System.out.println("Отправляем отзыв: " + review);
            
            // Создаем запрос с текстом отзыва на фильм
            Request request@@ = new Request(review);
            
            // Отправляем запрос серверу
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(request);
            
            // Получаем и обрабатываем ответ
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            Response response = (Response) input.readObject();
            
            if (response.isSuccess()) {
                System.out.println("Эмоциональная оценка отзыва: " + response.getSentiment());
            } else {
                System.err.println("Ошибка при анализе: " + response.getSentiment());
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка клиента: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: 