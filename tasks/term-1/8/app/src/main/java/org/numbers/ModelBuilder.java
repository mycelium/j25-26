package org.numbers;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class ModelBuilder {

    public MultiLayerNetwork build() {

        MultiLayerConfiguration config =
                new NeuralNetConfiguration.Builder()
                        .seed(123)
                        .updater(new Adam(0.001))
                        .list()
                        .layer(new ConvolutionLayer.Builder(3, 3)
                                .nIn(1)
                                .nOut(32)
                                .activation(Activation.RELU)
                                .build())
                        .layer(new SubsamplingLayer.Builder(
                                SubsamplingLayer.PoolingType.MAX)
                                .kernelSize(2, 2)
                                .stride(2, 2)
                                .build())
                        .layer(new DenseLayer.Builder()
                                .nOut(128)
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
}
