package org.example;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.example.data.MnistDataLoader;
import org.example.eval.ModelEvaluator;
import org.example.model.DigitClassifierModel;
import org.example.predict.DigitPredictor;
import org.example.train.ModelTrainer;
import org.example.util.ModelIO;

public class App {
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("Usage:");
            System.out.println("train");
            System.out.println("predict <image>");
            return;
        }

        if (args[0].equals("train")) {

            MnistDataLoader loader = new MnistDataLoader();

            MultiLayerNetwork model = DigitClassifierModel.build();

            ModelTrainer.train(model, loader.getTrainIterator());

            ModelEvaluator.evaluate(model, loader.getTestIterator());

            ModelIO.save(model);

        } 
        else if (args[0].equals("predict")) {

            MultiLayerNetwork model = ModelIO.load();

            int digit = DigitPredictor.predict(model, args[1]);

            System.out.println("Predicted digit: " + digit);
        }
        else {
            System.out.println("Unknown command: " + args[0]);
            System.out.println("Usage:");
            System.out.println("train");
            System.out.println("predict <image>");
        }
    }
}