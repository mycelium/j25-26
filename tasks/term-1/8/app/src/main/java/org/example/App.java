package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class App {

    public static void main(String[] args) throws Exception {
        int batchSize = 128;
        int numEpochs = 1;
        int seed = 42;

        DataSetIterator trainIter = new MnistDataSetIterator(batchSize, true, seed);
        DataSetIterator testIter = new MnistDataSetIterator(batchSize, false, seed);

        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(trainIter);
        trainIter.setPreProcessor(scaler);
        testIter.setPreProcessor(scaler);

        MultiLayerNetwork model = buildCNN();
        System.out.println("learning begin");
        for (int i = 0; i < numEpochs; i++) {
            model.fit(trainIter);
            trainIter.reset();
        }
        System.out.println("learning end.");

        Evaluation eval = model.evaluate(testIter);
        System.out.println(eval.stats());

        if (args.length > 0) {
            int predicted = predictDigit(model, args[0]);
            System.out.println("predicted digit: " + predicted);
        }
    }

    public static MultiLayerNetwork buildCNN() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.RELU)
                .updater(new org.nd4j.linalg.learning.config.Adam(0.001))
                .list()
                .layer(new ConvolutionLayer.Builder(3, 3)
                        .nIn(1)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new ConvolutionLayer.Builder(3, 3)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new ConvolutionLayer.Builder(3, 3)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DropoutLayer(0.5))
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(10)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(org.deeplearning4j.nn.conf.inputs.InputType.convolutionalFlat(28, 28, 1))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        return model;
    }

    public static int predictDigit(MultiLayerNetwork model, String imagePath) throws Exception {
        BufferedImage img = ImageIO.read(new File(imagePath));

        if (img.getWidth() != 28 || img.getHeight() != 28) {
            throw new IllegalArgumentException("image must be 28x28.");
        }

        double[] pixels = new double[28 * 28];
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int rgb = img.getRGB(x, y);
                int gray = (int) (0.299 * ((rgb >> 16) & 0xFF) +
                        0.587 * ((rgb >> 8) & 0xFF) +
                        0.114 * (rgb & 0xFF));
                pixels[y * 28 + x] = gray / 255.0;
            }
        }
        INDArray input = Nd4j.create(pixels).reshape(1, 28 * 28);
        INDArray output = model.output(input);
        return Nd4j.argMax(output, 1).getInt(0);
    }
}