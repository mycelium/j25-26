package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
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
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;

public class Classifier {

    private final int gen = 3;
    private final int bsize = 64;
    private final int classes = 10;
    private final int seed = 1;

    private MultiLayerNetwork core;

    public Classifier() {
        assembleCore();
    }

    private void assembleCore() {
        MultiLayerConfiguration blueprint = new NeuralNetConfiguration.Builder()
                .seed(seed)
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
                        .nOut(500)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(classes)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .build();

        core = new MultiLayerNetwork(blueprint);
        core.init();
        core.setListeners(new ScoreIterationListener(100));
    }

    public void executeTrainingAndAssessment() throws IOException {
        DataSetIterator trainingSet = new MnistDataSetIterator(bsize, true, seed);
        DataSetIterator evaluationSet = new MnistDataSetIterator(bsize, false, seed);

        System.out.println("Start of learning");

        for (int epoch = 0; epoch < gen; epoch++) {
            System.out.println("epoch " + (epoch + 1) + " / " + gen);
            core.fit(trainingSet);
            trainingSet.reset();
        }

        Evaluation metrics = core.evaluate(evaluationSet);
        System.out.println(metrics.stats());

        System.out.println("Final correct predictions: " + String.format("%.2f", metrics.accuracy() * 100) + "%");
    }

    public int classify(DataNormalization preprocessor, INDArray sample) {
        if (preprocessor != null) {
            preprocessor.transform(sample);
        }

        INDArray prediction = core.output(sample);
        return Nd4j.argMax(prediction, 1).getInt(0);
    }
}