package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.File;

public class App {
    public static void main(String[] args) throws Exception {
        int batchSize = 64;
        int numEpochs = 1;


        DataSetIterator trainIter = new MnistDataSetIterator(batchSize, true, 12345);
        DataSetIterator testIter = new MnistDataSetIterator(batchSize, false, 12345);

        DigitClassifier model = new DigitClassifier();

        System.out.println("Training");
        model.fit(trainIter, numEpochs);

        System.out.println("Testing");
        model.evaluate(testIter);

        // Путь к изображению"C:\Users\kozlo\Desktop\laaab8\app\src\main\java\resources"
        String testImagePath = "app/src/main/resources/7.png";
        File testImageFile = new File(testImagePath);

        System.out.println("Image prediction");
        if (testImageFile.exists()) {
            try {
                int testImagePrediction = model.predictFromImage(testImagePath);
                System.out.println("Test image: " + testImageFile.getName() + ", predicted digit: " + testImagePrediction);
            } catch (Exception e) {
                System.out.println("Error processing test image: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Test image not found: " + testImagePath);
        }
    }
}
