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
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.datavec.image.loader.NativeImageLoader;

import java.io.File;
import java.io.IOException;

/**
 * Классификатор рукописных цифр на основе CNN и набора данных MNIST.
 */
public class MnistClassifier {

    private static final int IMAGE_HEIGHT = 28;
    private static final int IMAGE_WIDTH = 28;
    private static final int CHANNELS = 1;
    private static final int OUTPUT_CLASSES = 10;

    private MultiLayerNetwork model;
    private final int seed;

    public MnistClassifier() {
        this(123);
    }

    public MnistClassifier(int seed) {
        this.seed = seed;
    }

    /**
     * Создает конфигурацию модели CNN.
     */
    public MultiLayerConfiguration buildConfiguration() {
        return new NeuralNetConfiguration.Builder()
                .seed(seed)
                .l2(0.0005)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        .nIn(CHANNELS)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(5, 5)
                        .stride(1, 1)
                        .nOut(50)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(4, new DenseLayer.Builder()
                        .activation(Activation.RELU)
                        .nOut(500)
                        .build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(OUTPUT_CLASSES)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(IMAGE_HEIGHT, IMAGE_WIDTH, CHANNELS))
                .build();
    }

    /**
     * Инициализирует модель.
     */
    public void initModel() {
        MultiLayerConfiguration conf = buildConfiguration();
        model = new MultiLayerNetwork(conf);
        model.init();
    }

    /**
     * Инициализирует модель с заданной конфигурацией.
     */
    public void initModel(MultiLayerConfiguration conf) {
        model = new MultiLayerNetwork(conf);
        model.init();
    }

    /**
     * Обучает модель на MNIST данных.
     */
    public void train(int epochs, int batchSize) throws IOException {
        if (model == null) {
            initModel();
        }
        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, seed);
        model.fit(mnistTrain, epochs);
    }

    /**
     * Оценивает модель на тестовых данных.
     */
    public Evaluation evaluate(int batchSize) throws IOException {
        if (model == null) {
            throw new IllegalStateException("Model not initialized");
        }
        DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, seed);
        return model.evaluate(mnistTest);
    }

    /**
     * Предсказывает класс для входных данных.
     * @param input массив 1x784 (плоское изображение 28x28)
     * @return предсказанный класс (0-9)
     */
    public int predict(INDArray input) {
        if (model == null) {
            throw new IllegalStateException("Model not initialized");
        }
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        int[] predictions = model.predict(input);
        return predictions[0];
    }

    /**
     * Возвращает вероятности для всех классов.
     */
    public INDArray predictProbabilities(INDArray input) {
        if (model == null) {
            throw new IllegalStateException("Model not initialized");
        }
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        return model.output(input);
    }

    /**
     * Предсказывает класс для изображения из файла.
     */
    public int predictImage(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }
        INDArray image = preprocessImage(file);
        return predict(image);
    }

    /**
     * Предобрабатывает изображение для классификации.
     */
    public INDArray preprocessImage(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }

        NativeImageLoader loader = new NativeImageLoader(IMAGE_HEIGHT, IMAGE_WIDTH, CHANNELS);
        INDArray image = loader.asMatrix(file);

        // Нормализация
        image.divi(255.0);

        // Инверсия если нужно (белая цифра на черном фоне)
        if (image.meanNumber().doubleValue() > 0.5) {
            image = image.rsub(1.0);
        }

        // Преобразование в плоский вектор
        return image.reshape(1, IMAGE_HEIGHT * IMAGE_WIDTH);
    }

    /**
     * Нормализует массив пикселей.
     */
    public static double[] normalizePixels(double[] pixels) {
        if (pixels == null) {
            return null;
        }
        double[] normalized = new double[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            normalized[i] = pixels[i] / 255.0;
        }
        return normalized;
    }

    /**
     * Инвертирует нормализованные пиксели (для изображений с белым фоном).
     */
    public static double[] invertPixels(double[] pixels) {
        if (pixels == null) {
            return null;
        }
        double[] inverted = new double[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            inverted[i] = 1.0 - pixels[i];
        }
        return inverted;
    }

    /**
     * Вычисляет среднее значение пикселей.
     */
    public static double calculateMean(double[] pixels) {
        if (pixels == null || pixels.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (double pixel : pixels) {
            sum += pixel;
        }
        return sum / pixels.length;
    }

    /**
     * Проверяет, нужна ли инверсия изображения.
     */
    public static boolean needsInversion(double[] normalizedPixels) {
        return calculateMean(normalizedPixels) > 0.5;
    }

    /**
     * Возвращает индекс максимального значения (предсказанный класс).
     */
    public static int argmax(double[] probabilities) {
        if (probabilities == null || probabilities.length == 0) {
            return -1;
        }
        int maxIndex = 0;
        double maxValue = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxValue) {
                maxValue = probabilities[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Проверяет валидность входных данных.
     */
    public static boolean isValidInput(double[] pixels) {
        if (pixels == null) {
            return false;
        }
        if (pixels.length != IMAGE_HEIGHT * IMAGE_WIDTH) {
            return false;
        }
        return true;
    }

    public MultiLayerNetwork getModel() {
        return model;
    }

    public int getSeed() {
        return seed;
    }

    public static int getImageHeight() {
        return IMAGE_HEIGHT;
    }

    public static int getImageWidth() {
        return IMAGE_WIDTH;
    }

    public static int getOutputClasses() {
        return OUTPUT_CLASSES;
    }
}

