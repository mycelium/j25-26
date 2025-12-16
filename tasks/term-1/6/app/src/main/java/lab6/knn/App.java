package lab6.knn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class App {
    public static void main(String[] args) {
        System.out.println("KNN Classifier Demo");

        List<Point> trainingData = generateData(50);
        System.out.println("Generated " + trainingData.size() + " training points.");

        // 2. Define a new point
        Point newPoint = new Point(5.0, 5.0); // Input point
        System.out.println("New point to classify: " + newPoint);

        KNNClassifier classifier = new KNNClassifier();
        int k = 5;
        String predictedLabel = classifier.classify(trainingData, newPoint, k);
        System.out.println("Predicted class (k=" + k + "): " + predictedLabel);

        newPoint.setLabel(predictedLabel);

        Plotter plotter = new Plotter();
        plotter.plot(trainingData, newPoint, "knn_result.png");
    }

    private static List<Point> generateData(int pointsPerClass) {
        List<Point> data = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < pointsPerClass; i++) {
            double x = 2.0 + rand.nextGaussian();
            double y = 2.0 + rand.nextGaussian();
            data.add(new Point(x, y, "Class A"));
        }

        for (int i = 0; i < pointsPerClass; i++) {
            double x = 8.0 + rand.nextGaussian();
            double y = 8.0 + rand.nextGaussian();
            data.add(new Point(x, y, "Class B"));
        }

        for (int i = 0; i < pointsPerClass; i++) {
            double x = 8.0 + rand.nextGaussian();
            double y = 2.0 + rand.nextGaussian();
            data.add(new Point(x, y, "Class C"));
        }

        return data;
    }
}

