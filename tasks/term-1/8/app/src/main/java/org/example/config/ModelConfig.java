package org.example.config;

/**
 * Конфигурация модели и обучения.
 */
public class ModelConfig {

    public static final int HEIGHT = 28;
    public static final int WIDTH = 28;
    public static final int CHANNELS = 1;

    public static final int OUTPUTS = 10;

    public static final int BATCH_SIZE = 64;
    public static final int EPOCHS = 10;

    public static final int SEED = 123;

    public static final String MODEL_PATH = "mnist-model.zip";
}