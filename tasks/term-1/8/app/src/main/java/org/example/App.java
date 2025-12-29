package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class App {

        public static void main(String[] args) {
                try {
                        System.out.println("=== The beginning of model training ===");

                        int batchSize = 64;
                        int numEpochs = 3;
                        int rngSeed = 123;

                        DataSetIterator trainIter = new MnistDataSetIterator(batchSize, true, rngSeed);
                        DataSetIterator testIter = new MnistDataSetIterator(batchSize, false, rngSeed);

                        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                                        .seed(rngSeed)
                                        .updater(new Adam(0.001))
                                        .list()
                                        .layer(0, new ConvolutionLayer.Builder(5, 5)
                                                        .nIn(1).nOut(20).activation(Activation.RELU).build())
                                        .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                                                        .kernelSize(2, 2).stride(2, 2).build())
                                        .layer(2, new ConvolutionLayer.Builder(5, 5)
                                                        .nOut(50).activation(Activation.RELU).build())
                                        .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                                                        .kernelSize(2, 2).stride(2, 2).build())
                                        .layer(4, new DenseLayer.Builder()
                                                        .nOut(500).activation(Activation.RELU).build())
                                        .layer(5, new OutputLayer.Builder(
                                                        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                                        .nOut(10).activation(Activation.SOFTMAX).build())
                                        .setInputType(org.deeplearning4j.nn.conf.inputs.InputType.convolutionalFlat(28,
                                                        28, 1))
                                        .build();

                        MultiLayerNetwork model = new MultiLayerNetwork(conf);
                        model.init();
                        model.setListeners(new ScoreIterationListener(10));

                        System.out.println("Start training");
                        for (int i = 0; i < numEpochs; i++) {
                                System.out.println("Era " + (i + 1) + "/" + numEpochs);
                                model.fit(trainIter);
                                trainIter.reset();
                        }

                        System.out.println("\n=== Evaluating the model on a test set ===");
                        Evaluation eval = new Evaluation(10);
                        while (testIter.hasNext()) {
                                var next = testIter.next();
                                var output = model.output(next.getFeatures());
                                eval.eval(next.getLabels(), output);
                        }
                        System.out.println(eval.stats());

                        System.out.println("\n=== Exemple on test imsge ===");
                        testIter.reset();
                        var firstTestBatch = testIter.next();
                        var features = firstTestBatch.getFeatures();
                        var labels = firstTestBatch.getLabels();

                        var prediction = model.output(features.getRow(0).reshape(1, 1, 28, 28));
                        int predictedDigit = prediction.argMax(1).getInt(0);
                        int actualDigit = labels.getRow(0).argMax(0).getInt(0);
                        System.out.println("The predicted figure: " + predictedDigit);
                        System.out.println("The actual figure: " + actualDigit);

                        if (args.length > 0) {
                                String imagePath = args[0];
                                System.out.println("\n=== Classification of the user image: " + imagePath
                                                + " ===");
                                classifyCustomImage(model, imagePath);
                        }

                        System.out.println("\n=== The training and assessment are completed ===");

                } catch (Exception e) {
                        System.err.println("Error: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        private static void classifyCustomImage(MultiLayerNetwork model, String imagePath) {
                try {
                        File imageFile = new File(imagePath);
                        if (!imageFile.exists()) {
                                System.err.println("File not found: " + imagePath);
                                return;
                        }

                        BufferedImage img = ImageIO.read(imageFile);
                        if (img == null) {
                                System.err.println("The image cannot be read: " + imagePath);
                                return;
                        }

                        BufferedImage grayResized = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
                        var g = grayResized.createGraphics();
                        g.drawImage(img, 0, 0, 28, 28, null);
                        g.dispose();

                        var array = Nd4j.create(1, 28, 28);
                        for (int y = 0; y < 28; y++) {
                                for (int x = 0; x < 28; x++) {
                                        int pixel = grayResized.getRaster().getSample(x, y, 0) & 0xFF;
                                        double normalized = (255.0 - pixel) / 255.0;
                                        array.putScalar(new int[] { 0, y, x }, normalized);
                                }
                        }

                        var input = array.reshape(1, 1, 28, 28);
                        var output = model.output(input);
                        int predicted = output.argMax(1).getInt(0);
                        double confidence = output.getDouble(predicted);

                        System.out.printf("Prediction: figure %d (confidence: %.2f%%)\n", predicted,
                                        confidence * 100);
                        System.out.println("Probability distribution: ");
                        for (int i = 0; i < 10; i++) {
                                System.out.printf("  %d: %.3f%%\n", i, output.getDouble(i) * 100);
                        }

                } catch (IOException e) {
                        System.err.println("Image processing error: " + e.getMessage());
                }
        }
}