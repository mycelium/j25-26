package org.example;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class App {
    private KNNClassifier classifier;

    public App(int k) {
        this.classifier = new KNNClassifier(k);
    }

    public void initializeTrainingData() {
        List<Point> trainingData = Arrays.asList( //трениовочные данные
                new Point(1.0, 2.0, "A"),
                new Point(1.5, 1.8, "A"),
                new Point(2.0, 2.5, "A"),
                new Point(0.5, 1.5, "A"),
                new Point(1.2, 2.2, "A"),
                new Point(5.0, 3.0, "B"),
                new Point(4.5, 2.8, "B"),
                new Point(5.5, 3.5, "B"),
                new Point(6.0, 2.0, "B"),
                new Point(4.8, 3.2, "B"),
                new Point(3.0, 6.0, "C"),
                new Point(2.5, 5.5, "C"),
                new Point(3.5, 6.5, "C"),
                new Point(2.0, 7.0, "C"),
                new Point(3.2, 5.8, "C")
        );
        classifier.addTrainingPoints(trainingData);
    }

    public String classifyPoint(double x, double y) {
        Point unknownPoint = new Point(x, y);
        String predictedLabel = classifier.classify(unknownPoint);
        System.out.println("Point " + unknownPoint + " classified as: " + predictedLabel);
        return predictedLabel;
    }

    public void generateChart(double x, double y, String outputPath) throws IOException {
        Point unknownPoint = new Point(x, y);
        List<Point> allPoints = new java.util.ArrayList<>();

        initializeTrainingData();
        List<Point> trainingPoints = Arrays.asList(
                new Point(1.0, 2.0, "A"), new Point(1.5, 1.8, "A"), new Point(2.0, 2.5, "A"),
                new Point(5.0, 3.0, "B"), new Point(4.5, 2.8, "B"), new Point(5.5, 3.5, "B"),
                new Point(3.0, 6.0, "C"), new Point(2.5, 5.5, "C"), new Point(3.5, 6.5, "C")
        );
        allPoints.addAll(trainingPoints);
        Chart.generateChart(allPoints, unknownPoint, outputPath);
    }

    public static void main(String[] args) {
        App app = new App(3); // K=3

        try {
            app.initializeTrainingData(); //"тренировочные данные"
            System.out.println("K-Nearest Neighbors Classifier"); //классифицируем

            app.classifyPoint(1.8, 2.0); //А
            app.classifyPoint(5.2, 3.1); //В
            app.classifyPoint(2.8, 6.2); //С

            app.generateChart(3.0, 4.0, "knn_chart.png"); //график пнг

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}