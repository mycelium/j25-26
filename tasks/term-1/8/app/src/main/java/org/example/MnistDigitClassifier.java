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
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MnistDigitClassifier
{
    private static final int NUM_CLASSES = 10;
    private static final int SEED = 123;
    private static final String MODEL_FILE = "mnist-cnn-model.zip";
    private static final int BATCH_SIZE = 128;
    private static final int EPOCHS = 5;
    private static final double LEARNING_RATE = 0.01;
    private static final double MOMENTUM = 0.9;
    private static final double L2_REGULARIZATION = 0.0005;
    private MultiLayerNetwork model;

    public void loadOrTrainModel() throws IOException
    {
        File modelLocation = new File(MODEL_FILE);

        if (!modelLocation.exists())
        {
            System.out.println("Model not found. Starting learning...");
            model = trainModel();
            ModelSerializer.writeModel(model, modelLocation, true);
            System.out.println("Model saved to " + MODEL_FILE);
        }
        else
        {
            System.out.println("Loading existing model...");
            model = ModelSerializer.restoreMultiLayerNetwork(modelLocation);
        }
    }

    public void printStats() throws IOException
    {
        if (model == null)
        {
            System.out.println("Error: Model is not initialized. Call loadOrTrainModel() first.");
            return;
        }

        DataSetIterator testData = new MnistDataSetIterator(BATCH_SIZE, false, SEED);
        testData.setPreProcessor(new ImagePreProcessingScaler(0, 1));
        Evaluation eval = model.evaluate(testData);

        System.out.println("\n=== MODEL EVALUATION RESULTS ===");
        System.out.printf("Accuracy: %.2f%%\n", eval.accuracy() * 100);
        System.out.printf("Precision: %.4f\n", eval.precision());
        System.out.printf("Recall: %.4f\n", eval.recall());
        System.out.printf("F1-score: %.4f\n", eval.f1());
        System.out.println("=================================");
    }

    private MultiLayerNetwork trainModel() throws IOException
    {
        DataSetIterator trainData = new MnistDataSetIterator(BATCH_SIZE, true, SEED);
        DataSetIterator testData = new MnistDataSetIterator(BATCH_SIZE, false, SEED);
        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
        trainData.setPreProcessor(scaler);
        testData.setPreProcessor(scaler);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(LEARNING_RATE, MOMENTUM))
                .weightInit(WeightInit.RELU)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .gradientNormalizationThreshold(1.0)
                .l2(L2_REGULARIZATION)
                .list()
                // First layer
                .layer(new ConvolutionLayer.Builder(3, 3)
                        .name("conv1")
                        .stride(1, 1)
                        .padding(1, 1)
                        .nIn(1)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder()
                        .name("pool1")
                        .poolingType(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                // Second layer
                .layer(new ConvolutionLayer.Builder(3, 3)
                        .name("conv2")
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
                // Third layer
                .layer(new ConvolutionLayer.Builder(3, 3)
                        .name("conv3")
                        .stride(1, 1)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                // Fully connected layers with dropout
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
                // Output layer
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .name("output")
                        .nOut(NUM_CLASSES)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(50));

        for (int epoch = 1; epoch <= EPOCHS; epoch++)
        {
            model.fit(trainData);
            trainData.reset();
            double score = model.score();
            System.out.printf("Loss: %.4f\n", score);
        }

        System.out.println("Training completed!");
        System.out.println("Final Evaluation after training:");
        printStats();

        return model;
    }

    public int predictDigit(String imagePath) throws IOException
    {
        if (model == null)
        {
            loadOrTrainModel();
        }

        INDArray input = preprocessCustomImage(imagePath);
        INDArray output = model.output(input);

        System.out.println("\nPrediction probabilities:");

        for (int i = 0; i < NUM_CLASSES; i++)
        {
            double prob = output.getDouble(0, i) * 100;
            System.out.printf("  Digit %d: %.2f%%%n", i, prob);
        }

        int predicted = output.argMax(1).getInt(0);
        double confidence = output.getDouble(0, predicted) * 100;
        System.out.printf("\nPredicted digit: %d (confidence: %.1f%%)\n", predicted, confidence);

        return predicted;
    }

    private INDArray preprocessCustomImage(String imagePath) throws IOException
    {
        File imageFile = new File(imagePath);
        if (!imageFile.exists())
        {
            throw new IOException("File not found: " + imagePath);
        }

        BufferedImage image = ImageIO.read(imageFile);
        if (image == null)
        {
            throw new IOException("Cannot read image: " + imagePath);
        }

        BufferedImage processedImage = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = processedImage.createGraphics();
        g.drawImage(image, 0, 0, 28, 28, null);
        g.dispose();

        INDArray array = Nd4j.create(1, 28, 28);
        for (int y = 0; y < 28; y++)
        {
            for (int x = 0; x < 28; x++)
            {
                int pixel = processedImage.getRaster().getSample(x, y, 0) & 0xFF;
                double normalizedPixel = (255 - pixel) / 255.0;
                array.putScalar(new int[]{0, y, x}, normalizedPixel);
            }
        }
        return array.reshape(1, 1, 28, 28);
    }
}