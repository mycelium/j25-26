package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App {

    public static void main(String[] args) throws Exception {
        int batchSize = 64;
        int rngSeed = 123;
        int numEpochs = 1;

        // Загружаем MNIST
        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
        DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);

        // Создаём CNN
        MultiLayerNetwork model = buildCNN();
        System.out.println("start learning...");
        for (int i = 0; i < numEpochs; i++) {
            model.fit(mnistTrain);
        }
        System.out.println("end learning!");

        Evaluation eval = model.evaluate(mnistTest);
        System.out.println(eval.stats());
        
        String imagesFolder = "tasks/term-1/8/imag";
        File folder = new File(imagesFolder);
        
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Папка с изображениями не найдена: " + imagesFolder);
            return;
        }
        
        File[] imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        
        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("В папке " + imagesFolder + " не найдено PNG файлов");
            return;
        }
        
        System.out.println("Найдено " + imageFiles.length + " изображений для классификации:");
        
        for (File imageFile : imageFiles) {
            try {
                int predicted = classifyImage(model, imageFile.getPath());
                System.out.println("Предсказанная цифра для " + imageFile.getName() + ": " + predicted);
            } catch (Exception e) {
                System.out.println("Ошибка при обработке " + imageFile.getName() + ": " + e.getMessage());
            }
        }
    }

    public static MultiLayerNetwork buildCNN() {
        int channels = 1;
        int outputNum = 10;
        int seed = 123;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new org.nd4j.linalg.learning.config.Adam(0.001))
                .list()
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .nIn(channels)
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
                .layer(new DenseLayer.Builder().activation(Activation.RELU)
                        .nOut(500).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(outputNum)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(org.deeplearning4j.nn.conf.inputs.InputType.convolutionalFlat(28, 28, channels))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        return model;
    }

    public static int classifyImage(MultiLayerNetwork model, String imagePath) throws Exception {
        BufferedImage img = ImageIO.read(new File(imagePath));
        if (img.getWidth() != 28 || img.getHeight() != 28) {
            // Масштабируем изображение до 28x28 если оно другого размера
            BufferedImage resizedImg = new BufferedImage(28, 28, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = resizedImg.createGraphics();
            g.drawImage(img, 0, 0, 28, 28, null);
            g.dispose();
            img = resizedImg;
        }

        double[] pixels = new double[28 * 28];
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int rgb = img.getRGB(x, y);
                // Получаем среднее значение RGB
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3;
                
                pixels[y * 28 + x] = (255 - gray) / 255.0;
            }
        }
        
        INDArray input = Nd4j.create(pixels, new int[]{1, 1, 28, 28});

        INDArray output = model.output(input);
        return Nd4j.argMax(output, 1).getInt(0);
    }
    
    public String getGreeting() {
        return "Hello from App!";
    }
}
