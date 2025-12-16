package org.example;

import java.io.IOException;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GlobalPoolingLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.PoolingType;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class MNISTClassifier {
    private static final int NUM_CLASSES = 10;
    private static final int BATCH_SIZE = 64;
    private static final int EPOCHS = 5;
    private static final int SEED = 123;

    private MultiLayerNetwork model;

    public void buildModel() {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
            .seed(SEED)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(new Adam(0.001))
            .weightInit(WeightInit.XAVIER)
            .list()
            .setInputType(InputType.convolutionalFlat(28, 28, 1)) // MNIST: 28x28 grayscale images
            // First convolution layer
            .layer(new ConvolutionLayer.Builder(5, 5)
                .nIn(1) // Input channels (grayscale)
                .stride(1, 1)
                .nOut(20) // Number of filters
                .activation(Activation.RELU)
                .build())
            // First subsampling (max pooling) layer
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            // Second convolution layer
            .layer(new ConvolutionLayer.Builder(5, 5)
                .nIn(20) // Input channels from previous conv layer
                .stride(1, 1)
                .nOut(50) // Number of filters
                .activation(Activation.RELU)
                .build())
            // Second subsampling layer
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            // Global pooling to reduce dimensions
            .layer(new GlobalPoolingLayer.Builder(PoolingType.AVG)
                .build())
            // Dense layer
            .layer(new DenseLayer.Builder()
                .nIn(50) // From global pooling of 50 feature maps
                .activation(Activation.RELU)
                .nOut(500)
                .build())
            // Output layer
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nIn(500) // From previous dense layer
                .nOut(NUM_CLASSES)
                .activation(Activation.SOFTMAX)
                .build())
            .build();

        model = new MultiLayerNetwork(config);
        model.init();
        model.setListeners(new ScoreIterationListener(10));
    }

    public void train() throws IOException {
        System.out.println("Loading MNIST dataset...");

        // Load MNIST training data
        DataSetIterator trainIter = new MnistDataSetIterator(BATCH_SIZE, true, SEED);
        // Load MNIST test data
        DataSetIterator testIter = new MnistDataSetIterator(BATCH_SIZE, false, SEED);

        System.out.println("Training model...");
        for (int epoch = 0; epoch < EPOCHS; epoch++) {
            System.out.println("Epoch " + (epoch + 1) + "/" + EPOCHS);
            model.fit(trainIter);
            trainIter.reset();
        }

        System.out.println("Training completed!");
    }

    public void evaluate() throws IOException {
        System.out.println("Evaluating model...");

        DataSetIterator testIter = new MnistDataSetIterator(BATCH_SIZE, false, SEED);
        Evaluation eval = new Evaluation(NUM_CLASSES);

        while (testIter.hasNext()) {
            var ds = testIter.next();
            var output = model.output(ds.getFeatures());
            eval.eval(ds.getLabels(), output);
        }

        System.out.println(eval.stats());
        System.out.println("Accuracy: " + eval.accuracy());
        System.out.println("Precision: " + eval.precision());
        System.out.println("Recall: " + eval.recall());
    }

    public int predict(double[][][] image) {
        throw new UnsupportedOperationException("Prediction from raw image not implemented in this demo");
    }

    public MultiLayerNetwork getModel() {
        return model;
    }
}
