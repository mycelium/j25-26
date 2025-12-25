package org.example;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;


public class MNISTClassifier {
    
    private static final String MODEL_PATH = "mnist-cnn-model.zip";
    private static final int IMAGE_SIZE = 28;
    
    public static MultiLayerConfiguration createModelConfig() {
        return new NeuralNetConfiguration.Builder()
                .seed(12345)
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
                        .nOut(256)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(10)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(IMAGE_SIZE, IMAGE_SIZE, 1))
                .build();
    }
    
    public static MultiLayerNetwork createModel() {
        MultiLayerConfiguration config = createModelConfig();
        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        model.addListeners(new ScoreIterationListener(100));
        
        return model;
    }

    public static MultiLayerNetwork loadModelIfExists() throws IOException {
        File modelFile = new File(MODEL_PATH);
        if (modelFile.exists()) {
            System.out.println("Loading an existing model...");
            return ModelSerializer.restoreMultiLayerNetwork(modelFile);
        }
        return null;
    }
    
    public static void saveModel(MultiLayerNetwork model) throws IOException {
        File modelFile = new File(MODEL_PATH);
        ModelSerializer.writeModel(model, modelFile, true);
        System.out.println("Model is saved in: " + MODEL_PATH);
    }
    
    public static MultiLayerNetwork trainAndEvaluate() throws Exception {
        MultiLayerNetwork model = loadModelIfExists();
        
        if (model != null) {
            System.out.println("Model has been uploaded successfully!");
            System.out.println("Network architecture:");
            System.out.println(model.summary());
            return model;
        }
        System.out.println("Creating and training a new model...");
        
        int batchSize = 64;
        int numEpochs = 1;
        
        System.out.println("Uploading data MNIST...");
        MnistDataSetIterator trainData = new MnistDataSetIterator(batchSize, true, 12345);
        MnistDataSetIterator testData = new MnistDataSetIterator(batchSize, false, 12345);
        
        model = createModel();
        
        System.out.println("Network architecture:");
        System.out.println(model.summary());
        
        System.out.println("\nBeginning of training...");
        
        for (int epoch = 1; epoch <= numEpochs; epoch++) {
            System.out.println("Era " + epoch + "/" + numEpochs);
            model.fit(trainData);
            trainData.reset();
        }
        
        System.out.println("\nEvaluating the accuracy of the model...");
        
        Evaluation eval = model.evaluate(testData);
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("EVALUATION RESULTS");
        System.out.println("=".repeat(50));
        System.out.println(eval.stats());
        System.out.printf("Precision: %.2f%%%n", eval.accuracy() * 100);
        
        saveModel(model);
        
        return model;
    }
    
    public static INDArray loadAndProcessImage(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        
        if (!imageFile.exists()) {
            System.out.println("File " + imagePath + " not found. Creating a test image...");
            return createTestDigitImage(2); 
        }
        
        BufferedImage originalImage = ImageIO.read(imageFile);
        if (originalImage == null) {
            throw new IOException("Couldn't upload image: " + imagePath);
        }
        
        BufferedImage processedImage = new BufferedImage(
            IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_BYTE_GRAY
        );
        
        Graphics2D g = processedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, IMAGE_SIZE, IMAGE_SIZE, null);
        g.dispose();
        
        INDArray input = Nd4j.create(1, 1, IMAGE_SIZE, IMAGE_SIZE);
        
        for (int y = 0; y < IMAGE_SIZE; y++) {
            for (int x = 0; x < IMAGE_SIZE; x++) {
                int pixel = processedImage.getRGB(x, y) & 0xFF; // Получаем яркость (0-255)
                double normalizedValue = pixel / 255.0;
                
                if (pixel > 128) { // Если пиксель светлый
                    normalizedValue = 0.1; // Делаем почти белым
                } else {
                    normalizedValue = 0.9; // Делаем почти черным
                }
                
                input.putScalar(new int[]{0, 0, y, x}, normalizedValue);
            }
        }
        
        return input;
    }
    
    public static int predictFromImage(MultiLayerNetwork model, String imagePath) throws Exception {
        INDArray input = loadAndProcessImage(imagePath);
        INDArray output = model.output(input);
        return output.argMax(1).getInt(0);
    }
    
    public static INDArray createTestDigitImage(int digit) {
        INDArray input = Nd4j.create(1, 1, IMAGE_SIZE, IMAGE_SIZE);
        for (int y = 0; y < IMAGE_SIZE; y++) {
            for (int x = 0; x < IMAGE_SIZE; x++) {
                double value = 0.1; // Почти белый фон
                
                switch (digit) {
                    case 0: // Круг/овал
                        if ((x-14)*(x-14)/64.0 + (y-14)*(y-14)/100.0 <= 1) {
                            value = 0.9;
                        }
                        break;
                    case 1: // Вертикальная линия
                        if (x >= 12 && x <= 16) value = 0.9;
                        break;
                    case 2: // Двойка
                        if ((y >= 5 && y <= 8 && x >= 8 && x <= 20) || // Верхняя горизонталь
                            (y >= 12 && y <= 15 && x >= 8 && x <= 20) || // Средняя горизонталь
                            (y >= 20 && y <= 23 && x >= 8 && x <= 20) || // Нижняя горизонталь
                            (x >= 18 && x <= 21 && y >= 5 && y <= 15) || // Правая верхняя вертикаль
                            (x >= 8 && x <= 11 && y >= 12 && y <= 23)) { // Левая нижняя вертикаль
                            value = 0.9;
                        }
                        break;
                    case 3: // Тройка
                        if ((y >= 5 && y <= 8 && x >= 8 && x <= 20) || // Верхняя горизонталь
                            (y >= 12 && y <= 15 && x >= 8 && x <= 20) || // Средняя горизонталь
                            (y >= 20 && y <= 23 && x >= 8 && x <= 20) || // Нижняя горизонталь
                            (x >= 18 && x <= 21 && y >= 5 && y <= 23)) { // Правая вертикаль
                            value = 0.9;
                        }
                        break;
                    default: 
                        if (Math.random() > 0.7) value = 0.9;
                }
                
                input.putScalar(new int[]{0, 0, y, x}, value);
            }
        }
        
        return input;
    }
    
    public static int predictDigit(MultiLayerNetwork model, int digit) {
        INDArray input = createTestDigitImage(digit);
        INDArray output = model.output(input);
        return output.argMax(1).getInt(0);
    }
}