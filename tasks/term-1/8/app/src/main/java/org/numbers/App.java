package org.numbers;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class App {

    public static void main(String[] args) {

        System.out.println("CNN MNIST Classification");

        try {
            Data data = new Data();
            DataSetIterator train = data.loadTrain();
            DataSetIterator test = data.loadTest();

            ModelBuilder builder = new ModelBuilder();
            MultiLayerNetwork model = builder.build();

            ModelTrainer trainer = new ModelTrainer();
            trainer.train(model, train);

            ModelEvaluator evaluator = new ModelEvaluator();
            evaluator.evaluate(model, test);

        } catch (Exception e) {
            System.out.println("Error during execution:");
            e.printStackTrace();
        }
    }
}
