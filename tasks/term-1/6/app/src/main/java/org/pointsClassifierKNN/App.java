package org.pointsClassifierKNN;

import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        DataSimulator sim = new DataSimulator(2025);

        List<Point> train1 = sim.generateCircularClusters(3, 50, 0.12);
        List<Point> test1 = sim.generateRandomPoints(100);
        runDemo(train1, test1, "Три круговых кластера", 5);

        List<Point> train2 = sim.generateCornerGroups(4, 40);
        List<Point> test2 = sim.generateRandomPoints(100);
        runDemo(train2, test2, "Четыре угла", 7);

        List<Point> train3 = sim.generateDiagonalStripes(3, 120);
        List<Point> test3 = sim.generateRandomPoints(100);
        runDemo(train3, test3, "Диагональные полосы", 3);
    }

    private static void runDemo(List<Point> training, List<Point> testPoints, String title, int k) {
        KNNClassifier knn = new KNNClassifier(k);
        knn.train(training);

        List<String> predictions = knn.classifyMultiple(testPoints);
        List<Point> classified = new ArrayList<>();
        for (int i = 0; i < testPoints.size(); i++) {
            Point pt = testPoints.get(i);
            classified.add(new Point(pt.getX(), pt.getY(), predictions.get(i)));
        }

        Visualization.show(training, classified, title);
    }
}