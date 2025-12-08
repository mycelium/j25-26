package org.example;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.Pooling2D;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.List;


public class CNNModule {
    private MultiLayerNetwork model;
    private static final int numClasses = 10;
    private static final int inputHeight = 28;
    private static final int inputWidth = 28;
    private static final int channels = 1;

    public CNNModule(double learningRate) {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .weightInit(WeightInit.RELU)
                .updater(new Adam(learningRate))
                .list()
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        .nIn(channels)
                        .nOut(20)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new Pooling2D.Builder(2, 2).build())
                .layer(2, new ConvolutionLayer.Builder(5, 5)
                        .nOut(50)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new Pooling2D.Builder(2, 2).build())
                .layer(4, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nOut(numClasses)
                        .build())
                .setInputType(InputType.convolutional(inputHeight, inputWidth, channels))
                .build();

        model = new MultiLayerNetwork(config);
        model.init();
    }

    public void fit(DatasetLoader loader, int batchSize) {
        List<double[]> images = loader.getImages();
        List<Integer> labels = loader.getLabels();

        for (int i = 0; i < images.size(); i += batchSize) {
            int end = Math.min(i + batchSize, images.size());
            int correctedBatchSize = end - i;

            INDArray features = Nd4j.create(correctedBatchSize, channels, inputHeight, inputWidth);
            INDArray labelFeatures = Nd4j.create(correctedBatchSize, 10);

            for (int j = 0; j < correctedBatchSize; j++) {
                int idx = i + j;
                double[] image = images.get(idx);
                int label = labels.get(idx);

                for (int k = 0; k < image.length; k++) {
                    features.putScalar(new int[]{j, 0, k/inputHeight, k%inputWidth}, image[k]);
                }

                labelFeatures.putScalar(new int[]{j, label}, 1.0);
            }

            model.fit(new DataSet(features, labelFeatures));
        }
    }

    public int predict(double[] image) {
        INDArray features = Nd4j.create(1, channels, inputHeight, inputWidth);
        for (int i = 0; i < image.length; i++) {
            features.putScalar(new int[]{0, 0, i/inputHeight, i%inputWidth}, image[i]);
        }

        INDArray output = model.output(features);
        return output.argMax(1).getInt(0);
    }

    public void evaluate(DatasetLoader loader) {
        List<double[]> images = loader.getImages();
        List<Integer> labels = loader.getLabels();

        if (images.isEmpty()) return;

        int[] accPerNum = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] totalPerNum = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        int correct = 0;
        for (int i = 0; i < images.size(); i++) {
            int predictedLabel = predict(images.get(i));
            int actualLabel = labels.get(i);
            totalPerNum[actualLabel] += 1;
            if (predictedLabel == actualLabel) {
                correct++;
                accPerNum[actualLabel] += 1;
            }
        }

        double accuracy = (double) correct / images.size();
        System.out.println("Accuracy: "
                + String.format("%.2f", (accuracy * 100)) + "%");

        System.out.println("Accuracy per number: ");
        for (int i = 0; i < 10; i++) {
            if (totalPerNum[i] == 0) continue;
            System.out.println(i+1
                    + ": "
                    + String.format("%.2f", (((double) accPerNum[i]/totalPerNum[i]) * 100))
                    + "%"
            );
        }
    }

}