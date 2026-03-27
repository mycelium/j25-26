package org.example;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.learning.config.Adam;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;

public class ModelManager {

    private static final String MODEL_PATH = "mnist_cnn_model.zip";
    private static final int CHANNELS = 1;
    private static final int OUTPUT_NUM = 10;
    private static final int BATCH_SIZE = 64;
    private static final int N_EPOCHS = 2;

    public static MultiLayerNetwork getModel() throws Exception {
        File modelFile = new File(MODEL_PATH);
        if (modelFile.exists()) {
            System.out.println("Loading pre-trained model from " + MODEL_PATH);
            return ModelSerializer.restoreMultiLayerNetwork(modelFile);
        } else {
            return trainAndSaveModel();
        }
    }

    private static MultiLayerNetwork trainAndSaveModel() throws Exception {
        System.out.println("Training model (first-time only)...");

        DataSetIterator mnistTrain = new org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator(BATCH_SIZE,
                true, 123);
        DataSetIterator mnistTest = new org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator(BATCH_SIZE,
                false, 123);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(1e-3))
                .list()
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .nIn(CHANNELS)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2).stride(2, 2).build())
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .stride(1, 1)
                        .nOut(50)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2).stride(2, 2).build())
                .layer(new DenseLayer.Builder().activation(Activation.RELU)
                        .nOut(500).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(OUTPUT_NUM)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(org.deeplearning4j.nn.conf.inputs.InputType.convolutionalFlat(28, 28, 1))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));

        for (int epoch = 0; epoch < N_EPOCHS; epoch++) {
            model.fit(mnistTrain);
            System.out.println("Completed epoch " + (epoch + 1));
        }

        System.out.println("Evaluating model...");
        var eval = model.evaluate(mnistTest);
        System.out.println(eval.stats());

        System.out.println("Saving model to " + MODEL_PATH);
        ModelSerializer.writeModel(model, MODEL_PATH, true);

        return model;
    }
}