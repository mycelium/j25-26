package org.example;

import java.io.*;
import java.net.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;
import java.util.List;

public class Server {

    private static final int PORT = 8081;
    private static StanfordCoreNLP pipeline;

    static {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server has started on port" + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new RequestHandler(clientSocket)).start();
            }

        } catch (Exception e) {
            System.err.println("Critical server error: " + e.getMessage());
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
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
                
                Request request = (Request) in.readObject();
                String reviewText = request.getReviewText();

                System.out.println("Received review for analysis: " +
                        (reviewText.length() > 50 ? reviewText.substring(0, 50) + "..." : reviewText));

                String sentiment = analyzeSentiment(reviewText);

                Response response = new Response(sentiment, true);
                out.writeObject(response);
                System.out.println("Sent result: " + sentiment);

            } catch (IOException | ClassNotFoundException e) {
                try {
                    ObjectOutputStream errorOut = new ObjectOutputStream(clientSocket.getOutputStream());
                    errorOut.writeObject(new Response("Error processing request: " + e.getMessage(), false));
                } catch (IOException ioEx) {
                    System.err.println("Failed to send error message to client");
                }
                System.err.println("Error processing client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Failed to close connection: " + e.getMessage());
                }
            }
        }

        private String analyzeSentiment(String text) {
            try {
                return getSentiment(text);
            } catch (Exception e) {
                System.err.println("Error in NLP analysis: " + e.getMessage());
                return "neutral"; 
            }
        }

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