import jdk.incubator.vector.*;
import java.util.Random;

public class MNISTVectorCNN {

    //конфигурация Vector API
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    private static final int IMAGE_SIZE = 28;     //константы
    private static final int NUM_CLASSES = 10;
    private static final float LEARNING_RATE = 0.01f;
    private float[][] denseWeights;  // веса сети [10][784] - 28x28 = 784 пикселя
    private float[] denseBias;       // [10]

    private float[] lastInput; //для хранения последнего входа

    public MNISTVectorCNN() {
        System.out.println("Создаем CNN...");
        initializeWeights();
    }

    private void initializeWeights() {
        Random rand = new Random(42);
        denseWeights = new float[NUM_CLASSES][IMAGE_SIZE * IMAGE_SIZE]; //полносвязный слой: 784 входа -> 10 выходов
        denseBias = new float[NUM_CLASSES];

        float scale = (float) Math.sqrt(2.0 / (IMAGE_SIZE * IMAGE_SIZE)); //Xavier инициализация
        for (int i = 0; i < NUM_CLASSES; i++) {
            for (int j = 0; j < IMAGE_SIZE * IMAGE_SIZE; j++) {
                denseWeights[i][j] = rand.nextFloat() * scale * 2 - scale; // [-scale, scale]
            }
            denseBias[i] = 0.1f;
        }
    }

    public float[] predict(float[][] image) { //предсказание для 1 изображения
        //выравниваем изображение
        float[] flattened = new float[IMAGE_SIZE * IMAGE_SIZE];
        int idx = 0;
        for (int i = 0; i < IMAGE_SIZE; i++) {
            System.arraycopy(image[i], 0, flattened, idx, IMAGE_SIZE);
            idx += IMAGE_SIZE;
        }
        lastInput = flattened;

        //полносвязный слой (векторизованное умножение)
        float[] output = denseLayer(flattened);

        //Softmax
        return softmax(output);
    }

     //полносвязный слой с векторизацией
    private float[] denseLayer(float[] input) {
        float[] output = new float[NUM_CLASSES];

        for (int neuron = 0; neuron < NUM_CLASSES; neuron++) {
            //векторизованное скалярное произведение
            float sum = vectorizedDotProduct(denseWeights[neuron], input);
            output[neuron] = sum + denseBias[neuron];
        }
        return output;
    }

     //векторизованное скалярное произведение с использованием Vector API
    private float vectorizedDotProduct(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Массивы разной длины: " + a.length + " vs " + b.length);
        }

        FloatVector sumVector = FloatVector.zero(SPECIES);
        int i = 0;
        int upperBound = SPECIES.loopBound(a.length);

        for (; i < upperBound; i += SPECIES.length()) { //векторизованная часть
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
            sumVector = sumVector.add(va.mul(vb));
        }

        float sum = sumVector.reduceLanes(VectorOperators.ADD);

