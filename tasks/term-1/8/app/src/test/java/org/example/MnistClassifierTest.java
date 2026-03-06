package org.example;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.jupiter.api.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.jupiter.api.Assertions.*;

/*
Важные замечания:
- Тесты проверяют корректность конфигурации CNN и вспомогательных функций.
- Полное обучение модели занимает много времени, поэтому тестируется
  только инициализация и базовая функциональность.
- Тесты охватывают нормализацию, инверсию, argmax и валидацию входных данных.
*/

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MnistClassifierTest {

    private static final double DELTA = 0.0001;

    // Category 1: Model Configuration

    @Test
    @Order(1)
    @DisplayName("Test 1.1: Build configuration returns non-null")
    void testBuildConfigurationNotNull() {
        MnistClassifier classifier = new MnistClassifier();
        MultiLayerConfiguration config = classifier.buildConfiguration();
        
        assertNotNull(config, "buildConfiguration() returned null. "
                + "The method must return a valid MultiLayerConfiguration object.");
    }

    @Test
    @Order(2)
    @DisplayName("Test 1.2: Configuration has 6 layers")
    void testConfigurationLayerCount() {
        MnistClassifier classifier = new MnistClassifier();
        MultiLayerConfiguration config = classifier.buildConfiguration();
        
        assertEquals(6, config.getConfs().size(),
                "buildConfiguration() should produce a network with 6 layers, but got "
                        + config.getConfs().size() + " layers.");
    }

    @Test
    @Order(3)
    @DisplayName("Test 1.3: First layer is ConvolutionLayer")
    void testFirstLayerType() {
        MnistClassifier classifier = new MnistClassifier();
        MultiLayerConfiguration config = classifier.buildConfiguration();
        
        assertTrue(config.getConf(0).getLayer() instanceof ConvolutionLayer,
                "Layer 0 should be ConvolutionLayer, but got "
                        + config.getConf(0).getLayer().getClass().getSimpleName());
    }

    @Test
    @Order(4)
    @DisplayName("Test 1.4: Second layer is SubsamplingLayer (MaxPool)")
    void testSecondLayerType() {
        MnistClassifier classifier = new MnistClassifier();
        MultiLayerConfiguration config = classifier.buildConfiguration();
        
        assertTrue(config.getConf(1).getLayer() instanceof SubsamplingLayer,
                "Layer 1 should be SubsamplingLayer (MaxPool), but got "
                        + config.getConf(1).getLayer().getClass().getSimpleName());
    }

    @Test
    @Order(5)
    @DisplayName("Test 1.5: Fifth layer is DenseLayer")
    void testFifthLayerType() {
        MnistClassifier classifier = new MnistClassifier();
        MultiLayerConfiguration config = classifier.buildConfiguration();
        
        assertTrue(config.getConf(4).getLayer() instanceof DenseLayer,
                "Layer 4 should be DenseLayer, but got "
                        + config.getConf(4).getLayer().getClass().getSimpleName());
    }

    @Test
    @Order(6)
    @DisplayName("Test 1.6: Last layer is OutputLayer")
    void testLastLayerType() {
        MnistClassifier classifier = new MnistClassifier();
        MultiLayerConfiguration config = classifier.buildConfiguration();
        
        assertTrue(config.getConf(5).getLayer() instanceof OutputLayer,
                "Layer 5 (last) should be OutputLayer, but got "
                        + config.getConf(5).getLayer().getClass().getSimpleName());
    }

    // Category 2: Model Initialization

    @Test
    @Order(10)
    @DisplayName("Test 2.1: Model initialization creates non-null model")
    void testModelInitialization() {
        MnistClassifier classifier = new MnistClassifier();
        classifier.initModel();
        
        assertNotNull(classifier.getModel(),
                "getModel() returned null after initModel(). "
                        + "initModel() must create and store a MultiLayerNetwork instance.");
    }

    @Test
    @Order(11)
    @DisplayName("Test 2.2: Model has correct number of parameters")
    void testModelParameters() {
        MnistClassifier classifier = new MnistClassifier();
        classifier.initModel();
        
        MultiLayerNetwork model = classifier.getModel();
        assertTrue(model.numParams() > 0,
                "Model should have parameters after initialization, but numParams() returned "
                        + model.numParams());
    }

    @Test
    @Order(12)
    @DisplayName("Test 2.3: Different seeds create different models")
    void testDifferentSeeds() {
        MnistClassifier classifier1 = new MnistClassifier(123);
        MnistClassifier classifier2 = new MnistClassifier(456);
        
        classifier1.initModel();
        classifier2.initModel();
        
        assertNotEquals(classifier1.getSeed(), classifier2.getSeed());
    }

    @Test
    @Order(13)
    @DisplayName("Test 2.4: Model can produce output after init")
    void testModelOutput() {
        MnistClassifier classifier = new MnistClassifier();
        classifier.initModel();
        
        // Create dummy input (1x784 flat image)
        INDArray input = Nd4j.zeros(1, 784);
        INDArray output = classifier.predictProbabilities(input);
        
        assertNotNull(output,
                "predictProbabilities(zeros(1,784)) returned null after initModel(). "
                        + "The model must produce output for valid input.");
        assertEquals(10, output.columns(),
                "predictProbabilities() output should have 10 columns (one per digit 0-9), "
                        + "but got " + output.columns() + " columns. Output shape: " + java.util.Arrays.toString(output.shape()));
    }

    // Category 3: Pixel Normalization

    @Test
    @Order(20)
    @DisplayName("Test 3.1: Normalize pixels - basic case")
    void testNormalizePixelsBasic() {
        double[] pixels = {0, 127.5, 255};
        double[] normalized = MnistClassifier.normalizePixels(pixels);
        
        double[][] cases = {{0, 0.0}, {1, 0.5}, {2, 1.0}};
        for (double[] c : cases) {
            int idx = (int) c[0];
            assertEquals(c[1], normalized[idx], DELTA,
                    "normalizePixels([0, 127.5, 255])[" + idx + "] should be " + c[1]
                            + ", but got " + normalized[idx]
                            + ". Input pixel: " + pixels[idx] + ", expected: pixel/255");
        }
    }

    @Test
    @Order(21)
    @DisplayName("Test 3.2: Normalize pixels - all zeros")
    void testNormalizePixelsAllZeros() {
        double[] pixels = {0, 0, 0, 0};
        double[] normalized = MnistClassifier.normalizePixels(pixels);
        
        for (double val : normalized) {
            assertEquals(0.0, val, DELTA);
        }
    }

    @Test
    @Order(22)
    @DisplayName("Test 3.3: Normalize pixels - all 255")
    void testNormalizePixelsAll255() {
        double[] pixels = {255, 255, 255, 255};
        double[] normalized = MnistClassifier.normalizePixels(pixels);
        
        for (double val : normalized) {
            assertEquals(1.0, val, DELTA);
        }
    }

    @Test
    @Order(23)
    @DisplayName("Test 3.4: Normalize pixels - null input")
    void testNormalizePixelsNull() {
        double[] normalized = MnistClassifier.normalizePixels(null);
        
        assertNull(normalized,
                "normalizePixels(null) should return null, but got a non-null array of length "
                        + (normalized != null ? normalized.length : "N/A"));
    }

    @Test
    @Order(24)
    @DisplayName("Test 3.5: Normalize pixels - empty array")
    void testNormalizePixelsEmpty() {
        double[] pixels = {};
        double[] normalized = MnistClassifier.normalizePixels(pixels);
        
        assertEquals(0, normalized.length,
                "normalizePixels({}) should return empty array, but got array of length " + normalized.length);
    }

    // Category 4: Pixel Inversion

    @Test
    @Order(30)
    @DisplayName("Test 4.1: Invert pixels - basic case")
    void testInvertPixelsBasic() {
        double[] pixels = {0.0, 0.5, 1.0};
        double[] inverted = MnistClassifier.invertPixels(pixels);
        
        assertEquals(1.0, inverted[0], DELTA);
        assertEquals(0.5, inverted[1], DELTA);
        assertEquals(0.0, inverted[2], DELTA);
    }

    @Test
    @Order(31)
    @DisplayName("Test 4.2: Invert pixels - all zeros become ones")
    void testInvertPixelsAllZeros() {
        double[] pixels = {0.0, 0.0, 0.0};
        double[] inverted = MnistClassifier.invertPixels(pixels);
        
        for (double val : inverted) {
            assertEquals(1.0, val, DELTA);
        }
    }

    @Test
    @Order(32)
    @DisplayName("Test 4.3: Invert pixels - all ones become zeros")
    void testInvertPixelsAllOnes() {
        double[] pixels = {1.0, 1.0, 1.0};
        double[] inverted = MnistClassifier.invertPixels(pixels);
        
        for (double val : inverted) {
            assertEquals(0.0, val, DELTA);
        }
    }

    @Test
    @Order(33)
    @DisplayName("Test 4.4: Invert pixels - null input")
    void testInvertPixelsNull() {
        double[] inverted = MnistClassifier.invertPixels(null);
        
        assertNull(inverted,
                "invertPixels(null) should return null, but got a non-null array of length "
                        + (inverted != null ? inverted.length : "N/A"));
    }

    @Test
    @Order(34)
    @DisplayName("Test 4.5: Double inversion returns original")
    void testDoubleInversion() {
        double[] original = {0.2, 0.4, 0.6, 0.8};
        double[] inverted = MnistClassifier.invertPixels(original);
        double[] restored = MnistClassifier.invertPixels(inverted);
        
        for (int i = 0; i < original.length; i++) {
            assertEquals(original[i], restored[i], DELTA);
        }
    }

    // Category 5: Calculate Mean

    @Test
    @Order(40)
    @DisplayName("Test 5.1: Calculate mean - basic case")
    void testCalculateMeanBasic() {
        double[] pixels = {0.0, 0.5, 1.0};
        double mean = MnistClassifier.calculateMean(pixels);
        
        assertEquals(0.5, mean, DELTA);
    }

    @Test
    @Order(41)
    @DisplayName("Test 5.2: Calculate mean - all same values")
    void testCalculateMeanSameValues() {
        double[] pixels = {0.7, 0.7, 0.7, 0.7};
        double mean = MnistClassifier.calculateMean(pixels);
        
        assertEquals(0.7, mean, DELTA);
    }

    @Test
    @Order(42)
    @DisplayName("Test 5.3: Calculate mean - null input")
    void testCalculateMeanNull() {
        double mean = MnistClassifier.calculateMean(null);
        
        assertEquals(0.0, mean, DELTA,
                "calculateMean(null) should return 0.0, but got " + mean);
    }

    @Test
    @Order(43)
    @DisplayName("Test 5.4: Calculate mean - empty array")
    void testCalculateMeanEmpty() {
        double[] pixels = {};
        double mean = MnistClassifier.calculateMean(pixels);
        
        assertEquals(0.0, mean, DELTA,
                "calculateMean({}) should return 0.0 for empty array, but got " + mean);
    }

    // Category 6: Needs Inversion Check

    @Test
    @Order(50)
    @DisplayName("Test 6.1: Needs inversion - white background (mean > 0.5)")
    void testNeedsInversionTrue() {
        double[] pixels = {0.8, 0.9, 0.7, 0.6}; // mean = 0.75
        
        assertTrue(MnistClassifier.needsInversion(pixels));
    }

    @Test
    @Order(51)
    @DisplayName("Test 6.2: Needs inversion - black background (mean < 0.5)")
    void testNeedsInversionFalse() {
        double[] pixels = {0.1, 0.2, 0.3, 0.4}; // mean = 0.25
        
        assertFalse(MnistClassifier.needsInversion(pixels));
    }

    @Test
    @Order(52)
    @DisplayName("Test 6.3: Needs inversion - exactly 0.5")
    void testNeedsInversionExactlyHalf() {
        double[] pixels = {0.5, 0.5, 0.5, 0.5};
        
        assertFalse(MnistClassifier.needsInversion(pixels));
    }

    // Category 7: Argmax Function

    private void assertArgmax(double[] probs, int expected, String desc) {
        int result = MnistClassifier.argmax(probs);
        assertEquals(expected, result,
                "argmax(" + desc + ") should return " + expected + ", but got " + result
                        + ". Input: " + (probs != null ? java.util.Arrays.toString(probs) : "null"));
    }

    @Test
    @Order(60)
    @DisplayName("Test 7.1: Argmax - first element is max")
    void testArgmaxFirst() {
        assertArgmax(new double[]{0.9, 0.05, 0.03, 0.02}, 0, "[0.9, 0.05, 0.03, 0.02]");
    }

    @Test
    @Order(61)
    @DisplayName("Test 7.2: Argmax - last element is max")
    void testArgmaxLast() {
        assertArgmax(new double[]{0.1, 0.1, 0.1, 0.7}, 3, "[0.1, 0.1, 0.1, 0.7]");
    }

    @Test
    @Order(62)
    @DisplayName("Test 7.3: Argmax - middle element is max")
    void testArgmaxMiddle() {
        assertArgmax(new double[]{0.1, 0.1, 0.6, 0.1, 0.1}, 2, "[0.1, 0.1, 0.6, 0.1, 0.1]");
    }

    @Test
    @Order(63)
    @DisplayName("Test 7.4: Argmax - 10 classes (MNIST)")
    void testArgmax10Classes() {
        assertArgmax(new double[]{0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.9, 0.02, 0.01}, 7,
                "10-class MNIST probabilities with max at index 7");
    }

    @Test
    @Order(64)
    @DisplayName("Test 7.5: Argmax - null input")
    void testArgmaxNull() {
        assertArgmax(null, -1, "null");
    }

    @Test
    @Order(65)
    @DisplayName("Test 7.6: Argmax - empty array")
    void testArgmaxEmpty() {
        assertArgmax(new double[]{}, -1, "empty array");
    }

    @Test
    @Order(66)
    @DisplayName("Test 7.7: Argmax - single element")
    void testArgmaxSingle() {
        assertArgmax(new double[]{0.5}, 0, "[0.5]");
    }

    // Category 8: Input Validation

    @Test
    @Order(70)
    @DisplayName("Test 8.1: Valid input - 784 pixels")
    void testValidInput784() {
        double[] pixels = new double[784];
        
        assertTrue(MnistClassifier.isValidInput(pixels),
                "isValidInput(double[784]) should return true (28x28=784 is the correct MNIST size)");
    }

    @Test
    @Order(71)
    @DisplayName("Test 8.2: Invalid input - wrong size (100 pixels)")
    void testInvalidInputWrongSize() {
        double[] pixels = new double[100];
        
        assertFalse(MnistClassifier.isValidInput(pixels),
                "isValidInput(double[100]) should return false (expected 784 pixels for 28x28 image, got 100)");
    }

    @Test
    @Order(72)
    @DisplayName("Test 8.3: Invalid input - null")
    void testInvalidInputNull() {
        assertFalse(MnistClassifier.isValidInput(null),
                "isValidInput(null) should return false (null is not a valid pixel array)");
    }

    @Test
    @Order(73)
    @DisplayName("Test 8.4: Invalid input - empty array")
    void testInvalidInputEmpty() {
        double[] pixels = {};
        
        assertFalse(MnistClassifier.isValidInput(pixels),
                "isValidInput(double[0]) should return false (empty array is not a valid 28x28 image)");
    }

    @Test
    @Order(74)
    @DisplayName("Test 8.5: Invalid input - too large")
    void testInvalidInputTooLarge() {
        double[] pixels = new double[1000];
        
        assertFalse(MnistClassifier.isValidInput(pixels),
                "isValidInput(double[1000]) should return false (expected 784 pixels, got 1000)");
    }

    // Category 9: Constants

    @Test
    @Order(80)
    @DisplayName("Test 9.1: Image height is 28")
    void testImageHeight() {
        assertEquals(28, MnistClassifier.getImageHeight());
    }

    @Test
    @Order(81)
    @DisplayName("Test 9.2: Image width is 28")
    void testImageWidth() {
        assertEquals(28, MnistClassifier.getImageWidth());
    }

    @Test
    @Order(82)
    @DisplayName("Test 9.3: Output classes is 10")
    void testOutputClasses() {
        assertEquals(10, MnistClassifier.getOutputClasses());
    }

    @Test
    @Order(83)
    @DisplayName("Test 9.4: 28x28 = 784 pixels")
    void testTotalPixels() {
        assertEquals(784, MnistClassifier.getImageHeight() * MnistClassifier.getImageWidth());
    }

    // Category 10: Error Handling

    @Test
    @Order(90)
    @DisplayName("Test 10.1: Predict throws exception when model not initialized")
    void testPredictWithoutInit() {
        MnistClassifier classifier = new MnistClassifier();
        INDArray input = Nd4j.zeros(1, 784);
        
        assertThrows(IllegalStateException.class, () -> classifier.predict(input),
                "predict() should throw IllegalStateException when model is not initialized via initModel()");
    }

    @Test
    @Order(91)
    @DisplayName("Test 10.2: Predict throws exception for null input")
    void testPredictNullInput() {
        MnistClassifier classifier = new MnistClassifier();
        classifier.initModel();
        
        assertThrows(IllegalArgumentException.class, () -> classifier.predict(null),
                "predict(null) should throw IllegalArgumentException for null input");
    }

    @Test
    @Order(92)
    @DisplayName("Test 10.3: PredictProbabilities throws exception when model not initialized")
    void testPredictProbabilitiesWithoutInit() {
        MnistClassifier classifier = new MnistClassifier();
        INDArray input = Nd4j.zeros(1, 784);
        
        assertThrows(IllegalStateException.class, () -> classifier.predictProbabilities(input),
                "predictProbabilities() should throw IllegalStateException when model is not initialized");
    }

    @Test
    @Order(93)
    @DisplayName("Test 10.4: Evaluate throws exception when model not initialized")
    void testEvaluateWithoutInit() {
        MnistClassifier classifier = new MnistClassifier();
        
        assertThrows(IllegalStateException.class, () -> classifier.evaluate(64),
                "evaluate() should throw IllegalStateException when model is not initialized");
    }
}

