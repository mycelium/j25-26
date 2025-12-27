package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.InputPreProcessor;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;

public class CnnMNIST {

    private static final String MODEL_PATH = "mnist-cnn-model.zip";

    private static final int IMAGE_HEIGHT = 28;
    private static final int IMAGE_WIDTH = 28;
    private static final int CHANNELS = 1;
    private static final int OUTPUT_CLASSES = 10;

    private static final int SEED = 123;
    private static final int BATCH_SIZE = 64;
    private static final int EPOCHS = 1;



    public static void saveModel(MultiLayerNetwork model) throws IOException {
        ModelSerializer.writeModel(model, new File(MODEL_PATH), true);
    }

    public static MultiLayerNetwork loadModelIfExists() throws IOException {
        File modelFile = new File(MODEL_PATH);
        if (modelFile.exists()) {
            System.out.println("Existing model found. Loading...");
            return ModelSerializer.restoreMultiLayerNetwork(modelFile);
        }
        return null;
    }


    public static MultiLayerNetwork createModel() {

        MultiLayerConfiguration configuration =
                new NeuralNetConfiguration.Builder()
                        .seed(SEED)
                        .updater(new Adam(0.001))
                        .list()

                        .layer(new ConvolutionLayer.Builder(5, 5)
                                .nIn(CHANNELS)
                                .nOut(20)
                                .stride(1, 1)
                                .activation(Activation.RELU)
                                .build())

                        .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                                .kernelSize(2, 2)
                                .stride(2, 2)
                                .build())

                        .layer(new ConvolutionLayer.Builder(5, 5)
                                .nOut(50)
                                .stride(1, 1)
                                .activation(Activation.RELU)
                                .build())

                        .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                                .kernelSize(2, 2)
                                .stride(2, 2)
                                .build())

                        .layer(new DenseLayer.Builder()
                                .nOut(256)
                                .activation(Activation.RELU)
                                .build())

                        .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                .nOut(OUTPUT_CLASSES)
                                .activation(Activation.SOFTMAX)
                                .build())

                        .setInputType(InputType.convolutionalFlat(
                                IMAGE_HEIGHT, IMAGE_WIDTH, CHANNELS))
                        .build();

        MultiLayerNetwork model = new MultiLayerNetwork(configuration);
        model.init();
        return model;
    }


    public static MultiLayerNetwork trainAndEvaluate() throws Exception {

        MultiLayerNetwork model = loadModelIfExists();
        if (model != null) {
            return model;
        }

        MnistDataSetIterator trainIterator =
                new MnistDataSetIterator(BATCH_SIZE, true, SEED);

        MnistDataSetIterator testIterator =
                new MnistDataSetIterator(BATCH_SIZE, false, SEED);

        model = createModel();

        System.out.println("Training CNN on MNIST...");
        for (int epoch = 1; epoch <= EPOCHS; epoch++) {
            model.fit(trainIterator);
            trainIterator.reset();
            System.out.println("Epoch " + epoch + " finished");
        }

        System.out.println("Evaluating model...");
        Evaluation evaluation = model.evaluate(testIterator);

        System.out.println(evaluation.stats());
        System.out.println("Accuracy: " + evaluation.accuracy());

        saveModel(model);
        System.out.println("Model saved to disk");

        return model;
    }


    public static int predictFromImage(MultiLayerNetwork model, String imagePath) throws Exception {
        INDArray input = ImageReader.loadAndProcessImage(imagePath);
        INDArray output = model.output(input);
        return output.argMax(1).getInt(0);
    }
}