        for (; i < a.length; i++) { //скалярная обработка оставшихся элементов
            sum += a[i] * b[i];
        }
        return sum;
    }

    // Softmax преобразование
    private float[] softmax(float[] input) {
        float[] result = new float[input.length];

        float max = Float.NEGATIVE_INFINITY; //находим максимум для численной стабильности
        for (float val : input) {
            if (val > max) max = val;
        }

        float sum = 0; //вычисляем экспоненты
        for (int i = 0; i < input.length; i++) {
            result[i] = (float) Math.exp(input[i] - max);
            sum += result[i];
        }

        for (int i = 0; i < result.length; i++) { //нормализация
            result[i] /= sum;
        }
        return result;
    }

    //обучение на одном примере
    public float train(float[][] image, int trueLabel) {
        float[] predictions = predict(image); //прямой проход
        float loss = crossEntropyLoss(predictions, trueLabel); //вычисление потерь
        backpropagate(predictions, trueLabel); //обратное распространение
        return loss;
    }

    private float crossEntropyLoss(float[] predictions, int trueLabel) { //функция потерь
        return (float) -Math.log(Math.max(predictions[trueLabel], 1e-10f));
    }

    private void backpropagate(float[] predictions, int trueLabel) { //обратное распространение
        //градиент для softmax + cross-entropy: predictions - one_hot(trueLabel)
        float[] gradOutput = predictions.clone();
        gradOutput[trueLabel] -= 1.0f;

        for (int i = 0; i < NUM_CLASSES; i++) { //обновление весов и смещений
            denseBias[i] -= LEARNING_RATE * gradOutput[i];
            updateWeightsVectorized(denseWeights[i], lastInput, gradOutput[i]);
        }
    }

    //векторизованное обновление весов
    private void updateWeightsVectorized(float[] weights, float[] input, float gradient) {
        float learningRateGrad = LEARNING_RATE * gradient;

        int i = 0;
        int upperBound = SPECIES.loopBound(weights.length);
        for (; i < upperBound; i += SPECIES.length()) {
            FloatVector w = FloatVector.fromArray(SPECIES, weights, i);
            FloatVector x = FloatVector.fromArray(SPECIES, input, i);
            FloatVector update = x.mul(learningRateGrad);
            w.sub(update).intoArray(weights, i);
        }

        //скалярная обработка оставшихся
        for (; i < weights.length; i++) {
            weights[i] -= learningRateGrad * input[i];
        }
    }

    //предсказание класса (цифры 0-9)
    public int predictClass(float[][] image) {
        float[] probs = predict(image);
        int best = 0;
        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > probs[best]) {
                best = i;
            }
        }
        return best;
    }

    // генерация синтетических данных MNIST
    public static float[][][] generateSyntheticData(int numSamples, int seed) {
        Random rand = new Random(seed);
        float[][][] data = new float[numSamples][IMAGE_SIZE][IMAGE_SIZE];
        for (int n = 0; n < numSamples; n++) {
            int digit = rand.nextInt(10);

            //создаем простой паттерн цифры
            for (int i = 0; i < IMAGE_SIZE; i++) {
                for (int j = 0; j < IMAGE_SIZE; j++) {
                    //простой паттерн - центр изображения имеет более высокие значения
                    boolean isCenter = (i > 8 && i < 20 && j > 8 && j < 20);
                    boolean isDigit = false;

                    switch (digit) { //разные паттерны для разных цифр
                        case 0: //круг
                            isDigit = (i-14)*(i-14) + (j-14)*(j-14) < 64;
                            break;
                        case 1: //вертикальная линия
                            isDigit = (j > 10 && j < 18);
                            break;
                        case 8: //два круга (восьмерка)
                            isDigit = (i > 6 && i < 12 && j > 8 && j < 20) ||
                                    (i > 14 && i < 20 && j > 8 && j < 20);
                            break;
                        default: //случайные паттерны для других цифр (упрощенная версия)
                            isDigit = (digit % 2 == 0) ? (i % 3 == 0) : (j % 3 == 0);
                    }

                    float value = 0.1f; //фоновый шум
                    if (isCenter) value += 0.3f;
                    if (isDigit) value += 0.6f;

                    data[n][i][j] = Math.min(value, 1.0f);
                }
            }
        }

        return data;
    }

    public static void main(String[] args) {
        System.out.println("CNN для MNIST с Vector API\n");

        MNISTVectorCNN cnn = new MNISTVectorCNN(); //создание сети

        //данные для обучения
        int numSamples = 200; //колво примеров
        float[][][] trainImages = generateSyntheticData(numSamples, 42);
        int[] trainLabels = new int[numSamples];
        Random rand = new Random(42);
        for (int i = 0; i < numSamples; i++) {
            trainLabels[i] = rand.nextInt(10);
        }

        System.out.println("Генерируем " + numSamples + " примеров...");
        System.out.println("Первые 5 меток: ");
        for (int i = 0; i < 5; i++) {
            System.out.print(trainLabels[i] + " ");
        }
        System.out.println("\n");

        //обучение
        System.out.println("Обучение...");
        int epochs = 3;

        for (int epoch = 0; epoch < epochs; epoch++) {
            float totalLoss = 0;
            int correct = 0;
            for (int i = 0; i < numSamples; i++) {
                float loss = cnn.train(trainImages[i], trainLabels[i]);
                totalLoss += loss;
                int predicted = cnn.predictClass(trainImages[i]);
                if (predicted == trainLabels[i]) {
                    correct++;
                }

                //прогресс
                if ((i + 1) % 40 == 0) {
                    System.out.printf("Эпоха %d: %d/%d (%.0f%%) Loss: %.3f\n",
                            epoch + 1, i + 1, numSamples,
                            (i + 1) * 100.0 / numSamples, loss);
                }
            }

            //результаты эпохи
            System.out.printf("\nЭпоха %d/%d завершена:\n", epoch + 1, epochs);
            System.out.printf("  Точность: %d/%d (%.1f%%)\n\n", correct, numSamples, (float) correct / numSamples * 100);
        }

        //тест на 10 примерах
        System.out.println(" Тестирование на 10 примерах");
        int testSamples = Math.min(10, numSamples);
        int testCorrect = 0;

        for (int i = 0; i < testSamples; i++) {
            int predicted = cnn.predictClass(trainImages[i]);
            int actual = trainLabels[i];
            boolean isCorrect = (predicted == actual);
            if (isCorrect) testCorrect++;
            System.out.printf("Пример %2d: Предсказано = %d, Истина = %d %s\n",
                    i + 1, predicted, actual, isCorrect ? "✓" : "✗");
        }
    }
}