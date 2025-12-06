package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.evaluation.classification.Evaluation;

public class App {
    private static final String PROJECT_PATH = "tasks\\term-1\\8\\app\\src\\main\\";

    private static final String EXAMPLE_IMAGE_FORMAT = ".png";
    private static final String EXAMPLE_IMAGE_PATH = PROJECT_PATH + "resources\\test data\\";

    private static final String MNIST_PATH = PROJECT_PATH + "resources\\MNIST dataset\\";

    public static void main(String[] args) throws Exception {
        System.out.println("Loading MNIST...");
        INDArray trainImages = MNISTParser.loadImages(MNIST_PATH + "train-images.idx3-ubyte");
        INDArray trainLabels = MNISTParser.loadLabels(MNIST_PATH + "train-labels.idx1-ubyte");
        INDArray testImages = MNISTParser.loadImages(MNIST_PATH + "t10k-images.idx3-ubyte");
        INDArray testLabels = MNISTParser.loadLabels(MNIST_PATH + "t10k-labels.idx1-ubyte");

        MNISTModel model = new MNISTModel();
        model.train(trainImages, trainLabels, 64, 3);

        Evaluation eval = model.evaluate(testImages, testLabels);
        System.out.println(eval.stats());

        ImagePreprocessor preprocessor = new ImagePreprocessor();
        String[] imageExamples = {"two", "six", "seven"};
        for (String imageName : imageExamples) {
            String path = EXAMPLE_IMAGE_PATH + imageName + EXAMPLE_IMAGE_FORMAT;
            INDArray img = preprocessor.preprocess(path);
            int pred = model.predict(img);
            System.out.println("File: " + imageName + EXAMPLE_IMAGE_FORMAT + ". Prediction: " + pred);
        }
    }
}