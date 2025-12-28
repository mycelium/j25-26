package org.example;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        int classCount = generateClassCount();
        System.out.println("Number of classes: " + classCount);

        List<Point> dataset = buildTrainingSet(classCount);
        Point testSample = createRandomPoint();

        KNN classifier = new KNN(dataset, 10);
        String resultLabel = classifier.classify(testSample);

        System.out.println("New point: " + testSample);
        System.out.println("Predicted class: " + resultLabel);

        var plot = Plotter.createPlot(dataset, testSample, resultLabel);
        Plotter.showPlot(plot);
    }

    private static int generateClassCount() {
        return 3 + RANDOM.nextInt(8);
    }

    private static List<Point> buildTrainingSet(int classes) {
        List<Point> points = new LinkedList<>();

        for (int index = 0; index < classes; index++) {
            String className = createClassLabel(index);
            double[] center = generateCenter();

            int samples = 30;
            while (samples-- > 0) {
                points.add(generatePointNear(center[0], center[1], className));
            }
        }
        return points;
    }

    private static String createClassLabel(int index) {
        return "Class_" + (char) ('A' + index);
    }

    private static double[] generateCenter() {
        return new double[]{
                RANDOM.nextDouble() * 20 - 10,
                RANDOM.nextDouble() * 20 - 10
        };
    }

    private static Point generatePointNear(double cx, double cy, String label) {
        double px = cx + RANDOM.nextDouble() * 2 - 1;
        double py = cy + RANDOM.nextDouble() * 2 - 1;
        return new Point(px, py, label);
    }

    private static Point createRandomPoint() {
        return new Point(
                RANDOM.nextDouble() * 20 - 10,
                RANDOM.nextDouble() * 20 - 10
        );
    }
}
