package knn;

import java.util.*;

public class App {
    public static void main(String[] args) {
        KNNClassifier knn = new KNNClassifier(3);
        Random rand = new Random(123);

        List<Point> allPoints = new ArrayList<>();

        generateTrainingData(knn, rand, allPoints);

        testPoints(knn, rand, allPoints);

        GraphPlotter plotter = new GraphPlotter(allPoints);
        plotter.display();
    }

    private static void generateTrainingData(KNNClassifier knn, Random rand, List<Point> allPoints) {
        for (int i = 0; i < 8; i++) {
            // Класс A
            Point a = new Point(10 + rand.nextDouble() * 30, 60 + rand.nextDouble() * 30, "A");
            knn.addTrainingPoint(a);
            allPoints.add(a);

            // Класс B
            Point b = new Point(60 + rand.nextDouble() * 30, 10 + rand.nextDouble() * 30, "B");
            knn.addTrainingPoint(b);
            allPoints.add(b);

            // Класс C
            Point c = new Point(40 + rand.nextDouble() * 20, 40 + rand.nextDouble() * 20, "C");
            knn.addTrainingPoint(c);
            allPoints.add(c);
        }
    }

    private static void testPoints(KNNClassifier knn, Random rand, List<Point> allPoints) {
        // Фиксированные тестовые точки
        classifyAndAdd(knn, 25, 75, allPoints);
        classifyAndAdd(knn, 75, 25, allPoints);
        classifyAndAdd(knn, 50, 50, allPoints);

        // Случайные тестовые точки
        for (int i = 0; i < 3; i++) {
            classifyAndAdd(knn, rand.nextDouble() * 100, rand.nextDouble() * 100, allPoints);
        }
    }

    private static void classifyAndAdd(KNNClassifier knn, double x, double y, List<Point> allPoints) {
        Point testPoint = new Point(x, y); // без метки
        String predictedLabel = knn.classify(testPoint);
        Point labeledTestPoint = new Point(x, y, predictedLabel);
        allPoints.add(labeledTestPoint);
        System.out.printf("(%.1f, %.1f) -> %s\n", x, y, predictedLabel);
    }
}
