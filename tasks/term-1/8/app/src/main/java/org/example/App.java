package org.example;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class App {
    public static void main(String[] args) throws IOException {
        int batchCount = 64;
        int epochCount = 1;
        int randomSeed = 123;
        
        System.out.println("Loading MNIST dataset");
        
        DataSetIterator trainingSet = new MnistDataSetIterator(batchCount, true, randomSeed);
        DataSetIterator testingSet = new MnistDataSetIterator(batchCount, false, randomSeed);
        
        System.out.println("Creating CNN architecture");
        
        MultiLayerConfiguration networkConfig = new NeuralNetConfiguration.Builder()
            .seed(randomSeed)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(0.001))
            .list()
            .layer(new ConvolutionLayer.Builder(5, 5)
                .nIn(1)
                .stride(1, 1)
                .nOut(20)
                .activation(Activation.RELU)
                .build())
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            .layer(new ConvolutionLayer.Builder(5, 5)
                .stride(1, 1)
                .nOut(50)
                .activation(Activation.RELU)
                .build())
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            .layer(new DenseLayer.Builder()
                .activation(Activation.RELU)
                .nOut(500)
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(10)
                .activation(Activation.SOFTMAX)
                .build())
            .setInputType(InputType.convolutionalFlat(28, 28, 1))
            .build();
        
        MultiLayerNetwork networkModel = new MultiLayerNetwork(networkConfig);
        networkModel.init();
        
        System.out.println("Model parameters: " + networkModel.numParams());
        
        networkModel.setListeners(new ScoreIterationListener(100));
        
        System.out.println("Starting training");
        
        for (int currentEpoch = 0; currentEpoch < epochCount; currentEpoch++) {
            System.out.println("Epoch " + (currentEpoch + 1) + "/" + epochCount);
            networkModel.fit(trainingSet);
            trainingSet.reset();
        }
        
        System.out.println("Evaluating model on test data");
        
        var evaluationResult = networkModel.evaluate(testingSet);
        
        System.out.println("\nResults");
        System.out.println("Accuracy: " + String.format("%.2f", evaluationResult.accuracy() * 100) + "%");
        System.out.println("Precision: " + evaluationResult.precision());
        System.out.println("Recall: " + evaluationResult.recall());
        System.out.println("F1 Score: " + evaluationResult.f1());
        System.out.println("Confusion matrix:");
        System.out.println(evaluationResult.confusionMatrix());
        
        System.out.println("\nPredictions for test images");
        testingSet.reset();
        var testBatch = testingSet.next();
        var modelOutputs = networkModel.output(testBatch.getFeatures());
        
        for (int imageIndex = 0; imageIndex < Math.min(5, batchCount); imageIndex++) {
            int predictedValue = modelOutputs.argMax(1).getInt(imageIndex);
            int actualValue = testBatch.getLabels().argMax(1).getInt(imageIndex);
            System.out.println("Image " + (imageIndex + 1) + " predicted " + predictedValue + " actual " + actualValue);
        }
        
        System.out.println("\nCustom Image Prediction");
        
        Path imageLocation = Paths.get("2.png");
        
        try {
            INDArray imageData = loadAndProcessImage(imageLocation);
            INDArray predictionResult = networkModel.output(imageData);
            int predictedNumber = predictionResult.argMax(1).getInt(0);
            double predictionConfidence = predictionResult.getDouble(0, predictedNumber);
            
            System.out.println("\nPredicted : " + predictedNumber);
            System.out.println("Actual : 2");
            System.out.println("Confidence: " + String.format("%.2f", predictionConfidence * 100) + "%");
            System.out.println("\nDigit probabilities:");
            for (int digit = 0; digit < 10; digit++) {
                double probability = predictionResult.getDouble(0, digit) * 100;
                System.out.println("Digit " + digit + ": " + String.format("%.2f", probability) + "%");
            }
          
        } catch (Exception error) {
            System.err.println("Error processing custom image: " + error.getMessage());
            System.err.println("Image location: " + imageLocation.toAbsolutePath());
            System.err.println("Image must be 28x28 grayscale with white digit on black background");
        }
        
    }
    
    private static INDArray loadAndProcessImage(Path imageLocation) throws IOException {
        if (!Files.exists(imageLocation)) {
            throw new IOException("File not found: " + imageLocation.toAbsolutePath());
        }
        
        if (!Files.isReadable(imageLocation)) {
            throw new IOException("File is not readable: " + imageLocation.toAbsolutePath());
        }
        
        BufferedImage inputImage = ImageIO.read(imageLocation.toFile());
        
        if (inputImage == null) {
            throw new IOException("Cannot read image file: " + imageLocation.toAbsolutePath());
        }
        
        if (inputImage.getWidth() != 28 || inputImage.getHeight() != 28) {
            BufferedImage resizedImage = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
            resizedImage.getGraphics().drawImage(inputImage, 0, 0, 28, 28, null);
            inputImage = resizedImage;
        }
        
        float[] pixelValues = new float[28 * 28];
        for (int row = 0; row < 28; row++) {
            for (int col = 0; col < 28; col++) {
                int rgb = inputImage.getRGB(col, row);
                
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                float grayValue = (red + green + blue) / 3.0f;
                
                pixelValues[row * 28 + col] = grayValue / 255.0f;
            }
        }
        
        return Nd4j.create(pixelValues, new long[]{1, 1, 28, 28});
    }
}