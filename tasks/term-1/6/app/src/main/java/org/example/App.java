package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class App {

    private static final String SEP = "=".repeat(70);

    public static void main(String[] args) {
        runScenario("3 classes",
                DataSet.buildClusteredSet(),
                new KNNClassifier(5, DistanceMode.UNIFORM),
                6);

        pause(1200);

        runScenario("5 classes",
                DataSet.buildGridWithCenter(),
                new KNNClassifier(7, DistanceMode.DISTANCE_WEIGHTED),
                6);

        pause(1200);

        runScenario("6 classes",
                DataSet.buildRandomCloud(6, 260),
                new KNNClassifier(9, DistanceMode.UNIFORM),
                8);

        System.out.println();
        System.out.println("Done.");
    }

    private static void runScenario(String title, DataSet trainSet, KNNClassifier knn, int testCount) {
        System.out.println("\n=== " + title + " ===");
        System.out.printf("Data: %d pts | k: %d | Mode: %s%n",
                          trainSet.size(), knn.getNeighborsCount(), knn.getMode());

        knn.fit(trainSet);

        List<Point> testPoints = generateTestPointsInBounds(trainSet, testCount);
        List<Point> decorated = new ArrayList<>();

        int idx = 1;
        for (Point q : testPoints) {
            String label = knn.classify(q);
            q.setLabel("Q" + idx + " -> " + label);
            decorated.add(q);

            System.out.printf("Q%d: (%.2f, %.2f) -> %s%n", idx, q.getX(), q.getY(), label);
            idx++;
        }

        KNNCanvas.show(trainSet, decorated,
                String.format("%s (k=%d)", title, knn.getNeighborsCount()));
    }

    private static List<Point> generateTestPointsInBounds(DataSet trainSet, int count) {
        Bounds b = Bounds.fromData(trainSet.getPoints());
        Random rnd = new Random();

        double dx = b.maxX() - b.minX();
        double dy = b.maxY() - b.minY();

        double minX = b.minX() - dx * 0.15;
        double maxX = b.maxX() + dx * 0.15;
        double minY = b.minY() - dy * 0.15;
        double maxY = b.maxY() + dy * 0.15;

        List<Point> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double x = minX + rnd.nextDouble() * (maxX - minX);
            double y = minY + rnd.nextDouble() * (maxY - minY);
            result.add(new Point(x, y, null));
        }
        return result;
    }

    private static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}