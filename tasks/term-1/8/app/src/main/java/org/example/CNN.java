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
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.util.List;

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
    
    public void fit(MNISTLoader loader, int batchSize) {
        List<double[]> images = loader.getAllImages();
        List<Integer> labels = loader.getAllLabels();
        
        for (int i = 0; i < images.size(); i += batchSize) {
            int end = Math.min(i + batchSize, images.size());
            int actualBatchSize = end - i;
            
            INDArray features = Nd4j.create(actualBatchSize, 1, 28, 28);
            INDArray labelFeatures = Nd4j.create(actualBatchSize, 10);
            
            for (int j = 0; j < actualBatchSize; j++) {
                int idx = i + j;
                double[] image = images.get(idx);
                int label = labels.get(idx);
                
                for (int k = 0; k < image.length; k++) {
                    features.putScalar(new int[]{j, 0, k/28, k%28}, image[k]);
                }
                
                labelFeatures.putScalar(new int[]{j, label}, 1.0);
            }
            
            model.fit(new DataSet(features, labelFeatures));
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
    
    public void evaluate(MNISTLoader loader) {
        List<double[]> images = loader.getAllImages();
        List<Integer> labels = loader.getAllLabels();
        
        int correct = 0;
        for (int i = 0; i < images.size(); i++) {
            int predicted = predict(images.get(i));
            int trueLabel = labels.get(i);
            if (predicted == trueLabel) {
                correct++;
            }
        }
        
        double accuracy = (double) correct / images.size();
        System.out.println("Accuracy: " + String.format("%.2f", (accuracy * 100)) + "% (" + correct + "/" + images.size() + ")");
    }
    
    public int predictFromImage(String imagePath) throws IOException {
        double[] processedImage = ImageProcessor.loadAndProcessImage(imagePath);
        return predict(processedImage);
    }
}