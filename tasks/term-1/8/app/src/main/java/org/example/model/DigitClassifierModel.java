 package org.example.model;

import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.example.config.ModelConfig;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * Создание CNN модели классификации цифр.
 */
public class DigitClassifierModel {

    /**
     * Строит и инициализирует модель.
     *
     * @return инициализированная нейронная сеть
     */
    public static MultiLayerNetwork build() {

        MultiLayerNetwork model = new MultiLayerNetwork(

                new NeuralNetConfiguration.Builder() .seed(ModelConfig.SEED) .weightInit(WeightInit.XAVIER) .updater(new Adam(0.001)) .list()
                        // Первый сверточный слой: извлекаются базоыее признаки изображения.
                        .layer(new ConvolutionLayer.Builder(5,5) .nIn(ModelConfig.CHANNELS) .stride(1,1) .convolutionMode(ConvolutionMode.Same) .nOut(20) .activation(Activation.RELU) .build())
                        // Слой MaxPooling.
                        .layer(new SubsamplingLayer.Builder( SubsamplingLayer.PoolingType.MAX) .kernelSize(2,2) .stride(2,2) .build())
                        // Второй сверточный слой: извлекает более сложные признаки 50 фильтров.
                        .layer(new ConvolutionLayer.Builder(5,5) .stride(1,1) .convolutionMode(ConvolutionMode.Same) .nOut(50) .activation(Activation.RELU) .build())
                        // Слой MaxPooling.
                        .layer(new SubsamplingLayer.Builder( SubsamplingLayer.PoolingType.MAX) .kernelSize(2,2) .stride(2,2) .build())
                        // Полносвязный слой: объединяет в 500 нейронов
                        .layer(new DenseLayer.Builder() .nOut(500) .activation(Activation.RELU) .build())
                        // Выходной слой: выполняется класификация и выдаются вероятности классов
                        .layer(new OutputLayer.Builder( LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD) .nOut(ModelConfig.OUTPUTS) .activation(Activation.SOFTMAX) .build())

                        .setInputType( InputType.convolutionalFlat( ModelConfig.HEIGHT, ModelConfig.WIDTH, ModelConfig.CHANNELS ) )

                        .build()
        );

        model.init();

        return model;
    }
}