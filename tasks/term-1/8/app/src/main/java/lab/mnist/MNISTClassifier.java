package lab.mnist;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MNISTClassifier {
    private static final int NUM_CLASSES = 10;
    private static final int SEED = 123;
    private static final String MODEL_FILE = "mnist-cnn-model.zip";

    public static void main(String[] args) throws Exception {
        File modelLocation = new File(MODEL_FILE);
        MultiLayerNetwork model;

        if (!modelLocation.exists()) {
            System.out.println("Model not found. Starting learning...");
            model = trainModel();
            ModelSerializer.writeModel(model, modelLocation, true);
            System.out.println("Model saved to " + MODEL_FILE);
        } else {
            System.out.println("Loading existing model...");
            model = ModelSerializer.restoreMultiLayerNetwork(modelLocation);
        }

        //вывод статистики обучения
        System.out.println("\n>>> Running Model Evaluation Statistics:");
        printStats(model);

        if (args.length > 0) {
            String path = args[0];
            int digit = predictDigit(model, path);
            System.out.println("================================");
            System.out.println("Result for file " + path + ": " + digit);
            System.out.println("================================");
        } else {
            System.out.println("For classification of your own picture type: gradlew run --args=\"path/to/image.png\"");
        }
    }

    //метод для вывода таблиц и метрик
    private static void printStats(MultiLayerNetwork model) throws IOException {
        DataSetIterator testIter = new MnistDataSetIterator(64, false, SEED);
        testIter.setPreProcessor(new ImagePreProcessingScaler(0, 1));
        Evaluation eval = model.evaluate(testIter);
        System.out.println(eval.stats());
    }

    private static MultiLayerNetwork trainModel() throws IOException {
        DataSetIterator trainIter = new MnistDataSetIterator(64, true, SEED);
        DataSetIterator testIter = new MnistDataSetIterator(64, false, SEED);
        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
        trainIter.setPreProcessor(scaler);
        testIter.setPreProcessor(scaler);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(SEED)
            .updater(new Adam(0.001))
            .weightInit(WeightInit.XAVIER)
            .list()
            .layer(new ConvolutionLayer.Builder(5, 5).nIn(1).nOut(20).activation(Activation.RELU).build())
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2).stride(2, 2).build())
            .layer(new ConvolutionLayer.Builder(5, 5).nOut(50).activation(Activation.RELU).build())
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2).stride(2, 2).build())
            .layer(new DenseLayer.Builder().activation(Activation.RELU).nOut(500).build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(NUM_CLASSES).activation(Activation.SOFTMAX).build())
            .setInputType(InputType.convolutionalFlat(28, 28, 1))
            .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));

        for (int i = 0; i < 2; i++) {
            model.fit(trainIter);
            System.out.println("Epoch " + (i + 1) + " complete."); 
            trainIter.reset();
        }

        System.out.println("Final Evaluation after training:");
        printStats(model); 
        return model;
    }

    public static int predictDigit(MultiLayerNetwork model, String imagePath) throws IOException {
        BufferedImage img = ImageIO.read(new File(imagePath));
        Image scaled = img.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        BufferedImage gray = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();

        INDArray input = Nd4j.create(1, 1, 28, 28);
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 28; j++) {
                int pixel = gray.getRaster().getSample(j, i, 0);
                input.putScalar(new int[]{0, 0, i, j}, pixel / 255.0);
            }
        }
        INDArray output = model.output(input);
        return output.argMax(1).getInt(0);
    }
}
