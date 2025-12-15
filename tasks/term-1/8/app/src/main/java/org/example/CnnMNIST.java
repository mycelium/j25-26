package org.example;

import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import java.io.*;


public class CnnMNIST {
    private static final String MODEL_PATH = "mnist-cnn-model.zip";

    public static void saveModel(MultiLayerNetwork model) throws IOException {
        File file = new File(MODEL_PATH);
        ModelSerializer.writeModel(model, file, true);
    }

    public static MultiLayerNetwork loadModelIfExists() throws IOException {
        File file = new File(MODEL_PATH);
        if (file.exists()) {
            System.out.println("Loading existing model...");
            return ModelSerializer.restoreMultiLayerNetwork(file);
        }
        return null;
    }


    public static MultiLayerNetwork createModel() {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(0.001))
                .list()
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .nIn(1)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(
                        SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .stride(1, 1)
                        .nOut(50)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(
                        SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nOut(256)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(
                        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(10)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        return model;
    }

    public static MultiLayerNetwork trainAndEvaluate() throws Exception {

        MultiLayerNetwork model = loadModelIfExists();

        if (model == null) {
            int batchSize = 64;
            int epochs = 1;

            MnistDataSetIterator trainIter = new MnistDataSetIterator(batchSize, true, 12345);
            MnistDataSetIterator testIter = new MnistDataSetIterator(batchSize, false, 12345);

            model = createModel();

            System.out.println("Training started...");
            for (int i = 0; i < epochs; i++) {
                model.fit(trainIter);
                trainIter.reset();
            }

            System.out.println("Evaluating...");
            Evaluation eval = model.evaluate(testIter);
            System.out.println(eval.stats());
            System.out.println("Accuracy: " + eval.accuracy());

            saveModel(model);
            System.out.println("Model saved.");
        }

        return model;
    }

    public static int predictFromImage(MultiLayerNetwork model, String imagePath) throws Exception {
        INDArray input = ImageReader.loadAndProcessImage(imagePath);
        INDArray output = model.output(input);
        return output.argMax(1).getInt(0);
    }
}
