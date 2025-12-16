package model.digitPredictionModel;

import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import java.io.File;

public class App {
    public static void main(String[] args) throws Exception {
        DataLoader dataLoader = new DataLoader();
        var data = dataLoader.loadData();

        ModelBuilder modelBuilder = new ModelBuilder();
        var model = modelBuilder.buildModel();

        ModelTrainer trainer = new ModelTrainer(model);
        trainer.train(data.getTrainData(), data.getTestData());

        ModelSerializer.writeModel(model, new File("mnist-cnn-model.zip"), true);
        System.out.println("Model saved to mnist-cnn-model.zip");

        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);

        if (args.length > 0) {
            String imagePath = args[0];
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                DigitPredictor predictor = new DigitPredictor(model, scaler);
                int prediction = predictor.predictFromImage(imagePath);
                System.out.println("Predicted digit from image '" + imageFile.getName() + "': " + prediction);
            } else {
                System.err.println("Image file not found: " + imagePath);
            }
        } else {
            data.getTestData().reset();
            var testBatch = data.getTestData().next();
            DigitPredictor predictor = new DigitPredictor(model, scaler);
            int prediction = predictor.predict(testBatch.getFeatures());
            int actual = testBatch.getLabels().argMax(1).getInt(0);
            System.out.println("Sample from MNIST — Predicted: " + prediction + ", Actual: " + actual);
        }
    }
}