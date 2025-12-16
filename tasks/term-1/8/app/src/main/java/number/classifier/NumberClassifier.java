package number.classifier;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;

public class NumberClassifier {
    private MultiLayerNetwork model;
    private static final int batchSize = 64;
    private static final int numClasses = 10;
    private static final int seed = 123;
    private static final int numEpochs = 5;

    public NumberClassifier() {
        buildModel();
    }

    private void buildModel() {
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
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
                        .activation(Activation.RELU)
                        .nOut(500)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(numClasses)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .build();

        model = new MultiLayerNetwork(configuration);
        model.init();
        model.setListeners(new ScoreIterationListener(100));
    }

    public void trainAndEvaluate() throws IOException {

        DataSetIterator trainData = new MnistDataSetIterator(batchSize, true, seed);
        DataSetIterator testData = new MnistDataSetIterator(batchSize, false, seed);

        System.out.println("Starting training...");
        System.out.println("Model's architecture:\n" + model.summary());

        for (int i = 0; i < numEpochs; i++) {
            System.out.println("Epoch " + (i + 1) + "/" + numEpochs);
            model.fit(trainData);
            trainData.reset();
        }

        Evaluation evaluation = model.evaluate(testData);
        System.out.println(evaluation.stats());

        System.out.println("\n=== Results ===");
        System.out.println("Accuracy: " + String.format("%.2f", evaluation.accuracy() * 100) + "%");
    }

    public int predict(DataNormalization normalizer, INDArray image) {
        if (normalizer != null) {
            normalizer.transform(image);
        }

        INDArray output = model.output(image);

        return Nd4j.argMax(output, 1).getInt(0);
    }

    public MultiLayerNetwork getModel() {
        return model;
    }
}