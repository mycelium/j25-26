package org.example;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.nn.conf.inputs.InputType;

import java.io.File;
import java.io.IOException;

public class ImageClassifier {
    private MultiLayerNetwork model;
    private MultiLayerConfiguration createNetworkConfig() {
        return new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .list()
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .nIn(1)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(
                        SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .stride(1, 1)
                        .nOut(50)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(
                        SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new DenseLayer.Builder()
                        .activation(Activation.RELU)
                        .nOut(500)
                        .build())
                .layer(new OutputLayer.Builder(
                        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(10)
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();
    }
    public void trainModel() throws IOException {
        MultiLayerConfiguration config = createNetworkConfig();
        model = new MultiLayerNetwork(config);
        model.init();
        model.setListeners(new ScoreIterationListener(100));
        int batchSize = 64;
        int numEpochs = 5;
        DataSetIterator train = new MnistDataSetIterator(batchSize, true, 123);
        DataSetIterator test = new MnistDataSetIterator(batchSize, false, 123);
        for (int i = 0; i < numEpochs; i++) {
            model.fit(train);
            train.reset();
        }
    }
    public void saveModel(String path) throws IOException {
        ModelSerializer.writeModel(model, new File(path), true);
        System.out.println("Model is saved: " + path);
    }
    public void loadModel(String filePath) throws IOException {
        model = ModelSerializer.restoreMultiLayerNetwork(new File(filePath));
    }
    public void recognizeFromFile(ImageClassifier recognizer, String filePath) {
        try {
            INDArray image = ImageReader.readImage(filePath);
            INDArray output = recognizer.model.output(image);
            int predictedDigit = Nd4j.argMax(output, 1).getInt(0);
            double confidence = output.getDouble(0, predictedDigit);
            System.out.println("Number: " + predictedDigit);
            System.out.printf("Precision: %.2f%%\n", confidence * 100);
            System.out.println("All probabilities:");
            for (int i = 0; i < 10; i++) {
                double prob = output.getDouble(0, i) * 100;
                System.out.printf("  %d: %.2f%%\n", i, prob);
            }
            System.out.printf("\n");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
