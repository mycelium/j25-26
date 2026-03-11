package org.example.data;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.example.config.ModelConfig;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

/**
 * Загрузка датасета MNIST.
 */
public class MnistDataLoader {

    private final DataSetIterator train;
    private final DataSetIterator test;

    /**
     * Создаёт train и test итераторы MNIST.
     */
    public MnistDataLoader() throws Exception {

        train = new MnistDataSetIterator( ModelConfig.BATCH_SIZE, true, ModelConfig.SEED );
        test = new MnistDataSetIterator( ModelConfig.BATCH_SIZE, false, ModelConfig.SEED );

        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);

        train.setPreProcessor(scaler);
        test.setPreProcessor(scaler);
    }

    /**
     * @return итератор обучающего датасета
     */
    public DataSetIterator getTrainIterator() {
        return train;
    }

    /**
     * @return итератор тестового датасета
     */
    public DataSetIterator getTestIterator() {
        return test;
    }
}