package org.example;

import org.nd4j.linalg.dataset.DataSet;

public class App {
    public static void main(String[] args) {
        try {
            MNISTLoader mnistLoader = new MNISTLoader(
                    "t10k-images.idx3-ubyte",
                    "t10k-labels.idx1-ubyte"
            );

            CNNClassifier cnn = new CNNClassifier();
            cnn.train(mnistLoader);

            double accuracy = cnn.evaluate(testData);
            System.out.printf("Model accuracy on test data: %.2f%%%n", accuracy * 100);

            String imagePath = "tasks\\term-1\\8\\app\\src\\main\\resources\\test data\\two.png";
            int prediction = cnn.predict(imagePath);
            System.out.printf("Prediction for %s: %d%n", imagePath, prediction);

            String[] testImages = {
                    "tasks\\term-1\\8\\app\\src\\main\\resources\\test data\\six.png",
                    "tasks\\term-1\\8\\app\\src\\main\\resources\\test data\\seven.png"
            };

            for (String imgPath : testImages) {
                int imgPrediction = cnn.predict(imgPath);
                System.out.printf("Prediction for %s: %d%n", imgPath, imgPrediction);
            }
        }
        catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }
}