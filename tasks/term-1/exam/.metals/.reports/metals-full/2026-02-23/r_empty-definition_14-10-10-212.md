error id: file:///C:/Users/79090/j25-26/tasks/term-1/exam/app/src/main/java/org/example/Server.java:java/lang/Thread#start().
file:///C:/Users/79090/j25-26/tasks/term-1/exam/app/src/main/java/org/example/Server.java
empty definition using pc, found symbol in pc: java/lang/Thread#start().
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1168
uri: file:///C:/Users/79090/j25-26/tasks/term-1/exam/app/src/main/java/org/example/Server.java
text:
```scala
package org.example;

import java.io.*;
import java.net.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.pipeline.Annotation;
import java.util.Properties;
import java.util.List;

public class Server {

    private static final int PORT = 8080;
    private static StanfordCoreNLP pipeline;

    static {
        // Инициализация NLP-модели один раз при старте сервера
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер Варвары запущен на порту " + PORT);
            System.out.println("NLP-модель Stanford CoreNLP успешно инициализирована");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new RequestHandler(clientSocket)).st@@art();
            }

        } catch (Exception e) {
            System.err.println("Критическая ошибка сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class RequestHandler implements Runnable {
        private Socket clientSocket;

        public RequestHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
                // Получаем запрос от клиента
                Request request = (Request) in.readObject();
                String reviewText = request.getReviewText();

                System.out.println("Получен отзыв для анализа: " +
                        (reviewText.length() > 50 ? reviewText.substring(0, 50) + "..." : reviewText));

                // Анализируем эмоциональную окраску с использованием вашей NLP-логики
                String sentiment = analyzeSentiment(reviewText);

                // Формируем и отправляем ответ
                Response response = new Response(sentiment, true);
                out.writeObject(response);
                System.out.println("Отправлен результат: " + sentiment);

            } catch (IOException | ClassNotFoundException e) {
                try {
                    ObjectOutputStream errorOut = new ObjectOutputStream(clientSocket.getOutputStream());
                    errorOut.writeObject(new Response("Ошибка обработки запроса: " + e.getMessage(), false));
                } catch (IOException ioEx) {
                    System.err.println("Не удалось отправить сообщение об ошибке клиенту");
                }
                System.err.println("Ошибка обработки клиента: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Не удалось закрыть соединение: " + e.getMessage());
                }
            }
        }

        /**
         * Анализ эмоциональной окраски текста с использованием вашей реализации из
         * лабораторной работы №5
         */
        private String analyzeSentiment(String text) {
            try {
                String nlpSentiment = getSentiment(text);

                // Преобразуем английские термины в русские согласно вашему заданию
                switch (nlpSentiment) {
                    case "positive":
                        return "позитивный";
                    case "negative":
                        return "негативный";
                    default:
                        return "нейтральный";
                }
            } catch (Exception e) {
                System.err.println("Ошибка при NLP-анализе: " + e.getMessage());
                return "неопределенный";
            }
        }

        /**
         * Полная реализация из вашей лабораторной работы №5 без изменений
         */
        private String getSentiment(String text) {
            if (text == null || text.trim().isEmpty()) {
                return "neutral";
            }

            Annotation annotation = new Annotation(text);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            if (sentences == null || sentences.isEmpty()) {
                return "neutral";
            }

            // Считаем голоса за каждый класс
            int negative = 0, neutral = 0, positive = 0;

            for (CoreMap sentence : sentences) {
                String label = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                if (label == null)
                    continue;

                label = label.toLowerCase();
                if ("very negative".equals(label) || "negative".equals(label)) {
                    negative++;
                } else if ("very positive".equals(label) || "positive".equals(label)) {
                    positive++;
                } else {
                    neutral++;
                }
            }

            // Выбираем класс с наибольшим числом голосов
            if (negative > positive && negative >= neutral) {
                return "negative";
            } else if (positive > negative && positive >= neutral) {
                return "positive";
            } else {
                return "neutral";
            }
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: java/lang/Thread#start().