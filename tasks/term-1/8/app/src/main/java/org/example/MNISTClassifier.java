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
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;

public class MNISTClassifier {
    private static final int NUM_CLASSES = 10;
    private static final int BATCH_SIZE = 64;
    private static final int EPOCHS = 5;
    private static final int SEED = 123;
    private MultiLayerNetwork model;

    public void buildModel() {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
            .seed(SEED)
            .updater(new Adam(0.001))
            .weightInit(WeightInit.XAVIER)
            .list()
            .setInputType(InputType.convolutionalFlat(28, 28, 1))
            
            .layer(new ConvolutionLayer.Builder(5, 5)
                .nIn(1)
                .nOut(20)
                .activation(Activation.RELU)
                .build())
            
            .layer(new SubsamplingLayer.Builder(PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            
            .layer(new ConvolutionLayer.Builder(5, 5)
                .nIn(20)
                .nOut(50)
                .activation(Activation.RELU)
                .build())
            
            .layer(new SubsamplingLayer.Builder(PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            
            .layer(new DenseLayer.Builder()
                .nOut(500)
                .activation(Activation.RELU)
                .build())
            
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(NUM_CLASSES)
                .activation(Activation.SOFTMAX)
                .build())
            
            .build();

        model = new MultiLayerNetwork(config);
        model.init();
        model.setListeners(new ScoreIterationListener(10));
    }

    public void train() throws IOException {
        System.out.println("Loading MNIST...");
        // ★ СТАРЫЙ API - без builder()
        DataSetIterator trainIter = new MnistDataSetIterator(BATCH_SIZE, true, SEED);
        
        for (int epoch = 0; epoch < EPOCHS; epoch++) {
            System.out.println("Epoch " + (epoch + 1));
            model.fit(trainIter);
            trainIter.reset();
        }
        System.out.println("Training done!");
    }

    public void evaluate() throws IOException {
        System.out.println("Evaluating...");
        // ★ СТАРЫЙ API - без builder()
        DataSetIterator testIter = new MnistDataSetIterator(BATCH_SIZE, false, SEED);
        
        Evaluation eval = new Evaluation(NUM_CLASSES);
        // ★ УБРАЛ getInputPreProcessor() - не нужно в этой версии
        
        while (testIter.hasNext()) {
            var ds = testIter.next();
            var output = model.output(ds.getFeatures());
            eval.eval(ds.getLabels(), output);
        }
        
        System.out.println(eval.stats());
        System.out.println("Accuracy: " + eval.accuracy());
    }

    public static void main(String[] args) throws IOException {
        MNISTClassifier classifier = new MNISTClassifier();
        classifier.buildModel();
        classifier.train();
        classifier.evaluate();
    }
}
