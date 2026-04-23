package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
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
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Лабораторная работа 8: Классификация изображений с использованием свёрточной нейронной сети
 */
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    
    public static void main(String[] args) {
        try {
            System.out.println("==========================================");
            System.out.println("MNIST Digit Classifier with DeepLearning4j");
            System.out.println("==========================================");
            
            DigitClassifier classifier = new DigitClassifier();
            
            System.out.println("\n[Шаг 1] Начало обучения модели...");
            classifier.train();
            
            System.out.println("\n[Шаг 2] Оценка точности модели...");
            classifier.evaluate();
            
            System.out.println("\n[Шаг 3] Демонстрация работы модели...");
            classifier.demo();
            
            if (args.length > 0) {
                String imagePath = args[0];
                System.out.println("\n[Шаг 4] Классификация изображения: " + imagePath);
                classifier.classifyImage(imagePath);
            }
            
            System.out.println("\n==========================================");
            System.out.println("Работа программы завершена успешно!");
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("Произошла ошибка: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

class DigitClassifier {
    private static final Logger log = LoggerFactory.getLogger(DigitClassifier.class);
    
    private static final int NUM_CLASSES = 10;
    private static final int IMAGE_WIDTH = 28;
    private static final int IMAGE_HEIGHT = 28;
    private static final int IMAGE_CHANNELS = 1;
    
    private static final int BATCH_SIZE = 64;
    private static final int TRAINING_EPOCHS = 1;
    private static final double LEARNING_RATE = 0.001;
    private static final int SEED = 123;
    
    private MultiLayerNetwork model;
    private DataNormalization scaler;
    
    public DigitClassifier() {
        initializeScaler();
        initializeNetwork();
    }
    
    private void initializeScaler() {
        scaler = new ImagePreProcessingScaler(0, 1);
    }
    
    private void initializeNetwork() {
        log.info("Инициализация архитектуры CNN...");
        
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(LEARNING_RATE))
                .weightInit(WeightInit.XAVIER)
                .l2(0.0001)
                .list()
                
                .layer(new ConvolutionLayer.Builder()
                        .name("conv1")
                        .kernelSize(5, 5)
                        .stride(1, 1)
                        .nIn(IMAGE_CHANNELS)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                
                .layer(new SubsamplingLayer.Builder()
                        .name("pool1")
                        .poolingType(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                
                .layer(new ConvolutionLayer.Builder()
                        .name("conv2")
                        .kernelSize(5, 5)
                        .stride(1, 1)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                
                .layer(new SubsamplingLayer.Builder()
                        .name("pool2")
                        .poolingType(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                
                .layer(new DenseLayer.Builder()
                        .name("fc1")
                        .nOut(1024)
                        .activation(Activation.RELU)
                        .dropOut(0.5)
                        .build())
                
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .name("output")
                        .nOut(NUM_CLASSES)
                        .activation(Activation.SOFTMAX)
                        .build())
                
                .setInputType(InputType.convolutionalFlat(IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_CHANNELS))
                .build();
        
        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));
        
        log.info("Архитектура CNN создана. Параметров: {}", model.numParams());
    }
    
    public void train() throws IOException {
        log.info("Загрузка MNIST тренировочного датасета...");
        
        DataSetIterator trainData = new MnistDataSetIterator(BATCH_SIZE, true, SEED);
        scaler.fit(trainData);
        trainData.setPreProcessor(scaler);
        
        log.info("Начало обучения на {} эпохах...", TRAINING_EPOCHS);
        
        for (int epoch = 1; epoch <= TRAINING_EPOCHS; epoch++) {
            log.info("Эпоха {}/{}", epoch, TRAINING_EPOCHS);
            model.fit(trainData);
            trainData.reset();
            double loss = model.score();
            log.info("Функция потерь после эпохи {}: {:.4f}", epoch, loss);
        }
        
        saveModel();
        log.info("Обучение завершено!");
    }
    
    public void evaluate() throws IOException {
        log.info("Загрузка MNIST тестового датасета...");
        
        DataSetIterator testData = new MnistDataSetIterator(BATCH_SIZE, false, SEED);
        testData.setPreProcessor(scaler);
        
        Evaluation evaluation = model.evaluate(testData);
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("РЕЗУЛЬТАТЫ ОЦЕНКИ МОДЕЛИ");
        System.out.println("=".repeat(50));
        
        System.out.printf("Общая точность (Accuracy): %.2f%%\n", evaluation.accuracy() * 100);
        System.out.printf("Precision (точность): %.4f\n", evaluation.precision());
        System.out.printf("Recall (полнота): %.4f\n", evaluation.recall());
        System.out.printf("F1-Score: %.4f\n", evaluation.f1());
        
        System.out.println("\nМатрица ошибок:");
        System.out.println(evaluation.confusionToString());
    }
    
    public void demo() throws IOException {
        log.info("Демонстрация работы модели...");
        
        DataSetIterator demoData = new MnistDataSetIterator(5, false, SEED);
        demoData.setPreProcessor(scaler);
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("ДЕМОНСТРАЦИЯ РАБОТЫ МОДЕЛИ");
        System.out.println("=".repeat(50));
        
        for (int i = 0; i < 5; i++) {
            if (!demoData.hasNext()) break;
            
            var dataSet = demoData.next();
            INDArray features = dataSet.getFeatures();
            INDArray labels = dataSet.getLabels();
            
            INDArray output = model.output(features);
            int predicted = Nd4j.argMax(output, 1).getInt(0);
            int actual = Nd4j.argMax(labels, 1).getInt(0);
            double confidence = output.getDouble(predicted);
            
            System.out.printf("\nПример %d:\n", i + 1);
            System.out.printf("  Реальная цифра: %d\n", actual);
            System.out.printf("  Предсказанная цифра: %d\n", predicted);
            System.out.printf("  Уверенность: %.2f%%\n", confidence * 100);
            System.out.printf("  Верно? %s\n", predicted == actual ? "✓" : "✗");
        }
    }
    
    public void classifyImage(String imagePath) {
        try {
            log.info("Классификация изображения: {}", imagePath);
            
            INDArray processedImage = preprocessImage(imagePath);
            INDArray output = model.output(processedImage);
            int predictedDigit = Nd4j.argMax(output, 1).getInt(0);
            double confidence = output.getDouble(predictedDigit);
            
            System.out.println("\n" + "=".repeat(50));
            System.out.println("РЕЗУЛЬТАТ КЛАССИФИКАЦИИ");
            System.out.println("=".repeat(50));
            
            System.out.printf("Изображение: %s\n", new File(imagePath).getName());
            System.out.printf("Предсказанная цифра: %d\n", predictedDigit);
            System.out.printf("Уверенность предсказания: %.2f%%\n", confidence * 100);
            
        } catch (Exception e) {
            log.error("Ошибка при классификации: {}", e.getMessage());
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
    
    private INDArray preprocessImage(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IOException("Файл не найден: " + imagePath);
        }
        
        BufferedImage originalImage = ImageIO.read(imageFile);
        BufferedImage resizedImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
        g.dispose();
        
        INDArray array = Nd4j.create(1, IMAGE_HEIGHT, IMAGE_WIDTH);
        
        for (int y = 0; y < IMAGE_HEIGHT; y++) {
            for (int x = 0; x < IMAGE_WIDTH; x++) {
                int pixel = resizedImage.getRaster().getSample(x, y, 0) & 0xFF;
                double normalizedValue = pixel / 255.0;
                array.putScalar(new int[]{0, y, x}, normalizedValue);
            }
        }
        
        scaler.transform(array);
        return array.reshape(1, IMAGE_CHANNELS, IMAGE_HEIGHT, IMAGE_WIDTH);
    }
    
    private void saveModel() {
        try {
            File modelFile = new File("mnist-cnn-model.zip");
            ModelSerializer.writeModel(model, modelFile, true);
            log.info("Модель сохранена: {}", modelFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Ошибка сохранения модели: {}", e.getMessage());
        }
    }
}
