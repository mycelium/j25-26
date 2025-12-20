package org.numbers;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class ModelTrainer {

    private static final int EPOCHS = 2;

    public void train(MultiLayerNetwork model,
                      DataSetIterator trainData) {

        for (int i = 0; i < EPOCHS; i++) {
            System.out.println("Epoch " + (i + 1));
            model.fit(trainData);
        }
    }
}
