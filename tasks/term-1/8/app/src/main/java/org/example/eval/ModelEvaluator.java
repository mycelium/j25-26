package org.example.eval;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 * Оценка точности модели.
 */
public class ModelEvaluator {

    /**
     * Вычисляет метрики на тестовом наборе.
     *
     * @param model обученная модель
     * @param test тестовый датасет
     */
    public static void evaluate( MultiLayerNetwork model, DataSetIterator test) 
    {

        Evaluation eval = model.evaluate(test);

        System.out.println(eval.stats());

        System.out.println("Accuracy: " + eval.accuracy());

        test.reset();
    }
}