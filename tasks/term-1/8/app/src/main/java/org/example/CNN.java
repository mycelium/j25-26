package org.example;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;

public class CNN {
    private MultiLayerNetwork model;
    
    public CNN() {
        buildModel();
    }
    
    private void buildModel() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(42)
            .weightInit(WeightInit.SIGMOID_UNIFORM)
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
                .activation(Activation.SOFTMAX)
                .nOut(10)
                .build())
            .setInputType(InputType.convolutionalFlat(28, 28, 1))
            .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        System.out.println("---===[CNN model created]===---");
    }
    
    public void fit(DataSetIterator trainIter, int numEpochs) {
        for (int epoch = 0; epoch < numEpochs; epoch++) {
            System.out.println("Epoch " + (epoch + 1));
            model.fit(trainIter);
            trainIter.reset();
        }
    }
    
    public int predict(double[] image) {
        INDArray features = Nd4j.create(1, 1, 28, 28);
        for (int i = 0; i < image.length; i++) {
            features.putScalar(new int[]{0, 0, i/28, i%28}, image[i]);
        }
        
        INDArray output = model.output(features);
        return output.argMax(1).getInt(0);
    }
    
    public void evaluate(DataSetIterator testIter) {
        Evaluation eval = new Evaluation(10);
        
        while (testIter.hasNext()) {
            var batch = testIter.next();
            INDArray output = model.output(batch.getFeatures());
            eval.eval(batch.getLabels(), output);
        }
        
        System.out.println(eval.stats());
    }
    
    public int predictFromImage(String imagePath) throws IOException {
        double[] processedImage = ImageProcessor.loadAndProcessImage(imagePath);
        return predict(processedImage);
    }
}