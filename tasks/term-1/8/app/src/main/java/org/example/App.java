package org.example;

import java.io.File;

import javax.imageio.ImageIO;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer.PoolingType;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {

    public static int predict(MultiLayerNetwork model, INDArray image) {
        INDArray output = model.output(image);  
        return Nd4j.argMax(output, 1).getInt(0);    
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.printf("add path to image");
            return;
        }
        String path = args[0];
        int nChannels = 1;
        int outputNum = 10;
        int batchSize = 64;
        int nEpochs = 15;
        int seed = 555;

        System.out.println("Load data...");
        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize,true,12345);
        DataSetIterator mnistTest = new MnistDataSetIterator(batchSize,false,12345);

        System.out.println("Build model...");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .l2(0.0005)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(0.01, 0.9))
                .list()
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        .nIn(nChannels)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(1, new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(2,2)
                        .stride(2,2)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(5, 5)
                        .stride(1, 1)
                        .nOut(50)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(3, new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(2,2)
                        .stride(2,2)
                        .build())
                .layer(4, new DenseLayer.Builder().activation(Activation.RELU)
                        .nOut(500).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(outputNum)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(28,28,1))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        System.out.printf("Train model");
        model.setListeners(new ScoreIterationListener(10));
        for( int i=0; i<nEpochs; i++ ) {
            model.fit(mnistTrain);
            System.out.printf("Completed epoch: %n", i);

            System.out.printf("Evaluate model");
            Evaluation eval = model.evaluate(mnistTest);
            System.out.printf(eval.stats());
            mnistTest.reset();
        }
        
        System.out.println("Predicting: " + path);
        
        BufferedImage img = ImageIO.read(new File(path));
        
        INDArray input = Nd4j.create(1, 1, 28, 28);      
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {    
                double value = (((img.getRGB(x, y)) >> 16) & 0xFF) / 255.0;           
                input.putScalar(0, 0, y, x, value);     
            }
        }

        int predicted = predict(model, input);
        System.out.println("Prediction: " + predicted);
    }
}


//  ?
//https://github.com/deeplearning4j/deeplearning4j-examples/tree/master/oreilly-book-dl4j-examples/nd4j-examples/src/main/java/org/nd4j/examples


