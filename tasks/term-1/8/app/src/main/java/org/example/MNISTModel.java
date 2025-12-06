package org.example;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;

import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class MNISTModel {

    private final MultiLayerNetwork model;
    private static final int NUM_CLASSES = 10;

    public MNISTModel() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(6767)
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

        this.model = new MultiLayerNetwork(conf);
        this.model.init();
        this.model.setListeners(new ScoreIterationListener(250));
    }

    public void train(INDArray trainImages, INDArray trainLabels, int batchSize, int epochs) {
        DataSet trainSet = new DataSet(trainImages, trainLabels);
        for (int epoch = 0; epoch < epochs; epoch++) {
            System.out.println("Epoch " + (epoch + 1) + "/" + epochs);
            for (int i = 0; i < trainSet.numExamples(); i += batchSize) {
                int end = Math.min(i + batchSize, trainSet.numExamples());
                DataSet batch = trainSet.getRange(i, end).copy();
                model.fit(batch);
            }
        }
    }

    public Evaluation evaluate(INDArray testImages, INDArray testLabels) {
        INDArray output = model.output(testImages);
        Evaluation eval = new Evaluation(NUM_CLASSES);
        eval.eval(testLabels, output);
        return eval;
    }

    public int predict(INDArray image) {
        INDArray output = model.output(image);
        return Nd4j.argMax(output, 1).getInt(0);
    }

}