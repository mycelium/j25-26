package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.InputStream;

public class App {
    private static final int BATCH_SIZE = 64;
    private static final int SEED = 123;

    private static final String TARGET_RESOURCE = "test data/two.png";

    public static void main(String[] args) {
        try {
            System.out.println(">>> Initializing MNIST datasets");
            DataSetIterator trainIter = new MnistDataSetIterator(BATCH_SIZE, true, SEED);
            DataSetIterator testIter = new MnistDataSetIterator(BATCH_SIZE, false, SEED);

            System.out.println(">>> Configuring LeNet-5 Architecture...");
            CNN modelFactory = new CNN();
            MultiLayerNetwork engine = modelFactory.setupStructure();

            System.out.println(">>> Training phase started");
            engine.fit(trainIter);

            System.out.println(">>> Running evaluation...");
            Evaluation eval = engine.evaluate(testIter);
            System.out.println(eval.stats());

            processExternalImage(engine);

        } catch (Exception ex) {
            System.err.println("Fatal error during execution: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void processExternalImage(MultiLayerNetwork model) throws Exception {
        System.out.println("\n>>> Inference for custom resource: " + TARGET_RESOURCE);

        ClassLoader loader = App.class.getClassLoader();
        try (InputStream rawStream = loader.getResourceAsStream(TARGET_RESOURCE)) {
            if (rawStream == null) {
                throw new java.io.FileNotFoundException("Resource not found in classpath: " + TARGET_RESOURCE);
            }

            ImagePreprocessor handler = new ImagePreprocessor(28, 28);
            INDArray inputTensor = handler.transformStream(rawStream);

            INDArray outputRaw = model.output(inputTensor);
            int predictedClass = outputRaw.argMax(1).getInt(0);
            double score = outputRaw.getDouble(0, predictedClass) * 100.0;

            System.out.printf("Inference Result: Digit %d (Confidence: %.2f%%)%n", predictedClass, score);
        }
    }
}