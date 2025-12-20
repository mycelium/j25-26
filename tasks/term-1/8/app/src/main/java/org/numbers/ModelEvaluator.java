package org.numbers;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class ModelEvaluator {

    public void evaluate(MultiLayerNetwork model,
                         DataSetIterator testData) {

        Evaluation evaluation = model.evaluate(testData);
        System.out.println(evaluation.stats());
    }
}
