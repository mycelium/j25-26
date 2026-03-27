package org.example;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

public class App {
        public String getGreeting() {
                return "Hello World!";
        }

        public static void main(String[] args) throws Exception {
                System.out.println("Initializing MNIST CNN predictor...");

                MultiLayerNetwork model = ModelManager.getModel();

                ImagePredictor.predictFromImage(model, "4.png");
        }
}