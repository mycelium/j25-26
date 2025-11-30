package org.example;

import java.io.File;

public class App {
    public static void main(String[] args) throws Exception {
        int batchSize = 64;
        int numEpochs = 1;

        System.out.println("---===[Loading MNIST]===---");
        
        MNISTLoader trainLoader = new MNISTLoader(
            "tasks\\term-1\\8\\app\\src\\main\\resources\\MNIST\\train-images.idx3-ubyte",
            "tasks\\term-1\\8\\app\\src\\main\\resources\\MNIST\\train-labels.idx1-ubyte"
        );
        
        MNISTLoader testLoader = new MNISTLoader(
            "tasks\\term-1\\8\\app\\src\\main\\resources\\MNIST\\t10k-images.idx3-ubyte",
            "tasks\\term-1\\8\\app\\src\\main\\resources\\MNIST\\t10k-labels.idx1-ubyte"
        );
        
        CNN model = new CNN();
        
        System.out.println("---===[Training]===---");
        for (int epoch = 0; epoch < numEpochs; epoch++) {
            System.out.println("Epoch " + (epoch + 1));
            model.fit(trainLoader, batchSize);
        }
        
        System.out.println("---===[Testing]===---");
        model.evaluate(testLoader);
        
        double[] testImage = testLoader.getImage(0); 
        int actualLabel = testLoader.getLabel(0);
        int predicted = model.predict(testImage);
        
        System.out.println("---===[First value prediction]===---");
        System.out.println("Predicted: " + predicted + ", Actual: " + actualLabel);

        String testImagePath = "tasks\\term-1\\8\\app\\src\\main\\resources\\testExamples\\3.png";
        File testImageFile = new java.io.File(testImagePath);
        
        System.out.println("---===[Image prediction]===---");
        if (testImageFile.exists()) {
            try {
                int testImagePrediction = model.predictFromImage(testImagePath);
                System.out.println("Test image: " + testImageFile.getName() + ", predicted digit: " + testImagePrediction);
            } catch (Exception e) {
                System.out.println("Error processing test image: " + e.getMessage());
            }
        } else {
            System.out.println("Test image not found: " + testImagePath);
        }
    }
}