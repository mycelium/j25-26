package org.example;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;

public class DigitClassifier {

    private MultiLayerNetwork network;

    public DigitClassifier() {
        constructNetworkArchitecture();
    }

    private void constructNetworkArchitecture() {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(12345L)
                .weightInit(WeightInit.RELU)
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
                        .nIn(20)
                        .stride(1, 1)
                        .nOut(50)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nOut(500)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder()
                        .nOut(10)
                        .activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .build();

        network = new MultiLayerNetwork(config);
        network.init();
        System.out.println("---===[Neural network initialized]===---");
    }

    public void fit(DataSetIterator trainingData, int epochs) {
        for (int e = 0; e < epochs; e++) {
            System.out.println("Starting epoch " + (e + 1));
            network.fit(trainingData);
            trainingData.reset();
        }
    }

    public int predict(double[] input) {
        INDArray inputTensor = Nd4j.create(1, 1, 28, 28);
        for (int i = 0; i < 784; i++) {
            inputTensor.putScalar(0, 0, i / 28, i % 28, input[i]);
        }
        INDArray probabilities = network.output(inputTensor);
        return Nd4j.argMax(probabilities, 1).getInt(0);
    }

    public void evaluate(DataSetIterator testData) {
        Evaluation metrics = new Evaluation(10);
        while (testData.hasNext()) {
            var batch = testData.next();
            INDArray predictions = network.output(batch.getFeatures());
            metrics.eval(batch.getLabels(), predictions);
        }
        System.out.println(metrics.stats());
    }

    public int predictFromImage(String imagePath) throws IOException {
        double[] preprocessed = MnistImageLoader.loadAndProcessImage(imagePath);
        return predict(preprocessed);
    }
}