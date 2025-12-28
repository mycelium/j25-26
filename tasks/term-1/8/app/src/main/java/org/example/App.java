package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class App {
    public static void main(String[] args) {
        System.out.println("HANDWRITTEN DIGIT RECOGNITION SYSTEM");
        try {
            DigitClassifier classifier = new DigitClassifier();
            classifier.trainAndEvaluate();
            classifier.demoClassification();

            if (args.length > 0) {
                String imagePath = args[0];
                System.out.println("\n=== CUSTOM IMAGE CLASSIFICATION ===");
                Integer result = classifier.classifyUserImage(imagePath);
                if (result != null) {
                    System.out.println("Final prediction: " + result);
                }
            }

        } catch (Exception e) {
            System.err.println("System error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class DigitClassifier {
    private static final int IMAGE_WIDTH = 28;
    private static final int IMAGE_HEIGHT = 28;
    private static final int CHANNELS = 1;
    private static final int NUM_CLASSES = 10;
    private MultiLayerNetwork network;
    private final Random random = new Random(42);

    private final int batchSize = 128;
    private final int trainingEpochs = 1;
    private final double learningRate = 0.01;
    private final double momentum = 0.9;

    public DigitClassifier() {
        initializeNetwork();
    }

    private void initializeNetwork() {
        System.out.println("\nInitializing Convolutional Network Architecture");

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(42)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(learningRate, momentum))
                .weightInit(WeightInit.RELU)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .gradientNormalizationThreshold(1.0)
                .l2(0.0005)
                .list()

                .layer(new ConvolutionLayer.Builder()
                        .name("conv1")
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nIn(CHANNELS)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder()
                        .name("pool1")
                        .poolingType(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())

                .layer(new ConvolutionLayer.Builder()
                        .name("conv2")
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder()
                        .name("pool2")
                        .poolingType(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())

                .layer(new ConvolutionLayer.Builder()
                        .name("conv3")
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())

                .layer(new DenseLayer.Builder()
                        .name("fc1")
                        .nOut(128)
                        .activation(Activation.RELU)
                        .dropOut(0.5)
                        .build())
                .layer(new DenseLayer.Builder()
                        .name("fc2")
                        .nOut(64)
                        .activation(Activation.RELU)
                        .dropOut(0.5)
                        .build())

                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .name("output")
                        .nOut(NUM_CLASSES)
                        .activation(Activation.SOFTMAX)
                        .build())

                .setInputType(InputType.convolutionalFlat(IMAGE_HEIGHT, IMAGE_WIDTH, CHANNELS))
                .build();

        network = new MultiLayerNetwork(config);
        network.init();
        network.setListeners(new ScoreIterationListener(50));
        System.out.println("Network architecture initialized");
    }

    public void trainAndEvaluate() throws IOException {
        DataSetIterator trainData = new MnistDataSetIterator(batchSize, true, 42);

        System.out.println("Starting training");
        for (int epoch = 1; epoch <= trainingEpochs; epoch++) {
            System.out.println("\n--- Epoch " + epoch + "/" + trainingEpochs + " ---");
            network.fit(trainData);
            trainData.reset();
            double score = network.score();
            System.out.printf("Loss function: %.4f\n", score);
        }

        saveModel();
        performComprehensiveEvaluation();
    }

    private void saveModel() {
        try {
            File modelFile = new File("digit-classifier-cnn.zip");
            ModelSerializer.writeModel(network, modelFile, true);
            System.out.println("\nModel saved: " + modelFile.getName());
        } catch (IOException e) {
            System.err.println("Error saving model: " + e.getMessage());
        }
    }

    private void performComprehensiveEvaluation() throws IOException {
        DataSetIterator testData = new MnistDataSetIterator(batchSize, false, 42);
        Evaluation evaluation = network.evaluate(testData);
        System.out.println("\nCLASSIFICATION RESULTS");
        displayMetrics(evaluation);
    }

    private void displayMetrics(Evaluation evaluation) {
        System.out.printf("Accuracy: %.2f%%\n", evaluation.accuracy() * 100);
        System.out.printf("Precision: %.4f\n", evaluation.precision());
        System.out.printf("Recall: %.4f\n", evaluation.recall());
        System.out.printf("F1-score: %.4f\n", evaluation.f1());
    }

    public void demoClassification() throws IOException {
        System.out.println("\nEXAMPLE DEMONSTRATION");

        DataSetIterator demoData = new MnistDataSetIterator(1, false, 42);
        INDArray sample = demoData.next().getFeatures();
        INDArray output = network.output(sample);
        int predicted = output.argMax(1).getInt(0);
        double confidence = output.getDouble(predicted);

        System.out.printf("Prediction: digit %d (confidence: %.2f%%)\n", predicted, confidence * 100);
        System.out.println("Probability distribution:");
        for (int i = 0; i < NUM_CLASSES; i++) {
            System.out.printf("  %d: %.3f%%\n", i, output.getDouble(i) * 100);
        }
    }

    public Integer classifyUserImage(String imagePath) {
        try {
            INDArray processedImage = preprocessCustomImage(imagePath);
            INDArray output = network.output(processedImage);
            int predicted = output.argMax(1).getInt(0);
            double confidence = output.getDouble(predicted);

            System.out.printf("Image processed: %s\n", imagePath);
            System.out.printf("Prediction: digit %d (confidence: %.2f%%)\n", predicted, confidence * 100);

            System.out.println("Probability distribution:");
            for (int i = 0; i < NUM_CLASSES; i++) {
                System.out.printf("  %d: %.3f%%\n", i, output.getDouble(i) * 100);
            }

            return predicted;
        } catch (Exception e) {
            System.err.println("Image processing error: " + e.getMessage());
            return null;
        }
    }

    private INDArray preprocessCustomImage(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IOException("Image file not found: " + imagePath);
        }

        BufferedImage image = ImageIO.read(imageFile);
        BufferedImage processedImage = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        java.awt.Graphics2D g = processedImage.createGraphics();
        g.drawImage(image, 0, 0, 28, 28, null);
        g.dispose();

        INDArray array = Nd4j.create(1, 28, 28);
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int pixel = processedImage.getRaster().getSample(x, y, 0) & 0xFF;
                double normalizedPixel = (255 - pixel) / 255.0;
                array.putScalar(new int[]{0, y, x}, normalizedPixel);
            }
        }

        return array.reshape(1, 1, 28, 28);
    }
}