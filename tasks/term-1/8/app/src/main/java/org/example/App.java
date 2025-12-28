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
import java.awt.*;
import java.awt.Color;
import java.awt.Graphics2D;

public class App {

    public static void main(String[] args) throws Exception {
        int batchSize = 64;
        int rngSeed = 123;
        int numEpochs = 1;

        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
        DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);

        MultiLayerNetwork model = buildCNN();
        System.out.println("start learning...");
        for (int i = 0; i < numEpochs; i++) {
            model.fit(mnistTrain);
        }
        System.out.println("end learning!");


        Evaluation eval = model.evaluate(mnistTest);
        System.out.println(eval.stats());
        if (args.length > 0) {
            String imagePath = args[0];
            int predicted = classifyImage(model, imagePath);
            System.out.println("Predicted digit for " + imagePath + ": " + predicted);
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
        File file = new File(imagePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file.getAbsolutePath());
        }

        BufferedImage original = ImageIO.read(file);
        if (original == null) {
            throw new IllegalArgumentException("ImageIO could not decode the image. Possible cause: unsupported PNG format (e.g., RGBA).");
        }

        BufferedImage img;
        if (original.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = original;
        } else {
            img = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 28, 28);
            g2d.drawImage(original, 0, 0, null);
            g2d.dispose();
        }

        if (img.getWidth() != 28 || img.getHeight() != 28) {
            throw new IllegalArgumentException("Expected 28x28 image");
        }

        double[] pixels = new double[28 * 28];
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int gray = img.getRaster().getSample(x, y, 0);
                pixels[y * 28 + x] = 1.0 - (gray / 255.0);
            }
        }

        INDArray input = Nd4j.create(pixels).reshape(1, 28 * 28);
        INDArray output = model.output(input);
        return Nd4j.argMax(output, 1).getInt(0);
    }

    public String getGreeting() {
        return "Hello from App!";
    }
}