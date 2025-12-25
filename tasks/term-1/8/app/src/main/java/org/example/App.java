package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.evaluation.classification.Evaluation;

public class App {

    public static void main(String[] args) throws Exception {
        int batchSize = 64;
        int numEpochs = 1;         // чтобы обучение не шло очень долго
        int numClasses = 10;
        int height = 28;
        int width = 28;
        int channels = 1;          // чёрно-белые картинки

        // 1. Загрузка MNIST
        DataSetIterator trainIterator = new MnistDataSetIterator(batchSize, true, 123);
        DataSetIterator testIterator = new MnistDataSetIterator(batchSize, false, 123);

        // 2. Конфигурация простой CNN
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

        // 3. Создание и обучение модели
        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        model.setListeners(new ScoreIterationListener(100));

        System.out.println("Start training...");

        for (int i = 0; i < numEpochs; i++) {
            System.out.println("Epoch " + (i + 1));
            model.fit(trainIterator);
            trainIterator.reset();
        }

        // 4. Оценка точности
        System.out.println("Evaluate on test data...");

        Evaluation eval = model.evaluate(testIterator);
        System.out.println(eval.stats());

        // 5. Пример: прогон одной картинки из теста и вывод предсказания
        testIterator.reset();
        if (testIterator.hasNext()) {
            var dataSet = testIterator.next();
            var features = dataSet.getFeatures();
            var labels = dataSet.getLabels();

            var output = model.output(features);
            int realDigit = labels.argMax(1).getInt(0);
            int predictedDigit = output.argMax(1).getInt(0);

            System.out.println("Prediction example:");
            System.out.println("Real digit: " + realDigit);
            System.out.println("Predicted digit: " + predictedDigit);
        }

        System.out.println("Done.");
    }
}
