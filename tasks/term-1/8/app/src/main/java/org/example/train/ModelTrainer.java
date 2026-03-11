package org.example.train;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.example.config.ModelConfig;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 * Обучение модели.
 */
public class ModelTrainer {

    /**
     * Выполняет обучение модели.
     *
     * @param model нейронная сеть
     * @param train итератор обучающих данных
     */
    public static void train(MultiLayerNetwork model, DataSetIterator train) {

        for (int i = 0; i < ModelConfig.EPOCHS; i++) {

            model.fit(train);

            train.reset();

            System.out.println("Epoch " + (i+1) + " finished");
        }
    }
}