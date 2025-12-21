package ru.cnn;

import java.io.IOException;

public class App {
    public static void main(String[] args) {
        int batchSize = 64;
        int numEpochs = 1;
        double learningRate = 0.001;

        try {
            DatasetLoader trainLoader = new DatasetLoader(
                    "src/main/resources/train-images.idx3-ubyte",
                    "src/main/resources/train-labels.idx1-ubyte"
            );
            DatasetLoader testLoader = new DatasetLoader(
                    "src/main/resources/t10k-images.idx3-ubyte",
                    "src/main/resources/t10k-labels.idx1-ubyte"
            );


            CNNModule model = new CNNModule(learningRate);

            System.out.println("===== Training =====");
            for (int epoch = 0; epoch < numEpochs; epoch++) {
                System.out.println("Epoch " + (epoch + 1));
                model.fit(trainLoader, batchSize);
            }

            System.out.println("===== Evaluating =====");
            model.evaluate(testLoader);

        } catch (IOException e) {
            System.out.println("Was not able to load datasets.");
        }
    }
}