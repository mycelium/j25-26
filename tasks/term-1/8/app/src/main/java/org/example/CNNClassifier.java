package org.example;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;

import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.util.List;

public class CNNClassifier {

    private MultiLayerNetwork model;

    public CNNClassifier() {
        initializeModel();
    }

    private void initializeModel() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(67)
                .weightInit(WeightInit.SIGMOID_UNIFORM)
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
                        .activation(Activation.SOFTMAX)
                        .nOut(10)
                        .build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
    }

    public void train(MNISTLoader loader) {
        List<double[]> mnistImages = loader.getAllImages();
        List<Integer> labels = loader.getAllLabels();
        
        System.out.println("Starting model training...");
        for (int epoch = 0; epoch < 30; epoch++) {
            trainData.shuffle();
            model.fit(trainData);
            System.out.println("Epoch " + (epoch + 1) + " completed.");
        }
        System.out.println("Training is ended.");
    }

    public double evaluate(DataSet testData) {
        INDArray predictions = model.output(testData.getFeatures());
        INDArray labels = testData.getLabels();

        int correct = 0;
        int total = (int) labels.size(0);

        for (int i = 0; i < total; i++) {
            int predictedLabel = getPredictedLabel(predictions.getRow(i));
            int actualLabel = getActualLabel(labels.getRow(i));

            if (predictedLabel == actualLabel) {
                correct++;
            }
        }

        return (double) correct / total;
    }

    public int predict(String imagePath) throws IOException {
        ImageProcessor processor = new ImageProcessor();
        INDArray image = processor.loadAndPreprocessImage(imagePath);

        INDArray output = model.output(image);
        return getPredictedLabel(output);
    }

    private int getPredictedLabel(INDArray output) {
        double maxVal = Double.MIN_VALUE;
        int maxIdx = 0;

        for (int i = 0; i < output.length(); i++) {
            if (output.getDouble(i) > maxVal) {
                maxVal = output.getDouble(i);
                maxIdx = i;
            }
        }

        return maxIdx;
    }

    private int getActualLabel(INDArray label) {
        for (int i = 0; i < label.length(); i++) {
            if (label.getDouble(i) == 1.0) {
                return i;
            }
        }
        return -1;
    }

}