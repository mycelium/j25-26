package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    // создаём такую же модель, как в App
    private MultiLayerNetwork createModel() {
        int height = 28;
        int width = 28;
        int channels = 1;
        int numClasses = 10;

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Nesterovs(0.01, 0.9))
                .list()
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        .nIn(channels)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(5, 5)
                        .stride(1, 1)
                        .nOut(50)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(4, new DenseLayer.Builder()
                        .nOut(100)
                        .activation(Activation.RELU)
                        .build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(numClasses)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(height, width, channels))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        return model;
    }

    @Test
    public void testModelCanTrainOnSmallBatch() throws Exception {
        int batchSize = 32;

        DataSetIterator trainIterator = new MnistDataSetIterator(batchSize, true, 123);
        DataSetIterator testIterator = new MnistDataSetIterator(batchSize, false, 123);

        MultiLayerNetwork model = createModel();

        assertNotNull(model);

        var trainBatch = trainIterator.next();
        model.fit(trainBatch);

        // оцениваем на одном маленьком тестовом наборе
        var testBatch = testIterator.next();
        Evaluation evaluation = new Evaluation(10);
        var output = model.output(testBatch.getFeatures());
        evaluation.eval(testBatch.getLabels(), output);

        double accuracy = evaluation.accuracy();

        // ожидаем, что точность не нулевая
        assertTrue(accuracy > 0.0, "Accuracy should be greater than 0");
    }

    @Test
    public void testModelCanPredictOneImage() throws Exception {
        int batchSize = 1;

        DataSetIterator testIterator = new MnistDataSetIterator(batchSize, false, 123);
        MultiLayerNetwork model = createModel();

        // обучим модель на одном батче, чтобы веса не были случайными
        DataSetIterator trainIterator = new MnistDataSetIterator(16, true, 123);
        model.fit(trainIterator.next());

        assertTrue(testIterator.hasNext(), "Test iterator should have data");

        var dataSet = testIterator.next();
        var features = dataSet.getFeatures();
        var labels = dataSet.getLabels();

        var output = model.output(features);

        int realDigit = labels.argMax(1).getInt(0);
        int predictedDigit = output.argMax(1).getInt(0);

        // просто проверим, что предсказание в допустимом диапазоне
        assertTrue(realDigit >= 0 && realDigit <= 9);
        assertTrue(predictedDigit >= 0 && predictedDigit <= 9);
    }
}
