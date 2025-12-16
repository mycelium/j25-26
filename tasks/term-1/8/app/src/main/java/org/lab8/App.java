package org.lab8;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.evaluation.classification.Evaluation; // Класс для оценки точности
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;

public class App {

    public static void main(String[] args) throws Exception {

        int batchSize = 64;
        int seed = 123;
        
        DataSetIterator trainIter = new MnistDataSetIterator(batchSize, true, seed);
        DataSetIterator testIter = new MnistDataSetIterator(batchSize, false, seed);

        // Нормализация (0..255 -> 0..1)
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(trainIter);
        trainIter.setPreProcessor(scaler);
        testIter.setPreProcessor(scaler); // тестовые данные нормализуем так же

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .l2(0.0005)
                .updater(new Adam(0.001))
                .list()
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .nIn(1)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .stride(1, 1)
                        .nOut(50)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new DenseLayer.Builder().activation(Activation.RELU)
                        .nOut(500).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(10)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.fit(trainIter, 1); // 1 эпоха

        Evaluation eval = model.evaluate(testIter);
        
        System.out.println("Accuracy:  " + String.format("%.2f%%", eval.accuracy() * 100));

        if (args.length > 0) {
            String imagePath = args[0];
            File file = new File(imagePath);
            if (file.exists()) {
                NativeImageLoader loader = new NativeImageLoader(28, 28, 1);
                INDArray image = loader.asMatrix(file);
                scaler.transform(image);

                if (image.meanNumber().doubleValue() > 0.5) {
                    image = image.rsub(1);
                }

                INDArray output = model.output(image);
                int result = output.argMax(1).getInt(0);
                
                System.out.println("Input file: " + imagePath);
                System.out.println("Number: " + result);
            } else {
                System.out.println("File " + imagePath + " not found");
            }
        }
    }
}