package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;

public class MNISTClassifier {

    private static final int NUM_CLASSES = 10;  
    private static final int CHANNELS = 1;      
    private static final int HEIGHT = 28;       
    private static final int WIDTH = 28;        
    
    private static final int BATCH_SIZE = 64;   
    private static final int EPOCHS = 5;        
    private static final int RANDOM_SEED = 12345;
    private static final double LEARNING_RATE = 0.001;

    public static void main(String[] args) throws Exception {
        System.out.println("=== MNIST Image Classification with Convolutional Neural Network ===");
        System.out.println("Implementing: Convolutional Neural Network (CNN) for digit recognition");
        
        System.out.println("\n1. Loading MNIST dataset...");
        DataSetIterator mnistTrain = new MnistDataSetIterator(BATCH_SIZE, true, RANDOM_SEED);
        DataSetIterator mnistTest = new MnistDataSetIterator(BATCH_SIZE, false, RANDOM_SEED);
        
        System.out.println("Applying data normalization for CNN...");
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(mnistTrain);
        mnistTrain.setPreProcessor(scaler);
        mnistTest.setPreProcessor(scaler);
        
        System.out.println("MNIST dataset loaded and normalized successfully");
        System.out.println("  - Training data: 60,000 images");
        System.out.println("  - Test data: 10,000 images");
        System.out.println("  - Image size: " + HEIGHT + "x" + WIDTH + " pixels");
        System.out.println("  - Number of classes: " + NUM_CLASSES + " (digits 0-9)");
        
        System.out.println("\n2. Building Convolutional Neural Network...");
        MultiLayerNetwork model = createCNNModel();
        System.out.println("CNN model created successfully");
        System.out.println("  - Architecture: Conv -> Pool -> Conv -> Pool -> Dense -> Output");
        System.out.println("  - Convolutional layers: 2");
        System.out.println("  - Pooling layers: 2");
        System.out.println("  - Fully connected layers: 1");
        
        System.out.println("\n3. Training CNN model...");
        trainModel(model, mnistTrain);
        
        System.out.println("\n4. Evaluating model performance...");
        evaluateModel(model, mnistTest);
        
        System.out.println("\n5. Saving trained model...");
        saveModel(model);
        
        System.out.println("\n=== CNN Training Completed Successfully! ===");
    }
    
    private static MultiLayerNetwork createCNNModel() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(RANDOM_SEED)
            .updater(new Adam(LEARNING_RATE))
            .weightInit(WeightInit.XAVIER)
            .list()
            
            .layer(new ConvolutionLayer.Builder(5, 5)
                .nIn(CHANNELS)          
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
                .nOut(NUM_CLASSES)     
                .activation(Activation.SOFTMAX)
                .build())

            .setInputType(InputType.convolutionalFlat(HEIGHT, WIDTH, CHANNELS))
            .build();
        
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        
        model.setListeners(new ScoreIterationListener(100));
        
        return model;
    }
    
    
    private static void trainModel(MultiLayerNetwork model, DataSetIterator trainData) {
        System.out.println("Starting training for " + EPOCHS + " epochs...");
        System.out.println("Batch size: " + BATCH_SIZE);
        System.out.println("Learning rate: " + LEARNING_RATE);
        
        for (int i = 0; i < EPOCHS; i++) {
            System.out.println("\n--- Epoch " + (i + 1) + "/" + EPOCHS + " ---");
            long startTime = System.currentTimeMillis();
            
            model.fit(trainData);
            trainData.reset();
            
            long endTime = System.currentTimeMillis();
            System.out.println("Epoch " + (i + 1) + " completed in " + (endTime - startTime) / 1000 + "s");
        }
        
        System.out.println("✓ Training finished!");
    }
    

    private static void evaluateModel(MultiLayerNetwork model, DataSetIterator testData) {
        System.out.println("Evaluating model on test dataset...");
        
        Evaluation eval = model.evaluate(testData);
        
        System.out.println("\n" + eval.stats());
        
        System.out.println("\n=== CNN MODEL PERFORMANCE SUMMARY ===");
        System.out.println("Accuracy:    " + String.format("%.4f", eval.accuracy()) + 
                          " (" + String.format("%.2f", eval.accuracy() * 100) + "%)");
        System.out.println("Precision:   " + String.format("%.4f", eval.precision()) + 
                          " (" + String.format("%.2f", eval.precision() * 100) + "%)");
        System.out.println("Recall:      " + String.format("%.4f", eval.recall()) + 
                          " (" + String.format("%.2f", eval.recall() * 100) + "%)");
        System.out.println("F1 Score:    " + String.format("%.4f", eval.f1()) + 
                          " (" + String.format("%.2f", eval.f1() * 100) + "%)");
        

        System.out.println("\n=== PER-CLASS ACCURACY ===");
        for (int i = 0; i < NUM_CLASSES; i++) {
            int correct = (int) eval.getConfusionMatrix().getCount(i, i);
            int total = 0;
            for (int j = 0; j < NUM_CLASSES; j++) {
                total += (int) eval.getConfusionMatrix().getCount(i, j);
            }
            double accuracy = total > 0 ? (double) correct / total : 0.0;
            System.out.println("Digit " + i + ": " + String.format("%.2f", accuracy * 100) + "%" +
                             " (" + correct + "/" + total + " correct)");
        }
        
        System.out.println("\n=== CONFUSION MATRIX ANALYSIS ===");
        for (int i = 0; i < NUM_CLASSES; i++) {
            int correct = (int) eval.getConfusionMatrix().getCount(i, i);
            int total = 0;
            for (int j = 0; j < NUM_CLASSES; j++) {
                total += (int) eval.getConfusionMatrix().getCount(i, j);
            }
            double accuracy = total > 0 ? (double) correct / total : 0.0;
            System.out.println("Digit " + i + ": " + correct + "/" + total + 
                             " correct (" + String.format("%.2f", accuracy * 100) + "%)");
        }
        
        System.out.println("\n=== ERROR ANALYSIS ===");
        for (int i = 0; i < NUM_CLASSES; i++) {
            int correct = (int) eval.getConfusionMatrix().getCount(i, i);
            int total = 0;
            for (int j = 0; j < NUM_CLASSES; j++) {
                total += (int) eval.getConfusionMatrix().getCount(i, j);
            }
            int errors = total - correct;
            if (errors > 0) {
                System.out.println("Digit " + i + ": " + errors + " misclassifications");
            }
        }
        
        System.out.println("\n=== CONFUSION MATRIX VISUALIZATION ===");
        System.out.print("     ");
        for (int j = 0; j < NUM_CLASSES; j++) {
            System.out.print(String.format("%-4d", j));
        }
        System.out.println();
        for (int i = 0; i < NUM_CLASSES; i++) {
            System.out.print(i + " | ");
            for (int j = 0; j < NUM_CLASSES; j++) {
                int count = (int) eval.getConfusionMatrix().getCount(i, j);
                if (i == j) {
                   
                    System.out.print(String.format("%-4d", count));
                } else if (count > 0) {
                   
                    System.out.print(String.format("%-4d", count));
                } else {
                    System.out.print(".   ");
                }
            }
            System.out.println();
        }
    }

    private static void saveModel(MultiLayerNetwork model) throws Exception {
        File modelFile = new File("mnist-cnn-model.zip");
        ModelSerializer.writeModel(model, modelFile, true);
        System.out.println("Trained CNN model saved to: " + modelFile.getAbsolutePath());
        System.out.println("Model can be used for digit recognition on new images");
    }
}
