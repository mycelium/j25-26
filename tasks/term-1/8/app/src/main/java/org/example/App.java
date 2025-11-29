package org.example;

public class App {
    public String getGreeting() {
        return "MNIST Image Classification Project - DeepLearning4j";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
        System.out.println("=====================================");
        
        try {
            MNISTClassifier.main(args);
        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
