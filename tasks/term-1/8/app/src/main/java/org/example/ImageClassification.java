package org.example;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;

public class ImageClassification {
    public static void main(String[] args) throws Exception {
        File modelFile = new File("mnist-model.zip");

        if (args.length > 0 && "predict".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                System.out.println("predict <path>");
                return;
            }

            if (!modelFile.exists()) {
                System.out.println("model not found: " + modelFile.getAbsolutePath());
                return;
            }

            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(modelFile);

            File img = new File(args[1]);
            if (!img.exists()) {
                System.out.println("image not found: " + img.getAbsolutePath());
                return;
            }

            NativeImageLoader loader = new NativeImageLoader(28, 28, 1);
            INDArray image = loader.asMatrix(img);

            ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
            scaler.transform(image);

            INDArray out = model.output(image);
            int predicted = Nd4j.argMax(out, 1).getInt(0);

            System.out.println(out);
            System.out.println("Predicted: " +predicted);
            return;
        }

        int batchSize = 64;
        DataSetIterator train = new MnistDataSetIterator(batchSize, true, 12345);
        DataSetIterator test = new MnistDataSetIterator(batchSize, false, 12345);

        MultiLayerNetwork model = new MultiLayerNetwork(
                new NeuralNetConfiguration.Builder()
                        .seed(12345)
                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                        .updater(new Adam(1e-3))
                        .l2(0.0005)
                        .list()
                        .layer(new ConvolutionLayer.Builder(5, 5)
                                .nIn(1)
                                .nOut(32)
                                .stride(1, 1)
                                .padding(2, 2)
                                .activation(Activation.RELU)
                                .build())
                        .layer(new SubsamplingLayer.Builder()
                                .poolingType(SubsamplingLayer.PoolingType.MAX)
                                .kernelSize(2, 2)
                                .stride(2, 2)
                                .build())
                        .layer(new ConvolutionLayer.Builder(5, 5)
                                .nOut(64)
                                .stride(1, 1)
                                .padding(2, 2)
                                .activation(Activation.RELU)
                                .build())
                        .layer(new SubsamplingLayer.Builder()
                                .poolingType(SubsamplingLayer.PoolingType.MAX)
                                .kernelSize(2, 2)
                                .stride(2, 2)
                                .build())
                        .layer(new DenseLayer.Builder()
                                .nOut(128)
                                .activation(Activation.RELU)
                                .build())
                        .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                .nOut(10)
                                .activation(Activation.SOFTMAX)
                                .build())
                        .setInputType(InputType.convolutionalFlat(28, 28, 1))
                        .build()
        );

        model.init();
        model.setListeners(new ScoreIterationListener(10));

        int epochs = 5;
        for (int i = 0; i < epochs; i++) {
            System.out.println("epoch " + (i + 1));
            model.fit(train);
            train.reset();
        }

        Evaluation eval = new Evaluation(10);
        test.reset();
        while (test.hasNext()) {
            DataSet ds = test.next();
            INDArray out = model.output(ds.getFeatures());
            eval.eval(ds.getLabels(), out);
        }
        System.out.println(eval.stats());

        ModelSerializer.writeModel(model, modelFile, true);
        System.out.println("saved: " + modelFile.getAbsolutePath());
    }
}
