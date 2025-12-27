package org.example;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class ModelTrainer {
    private final MultiLayerNetwork model;
    private static final int EPOCHS = 5;

    public ModelTrainer(MultiLayerNetwork model) {
        this.model = model;
    }

    public void train(DataSetIterator trainData, DataSetIterator testData) {
        model.init();


        for (int i = 0; i < EPOCHS; i++) {
            model.fit(trainData);
            trainData.reset();

            Evaluation eval = model.evaluate(testData);
            System.out.println("Epoch " + (i + 1) + " - Accuracy: " + eval.accuracy());
            testData.reset();
        }
    }
}