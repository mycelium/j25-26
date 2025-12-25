package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class App {

    private static final int HEIGHT = 28;
    private static final int WIDTH = 28;
    private static final int CHANNELS = 1;
    private static final int NUM_CLASSES = 10;
    private static final int BATCH_SIZE = 64;
    private static final int EPOCHS = 3;
    private static final double LEARNING_RATE = 1e-3;
    private static final int SEED = 123;

    private static MultiLayerNetwork model;
    private static final ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);

    public static void main(String[] args) {
        try {
            
            System.out.println("MNIST Digit Classifier using CNN (DeepLearning4j)");
            
            trainModel();
            evaluateModel();
            demoPredictions();

            if (args.length > 0) {
                classifyExternalImage(args[0]);
            }

            System.out.println("\nProgram finished successfully.");

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void trainModel() throws IOException {
        System.out.println("\n[1/4] Loading MNIST training dataset...");

        DataSetIterator trainIter = new MnistDataSetIterator(BATCH_SIZE, true, SEED);
        scaler.fit(trainIter);
        trainIter.setPreProcessor(scaler);

        System.out.println("[1/4] Configuring Convolutional Neural Network...");

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(LEARNING_RATE))
                .weightInit(WeightInit.XAVIER)
                .l2(1e-4)
                .list()
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .nIn(CHANNELS).nOut(32)
                        .activation(Activation.RELU).build())
                .layer(new SubsamplingLayer.Builder()
                        .poolingType(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2).stride(2, 2).build())
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .nOut(64)
                        .activation(Activation.RELU).build())
                .layer(new SubsamplingLayer.Builder()
                        .poolingType(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2).stride(2, 2).build())
                .layer(new DenseLayer.Builder()
                        .nOut(1024)
                        .activation(Activation.RELU)
                        .dropOut(0.5).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(NUM_CLASSES)
                        .activation(Activation.SOFTMAX).build())
                .setInputType(InputType.convolutionalFlat(HEIGHT, WIDTH, CHANNELS))
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));

        System.out.println("[1/4] Training model...");
        for (int epoch = 1; epoch <= EPOCHS; epoch++) {
            model.fit(trainIter);
            trainIter.reset();
            System.out.println("  Completed epoch " + epoch + " of " + EPOCHS);
        }
        System.out.println("Training completed successfully.");
    }

    private static void evaluateModel() throws IOException {
        System.out.println("\n[2/4] Evaluating model accuracy on test set...");

        DataSetIterator testIter = new MnistDataSetIterator(BATCH_SIZE, false, SEED);
        testIter.setPreProcessor(scaler);

        Evaluation eval = model.evaluate(testIter);
        System.out.println("\nEvaluation Results:");
        System.out.printf("Overall accuracy: %.2f%%\n", eval.accuracy() * 100);
        System.out.println(eval.stats());
    }

    private static void demoPredictions() throws IOException {
        System.out.println("\n[3/4] Demo: predictions on 5 test examples...");

        DataSetIterator demoIter = new MnistDataSetIterator(1, false, SEED);
        demoIter.setPreProcessor(scaler);

        for (int i = 0; i < 5 && demoIter.hasNext(); i++) {
            var batch = demoIter.next();
            INDArray features = batch.getFeatures();
            INDArray labels = batch.getLabels();

            INDArray output = model.output(features);
            int predicted = Nd4j.argMax(output, 1).getInt(0);
            int actual = Nd4j.argMax(labels, 1).getInt(0);
            double confidence = output.getDouble(0, predicted);

            System.out.printf("Example %d: true label = %d, prediction = %d, confidence = %.1f%%, correct = %s\n",
                    i + 1, actual, predicted, confidence * 100,
                    (predicted == actual) ? "yes" : "no");
        }
    }

    private static void classifyExternalImage(String imagePath) {
        System.out.println("\n[4/4] Classifying user-provided image: " + imagePath);
        try {
            INDArray image = preprocessImage(imagePath);
            INDArray output = model.output(image);
            int predicted = Nd4j.argMax(output, 1).getInt(0);
            double confidence = output.getDouble(0, predicted);

            System.out.println("\nClassification result:");
            System.out.println("Predicted digit: " + predicted);
            System.out.printf("Model confidence: %.2f%%\n", confidence * 100);

        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }

    private static INDArray preprocessImage(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("File not found: " + path);
        }

        BufferedImage original = ImageIO.read(file);
        BufferedImage gray = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, WIDTH, HEIGHT, null);
        g.dispose();

        INDArray array = Nd4j.create(1, HEIGHT, WIDTH);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int pixel = gray.getRaster().getSample(x, y, 0) & 0xFF;
                array.putScalar(new int[]{0, y, x}, pixel / 255.0);
            }
        }

        scaler.transform(array);
        return array.reshape(1, CHANNELS, HEIGHT, WIDTH);
    }
}